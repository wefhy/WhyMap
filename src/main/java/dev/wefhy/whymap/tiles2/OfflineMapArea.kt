// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles2

import dev.wefhy.whymap.communication.quickaccess.BiomeManager
import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.utils.LocalTileRegion
import net.minecraft.client.MinecraftClient
import net.minecraft.world.chunk.Chunk

class WhyWorldProvider<out T : OfflineWhyWorld>(val currentWorld: T)

abstract class OfflineWhyWorld(
    val name: String,
    val dimensionName: String
) {
    val worldPath by lazy { WhyMapConfig.modPath.resolve(name).resolve(dimensionName) }
    val mapTilesPath by lazy { worldPath.resolve("tiles") }

    fun getFile(location: LocalTileRegion) = mapTilesPath.resolve("Region_${location.x}_${location.z}.whymap")
}

class BlockManager() {

}

class MinecraftStuffProvider(
    val mc: MinecraftClient,
    val biomeManager: BiomeManager,
    val blockManager: BlockManager,
) {
    val world = mc.world!!
    val biomeAccess = world.biomeAccess
    val lightingProvider = world.lightingProvider
}

context (WhyWorldProvider<OfflineWhyWorld>)
class OfflineRegion private constructor(val location: LocalTileRegion) {
    val blockIdMap: Array<ShortArray> = Array(WhyMapConfig.storageTileBlocks) { ShortArray(WhyMapConfig.storageTileBlocks) { 0 } } // at least 12 bits, possibly 16
    val blockOverlayIdMap: Array<ShortArray> = Array(WhyMapConfig.storageTileBlocks) { ShortArray(WhyMapConfig.storageTileBlocks) { 0 } } // at least 12 bits, possibly 16
    val heightMap: Array<ShortArray> = Array(WhyMapConfig.storageTileBlocks) { ShortArray(WhyMapConfig.storageTileBlocks) { 0 } } // at least 9 bits
    val biomeMap: Array<ByteArray> = Array(WhyMapConfig.storageTileBlocks) { ByteArray(WhyMapConfig.storageTileBlocks) { 0 } } // at least 7 bits, possibly 8
    val lightMap: Array<ByteArray> = Array(WhyMapConfig.storageTileBlocks) { ByteArray(WhyMapConfig.storageTileBlocks) { 0 } } // at least 4 bits
    val depthMap: Array<ByteArray> = Array(WhyMapConfig.storageTileBlocks) { ByteArray(WhyMapConfig.storageTileBlocks) { 0 } } // at least 8 bits

    val file = currentWorld.getFile(location)
    var modifiedSinceSave = false

    init {
        if (file.exists())
            load()
    }

    //TODO store data by chunks. It's not that bad, even normal calculation can be working fine

    fun load() {}
    fun save() {}

    context(MinecraftStuffProvider)
    fun updateChunk(chunk: Chunk) {
        modifiedSinceSave = true
    }

}