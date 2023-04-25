// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.ui.minimap

import com.mojang.blaze3d.systems.RenderSystem
import dev.wefhy.whymap.WhyMapClient
import dev.wefhy.whymap.WhyMapMod
import dev.wefhy.whymap.config.UserSettings
import dev.wefhy.whymap.context.drawing.MinimapDrawContext
import dev.wefhy.whymap.utils.LocalTile
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.utils.optimisticSign
import kotlinx.coroutines.runBlocking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

class Minimap(val mc: MinecraftClient) {

    private val nativeImageBackedTextures by lazy {  //TODO use better way of reusing native textures
        Array(4) { NativeImageBackedTexture(512, 512, false) }
    }

    val playerIcon = loadPngIntoNativeImage()

    context(MinimapDrawContext)
    fun draw() {
        val mapPosX = when (position) {
            UserSettings.MinimapPosition.TOP_LEFT -> radius + padding
            UserSettings.MinimapPosition.TOP_RIGHT -> mc.window.scaledWidth - radius - padding
            UserSettings.MinimapPosition.TOP_CENTER -> mc.window.scaledWidth / 2f
        }
        val mapPosY = radius + padding

        if (!mode.visible) return
        val player = mc.player ?: return println("No player!")
        val playerPos = player.pos ?: return println("No player pos!")
        val mrm = WhyMapMod.activeWorld?.mapRegionManager ?: return println("No map region manager!")
        val block = LocalTile.Block(playerPos.x.toInt(), playerPos.z.toInt())
        val region = block.parent(TileZoom.RegionZoom)
        val center = region.getCenter()
//            val diffX = center.x - block.x
//            val diffZ = center.z - block.z
        val diffX = block.x - center.x
        val diffZ = block.z - center.z
//            println("X: ${center.x}, ${block.x}, ${diffX}, ${diffX.sign}, Z: ${center.z}, ${block.z}, ${diffZ}, ${diffZ.sign}")

        val regions = listOf(
            region,
            LocalTile.Region(region.x, region.z + diffZ.optimisticSign),
            LocalTile.Region(region.x + diffX.optimisticSign, region.z),
            LocalTile.Region(region.x + diffX.optimisticSign, region.z + diffZ.optimisticSign)
        )


        val rendered = runBlocking {
            regions.associateWith {
                mrm.getRegionForMinimapRendering(it) {
                    renderWhyImageBuffered()
//                        renderNativeImage()
                }
            }
        }

        if(true) {
            val cropXstart = mapPosX - radius
            val cropYstart = mapPosY - radius
            val cropXend = mapPosX + radius
            val cropYend = mapPosY + radius
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
            DrawableHelper.fill(matrixStack, 0, 0, mc.window.scaledWidth, mc.window.scaledHeight, 0xFF000000.toInt())
        }

        matrixStack.push()
        if (mode == UserSettings.MapMode.ROTATED) {
            matrixStack.translate(mapPosX, mapPosY, 0f)
            matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(player.yaw + 180))
            matrixStack.translate(-mapPosX, -mapPosY, 0f)
        }



        for ((region, rendered) in rendered) {
            if (rendered == null) continue
            val start = region.getStart()
            val diffX = start.x - block.x //TODO minus playerPos.x
            val diffZ = start.z - block.z //TODO minus playerPos.z
            matrixStack.push()
            matrixStack.translate(diffX.toFloat() * scale + mapPosX, diffZ.toFloat() * scale + mapPosY, 0f)
//                val texture: WhyTiledImage = rendered
            val texture: NativeImage = rendered.toNativeImage() //TODO cache result! Make sure cache is not cleared by NativeImageBackedTexture image setter
            val i =
                region.x.mod(2) + region.z.mod(2) * 2 //TODO this is so hacky and will cause issues if some part of the rendering is modified (ie more than 4 regions are rendered)
            draw(mc, matrixStack, texture, scale, nativeImageBackedTextures[i], i)
            matrixStack.pop()
            texture.close()
        }
        matrixStack.pop()
        matrixStack.push()
        matrixStack.translate(mapPosX, mapPosY, 0f)
        matrixStack.scale(0.1f, 0.1f, 0.1f)
        if (mode == UserSettings.MapMode.NORTH_LOCKED) {
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

    companion object {
        fun loadPngIntoNativeImage(): (MatrixStack) -> Unit {
            val image = NativeImage.read(WhyMapClient::class.java.getResourceAsStream("/assets/whymap/player.png"))

            return { matrixStack ->
                val texture = NativeImageBackedTexture(image)
//            val identifier = Identifier("whymap", "icon")
                val identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("playericon", texture)
                drawCenter(matrixStack, identifier, image.width.toFloat(), image.height.toFloat())
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
            val buffer = tessellator.buffer
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
            val halfWidth = width * 0.5f
            val halfHeight = height * 0.5f
            buffer.vertex(positionMatrix, -halfWidth, -halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f).next()
            buffer.vertex(positionMatrix, -halfWidth, halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(0f, 1f).next()
            buffer.vertex(positionMatrix, halfWidth, halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(1f, 1f).next()
            buffer.vertex(positionMatrix, halfWidth, -halfHeight, 0f).color(1f, 1f, 1f, 1f).texture(1f, 0f).next()
            RenderSystem.setShader { GameRenderer.getPositionColorTexProgram() }
            RenderSystem.setShaderTexture(0, textureId)
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            tessellator.draw()
        }

        fun draw(matrixStack: MatrixStack, textureId: Identifier, width: Float, height: Float) {
            val positionMatrix = matrixStack.peek().positionMatrix
            val tessellator = Tessellator.getInstance()
            val buffer = tessellator.buffer
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
            buffer.vertex(positionMatrix, 0f, 0f, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f).next()
            buffer.vertex(positionMatrix, 0f, height, 0f).color(1f, 1f, 1f, 1f).texture(0f, 1f).next()
            buffer.vertex(positionMatrix, width, height, 0f).color(1f, 1f, 1f, 1f).texture(1f, 1f).next()
            buffer.vertex(positionMatrix, width, 0f, 0f).color(1f, 1f, 1f, 1f).texture(1f, 0f).next()
            RenderSystem.setShader { GameRenderer.getPositionColorTexProgram() }
            RenderSystem.setShaderTexture(0, textureId)
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            tessellator.draw()
        }
    }
}