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
import net.minecraft.client.gl.ShaderProgramKeys.POSITION_TEX_COLOR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.*
import net.minecraft.client.texture.*
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.lwjgl.glfw.GLFW
import java.awt.image.BufferedImage
import kotlin.random.Random

class WhyMapClient : ClientModInitializer {

    private fun BufferedImage.toNativeImage() = createNativeImage(width, height) { x, y -> 127 shl 24 or getColorArgb(x, y) }

    val dynamicMinimapTextures by lazy {
        Array(4) {
            MinimapTile(
                Identifier.of("dynamic_minimap_$it"),
                512, 512
            ).also {
                MinecraftClient.getInstance().textureManager.registerTexture(it.textureId, it)
            }
        }
    }


    fun loadPngIntoNativeImage(): (MatrixStack) -> Unit {
        val image = NativeImage.read(WhyMapClient::class.java.getResourceAsStream("/assets/whymap/player.png"))
        val texture = NativeImageBackedTexture(image)
        val identifier = Identifier.of("playericon")
        MinecraftClient.getInstance().textureManager.registerTexture(Identifier.of("playericon"), texture)
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

        fun renderMiniMap(mc: MinecraftClient) {
            val mapMode = WhyUserSettings.mapSettings.minimapMode
            if (!mapMode.visible) return
            val player = mc.player ?: return WhyLog.t("No player!")
            val playerPos = player.pos ?: return WhyLog.t("No player pos!")
            val mrm = activeWorld?.mapRegionManager ?: return WhyLog.t("No map region manager!")
            val block = Block(playerPos.x.toInt(), playerPos.z.toInt())
            val region = block.parent(TileZoom.RegionZoom)
            val center = region.getCenter()
            val diffX = block.x - center.x
            val diffZ = block.z - center.z

            val regions = listOf(
                region,
                Region(region.x, region.z + diffZ.optimisticSign),
                Region(region.x + diffX.optimisticSign, region.z),
                Region(region.x + diffX.optimisticSign, region.z + diffZ.optimisticSign)
            )


            val rendereds = runBlocking {
                regions.associateWith {
                    mrm.getRegionForMinimapRendering(it) {
                        renderWhyImageBuffered()
//                        renderNativeImage()
                    }
                }
            }

            for ((region, result) in rendereds) {
                val (time, rendered) = result ?: continue
                if (rendered == null) continue
                val i = region.x.mod(2) + region.z.mod(2) * 2 //TODO this is so hacky and will casue issues if some part of the rendering is modified (ie more than 4 regions are rendered)
                val textureContainer = dynamicMinimapTextures[i]
                textureContainer.setBackBufferLocation(region)
                textureContainer.writeImage(time/*System.identityHashCode(rendered)*/) {
                    rendered.toNativeImage(it)
                }
                //TODO cache result!
            }
        }

        fun drawMiniMap(drawContext: DrawContext) {
            val matrixStack = drawContext.matrices
            val mapMode = WhyUserSettings.mapSettings.minimapMode
            val mapPosition = WhyUserSettings.mapSettings.minimapPosition

            if (!mapMode.visible) return

            val mapPosX = when (mapPosition) {
                MinimapPosition.TOP_LEFT -> mapRadius + mapPadding
                MinimapPosition.TOP_RIGHT -> mc.window.scaledWidth - mapRadius - mapPadding
                MinimapPosition.TOP_CENTER -> mc.window.scaledWidth / 2f
            }
            val mapPosY = mapRadius + mapPadding
            val player = mc.player ?: return WhyLog.t("No player!")
            val playerPos = player.pos ?: return WhyLog.t("No player pos!")



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
                // TODO this works in 1.21.2 but draws over the map
//                drawContext.fill(
//                    (cropXstart).toInt(),
//                    (cropYstart).toInt(),
//                    ((cropXstart + cropXsize)).toInt(),
//                    ((cropYstart + cropYsize)).toInt(),
//                    0xFF000000.toInt()
//                )
                // TODO this draws the black background but broke in 1.21.2 and instead paints whole screen black
//                drawContext.fill(0, 0, mc.window.scaledWidth, mc.window.scaledHeight, 0xFF000000.toInt())
            }

            matrixStack.push()
            if (mapMode == MapMode.ROTATED) {
                matrixStack.translate(mapPosX, mapPosY, 0f)
                matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(player.yaw + 180))
                matrixStack.translate(-mapPosX, -mapPosY, 0f)
            }

            //no leak till here

            for (textureContainer in dynamicMinimapTextures) {
                val region = textureContainer.frontBufferLocation
                //TODO what if buffers are swapped?
                val start = region.getStart()
                val diffX = start.x - playerPos.x //TODO using playerPos instead of block causes jelly effect 🤔
                val diffZ = start.z - playerPos.z
                matrixStack.push()
                matrixStack.translate(diffX.toFloat() * mapScale + mapPosX, diffZ.toFloat() * mapScale + mapPosY, 0f)
                draw(matrixStack, mapScale, textureContainer)
                matrixStack.pop()
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
        }

        HudRenderCallback.EVENT.register { drawContext: DrawContext, counter: RenderTickCounter ->

            drawMiniMap(drawContext)
            drawHud(drawContext)
        }

        ClientTickEvents.START_CLIENT_TICK.register { mc ->
            GlobalScope.launch { renderMiniMap(mc) }
        }

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
        }
    }
    inline fun createNativeImage(width: Int, height: Int, block: NativeImage.(x: Int, y: Int) -> Int): NativeImage {
        val image = NativeImage(width, height, false)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                image.setColorArgb(x, y, image.block(x, y))
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

    fun draw(matrixStack: MatrixStack, scale: Float, textureContainer: BufferedTexture) {
//        textureContainer.refresh()
//        val prevTex = GlStateManager.getBoundTexture()
        val texture = textureContainer.upload()
        draw(matrixStack, textureContainer.textureId, texture.width * scale, texture.height * scale)
//        GlStateManager._bindTexture(prevTex)
    }

    fun drawCenter(matrixStack: MatrixStack, textureId: Identifier, width: Float, height: Float) {
        draw(matrixStack, textureId, width, height, -(width * 0.5f), -(height * 0.5f))
    }

    fun draw(matrixStack: MatrixStack, textureId: Identifier, width: Float, height: Float, xOffset: Float = 0f, yOffset: Float = 0f) {
//        println("draw() called with: matrixStack = ${matrixStack}, textureId = $textureId, width = $width, height = $height")
        val positionMatrix = matrixStack.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        buffer.vertex(positionMatrix, xOffset, yOffset, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f)
        buffer.vertex(positionMatrix, xOffset, yOffset + height, 0f).color(1f, 1f, 1f, 1f).texture(0f, 1f)
        buffer.vertex(positionMatrix, xOffset + width, yOffset + height, 0f).color(1f, 1f, 1f, 1f).texture(1f, 1f)
        buffer.vertex(positionMatrix, xOffset + width, yOffset, 0f).color(1f, 1f, 1f, 1f).texture(1f, 0f)
        RenderSystem.setShader(POSITION_TEX_COLOR)
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
