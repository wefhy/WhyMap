// Copyright (c) 2023 wefhy

package dev.wefhy.whymap

import com.mojang.blaze3d.systems.RenderSystem
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.clothconfig.ConfigEntryPoint.getConfigScreen
import dev.wefhy.whymap.config.UserSettings.MinimapPosition
import dev.wefhy.whymap.config.WhyUserSettings
import dev.wefhy.whymap.events.FeatureUpdateQueue
import dev.wefhy.whymap.gui.WhyInputScreen
import dev.wefhy.whymap.hud.Hud
import dev.wefhy.whymap.hud.WhyHud
import dev.wefhy.whymap.utils.LocalTile.Companion.Block
import dev.wefhy.whymap.utils.LocalTile.Companion.Region
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.waypoints.CoordXYZ
import dev.wefhy.whymap.waypoints.LocalWaypoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.lwjgl.glfw.GLFW
import java.awt.image.BufferedImage
import kotlin.random.Random

class WhyMapClient : ClientModInitializer {

    private fun BufferedImage.toNativeImage() = createNativeImage(width, height) { x, y -> 127 shl 24 or getColor(x, y) }

    val nativeImageBackedTextures by lazy {  //TODO use better way of reusing native textures
        Array(4) { NativeImageBackedTexture(512, 512, false) }
    }


    fun loadPngIntoNativeImage(): (MatrixStack) -> Unit {
        val image = NativeImage.read(WhyMapClient::class.java.getResourceAsStream("/assets/whymap/player.png"))
        val texture = NativeImageBackedTexture(image)
//            val identifier = Identifier("whymap", "icon")
        val identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("playericon", texture)
        return { matrixStack ->

            drawCenter(matrixStack, identifier, image.width.toFloat(), image.height.toFloat())
        }
    }

    enum class MapMode(val visible: Boolean) {
        DISABLED(false),
        NORTH_LOCKED(true),
        ROTATED(true);

        fun next() = entries[(ordinal + 1) % entries.size]
    }

    private val Int.optimisticSign
        get() = when {
            this < 0 -> -1
            else -> 1
        }

    private val mapScale
        get() = 1/3f * WhyUserSettings.mapSettings.mapScale.toFloat()

    private val mapSize = 130f
    private val mapRadius
        get() = mapSize / 2f * WhyUserSettings.mapSettings.mapScale.toFloat()
    private val mapPadding = 5f


    override fun onInitializeClient() {
        val playerIcon by lazy { loadPngIntoNativeImage()}
        val mc = MinecraftClient.getInstance()
        val hud = WhyHud(mc)

        fun drawHud(drawContext: DrawContext) {
            if (!WhyUserSettings.generalSettings.displayHud) return
            with(drawContext.matrices) {
                push()
                if (WhyUserSettings.mapSettings.minimapMode.visible && WhyUserSettings.mapSettings.minimapPosition == MinimapPosition.TOP_LEFT) {
                    translate(mapPadding.toDouble(), (mapPadding + mapRadius) * 2.0, 0.0)
                } else {
                    translate(mapPadding.toDouble(), mapPadding.toDouble(), 0.0)
                }
                with(Hud.HudContext(drawContext, drawContext.matrices, WhyUserSettings.generalSettings.hudColor)) {
                    hud.draw()
                }
                pop()
            }
        }

        fun drawMiniMap(drawContext: DrawContext) {
            val matrixStack = drawContext.matrices
            val mapMode = WhyUserSettings.mapSettings.minimapMode
            val mapPosition = WhyUserSettings.mapSettings.minimapPosition

            val mapPosX = when (mapPosition) {
                MinimapPosition.TOP_LEFT -> mapRadius + mapPadding
                MinimapPosition.TOP_RIGHT -> mc.window.scaledWidth - mapRadius - mapPadding
                MinimapPosition.TOP_CENTER -> mc.window.scaledWidth / 2f
            }
            val mapPosY = mapRadius + mapPadding

            if (!mapMode.visible) return
            val player = mc.player ?: return println("No player!")
            val playerPos = player.pos ?: return println("No player pos!")
            val mrm = activeWorld?.mapRegionManager ?: return println("No map region manager!")
            val block = Block(playerPos.x.toInt(), playerPos.z.toInt())
            val region = block.parent(TileZoom.RegionZoom)
            val center = region.getCenter()
//            val diffX = center.x - block.x
//            val diffZ = center.z - block.z
            val diffX = block.x - center.x
            val diffZ = block.z - center.z
//            println("X: ${center.x}, ${block.x}, ${diffX}, ${diffX.sign}, Z: ${center.z}, ${block.z}, ${diffZ}, ${diffZ.sign}")

            val regions = listOf(
                region,
                Region(region.x, region.z + diffZ.optimisticSign),
                Region(region.x + diffX.optimisticSign, region.z),
                Region(region.x + diffX.optimisticSign, region.z + diffZ.optimisticSign)
            )


            val rendered = runBlocking {
                regions.associateWith {
                    mrm.getRegionForMinimapRendering(it) {
                        renderWhyImageBuffered()
//                        renderNativeImage()
                    }
                }
            }
            //no leak till here


            if(true) {
                val cropXstart = mapPosX - mapRadius
                val cropYstart = mapPosY - mapRadius
                val cropXend = mapPosX + mapRadius
                val cropYend = mapPosY + mapRadius
                val cropXsize = cropXend - cropXstart
                val cropYsize = cropYend - cropYstart
                val scaleX = mc.window.framebufferWidth.toFloat() / mc.window.scaledWidth.toFloat()
                val scaleY = mc.window.framebufferHeight.toFloat() / mc.window.scaledHeight.toFloat()
                RenderSystem.enableScissor(
                    (cropXstart * scaleX).toInt(),
                    mc.window.framebufferHeight - (cropYend * scaleY).toInt(),
                    (cropXsize * scaleX).toInt(),
                    (cropYsize * scaleY).toInt()
                )
                drawContext.fill(0, 0, mc.window.scaledWidth, mc.window.scaledHeight, 0xFF000000.toInt())
            }

            matrixStack.push()
            if (mapMode == MapMode.ROTATED) {
                matrixStack.translate(mapPosX, mapPosY, 0f)
                matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(player.yaw + 180))
                matrixStack.translate(-mapPosX, -mapPosY, 0f)
            }

            //no leak till here


            for ((region, rendered) in rendered) {
                if (rendered == null) continue
                val start = region.getStart()
                val diffX = start.x - block.x
                val diffZ = start.z - block.z
                matrixStack.push()
                matrixStack.translate(diffX.toFloat() * mapScale + mapPosX, diffZ.toFloat() * mapScale + mapPosY, 0f)
//                val texture: WhyTiledImage = rendered
                val texture: NativeImage = rendered.toNativeImage() //TODO cache result! Make sure cache is not cleared by NativeImageBackedTexture image setter
                val i = region.x.mod(2) + region.z.mod(2) * 2 //TODO this is so hacky and will casue issues if some part of the rendering is modified (ie more than 4 regions are rendered)
                draw(mc, matrixStack, texture, mapScale, nativeImageBackedTextures[i], i)
                matrixStack.pop()
//                texture.close() //No longer necessary since toNativeImage uses textures from the list
            }
            matrixStack.pop()
            matrixStack.push()
            matrixStack.translate(mapPosX, mapPosY, 0f)
            matrixStack.scale(0.1f, 0.1f, 0.1f)
            if (mapMode == MapMode.NORTH_LOCKED) {
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(player.yaw + 180))
            }
            playerIcon(matrixStack)
            matrixStack.pop()
            RenderSystem.disableScissor()

//
//            val rendered = runBlocking {
//                mrm.getRegionForTilesRendering(region) {
////                    getRendered()
//                    renderNativeImage()
//                }
//            } ?: return println("Nothing to render!")
//            println("Drawing minimap! ${region.x}, ${region.z}, diff: ${block.x - center.x}, ${block.z - center.z}")
//            matrixStack.push()
//            matrixStack.translate(diffX.toFloat() * mapScale + 100, diffZ.toFloat() * mapScale + 100, 0f)
//            val texture = rendered
////            val texture = createRandomTexture(512, 256)
//            draw(mc, matrixStack, texture, mapScale)
//            matrixStack.pop()
        }

        HudRenderCallback.EVENT.register { drawContext: DrawContext, counter: RenderTickCounter ->
            drawMiniMap(drawContext)
            drawHud(drawContext)
        }

//        UseBlockCallback.EVENT.register { playerEntity, world, hand, result ->
//            TODO("Whymap: no chat message confirmation. Assuming respawn point setting has failed.")
//            return@register ActionResult.PASS
//        }

        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if(mc.player != null) {
//                mc.player!!.spawn
            }
            if (kbNewWaypoint.wasPressed()) {
                GlobalScope.launch {
                    with(mc) {
                        val playerPos = player?.pos ?: return@with
                        val coords = CoordXYZ(playerPos.x.toInt(), playerPos.y.toInt(), playerPos.z.toInt())
                        WhyInputScreen("Adding new waypoint", "Do you want to add a new waypoint at $coords?") { answer, input ->
                            if (!answer) return@WhyInputScreen
                            val waypoint = LocalWaypoint(input, coords)
                            activeWorld?.waypoints?.add(waypoint) ?: println("Failed to add waypoint!")
                            FeatureUpdateQueue.addUpdate(waypoint.asOnlineWaypoint())
                        }.show()
                    }
                }
            }
            if (kbShowMinimap.wasPressed()) {
                WhyUserSettings.mapSettings.minimapMode = WhyUserSettings.mapSettings.minimapMode.next()
            }
            if (kbModSettings.wasPressed()) {
                mc.setScreen(getConfigScreen(null))
            }

            //TODO https://discord.com/channels/507304429255393322/807617488313516032/895854464060227665

            //TODO NativeImageBackedTexture(NativeImage.read(inputStream))


//                RenderSystem.depthMask(true)
//                RenderSystem.enableDepthTest()
//                RenderSystem.defaultBlendFunc()


//            if (mc.world != null) {
//                // Get the window width and height
//                val width: Int = mc.window.scaledWidth
//                val height: Int = mc.window.scaledHeight
//                // Get the matrix stack for rendering
//                val matrixStack = MatrixStack()
//                // Push a new matrix to the stack
//                matrixStack.push()
//                // Translate the matrix to the left corner of the screen
//                matrixStack.translate(0f, 0f, 0f)
//                // Get the immediate vertex consumer provider for rendering
//                val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
//                // Get the vertex consumer for rendering quads with solid color
//                val vertexConsumer = immediate.getBuffer(RenderLayer.getSolid())
//                // Set the color to red
//                val red = 255
//                val green = 0
//                val blue = 0
//                val alpha = 255
//                // Set the size of the square in pixels
//                val size = 20
//                // Draw a quad with four vertices
//                vertexConsumer.vertex(matrixStack.peek().positionMatrix, 0f, size.toFloat(), 0f).color(red, green, blue, alpha).texture(0f, 0f).next()
//                vertexConsumer.vertex(matrixStack.peek().positionMatrix, size.toFloat(), size.toFloat(), 0f).color(red, green, blue, alpha).texture(0f, 1f).next()
//                vertexConsumer.vertex(matrixStack.peek().positionMatrix, size.toFloat(), 0f, 0f).color(red, green, blue, alpha).texture(1f, 1f).next()
//                vertexConsumer.vertex(matrixStack.peek().positionMatrix, 0f, 0f, 0f).color(red, green, blue, alpha).texture(1f, 0f).next()
//                // Draw the quad to the screen
//                immediate.draw()
//                // Pop the matrix from the stack
//                matrixStack.pop()
//            }
//        }
//            val theTexture = NativeImageBackedTexture(64, 64, false)
//            thetexture.image.setColor(0, 0, 0)
//            theTexture.upload()
//            val something = RenderLayer.getText(theIdentifier)


        }
    }
    inline fun createNativeImage(width: Int, height: Int, block: NativeImage.(x: Int, y: Int) -> Int): NativeImage {
        val image = NativeImage(width, height, false)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                image.setColor(x, y, image.block(x, y))
            }
        }
        return image
    }

    fun createRandomTexture(width: Int, height: Int): NativeImage {
        return createNativeImage(width, height) { x, y ->
            val red: Int = Random.nextInt(128)
            val green: Int = Random.nextInt(128)
            val blue: Int = Random.nextInt(128)
            val alpha = Random.nextInt(128)
            // Pack the color components into an integer
            alpha shl 24 or (red shl 16) or (green shl 8) or blue
        }
    }

    fun draw(mc: MinecraftClient, matrixStack: MatrixStack, texture: NativeImage, scale: Float) {
        val textureId = mc.textureManager.registerDynamicTexture("dynamic_image", NativeImageBackedTexture(texture))
        draw(matrixStack, textureId, texture.width * scale, texture.height * scale)
    }

    fun draw(mc: MinecraftClient, matrixStack: MatrixStack, texture: NativeImage, scale: Float, textureContainer: NativeImageBackedTexture, i: Int) {
        if (textureContainer.image != texture) {
            textureContainer.image = texture
        }
        textureContainer.upload()
        val textureId = mc.textureManager.registerDynamicTexture("dynamicimage$i", textureContainer)
        draw(matrixStack, textureId, texture.width * scale, texture.height * scale)
    }

    fun drawCenter(matrixStack: MatrixStack, textureId: Identifier, width: Float, height: Float) {
        val positionMatrix = matrixStack.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f
        buffer.vertex(positionMatrix, -halfWidth, -halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f)
        buffer.vertex(positionMatrix, -halfWidth, halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(0f, 1f)
        buffer.vertex(positionMatrix, halfWidth, halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(1f, 1f)
        buffer.vertex(positionMatrix, halfWidth, -halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(1f, 0f)
        RenderSystem.setShader { GameRenderer.getPositionTexColorProgram() }
        RenderSystem.setShaderTexture(0, textureId)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        BufferRenderer.drawWithGlobalProgram(buffer.end())
    }

    fun draw(matrixStack: MatrixStack, textureId: Identifier, width: Float, height: Float) {
        val positionMatrix = matrixStack.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        buffer.vertex(positionMatrix, 0f, 0f, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f)
        buffer.vertex(positionMatrix, 0f, height, 0f).color(1f, 1f, 1f, 1f).texture(0f, 1f)
        buffer.vertex(positionMatrix, width, height, 0f).color(1f, 1f, 1f, 1f).texture(1f, 1f)
        buffer.vertex(positionMatrix, width, 0f, 0f).color(1f, 1f, 1f, 1f).texture(1f, 0f)
        RenderSystem.setShader { GameRenderer.getPositionTexColorProgram() }
        RenderSystem.setShaderTexture(0, textureId)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        BufferRenderer.drawWithGlobalProgram(buffer.end())
    }

    companion object {
        val kbNewWaypoint = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.newwaypoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.whymap"
            )
        )
        val kbShowMinimap = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.showminimap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.whymap"
            )
        )
        val kbModSettings = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.modsettings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.whymap"
            )
        )

    }
}