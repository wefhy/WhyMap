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
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.*
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.glfw.GLFW
import kotlin.random.Random

class WhyMapClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (keyBinding.wasPressed()) {
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

            //TODO https://discord.com/channels/507304429255393322/807617488313516032/895854464060227665

            //TODO NativeImageBackedTexture(NativeImage.read(inputStream))


//            if (mc.world != null) {
//                // Get the window width and height
//                val width: Int = mc.getWindow().getScaledWidth()
//                val height: Int = mc.getWindow().getScaledHeight()
//                // Create a new buffered image with the same size as the window
//                val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
//                // Create a random object for generating random colors
//                val random = Random
//                // Loop through all the pixels in the image
//                for (x in 0 until width) {
//                    for (y in 0 until height) {
//                        // Generate a random color
//                        val red: Int = random.nextInt(256)
//                        val green: Int = random.nextInt(256)
//                        val blue: Int = random.nextInt(256)
//                        val alpha = 255
//                        // Pack the color components into an integer
//                        val color = alpha shl 24 or (red shl 16) or (green shl 8) or blue
//                        // Set the pixel color in the image
//                        image.setRGB(x, y, color)
//                    }
//                }
//                // Get the matrix stack for rendering
//                val matrixStack = MatrixStack()
//                // Push a new matrix to the stack
//                matrixStack.push()
//                // Translate the matrix to the center of the screen
//                matrixStack.translate(width / 2.0, height / 2.0, 0.0)
//                // Get the immediate vertex consumer provider for rendering
//                val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
//                // Get a sprite renderer object for rendering images
//                val spriteRenderer = SpriteRenderer(mc.textureManager)
//                // Create a native image from the buffered image
//                val nativeImage: NativeImage = NativeImage.fromBufferedImage(image)
//                // Create a dynamic texture from the native image
//                val dynamicTexture = DynamicTexture(nativeImage)
//                // Get the identifier of the dynamic texture
//                val textureId: Identifier = mc.textureManager.registerDynamicTexture("tutorial_image", dynamicTexture)
//                // Get the sprite of the dynamic texture
//                val sprite: Sprite = spriteRenderer.getSprite(textureId)
//                // Render the sprite on the screen with full brightness and no tint
//                spriteRenderer.draw(sprite, matrixStack.peek().positionMatrix, immediate, width / 2, height / 2, 0, 1.0f, 1.0f, 1.0f, 1.0f, 15728880)
//                // Draw the sprite to the screen
//                immediate.draw()
//                // Pop the matrix from the stack
//                matrixStack.pop()
//            }


//            if (mc.world != null) {
//                RenderSystem.setShader(GameRenderer::getPositionTexProgram)
//                val tesselator = Tessellator.getInstance()
//                val bufferBuilder = tesselator.buffer
//                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
//                bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).next()
//                bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).next()
//                bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).next()
//                bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).next()
//                tesselator.draw()
//                RenderSystem.depthMask(true)
//                RenderSystem.enableDepthTest()
//                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
//                RenderSystem.defaultBlendFunc()
//
//
//            }


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
            val theTexture = NativeImageBackedTexture(64, 64, false)
            theTexture.use { texture ->
                val image = texture.image!!
                for (x in 0 until image.width) {
                    for (y in 0 until image.height) {
                        val red: Int = Random.nextInt(256)
                        val green: Int = Random.nextInt(256)
                        val blue: Int = Random.nextInt(256)
                        val alpha = 255
                        // Pack the color components into an integer
                        val color = alpha shl 24 or (red shl 16) or (green shl 8) or blue
                        // Set the pixel color in the image
                        image.setColor(x, y, color)
                    }
                }
            }


            val anotherImage = NativeImage(64, 64, false)
            for (x in 0 until anotherImage.width) {
                for (y in 0 until anotherImage.height) {
                    val red: Int = Random.nextInt(256)
                    val green: Int = Random.nextInt(256)
                    val blue: Int = Random.nextInt(256)
                    val alpha = 255
                    // Pack the color components into an integer
                    val color = alpha shl 24 or (red shl 16) or (green shl 8) or blue
                    // Set the pixel color in the image
                    anotherImage.setColor(x, y, color)
                }
            }
            val anotherTexture = NativeImageBackedTexture(anotherImage)


            val theIdentifier = mc.textureManager.registerDynamicTexture("dynamic_image", anotherTexture)
            val something = RenderLayer.getText(theIdentifier)

            HudRenderCallback.EVENT.register(HudRenderCallback { matrixStack: MatrixStack, tickDelta: Float ->
                val positionMatrix = matrixStack.peek().positionMatrix
                val tessellator = Tessellator.getInstance()
                val buffer = tessellator.buffer
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
                buffer.vertex(positionMatrix, 20f, 20f, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f).next()
                buffer.vertex(positionMatrix, 20f, 60f, 0f).color(1f, 0f, 0f, 1f).texture(0f, 1f).next()
                buffer.vertex(positionMatrix, 60f, 60f, 0f).color(0f, 1f, 0f, 1f).texture(1f, 1f).next()
                buffer.vertex(positionMatrix, 60f, 20f, 0f).color(0f, 0f, 1f, 1f).texture(1f, 0f).next()
                RenderSystem.setShader { GameRenderer.getPositionColorTexProgram() }
//                RenderSystem.setShaderTexture(0, Identifier("examplemod", "icon.png"))
                RenderSystem.setShaderTexture(0, theIdentifier)
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                tessellator.draw()
            })

        }
    }

    companion object {
        val keyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.newwaypoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.whymap"
            )
        )
    }
}