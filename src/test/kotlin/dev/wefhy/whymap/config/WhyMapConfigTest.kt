package dev.wefhy.whymap.config

import dev.wefhy.whymap.config.WhyMapConfig.blockZoom
import dev.wefhy.whymap.config.WhyMapConfig.blocksInChunkLog
import dev.wefhy.whymap.config.WhyMapConfig.chunkZoom
import dev.wefhy.whymap.config.WhyMapConfig.storageTileLog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhyMapConfigTest {
    @Test
    fun testConstants() {
        assertEquals(26, blockZoom)
        assertEquals(4, blocksInChunkLog)
        assertEquals(9, storageTileLog)
        assertEquals(2, WhyMapConfig.regionThumbnailScaleLog)
        assertEquals(5, WhyMapConfig.chunksPerRegionLog)
        assertEquals(22, chunkZoom)
        assertEquals(17, WhyMapConfig.regionZoom)
        assertEquals(15, WhyMapConfig.thumbnailZoom)
        assertEquals(67108864, WhyMapConfig.n_blocks)
        assertEquals(4194304, WhyMapConfig.n_chunks)
        assertEquals(131072, WhyMapConfig.n_regions)
        assertEquals(32768, WhyMapConfig.n_thumbnails)
        assertEquals(33554432, WhyMapConfig.block_per_quadrant)
        assertEquals(2097152, WhyMapConfig.chunk_per_quadrant)
        assertEquals(65536, WhyMapConfig.regionsPerQuadrant)
        assertEquals(512, WhyMapConfig.storageTileBlocks)
        assertEquals(262144, WhyMapConfig.storageTileBlocksSquared)
        assertEquals(32, WhyMapConfig.storageTileChunks)
        assertEquals(17, WhyMapConfig.nativeZoomLevel)
        assertEquals(512, WhyMapConfig.tileResolution)
        assertEquals(128, WhyMapConfig.regionThumbnailResolution)
        assertEquals(16, WhyMapConfig.legacyMetadataSize)
        assertEquals(64, WhyMapConfig.metadataSize)
        assertEquals(250, WhyMapConfig.nativeReRenderInterval)
        assertEquals(1, WhyMapConfig.reRenderInterval)
        assertEquals(60, WhyMapConfig.cleanupInterval)
        assertEquals(1024, WhyMapConfig.unloadDistance)
    }
}