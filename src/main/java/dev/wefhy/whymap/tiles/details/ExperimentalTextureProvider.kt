// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.details

import dev.wefhy.whymap.whygraphics.WhyTile
import dev.wefhy.whymap.whygraphics.WhyTile.Companion.asWhyTile
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
    private val loadedWhyTiles = mutableMapOf<String, Optional<WhyTile>>()

    fun getBitmap(block: Block): BufferedImage? {
        return getBitmap(block.translationKey.split('.').last())
    }

    fun getWhyTile(block: Block): WhyTile? {
        return getWhyTile(block.translationKey.split('.').last())
    }

    fun getWhyTile(name: String): WhyTile? {
        return loadedWhyTiles.getOrPut(name) {
            Optional.of(getBitmap(name)?.asWhyTile() ?: return@getOrPut Optional.empty())
        }.getOrNull()
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
                ?: getTopTexture(name)
                ?: getRegularTexture(name)
                ?: kotlin.run {
                    val shortName = name
                        .replace("_stairs", "")
                        .replace("_slab", "")
                        .replace("smooth_", "")
                        .replace("infested_", "")
                        .replace("stripped_", "")
                        .replace("wall_", "") //this handles coral wall fans
                        .replace("waxed_", "")
                        .replace("short_", "")
                        .replace("long_", "")
//                        .replace("hanging_sign", "sign")
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
                        ?: getRegularTexture(shortName.replace("pressure_plate", "planks"))
                        ?: getRegularTexture(shortName.replace("pressure_plate", "block"))
                        ?: getRegularTexture(shortName.replace("_pressure_plate", ""))
                        ?: tryGenerateCustomTexture(name)
                        ?: tryGenerateCustomTexture(shortName)
                        ?: return@getOrPut Optional.empty<BufferedImage>().also {
                            missingTextures += name
//                            LOGGER.warn("Skipping $name")
                        }
                }
//            LOGGER.info("Drawing $name")
            Optional.of(ImageIO.read(file))
        }?.getOrNull()?.run {
            when (type) {
                BufferedImage.TYPE_INT_ARGB -> this
                else -> {
                    val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val g2d = newImage.createGraphics()
                    g2d.drawImage(this, 0, 0, null)
                    g2d.dispose()
                    newImage
                }
            }
        }
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

    /**
     * Should generate textures for:
     * - {material}_stairs
     * - {fluid}_cauldron
     * - potted_{item}
     * - smooth_{material}
     * - infested_{material}
     * - {material}_wall
     * - {material}_button
     * - {mob}_head
     * - {color}_bed
     * - {color}_banner
     * - {material}_pressure_plate
     * - {coral}_wall_fan
     * - {material}_fence
     * - {material}_fence_gate
     * - waxed_{material}
     * - {material}_sign
     * - {material}_hanging_sign
     *
     * Should also find the best _stage texture for crops
     */
    private inline fun tryGenerateCustomTexture(name: String): URL? {
        return null
    }

    private fun getTopTexture(name: String): URL? {
        return classLoader.getResource("zoom-textures/${name}_top.png")
    }

    private fun getFrontTexture(name: String): URL? {
        return classLoader.getResource("zoom-textures/${name}_front.png")
    }

    private fun getSideTexture(name: String): URL? {
        return classLoader.getResource("zoom-textures/${name}_side.png")
    }

    private fun getRegularTexture(name: String): URL? {
        return classLoader.getResource("zoom-textures/${name}.png")
    }
}