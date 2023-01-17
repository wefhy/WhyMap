// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.config

import dev.wefhy.whymap.tiles.region.FileVersionManager
import java.io.File
import java.time.format.DateTimeFormatter


/**
 * zoom 0 - 1 tile for whole map
 * zoom 1 - 1 tile per each quadrant
 * zoom 17 - 1 tile per region
 * zoom 22 - 1 tile per chunk
 * zoom 26 - 1 tile per block (max 2^26 = 67 108 864 blocks, 33 554 432 in each direction)
 *
 * zoom - blocks per tile
 * 26 - 1
 * 22 - 16
 * 20 - 64
 * 18 - 256
 * 16 - 1024
 * 15 - 2048
 * 12 - 16384
 */
@Suppress("MemberVisibilityCanBePrivate")
object WhyMapConfig {

    val DEV_VERSION = false

    val blockZoom = 26
    val blocksInChunkLog = 4
    val storageTileLog = 9
    val regionThumbnailScaleLog = 2
    val chunksPerRegionLog = storageTileLog - blocksInChunkLog
    val chunkZoom = blockZoom - blocksInChunkLog // 22
    val regionZoom = blockZoom - storageTileLog // 17
    val thumbnailZoom = regionZoom - regionThumbnailScaleLog // 15
    val n_blocks = 1 shl blockZoom //67M
    val n_chunks = 1 shl chunkZoom //4M
    val n_regions = 1 shl regionZoom //131k
    val n_thumbnails = 1 shl thumbnailZoom //32k
    val block_per_quadrant = 1 shl (blockZoom - 1) //33M
    val chunk_per_quadrant = 1 shl (chunkZoom - 1) //2M
    val regionsPerQuadrant = 1 shl (regionZoom - 1) //65k

    val storageTileBlocks = 1 shl storageTileLog //512
    val storageTileBlocksSquared = storageTileBlocks * storageTileBlocks //512*512 = 262k
    val storageTileChunks = storageTileBlocks shr blocksInChunkLog // 32
    val nativeZoomLevel = blockZoom - storageTileLog //17

    val tileResolution = storageTileBlocks //512
    val regionThumbnailResolution = tileResolution shr regionThumbnailScaleLog

    val tileMetadataSize = 16 // bytes
    val latestFileVersion = FileVersionManager.WhyMapFileVersion.latest

    val reRenderInterval = 1 // seconds
    val cleanupInterval = 60 // seconds
    val unloadDistance = 1024 //blocks

    val minecraftPath = File("")
    val modPath = minecraftPath.resolve("WhyMap")
    val mappingsExportDir = modPath.resolve("mappings-export")
    val logsPath = modPath.resolve("logs")
    val logsDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val logsEntryTimeFormatter = DateTimeFormatter.ofPattern("HHmmss.SSS")

    val webExportDirectory = modPath.resolve("WebExport")
    val currentWorldName = "CurrentWorldName"
    val mapLink = "http://localhost:7542"
}