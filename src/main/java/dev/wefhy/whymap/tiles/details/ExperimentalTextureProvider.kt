// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.details

import net.minecraft.block.Block
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.jvm.optionals.getOrNull


object ExperimentalTextureProvider {
    val waterTexture by lazy { getBitmap("water")} //TODO move to separate file

    private val loadedTextures = mutableMapOf<String, Optional<BufferedImage>?>()
    private val classLoader = javaClass.classLoader

    fun getBitmap(block: Block): BufferedImage? {
        return getBitmap(block.translationKey.split('.').last())
    }

    val missingTextures = mutableListOf<String>()

    @OptIn(ExperimentalStdlibApi::class)
    fun getBitmap(name: String): BufferedImage? {
//        MinecraftClient.getInstance().resourceManager.getResource()
        //TODO read resources from current resource pack?
        //manager.getResource(id).getInputStream()
        //DataInput input = new DataInputStream(manager.getResource(id).getInputStream());
        //MY_HASH_MAP.put(id, NbtIo.read(input));
        //new Identifier("minecraft:block/water_still"),
        //new Identifier("minecraft:block/water_flow"),
        //FabricBlockSettings.copy(Blocks.LAVA)
        //new Identifier(MOD_ID, "textures/information.png")
        return loadedTextures.getOrPut(name) {
            val file = getTopTexture(name)
                ?: getRegularTexture(name)
                ?: kotlin.run {
                    val shortName = name.replace("_stairs", "").replace("_slab", "").replace("smooth_", "")
                    getTopTexture(shortName)
                        ?: getRegularTexture(shortName)
                        ?: getTopTexture(shortName + 's')
                        ?: getRegularTexture(shortName + 's')
                        ?: getTopTexture(shortName + "_block")
                        ?: getRegularTexture(shortName + "_block")
                        ?: getRegularTexture(shortName + "_planks")
                        ?: getRegularTexture(shortName.replace("_carpet", "_wool"))
                        ?: getRegularTexture(shortName.replace("_carpet", "_block"))
                        ?: getRegularTexture(shortName.replace("wood", "log"))
                        ?: return@getOrPut Optional.empty<BufferedImage>().also {
                            missingTextures += name
//                            LOGGER.warn("Skipping $name")
                        }
                }
//            LOGGER.info("Drawing $name")
            Optional.of(ImageIO.read(file))
        }?.getOrNull()
    }
    // TODO Source: https://discord.com/channels/507304429255393322/507982478276034570/976891500657008640
//    fun experimentalGetTexture() {
//        val stream = ClassLoader.getSystemClassLoader().getResourceAsStream("assets/$MOD_ID/textures/textures.registry")
//        var registry: String? = null
//        registry = try {
//            IOUtils.toString(stream, StandardCharsets.UTF_8)
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        }
//        registry.lines().forEach(Consumer { line: String ->
//            IdentifierUtils.registerTexture(
//                Identifier(MOD_ID, "textures/$line"),
//                "textures/$line"
//            )
//        })
//    }
//
//    fun registerBufferedImageTexture(i: Identifier?, bi: BufferedImage?) {
//        try {
//            val baos = ByteArrayOutputStream()
//            ImageIO.write(bi, "png", baos)
//            val bytes: ByteArray = baos.toByteArray()
//            val bb: ByteBuffer = BufferUtils.createByteBuffer(bytes.size).put(bytes)
//            bb.flip()
//            val nibt = NativeImageBackedTexture(NativeImage.read(bb))
//            MinecraftClient.getInstance().textureManager.registerTexture(i, nibt)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun registerTexture(i: Identifier?, path: String) {
//        val stream = ClassLoader.getSystemClassLoader().getResourceAsStream("assets/$MOD_ID/$path")
//        var bi: BufferedImage? = null
//        bi = try {
//            ImageIO.read(stream)
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        }
//        registerBufferedImageTexture(i, bi)
//    }


    private fun getTopTexture(name: String): URL? {
        return classLoader.getResource("zoom-textures/${name}_top.png")
    }

    private fun getRegularTexture(name: String): URL? {
        return classLoader.getResource("zoom-textures/${name}.png")
    }
}