// Copyright (c) 2023 wefhy

package dev.wefhy.whymap

import com.mojang.blaze3d.systems.RenderSystem
import dev.wefhy.whymap.events.FeatureUpdateQueue
import dev.wefhy.whymap.gui.WhyInputScreen
import dev.wefhy.whymap.waypoints.CoordXYZ
import dev.wefhy.whymap.waypoints.LocalWaypoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import kotlin.random.Random

class WhyMapClient : ClientModInitializer {
    override fun onInitializeClient() {

        val mc = MinecraftClient.getInstance()
        HudRenderCallback.EVENT.register{ matrixStack: MatrixStack, tickDelta: Float ->
            if (!isMinimapVisible) return@register
            matrixStack.push()
            matrixStack.translate(50f, 50f, 0f)
            val texture = createRandomTexture(512, 256)
            draw(mc, matrixStack, texture, 1f)
            matrixStack.pop()
        }

        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (kbNewWaypoint.wasPressed()) {
                GlobalScope.launch {
                    with(mc) {
                        val playerPos = player?.pos ?: return@with
                        val coords = CoordXYZ(playerPos.x.toInt(), playerPos.y.toInt(), playerPos.z.toInt())
                        WhyInputScreen("Adding new waypoint", "Do you want to add a new waypoint at $coords?") { answer, input ->
                            if (!answer) return@WhyInputScreen
                            val waypoint = LocalWaypoint(input, coords)
                            WhyMapMod.activeWorld?.waypoints?.add(waypoint) ?: println("Failed to add waypoint!")
                            FeatureUpdateQueue.addUpdate(waypoint.asOnlineWaypoint())
                        }.show()
                    }
                }
            }
            if (kbShowMinimap.wasPressed()) {
                isMinimapVisible = !isMinimapVisible
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

    companion object {
        var isMinimapVisible = true
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

    }
}