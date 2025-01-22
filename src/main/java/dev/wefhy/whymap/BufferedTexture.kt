package dev.wefhy.whymap

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderCall
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.NativeImage
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

open class BufferedTexture(val textureId: Identifier, width: Int, height: Int) : AbstractTexture() {
    private val lock = Any()
    private val frontBuffer = AtomicReference(NativeImage(width, height, false))
    private val backBuffer = AtomicReference(NativeImage(width, height, false))

    init {
        onRenderThread {
            TextureUtil.prepareImage(this.getGlId(), width, height)
            this.upload()
        }
    }

    fun writeImage(block: (NativeImage) -> Unit) {
        try {
            backBuffer.get().apply(block)
            swapBuffers()
        } catch (e: Exception) {
            System.err.println("Buffer operation failed: ${e.message}")
        }
    }

    fun upload(): NativeImage {
//        RenderSystem.recordRenderCall {
            return synchronized(lock) {
                try {
                    GlStateManager._bindTexture(glId)
                    frontBuffer.get().also { it.upload(0, 0, 0, false) }
                } catch (e: Exception) {
                    System.err.println("Texture upload failed: ${e.message}")
                    null!!
                }
            }
//        }
    }

    private fun swapBuffers() = synchronized(lock) {
        val oldFront = frontBuffer.get()
        val oldBack = backBuffer.get()
        frontBuffer.set(oldBack)
        backBuffer.set(oldFront)
        onSwapBuffers()
    }

    protected open fun onSwapBuffers() {}

    override fun close() {
        RenderSystem.recordRenderCall {
            super.close()
            frontBuffer.get().close()
            backBuffer.get().close()
        }
    }

    @Throws(IOException::class)
    override fun load(manager: ResourceManager?) {
        // Intentionally empty - we manage our own texture content
    }

    private fun onRenderThread(block: () -> Unit) {
        if (RenderSystem.isOnRenderThread()) {
            block()
        } else {
            RenderSystem.recordRenderCall(block)
        }
    }

//    // Utility extension function to safely use NativeImage
//    private inline fun <T : NativeImage, R> T.use(block: (T) -> R): R {
//        try {
//            return block(this)
//        } catch (e: Exception) {
//            close()
//            throw e
//        }
//    }
}