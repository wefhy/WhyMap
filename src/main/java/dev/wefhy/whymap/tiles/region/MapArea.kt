// Copyright (c) 2022-2023 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.communication.BlockData
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.decodeBlock
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.decodeBlockColor
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.encodeBlock
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.fastIgnoreLookup
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.foliageBlocksSet
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.ignoreDepthTint
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.isOverlay
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.waterBlocks
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.waterLoggedBlocks
import dev.wefhy.whymap.config.WhyMapConfig.blocksInChunkLog
import dev.wefhy.whymap.config.WhyMapConfig.legacyMetadataSize
import dev.wefhy.whymap.config.WhyMapConfig.metadataSize
import dev.wefhy.whymap.config.WhyMapConfig.reRenderInterval
import dev.wefhy.whymap.config.WhyMapConfig.regionThumbnailScaleLog
import dev.wefhy.whymap.config.WhyMapConfig.storageTileBlocks
import dev.wefhy.whymap.config.WhyMapConfig.storageTileBlocksSquared
import dev.wefhy.whymap.config.WhyMapConfig.storageTileChunks
import dev.wefhy.whymap.events.ChunkUpdateQueue
import dev.wefhy.whymap.events.RegionUpdateQueue
import dev.wefhy.whymap.events.ThumbnailUpdateQueue
import dev.wefhy.whymap.migrations.BiomeMapping
import dev.wefhy.whymap.migrations.BlockMapping
import dev.wefhy.whymap.migrations.FileMetadataManager.decodeMetadata
import dev.wefhy.whymap.migrations.MappingsManager
import dev.wefhy.whymap.migrations.MappingsManager.Companion.recognizeLegacyVersion
import dev.wefhy.whymap.migrations.MappingsManager.WhyMapLegacyMetadata
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.utils.ObfuscatedLogHelper.obfuscateObjectWithCommand
import dev.wefhy.whymap.whygraphics.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
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
import java.io.PushbackInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentLinkedQueue
import javax.imageio.ImageIO
import kotlin.math.atan

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
//    val exists = Array(storageTileChunks) { Array(storageTileChunks) { false } } // 1 bit

    val file = currentWorld.getFile(location)
    val thumbnailFile = currentWorld.getThumbnailFile(location)

    val lightingProvider = MinecraftClient.getInstance().world!!.lightingProvider // TODO context receiver for world!
    val biomeAccess get() = MinecraftClient.getInstance().world?.biomeAccess // TODO context receiver for world!
    lateinit var rendered: BufferedImage
    lateinit var renderedWhyImage: WhyTiledImage
    lateinit var renderedThumbnail: BufferedImage
    var lastUpdate = 0L
//    var lastNativeUpdate = 0L
    var lastThumbnailUpdate = 0L
    var nativeRenderInProgress = false //TODO atomic(false)
    var regionRelativeChunkUpdateQueue: ConcurrentLinkedQueue<LocalTileChunk> = ConcurrentLinkedQueue() // best queue type for this use case https://www.baeldung.com/java-concurrent-queues

    val areaCoroutineContext = SupervisorJob() + WhyDispatchers.Render
    val mapAreaScope = CoroutineScope(SupervisorJob() + WhyDispatchers.Render) //TODO create scope from parent
//    val mapAreaScope = CoroutineScope(Job()) //TODO create scope from parent

    init {
        currentWorld.writeToLog("Initialized ${obfuscateObjectWithCommand(location, "init")}, file exists: ${file.exists()}")
        if (file.exists())
            load()
//        else
//            UpdateQueue.addUpdate(location.x, location.z)
    }

    private inline fun<reified T> returnArrayFragment(position: ChunkPos, block: (startX: Int, z: Int) -> T): Array<T>? {
        //TODO load only if in exists array; save exists array to file
        if ((position.regionX != location.x) || (position.regionZ != location.z)) return null
        val startX = position.regionRelativeX shl 4
        val startZ = position.regionRelativeZ shl 4
        return Array(16) { z ->
            block(startX, startZ + z)
        }
    }

    private inline fun<reified T> Array<ShortArray>.getChunk(position: ChunkPos, block: (ShortArray) -> T): Array<T>? {
        return returnArrayFragment(position) { startX, z ->
            block(get(z).sliceArray(startX until (startX + 16)))
        }
    }

    private inline fun<reified T> Array<ByteArray>.getChunk(position: ChunkPos, block: (ByteArray) -> T): Array<T>? {
        return returnArrayFragment(position) { startX, z ->
            block(get(z).sliceArray(startX until (startX + 16)))
        }
    }

    private inline fun <reified T> generateChunk(position: ChunkPos, block: (x: Int, z: Int) -> T): Array<Array<T>>? {
        return returnArrayFragment(position) { startX, z ->
            Array(16) { x ->
                block(startX + x, z)
            }
        }
    }

    fun getChunk(position: ChunkPos) = blockIdMap.getChunk(position) { it.map{ decodeBlock(it)} }

    fun getChunkOverlay(position: ChunkPos) = blockOverlayIdMap.getChunk(position) { it.map { decodeBlock(it) } }

    fun getChunkBiomeFoliageAndWater(position: ChunkPos) = biomeMap.getChunk(position) { it.map {
        biomeManager.decodeBiomeFoliage(it) to biomeManager.decodeBiomeWaterColor(it)
    } }

    fun getChunkHeightmap(position: ChunkPos) = heightMap.getChunk(position) { it }

    fun getChunkLightmap(position: ChunkPos) = lightMap.getChunk(position) { it }

    fun getChunkDepthmap(position: ChunkPos) = depthMap.getChunk(position) { it }

    fun getChunkNormals(position: ChunkPos) = generateChunk(position) { x, z ->
        getNormalSharp(x, z)
    }

    suspend fun save() = withContext(WhyDispatchers.IO) {
        currentWorld.writeToLog("Saving ${obfuscateObjectWithCommand(location, "save")}, file existed: ${file.exists()}")
        if (!modifiedSinceSave)
            return@withContext
        val packedData = packData()
//        val uncompressedFile = file.parentFile.resolve(file.name + ".uncompressed")
//        uncompressedFile.writeBytes(packedData)
        file.useAtomicProxy {
            outputStream().use {
                it.write(currentWorld.mappingsManager.metadata)
                withContext(WhyDispatchers.LowPriority) {
                    packedData.compress().writeTo(it)
                }//.toByteArray()
            }
        }
        reRenderAndSaveThumbnail()
        LOGGER.debug("SAVED: ${file.absolutePath}")
//        MinecraftClient.getInstance().textureManager.getTexture()
        modifiedSinceSave = false
    }

    private fun packData(): ByteArray {
        val data = ByteArray(storageTileBlocksSquared * 9)
        val byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder())
        val shortBuffer = byteBuffer.asShortBuffer()
        for (y in 0 until storageTileBlocks) {
            shortBuffer.put(blockIdMap[y])       //2
            shortBuffer.put(blockOverlayIdMap[y])//2
            shortBuffer.put(heightMap[y])        //2
        }
        byteBuffer.position(storageTileBlocksSquared * 6)
        for (y in 0 until storageTileBlocks) {
            byteBuffer.put(biomeMap[y])        //1
            byteBuffer.put(lightMap[y])        //1
            byteBuffer.put(depthMap[y])        //1
        }
        return data
    }

    private fun ByteArray.compress(): ByteArrayOutputStream {
        val xzOutput = ByteArrayOutputStream()
        XZOutputStream(xzOutput, LZMA2Options(3)).use { xz ->
            xz.write(this)
            xz.close()
        }
        return xzOutput
    }

    private fun load() {
        currentWorld.writeToLog("Loading ${obfuscateObjectWithCommand(location, "load")}, file existed: ${file.exists()}")
        try {
            PushbackInputStream(file.inputStream(), metadataSize).use {
                val metabytes = ByteArray(metadataSize)
                it.read(metabytes)
                val metadata = decodeMetadata(metabytes)
                if (metadata == null) {
                    it.unread(metabytes)
                }

                val data = ByteArray(storageTileBlocksSquared * 9)
                val mappingsSet = if (metadata != null) {
                    XZInputStream(it).use { xz ->
                        xz.read(data)
                        xz.close()
                    }
                    currentWorld.mappingsManager.getMappings(metadata)
                } else {
                    MappingsManager.MappingsSet(
                        XZInputStream(it).use { xz ->
                            val legacyMetadata = ByteArray(legacyMetadataSize)
                            xz.read(legacyMetadata)
                            val version = recognizeLegacyVersion(WhyMapLegacyMetadata(legacyMetadata)) ?: BlockMapping.WhyMapBeta
                            if (version == BlockMapping.WhyMapBeta) { // Support WhyMap versions before 0.9.2 which didn't carry legacyMetadata
                                legacyMetadata.copyInto(data)
                                xz.read(data, legacyMetadata.size, data.size - legacyMetadata.size)
                            } else {
                                xz.read(data)
                            }
                            xz.close()
                            version
                        },
                        BiomeMapping.LegacyBiomeMapping
                    )
                }
                val blockMapping = mappingsSet.blockMappings
                val biomeMapping = mappingsSet.biomeMappings

                val byteBuffer = ByteBuffer.wrap(data).let {
                    if (metadata == null || metadata.fileVersion < 3)
                        it
                    else
                        it.order(metadata.byteOrder.order)
                }
                val shortBuffer = byteBuffer.asShortBuffer()

                for (y in 0 until storageTileBlocks) {
                    shortBuffer.get(blockIdMap[y])
                    shortBuffer.get(blockOverlayIdMap[y])
                    shortBuffer.get(heightMap[y])
                }

                byteBuffer.position(storageTileBlocksSquared * 6)

                for (y in 0 until storageTileBlocks) {
                    byteBuffer.get(biomeMap[y])
                    byteBuffer.get(lightMap[y])
                    byteBuffer.get(depthMap[y])
                }
                if (blockMapping == null || !blockMapping.isCurrent) {
                    val remapLookup = if (blockMapping != null)
                        currentWorld.mappingsManager.getCurrentRemapLookup(blockMapping)
                    else
                        currentWorld.mappingsManager.unsupportedAntiNPEBlockRemapLookup
                    blockOverlayIdMap.mapInPlace { i -> remapLookup.getOrElse(i.toInt()) { 0 } }
                    blockIdMap.mapInPlace { i -> remapLookup.getOrElse(i.toInt()) { 0 } }
                    modifiedSinceSave = true
                }
                if (biomeMapping == null || !biomeMapping.isCurrent) {
                    val remapLookup = if (biomeMapping != null)
                        currentWorld.mappingsManager.getCurrentRemapLookup(biomeMapping)
                    else
                        currentWorld.mappingsManager.unsupportedAntiNPEBiomeRemapLookup
                    biomeMap.mapInPlace { i -> remapLookup.getOrElse(i.toUByte().toInt()) { 0 }.toByte() }
                    modifiedSinceSave = true
                }
            }
        } catch (e: EOFException) {
            currentWorld.writeToLog("ERROR Loading ${obfuscateObjectWithCommand(location, "error")}")
            LOGGER.error("ERROR LOADING TILE: ${file.absolutePath}")
        } catch (e: IndexOutOfBoundsException) {
            currentWorld.writeToLog("ERROR Upgrading ${obfuscateObjectWithCommand(location, "error")}")
            LOGGER.error("ERROR UPGRADING TILE: ${file.absolutePath}\n ${e.message}\n ${e.stackTraceToString()}")
        }
    }

    private fun Chunk.getOceanFloorHeightMapHotFix(): Array<IntArray> {
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
//                while (!getBlockState(mutablePosition).material.isSolid && (y > bottomY) && (fastOverlayLookup.contains(getBlockState(mutablePosition).block.defaultState))) { //TODO or isOpaque
                while (isOverlay(getBlockState(mutablePosition)) && (y > bottomY)) { //TODO or isOpaque
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
            light = lightMap[z][x].toUByte()
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

        val worldLightView = lightingProvider[LightType.BLOCK]
//        val surface = chunkGetSurface(chunk, Heightmap.Type.OCEAN_FLOOR)
//        val heightmapFloor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR)
        val heightmapFloorTmp = chunk.getOceanFloorHeightMapHotFix() // TODO this should be replaced by heightmapFloor when it works
        val heightmapSurface =
            chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE) //TODO I should generate heightmap myself so I can ignore certain blocks completely (like vines, string etc) and not have them as overlays
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
                val biome = biomeAccess?.getBiome(absoluteBlockPos)?.value()?.takeUnless { biomeManager.isPlains(it) }
                    ?: biomeAccess?.getBiomeForNoiseGen(absoluteBlockPos)?.value()


                blockIdMap[regionRelativeZ][regionRelativeX] = encodeBlock(block)
                blockOverlayIdMap[regionRelativeZ][regionRelativeX] = encodeBlock(overlayBlock)
                biomeMap[regionRelativeZ][regionRelativeX] = biome?.let { biomeManager.encodeBiome(it) } ?: 0
                heightMap[regionRelativeZ][regionRelativeX] = posY.toShort()
                lightMap[regionRelativeZ][regionRelativeX] = light.toByte()
                depthMap[regionRelativeZ][regionRelativeX] = depth.coerceIn0255().toUByte().toByte()
                //TODO render chunk right away? Re-render nearby chunk edges? Queue it at least? Mark that chunk was changed?
//                MinecraftClient.getInstance().world!!.biomeAccess.

            }
        }
        modifiedSinceSave = true
        modifiedSinceRender = true
        regionRelativeChunkUpdateQueue.add(LocalTile.Chunk(chunk.pos.regionRelativeX, chunk.pos.regionRelativeZ))

//        CoroutineScope(Job()).launch {
//
//        }
//
//        CoroutineScope(Dispatchers.Default).launch {
//
//        }
//
//        CoroutineScope(EmptyCoroutineContext).launch {
//
//        }

        @OptIn(DelicateCoroutinesApi::class) //We want the thumbnail to be saved anyway
        GlobalScope.launch {
            RegionUpdateQueue.addUpdate(location)
            ChunkUpdateQueue.addUpdate(chunk.pos.x, chunk.pos.z)
            //reRenderAndSaveThumbnail() //TODO also uncache it somehow TODO IT ALSO SHOULDN'T BE CALLED EVERY TIME A CHUNK IS UPDATED
            val thumbnail = location.parent(TileZoom.ThumbnailZoom)
            ThumbnailUpdateQueue.addUpdate(thumbnail.x, thumbnail.z)
        }
    }

    private fun nativeUpdateChunks() {
        var chunk: LocalTile<TileZoom.ChunkZoom>
        while(true) {
            chunk = regionRelativeChunkUpdateQueue.poll() ?: break
            nativeUpdateChunk(chunk)
        }
    }

    private fun nativeUpdateChunk(localTileChunk: LocalTileChunk) {
        val regionRelativeChunk = LocalTile.Chunk(
            localTileChunk.x and (storageTileChunks - 1),
            localTileChunk.z and (storageTileChunks - 1)
        )

        val startBlockX = regionRelativeChunk.x shl blocksInChunkLog
        val startBlockZ = regionRelativeChunk.z shl blocksInChunkLog

        val renderedChunk = WhyTile { y, x ->
            calculateColor(
                startBlockZ + y,
                startBlockX + x,
                ::getNormalSharpChunk
            )
        }
        renderedWhyImage.data[regionRelativeChunk.z][regionRelativeChunk.x] = renderedChunk
    }

//    private fun nativeShouldBeReRendered(): Boolean {
//        val elapsed = currentMillis() - lastNativeUpdate
//        return (elapsed >= nativeReRenderInterval) && modifiedSinceNativeRender
//    }

    private fun shouldBeReRendered(scaleLog: Int): Boolean {
        return when (scaleLog) {
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
            withContext(WhyDispatchers.LowPriority) {
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
        //TODO if it's rendered but long time ago then maybe return previous result instantly and then update it? Return though callback twice? Maybe stateflow?
    }

    suspend fun getCustomRender(scaleLog: Int): BufferedImage = _render(scaleLog)

    fun renderWhyImageNow(): WhyTiledImage {
        return if (::renderedWhyImage.isInitialized)
            renderedWhyImage
        else {
//            nativeUpdateChunks() TODO maybe uncomment? Depends on the usecase
            nativeRenderInProgress = true
            _renderWhyImage().also { nativeRenderInProgress = false }
        }
    }

    fun renderWhyImageBuffered(): WhyTiledImage? {
        return if (!::renderedWhyImage.isInitialized) {
            launchWhyRender()
//            valStatPrintLog("waiting")
            null
        } else {
            mapAreaScope.launch {
                nativeUpdateChunks()
            }
//            valStatPrintLog("success")
            renderedWhyImage
        }
    }

    private fun launchWhyRender() {
        if (!nativeRenderInProgress) {
            nativeRenderInProgress = true
            mapAreaScope.launch {
                _renderWhyImage()
                nativeRenderInProgress = false
            }
        }
    }

    private fun _renderWhyImage(): WhyTiledImage {
        var failCounter = 0
        val image = WhyTiledImage.BuildForRegion { y, x ->
            try {
                calculateColor(y, x, ::getNormalSharp)
            } catch (_: IndexOutOfBoundsException) {
                failCounter++
                WhyColor.Transparent
            }
        }
        if (failCounter > 0)
            println("Failed to render $failCounter pixels in native map area (${location.x}, ${location.z})")
//        lastNativeUpdate = currentMillis()
        renderedWhyImage = image
        return image
    }

    private suspend fun _render(scaleLog: Int = 0): BufferedImage = withContext(areaCoroutineContext) {
        val bitmap = BufferedImage(storageTileBlocks shr scaleLog, storageTileBlocks shr scaleLog, BufferedImage.TYPE_3BYTE_BGR)
        val raster = bitmap.raster!!
        val scale = 1 shl scaleLog
        var failCounter = 0

        for (z in 0 until storageTileBlocks step scale) {
            ensureActive()
            for (x in 0 until storageTileBlocks step scale) {
                try { //TODO try block disables JVM optimizations, check whether it's worth using inside loop
                    val color = calculateColor(z, x, ::getNormalSharp)
                    val bitmapX = x shr scaleLog
                    val bitmapY = z shr scaleLog
//                    bitmap.setRGB(bitmapX, bitmapY, color.intRGB)
                    raster.setPixel(bitmapX, bitmapY, color.intArrayRGB)
//                    raster.setSample(bitmapX, bitmapY, 0, color.intR)
//                    raster.setSample(bitmapX, bitmapY, 1, color.intG)
//                    raster.setSample(bitmapX, bitmapY, 2, color.intB)
//                    raster.setSample(bitmapX, bitmapY, 3, color.intA)
                } catch (_: IndexOutOfBoundsException) {
                    failCounter++
//                    print("Failed to render web map area (${location.x}, ${location.z}), s: $scaleLog")
//                    OccurenceCounter.addAndPrintEvery100("rendering with scale $scaleLog")
                }
            }
        }
        if (failCounter > 0)
            println("Failed to render $failCounter pixels in web map area (${location.x}, ${location.z}), s: $scaleLog")

        if (scaleLog == 0) {
            rendered = bitmap
            lastUpdate = currentTime()
            modifiedSinceRender = false
        } else if (scaleLog == regionThumbnailScaleLog) {
            lastThumbnailUpdate = currentTime()
        }
        bitmap
    }

    fun analyzeRegion(
        localStartX: Int = 0,
        localStartZ: Int = 0,
        localEndX: Int = storageTileBlocks,
        localEndZ: Int = storageTileBlocks
    ): EncodedRegionStatistics {

        val blockCounter = mutableMapOf<Short, Int>()
        val overlayBlockCounter = mutableMapOf<Short, Int>()
        val biomeCounter = mutableMapOf<Byte, Int>()

        for(z in localStartZ..<localEndZ) {
            for(x in localStartX..<localEndX) {
                val block = blockIdMap[z][x]
                val overlayBlock = blockOverlayIdMap[z][x]
                val biome = biomeMap[z][x]
//                val height = heightMap[z][x]
//                val light = lightMap[z][x]
                val depth = depthMap[z][x]

                blockCounter[block] = (blockCounter[block] ?: 0) + 1
                if (depth != 0.toByte()) overlayBlockCounter[overlayBlock] = (overlayBlockCounter[overlayBlock] ?: 0) + 1
                biomeCounter[biome] = (biomeCounter[biome] ?: 0) + 1
            }
        }
        return EncodedRegionStatistics(
            blockCounter,
            overlayBlockCounter,
            biomeCounter
        )
    }

    private fun calculateColor(z: Int, x: Int, normalFinder: (z: Int, x: Int) -> Normal): WhyColor {
        val blockId = blockIdMap[z][x]
        val overlayId = blockOverlayIdMap[z][x]
        if (blockId == 0.toShort() && overlayId == 0.toShort()) return WhyColor.Transparent
        val biomeId = biomeMap[z][x]

        val block = decodeBlock(blockId)
        val foliageColor = biomeManager.decodeBiomeFoliage(biomeId)
        val baseBlockColor = decodeBlockColor(blockId)
        val overlayBlock = decodeBlock(overlayId)
        val overlayBlockColor = if (waterBlocks.contains(overlayBlock) || waterLoggedBlocks.contains(overlayBlock))
            biomeManager.decodeBiomeWaterColor(biomeId)
        else
            decodeBlockColor(overlayId) //TODO overlays should use correct alpha - it's not handled at all for now :(

        val normal = normalFinder(x, z)
        val depth = depthMap[z][x].toUByte()

        var color = (if (foliageBlocksSet.contains(block)) {
            baseBlockColor * foliageColor
        } else baseBlockColor) * normal.shade

        if (depth > 0u && !fastIgnoreLookup[overlayId.toInt()]) {
            val depthTint = if (!ignoreDepthTint.contains(overlayBlock)) {
                -depth.toInt() * 4
            } else 0

            var waterColor = overlayBlockColor + depthTint
            waterColor = if (foliageBlocksSet.contains(overlayBlock))
                waterColor * foliageColor
            else if (waterLoggedBlocks.contains(overlayBlock))
                waterColor.alphaOver(decodeBlockColor(overlayId))
            else
                waterColor
            color = waterColor.mixWeight(color, getDepthShade(depth))
        }
        return color
    }


    private fun currentTime() = Clock.System.now().epochSeconds
    private fun currentMillis() = Clock.System.now().toEpochMilliseconds()

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

    fun getNormalSharpChunk(x: Int, z: Int) = Normal(
        when (z and 0xF) {
            0 -> (heightMap[z + 1][x] - heightMap[z][x]) * 2
            else -> (heightMap[z][x] - heightMap[z - 1][x]) * 2
        },
        when (x and 0xF) {
            0 -> (heightMap[z][x + 1] - heightMap[z][x]) * 2
            else -> (heightMap[z][x] - heightMap[z][x - 1]) * 2
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
        val shade: WhyColor
            get() {
                val iShade = atanLookupTable[i + maxHeight]
                val jShade = atanLookupTable[j + maxHeight]
//                val iShade = atanLookupTable.getOrElse(i + maxHeight) { atanLookupTable[maxHeight] }
//                val jShade = atanLookupTable.getOrElse(j + maxHeight) { atanLookupTable[maxHeight] }
                return WhyColor(
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

    }
}
