// Copyright (c) 2023 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.clothconfig.ConfigEntryPoint.getConfigScreen
import dev.wefhy.whymap.config.UserSettings.MinimapPosition
import dev.wefhy.whymap.config.WhyUserSettings
import dev.wefhy.whymap.context.drawing.MinimapDrawContext
import dev.wefhy.whymap.events.FeatureUpdateQueue
import dev.wefhy.whymap.gui.WhyInputScreen
import dev.wefhy.whymap.ui.hud.WhyHud
import dev.wefhy.whymap.ui.minimap.Minimap
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
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.glfw.GLFW
import java.awt.image.BufferedImage
import kotlin.random.Random

class WhyMapClient : ClientModInitializer {

    private fun BufferedImage.toNativeImage() = createNativeImage(width, height) { x, y -> 127 shl 24 or getColor(x, y) }

    context(MinimapDrawContext)
    fun drawHud(hud: WhyHud) {
        if (!WhyUserSettings.generalSettings.displayHud) return
        with(matrixStack) {
            push()
            if (WhyUserSettings.mapSettings.minimapMode.visible && WhyUserSettings.mapSettings.minimapPosition == MinimapPosition.TOP_LEFT && WhyUserSettings.mapSettings.forceExperimentalMinmap) {
                translate(padding.toDouble(), (padding + radius) * 2.0, 0.0)
            } else {
                translate(padding.toDouble(), padding.toDouble(), 0.0)
            }
            hud.draw()
            pop()
        }
    }

    override fun onInitializeClient() {
        val mc = MinecraftClient.getInstance()
        val hud = WhyHud(mc)
        val minimap = Minimap(mc)

        HudRenderCallback.EVENT.register { matrixStack: MatrixStack, tickDelta: Float ->
            MinimapDrawContext(
                matrixStack,
                WhyUserSettings.mapSettings.minimapMode,
                WhyUserSettings.mapSettings.minimapPosition,
                130f,
                WhyUserSettings.mapSettings.mapScale.toFloat(),
                5f
            ).apply {
                if (WhyUserSettings.mapSettings.forceExperimentalMinmap)
                    minimap.draw()
                drawHud(hud)
            }
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

    companion object {
        private val kbNewWaypoint: KeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.newwaypoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.whymap"
            )
        )
        private val kbShowMinimap: KeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.showminimap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.whymap"
            )
        )
        private val kbModSettings: KeyBinding = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.whymap.modsettings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.whymap"
            )
        )
    }
}