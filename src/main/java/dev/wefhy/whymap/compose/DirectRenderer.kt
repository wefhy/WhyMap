// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asComposeCanvas
import com.mojang.blaze3d.systems.RenderSystem
import dev.wefhy.whymap.utils.WhyDispatchers.blockOnMain
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferRenderer
import org.jetbrains.skia.*
import org.jetbrains.skia.FramebufferFormat.Companion.GR_GL_RGBA8
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL14.GL_FUNC_ADD
import org.lwjgl.opengl.GL14.glBlendEquation
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING
import org.lwjgl.opengl.GL33

internal class DirectRenderer(var width: Int, var height: Int) : Renderer() {

    private val context: DirectContext by lazy { DirectContext.makeGL() }
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var composeCanvas: Canvas? = null

    init {
        println("Init DirectRenderer with width: $width, height: $height")
        System.setProperty("skiko.macos.opengl.enabled", "true")
    }

    private fun initilaize() {
        RenderSystem.assertOnRenderThread()
        surface?.close()
        renderTarget?.close()
        renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, glGetInteger(GL_FRAMEBUFFER_BINDING), GR_GL_RGBA8).also {
            surface = Surface.makeFromBackendRenderTarget(
                context, it, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
            )
        }
        composeCanvas = surface?.canvas?.asComposeCanvas()
    }

    override fun onSizeChange(width: Int, height: Int) {
        if (this.width == width && this.height == height) return
        println("DirectRenderer onSizeChange: $width, $height")
        RenderSystem.assertOnRenderThread()
        this.width = width
        this.height = height
        initilaize()
    }

    override fun render(drawContext: DrawContext, tickDelta: Float, block: (Canvas) -> Unit) = blockOnMain {
        RenderSystem.assertOnRenderThread()
        enterManaged()
        if (composeCanvas == null) {
            initilaize()
        }
        block(composeCanvas!!)
        surface!!.flush()
        exitManaged()
        context.resetAll()
    }

    override fun invalidate() {

    }

    override fun close() {
        surface?.close()
        renderTarget?.close()
        context.close()
    }

    private fun enterManaged() {
        RenderSystem.assertOnRenderThread()
        RenderSystem.pixelStore(GL_UNPACK_ROW_LENGTH, 0)
        RenderSystem.pixelStore(GL_UNPACK_SKIP_PIXELS, 0)
        RenderSystem.pixelStore(GL_UNPACK_SKIP_ROWS, 0)
        RenderSystem.pixelStore(GL_UNPACK_ALIGNMENT, 4)
    }

    private fun exitManaged() {
        RenderSystem.assertOnRenderThread()
        BufferRenderer.reset()
        GL33.glBindSampler(0, 0)
        RenderSystem.disableBlend()
        glDisable(GL_BLEND)
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE)
        RenderSystem.blendEquation(GL_FUNC_ADD)
        glBlendEquation(GL_FUNC_ADD)
        RenderSystem.colorMask(true, true, true, true)
        glColorMask(true, true, true, true)
        RenderSystem.depthMask(true)
        glDepthMask(true)
        RenderSystem.disableScissor()
        glDisable(GL_SCISSOR_TEST)
        glDisable(GL_STENCIL_TEST)
        RenderSystem.disableDepthTest()
        glDisable(GL_DEPTH_TEST)
        glActiveTexture(GL_TEXTURE0)
        RenderSystem.activeTexture(GL_TEXTURE0)
    }
}