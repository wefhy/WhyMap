package dev.wefhy.whymap

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.NativeImage
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class BufferedTexture(val textureId: Identifier, width: Int, height: Int) : AbstractTexture() {
    private val lock = Any()
    private val frontBuffer = AtomicReference(createNativeImage(width, height))
    private val backBuffer = AtomicReference(createNativeImage(width, height))

    init {
        initializeGlTexture(width, height)
    }

    private fun createNativeImage(width: Int, height: Int): NativeImage {
        return NativeImage(width, height, false)
    }

    private fun initializeGlTexture(width: Int, height: Int) {
        RenderSystem.recordRenderCall {
//            val prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
            try {
                TextureUtil.prepareImage(glId, width, height)
//                uploadInitialImage()
            } finally {
//                GlStateManager._bindTexture(prevTexture)
            }
        }
    }

    private fun uploadInitialImage() {
        GlStateManager._bindTexture(glId)
        frontBuffer.get().upload(0, 0, 0, false)
    }

    fun writeImage(block: (NativeImage) -> Unit) {
//        synchronized(lock) {
            try {
                backBuffer.get().use(block)
                swapBuffers()
            } catch (e: Exception) {
                System.err.println("Buffer operation failed: ${e.message}")
            }
//        }
    }

    fun upload(): NativeImage {
//        RenderSystem.recordRenderCall {
//            synchronized(lock) {
//                val prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                try {
                    GlStateManager._bindTexture(glId)
                    return frontBuffer.get().also { it.upload(0, 0, 0, false) }
                } catch (e: Exception) {
                    System.err.println("Texture upload failed: ${e.message}")
                } finally {
//                    GlStateManager._bindTexture(prevTexture)
                }
//            }
//        }
        return null!!
    }

    private fun swapBuffers() {
        val oldFront = frontBuffer.get()
        val oldBack = backBuffer.get()
        frontBuffer.set(oldBack)
        backBuffer.set(oldFront)
    }

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

    // Utility extension function to safely use NativeImage
    private inline fun <T : NativeImage, R> T.use(block: (T) -> R): R {
        try {
            return block(this)
        } catch (e: Exception) {
            close()
            throw e
        }
    }
}