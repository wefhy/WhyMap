package dev.wefhy.whymap

import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.NativeImage
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class BufferedTexture(val textureId: Identifier, width: Int, height: Int) : AbstractTexture() {
    private val lock = Any()

    private val frontBuffer = AtomicReference(createNativeImage(width, height))
    private val backBuffer = AtomicReference(createNativeImage(width, height))

    init {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall {
                TextureUtil.prepareImage(glId, width, height)
                uploadInitialImage()
            }
        } else {
            TextureUtil.prepareImage(glId, width, height)
            uploadInitialImage()
        }
    }

    private fun uploadInitialImage() {
        bindTexture()
        frontBuffer.get().upload(0, 0, 0, false)
    }

    private fun createNativeImage(width: Int, height: Int): NativeImage {
        return NativeImage(width, height, false)
    }

    fun writeImage(block: (NativeImage) -> Unit) {
        synchronized(lock) {
            try {
                backBuffer.get().use { image ->
                    block(image)
                }
                swapBuffers()
            } catch (e: Exception) {
                System.err.println("Failed to write to back buffer: ${e.message}")
            }
        }
    }

    fun upload(): NativeImage {
        return try {
            bindTexture()
            return frontBuffer.get().also {
                it.upload(0, 0, 0, false)
            }
        } catch (e: Exception) {
            System.err.println("Texture upload failed for $textureId: ${e.message}")
            null!!
        }
    }

    private fun swapBuffers() {
        val oldFront = frontBuffer.get()
        val oldBack = backBuffer.get()

        // Swap buffers
        frontBuffer.set(oldBack)
        backBuffer.set(oldFront)
    }

    override fun close() {
        super.close()
        frontBuffer.get().close()
        backBuffer.get().close()
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