// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.communication.BlockData
import dev.wefhy.whymap.config.RenderConfig.shouldBlockOverlayBeIgnored
import dev.wefhy.whymap.config.WhyMapConfig.reRenderInterval
import dev.wefhy.whymap.config.WhyMapConfig.regionThumbnailScaleLog
import dev.wefhy.whymap.config.WhyMapConfig.storageTileBlocks
import dev.wefhy.whymap.config.WhyMapConfig.storageTileBlocksSquared
import dev.wefhy.whymap.config.WhyMapConfig.storageTileChunks
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.utils.ObfuscatedLogHelper.obfuscateObjectWithCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.Heightmap
import net.minecraft.world.LightType
import net.minecraft.world.chunk.Chunk
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZInputStream
import org.tukaani.xz.XZOutputStream
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import kotlin.math.atan
import kotlin.random.Random

context(CurrentWorldProvider<WhyWorld>)
class MapArea private constructor(val location: LocalTileRegion) {

    val biomeManager = currentWorld.biomeManager
    var modifiedSinceRender = true
    var modifiedSinceSave = false

    val blockIdMap: Array<ShortArray> = Array(storageTileBlocks) { ShortArray(storageTileBlocks) { 0 } } // at least 12 bits, possibly 16
    val blockOverlayIdMap: Array<ShortArray> = Array(storageTileBlocks) { ShortArray(storageTileBlocks) { 0 } } // at least 12 bits, possibly 16
    val heightMap: Array<ShortArray> = Array(storageTileBlocks) { ShortArray(storageTileBlocks) { 0 } } // at least 9 bits
    val biomeMap: Array<ByteArray> = Array(storageTileBlocks) { ByteArray(storageTileBlocks) { 0 } } // at least 7 bits, possibly 8
    val lightMap: Array<ByteArray> = Array(storageTileBlocks) { ByteArray(storageTileBlocks) { 0 } } // at least 4 bits
    val depthMap: Array<ByteArray> = Array(storageTileBlocks) { ByteArray(storageTileBlocks) { 0 } } // at least 8 bits
    val exists = Array(storageTileChunks) { Array(storageTileChunks) { false } } // 1 bit

    val file = currentWorld.getFile(location)
    val thumbnailFile = currentWorld.getThumbnailFile(location)

    val lightingProvider = MinecraftClient.getInstance().world!!.lightingProvider // TODO context receiver for world!
    val biomeAccess = MinecraftClient.getInstance().world!!.biomeAccess // TODO context receiver for world!
    lateinit var rendered: BufferedImage
    lateinit var renderedThumbnail: BufferedImage
    var lastUpdate = 0L
    var lastThumbnailUpdate = 0L

    init {
        currentWorld.writeToLog("Initialized ${obfuscateObjectWithCommand(location, "init")}, file exists: ${file.exists()}")
        if(file.exists())
            load()
    }

    fun getChunk(position: ChunkPos): Array<List<BlockState>>? {
        //TODO load only if in exists array; save exists array to file
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) {z ->
            blockIdMap[startZ + z].slice(startX until (startX + 16)).map {
                decodeBlock(it)
            }
        }
    }
    fun getChunkOverlay(position: ChunkPos): Array<List<BlockState>>? {
        //TODO load only if in exists array; save exists array to file
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) {z ->
            blockOverlayIdMap[startZ + z].slice(startX until (startX + 16)).map {
                decodeBlock(it)
            }
        }
    }
    fun getChunkBiomeFoliageAndWater(position: ChunkPos): Array<List<Pair<FloatColor, Color>>>? {
        //TODO load only if in exists array; save exists array to file
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) {z ->
            biomeMap[startZ + z].slice(startX until (startX + 16)).map {
                Pair(biomeManager.decodeBiomeFoliage(it), biomeManager.decodeBiomeWaterColor(it))
            }
        }
    }
    fun getChunkHeightmap(position: ChunkPos): Array<ShortArray>? {
        //TODO load only if in exists array; save exists array to file
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) {z ->
            heightMap[startZ + z].sliceArray(startX until (startX + 16))
        }
    }
    fun getChunkDepthmap(position: ChunkPos): Array<ByteArray>? {
        //TODO load only if in exists array; save exists array to file
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) {z ->
            depthMap[startZ + z].sliceArray(startX until (startX + 16))
        }
    }

    fun getChunkNormals(position: ChunkPos): Array<Array<Normal>>? {
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) {z ->
            Array(16) {x ->
                getNormalSharp(startX + x, startZ + z)
            }
        }
    }


    val random = Random(0)

    suspend fun save() = withContext(Dispatchers.IO) {
        currentWorld.writeToLog("Saving ${obfuscateObjectWithCommand(location, "save")}, file existed: ${file.exists()}")
        if (!modifiedSinceSave)
            return@withContext
        //TODO write file versions and support migrations
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.outputStream().use {
            val compressed = withContext(Dispatchers.Default) {
                val data = ByteArray(storageTileBlocksSquared * 9)
                val shortBuffer = ByteBuffer.wrap(data, 0, storageTileBlocksSquared * 6)
                val byteBuffer = ByteBuffer.wrap(data, storageTileBlocksSquared * 6, storageTileBlocksSquared * 3)
                val shortShortBuffer = shortBuffer.asShortBuffer()
                for (y in 0 until storageTileBlocks) {
                    shortShortBuffer.put(blockIdMap[y])       //2
                    shortShortBuffer.put(blockOverlayIdMap[y])//2
                    shortShortBuffer.put(heightMap[y])        //2
                    byteBuffer.put(biomeMap[y])        //1
                    byteBuffer.put(lightMap[y])        //1
                    byteBuffer.put(depthMap[y])        //1
                }
                shortShortBuffer.flip()
                byteBuffer.flip()
                val xzOutupt = ByteArrayOutputStream()
                XZOutputStream(xzOutupt, LZMA2Options(3)).use {xz ->
                    xz.write(data)
                    xz.close()
                }
                xzOutupt.toByteArray() // TODO compression and saving can be multithreaded
            }
            it.write(compressed)
        }

        LOGGER.debug("SAVED: ${file.absolutePath}")
//        MinecraftClient.getInstance().textureManager.getTexture()
        modifiedSinceSave = false
    }

    private fun load() {
        currentWorld.writeToLog("Loading ${obfuscateObjectWithCommand(location, "load")}, file existed: ${file.exists()}")
        try {
            file.inputStream().use {
                val data = ByteArray(storageTileBlocksSquared * 9)
                XZInputStream(it).use { xz ->
                    xz.read(data)
                    xz.close()
                }

                val shortBuffer = ByteBuffer.wrap(data, 0, storageTileBlocksSquared * 6).asShortBuffer()
                val byteBuffer = ByteBuffer.wrap(data, storageTileBlocksSquared * 6, storageTileBlocksSquared * 3)

                for (y in 0 until storageTileBlocks) {
                    shortBuffer.get(blockIdMap[y])
                    shortBuffer.get(blockOverlayIdMap[y])
                    shortBuffer.get(heightMap[y])
                    byteBuffer.get(biomeMap[y])
                    byteBuffer.get(lightMap[y])
                    byteBuffer.get(depthMap[y])
                }
            }
        } catch (e: EOFException) {
            currentWorld.writeToLog("ERROR Loading ${obfuscateObjectWithCommand(location, "error")}")
            LOGGER.error("ERROR LOADING TILE: ${file.absolutePath}")
        }
    }

    fun Chunk.getOceanFloorHeightMapHotFix(): Array<IntArray> {
        val hm = getHeightmap(Heightmap.Type.WORLD_SURFACE)
        val output = Array(16) { IntArray(16) }
        val mutablePosition = BlockPos.Mutable()
        for (z in 0 until 16) {
            for (x in 0 until 16) {
                var y = hm[x, z] - 1
                mutablePosition.set(x, y, z)
//                getBlockState(mutablePosition).isFullCube()
//                getBlockState(mutablePosition).hasSidedTransparency()
//                getBlockState(mutablePosition).isOpaque
//                Block.isFaceFullSquare(null, Direction.UP)
                while (!getBlockState(mutablePosition).material.isSolid and (y > bottomY)) { //TODO or isOpaque
                    mutablePosition.y = --y
                }
                output[z][x] = y //TODO this will point to air block just like regular heightmap
            }
        }
        return output
    }

    fun getBlockInfo(block: LocalTileBlock): Any {
        val x = block.x and (storageTileBlocks - 1)
        val z = block.z and (storageTileBlocks - 1)

        return BlockData(
            block = decodeBlock(blockIdMap[z][x]).block.translationKey.replace("block.minecraft.", ""),
            overlay = decodeBlock(blockOverlayIdMap[z][x]).block.translationKey.replace("block.minecraft.", ""),
            biome = currentWorld.biomeManager.biomeGetName(currentWorld.biomeManager.decodeBiome(biomeMap[z][x])),
            height = heightMap[z][x],
            depth = depthMap[z][x].toUByte(),
            light = lightMap[z][x]
        )
    }

    fun updateChunk(chunk: Chunk) {
        if (
            chunk.pos.regionX != location.x ||
            chunk.pos.regionZ != location.z
        ) {
            LOGGER.error("Chunk wrong position!")
            println("WhyMap Update Error: Chunk wrong position!")
            currentWorld.writeToLog("ERROR ChunkUpdate ${obfuscateObjectWithCommand(location, "update-error")}")
            return
        }
        modifiedSinceSave = true
        exists[chunk.pos.regionRelativeZ][chunk.pos.regionRelativeX] = true

        val worldLightView = lightingProvider[LightType.BLOCK]
//        val surface = chunkGetSurface(chunk, Heightmap.Type.OCEAN_FLOOR)
//        val heightmapFloor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR)
        val heightmapFloorTmp = chunk.getOceanFloorHeightMapHotFix() // TODO this should be replaced by heightmapFloor when it works
        val heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE)
        val absoluteBlockPos = BlockPos.Mutable()
        val chunkBlockPos = BlockPos.Mutable()
        val chunkOverlayBlockPos = BlockPos.Mutable()
        val regionBlockPos = BlockPos.Mutable()

        val startX = chunk.pos.startX
        val startZ = chunk.pos.startZ

        val regionRelativeStartX = chunk.pos.regionRelativeX shl 4
        val regionRelativeStartZ = chunk.pos.regionRelativeZ shl 4


        for (posZ in 0 until 16) {
            for (posX in 0 until 16) {
                val posY = heightmapFloorTmp[posZ][posX]
//                val posY = heightmapSurface[posX, posZ] - 1 //TODO floor should be used!!!
//                val posY = heightmapFloor[posX, posZ] - 1
                val absoluteX = startX + posX
                val absoluteZ = startZ + posZ
                val surfaceHeight = heightmapSurface[posX, posZ] - 1

                chunkBlockPos.set(posX, posY, posZ)
                chunkOverlayBlockPos.set(posX, surfaceHeight, posZ)

                val regionRelativeX = regionRelativeStartX + posX
                val regionRelativeZ = regionRelativeStartZ + posZ
                regionBlockPos.set(regionRelativeX, posY, regionRelativeZ)
                absoluteBlockPos.set(absoluteX, posY, absoluteZ)

                val block = chunk.getBlockState(chunkBlockPos)
                val overlayBlock = chunk.getBlockState(chunkOverlayBlockPos)
//                val depth = heightmapSurface[posX, posZ] - heightmapFloor[posX, posZ]
                val depth = surfaceHeight - posY
                val light = worldLightView.getLightLevel(absoluteBlockPos)
//                val biome = biomeAccess.getBiome(absoluteBlockPos).value()
//                val biome = MinecraftClient.getInstance().world!!.getBiome(absoluteBlockPos).value()
                val biome = biomeAccess.getBiome(absoluteBlockPos).value().takeUnless { biomeManager.isPlains(it) }
                    ?: biomeAccess.getBiomeForNoiseGen(absoluteBlockPos).value()


//                if(biomeManager.biomeGetName(biome)?.contains("plains") == true) {
//                    println("plains")
//                }



                blockIdMap[regionRelativeZ][regionRelativeX] = encodeBlock(block)
                blockOverlayIdMap[regionRelativeZ][regionRelativeX] = encodeBlock(overlayBlock)
                biomeMap[regionRelativeZ][regionRelativeX] = biomeManager.encodeBiome(biome)
                heightMap[regionRelativeZ][regionRelativeX] = posY.toShort()
                lightMap[regionRelativeZ][regionRelativeX] = light.toByte()
                depthMap[regionRelativeZ][regionRelativeX] = depth.coerceIn0255().toUByte().toByte()
                //TODO render chunk right away? Re-render nearby chunk edges? Queue it at least? Mark that chunk was changed?
//                MinecraftClient.getInstance().world!!.biomeAccess.

            }
        }
        modifiedSinceSave = true
        modifiedSinceRender = true

//        CoroutineScope(Job()).launch {
//
//        }
//
//
//        CoroutineScope(Dispatchers.Default).launch {
//
//        }
//
//        CoroutineScope(EmptyCoroutineContext).launch {
//
//        }


        GlobalScope.launch {
            reRenderAndSaveThumbnail()
        }
    }

    private fun shouldBeReRendered(scaleLog: Int): Boolean {
        return when(scaleLog) {
            0 -> {
                val elapsed = currentTime() - lastUpdate
//                LOGGER.debug("Region: $location, elapsed: $elapsed, interval: $updateInterval, modified: $modifiedSinceRender, verdict: ${(elapsed >= updateInterval) && modifiedSinceRender}")
                (elapsed >= reRenderInterval) && modifiedSinceRender
            }
            regionThumbnailScaleLog -> {
                false
            }
            else -> true
        }
    }

    suspend fun getAndSaveThumbnail(): BufferedImage {
        return if (::renderedThumbnail.isInitialized && !shouldBeReRendered(regionThumbnailScaleLog))
            renderedThumbnail
        else {
            reRenderAndSaveThumbnail()
        }
    }

    private suspend fun reRenderAndSaveThumbnail(): BufferedImage {
        return _render(regionThumbnailScaleLog).also {
            withContext(Dispatchers.IO) {
                if (!thumbnailFile.parentFile.exists())
                    thumbnailFile.parentFile.mkdirs()
                ImageIO.write(it, "png", thumbnailFile) //TODO only save if not saved!!!!
            }
        }
    }

    suspend fun getRendered(): BufferedImage {
        return if (::rendered.isInitialized && !shouldBeReRendered(0))
            rendered
        else _render(0)
        //TODO if it's rendered but long time ago then maybe return previous result instantly and then update it? Return though callback twice?
    }

    suspend fun getCustomRender(scaleLog: Int): BufferedImage = _render(scaleLog)

    private suspend fun _render(scaleLog: Int = 0): BufferedImage = withContext(Dispatchers.Default) {
        val bitmap =
            BufferedImage(storageTileBlocks shr scaleLog, storageTileBlocks shr scaleLog, BufferedImage.TYPE_3BYTE_BGR)
        val scale = 1 shl scaleLog

        for (z in 0 until storageTileBlocks step scale) {
            for (x in 0 until storageTileBlocks step scale) {
                val block = decodeBlock(blockIdMap[z][x])
                val foliageColor = biomeManager.decodeBiomeFoliage(biomeMap[z][x])
                val baseBlockColor = Color(decodeBlockColor(blockIdMap[z][x]))
                val overlayBlock = decodeBlock(blockOverlayIdMap[z][x])
                val overlayBlockColor = if(waterBlocks.contains(overlayBlock))
                    biomeManager.decodeBiomeWaterColor(biomeMap[z][x])
                else
                    Color(decodeBlockColor(blockOverlayIdMap[z][x]))

                val normal = getNormalSharp(x, z)
                val depth = depthMap[z][x]

                var color = (if (foliageBlocks.contains(block)) {
                    baseBlockColor * foliageColor
                } else baseBlockColor) * normal.shade

                if (depth > 0 && !fastIgnoreLookup[blockOverlayIdMap[z][x].toInt()]) {
                    var waterColor = overlayBlockColor + -depth.toInt() * 4
                    waterColor = if (foliageBlocks.contains(overlayBlock))
                        waterColor * foliageColor
                    else
                        waterColor
                    color = waterColor.mixWeight(color, getDepthShade(depth))
                }
                bitmap.setRGB(x shr scaleLog, z shr scaleLog, color.toInt())
            }
        }
        if (scaleLog == 0) {
            rendered = bitmap
            lastUpdate = currentTime()
            modifiedSinceRender = false
        } else if (scaleLog == regionThumbnailScaleLog) {
            lastThumbnailUpdate = currentTime()
        }
//        if (scaleLog != 0) {
//            LOGGER.debug("RENDERED NON STANDARD $location, scale = $scaleLog")
//        }
        bitmap
    }

    private fun currentTime() = Clock.System.now().epochSeconds

    fun getNormalSmooth(x: Int, z: Int) = Normal(
        when (z) {
            0 -> (heightMap[1][x] - heightMap[0][x]) * 2
            storageTileBlocks - 1 -> (heightMap[storageTileBlocks - 1][x] - heightMap[storageTileBlocks - 2][x]) * 2
            else -> heightMap[z + 1][x] - heightMap[z - 1][x]
        },
        when (x) {
            0 -> (heightMap[z][1] - heightMap[z][0]) * 2
            storageTileBlocks - 1 -> (heightMap[z][storageTileBlocks - 1] - heightMap[z][storageTileBlocks - 2]) * 2
            else -> heightMap[z][x + 1] - heightMap[z][x - 1]
        }
    )

    fun getNormalSharp(x: Int, z: Int) = Normal(
        when (z) {
            0 -> (heightMap[1][x] - heightMap[0][x]) * 2
            else -> (heightMap[z][x] - heightMap[z - 1][x]) * 2
        },
        when (x) {
            0 -> (heightMap[z][1] - heightMap[z][0]) * 2
            else -> (heightMap[z][x] - heightMap[z][x - 1]) * 2
        }
    )

    fun getNormalShade(x: Int, z: Int) = Normal(
        when (z) {
            0 -> (heightMap[1][x] - heightMap[0][x]) * 2
            1 -> (heightMap[1][x] - heightMap[0][x]) * 2
            else -> heightMap[z][x] - heightMap[z - 2][x]
        },
        when (x) {
            0 -> (heightMap[z][1] - heightMap[z][0]) * 2
            1 -> (heightMap[z][1] - heightMap[z][0]) * 2
            else -> heightMap[z][x] - heightMap[z][x - 2]
        }
    )


    class Normal(val i: Int, val j: Int) {
        val shade: FloatColor
            get() {
                val iShade = atanLookupTable[i + maxHeight]
                val jShade = atanLookupTable[j + maxHeight]
                return FloatColor(
                    r = 1 + iShade * ri + jShade * rj,
                    g = 1 + iShade * gi + jShade * gj,
                    b = 1 + iShade * bi + jShade * bj
                )
            }

        companion object {
            const val ri = 0.8f
            const val gi = 0.6f
            const val bi = 0.2f
            const val rj = 1 - ri
            const val gj = 1 - gi
            const val bj = 1 - bi

            const val maxHeight = 384 * 2 //todo this is because 'sharp' normal map can have 2x as steep normals
            val atanLookupTable = FloatArray(maxHeight * 2) {
                atan((it - maxHeight) * 0.5f) * 0.35f
            }
        }
    }

    companion object {

        context(CurrentWorldProvider<WhyWorld>)
        fun GetIfExists(position: LocalTileRegion) = if (currentWorld.getFile(position).exists()) MapArea(position) else null

        context(CurrentWorldProvider<WhyWorld>)
        fun GetForWrite(position: LocalTileRegion) = MapArea(position)


        private val minecraftBlocks = Block.STATE_IDS.map { it.block.translationKey }.toSet().toTypedArray().sortedArray()
        private val blockNameMap = Block.STATE_IDS.map { it.block.defaultState }.associateBy { it.block.translationKey }
        private val fastIgnoreLookup = minecraftBlocks.map { shouldBlockOverlayBeIgnored(it) }.toTypedArray()
        val foliageBlocks = minecraftBlocks.filter {
            it.contains("vine") ||
                    it.contains("leaves") ||
                    it.contains("grass") ||
                    it.contains("sugar") ||
                    it.contains("fern") ||
                    it.contains("lily") ||
                    it.contains("bedrock")
        }.map { blockNameMap[it] }.toSet()

        val waterBlocks = minecraftBlocks.filter {
            it.contains("water")
        }.map { blockNameMap[it] }.toSet()

        val ignoreAlphaBlocks = minecraftBlocks.filter {
            it.contains("leaves")
        }.map { blockNameMap[it] }.toSet()

        val fastLookupBlocks = minecraftBlocks.map { blockNameMap[it]!! }.toTypedArray()
        val fastLookupBlockColor = fastLookupBlocks.map {
            ExperimentalTextureProvider.getBitmap(it.block)?.run {
                if(it in ignoreAlphaBlocks)
                    getAverageLeavesColor()
                else
                    getAverageColor()
            } ?: it.material.color.color
        }.toIntArray().also { LOGGER.warn("MISSING TEXTURES: ${ExperimentalTextureProvider.missingTextures}") }




//        val biomes = BiomeKeys::class.java.declaredFields.filter { Modifier.isStatic(it.modifiers) }.map {
//            val instance = it::class.java.newInstance()
//            it.get(instance)
//            instance as RegistryKey<Biome>
//        }
//        val biomes = Registry.BIOME_KEY
//        val biomes = Registry.BIOME_SOURCE
//        val biomees =

        fun encodeBlock(blockState: BlockState): Short {
            val defaultState = blockState.block.translationKey
            return minecraftBlocks.binarySearch(defaultState).toShort()
        }

        fun decodeBlock(id: Short) = fastLookupBlocks[id.toInt()]
        fun decodeBlockColor(id: Short) = fastLookupBlockColor[id.toInt()]

    }
}
