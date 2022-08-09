// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.CurrentWorld
import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.config.WhyMapConfig.unloadDistance
import dev.wefhy.whymap.utils.LocalTileBlock
import dev.wefhy.whymap.utils.LocalTileRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.util.math.Vec3d
import java.lang.ref.WeakReference
import kotlin.math.hypot

context(CurrentWorldProvider<WhyWorld>)
@Suppress("NOTHING_TO_INLINE")
class MapRegionManager {

    private val regions = mutableMapOf<LocalTileRegion, MapArea>()

    private val regionsWeakCache = mutableMapOf<LocalTileRegion, WeakReference<MapArea>>()

    private inline fun getLoadedOrCached(position: LocalTileRegion) = regions[position] ?: regionsWeakCache[position]?.get()

    private suspend fun unload(position: LocalTileRegion) {
        val region = regions[position]
        if (region != null) {
            regions.remove(position)
            region.save()
            regionsWeakCache[position] = WeakReference(region)
        }
    }

    private inline fun getOrPut(position: LocalTileRegion, defaultValue: () -> MapArea): MapArea {
        return regions.getOrPut(position) {
            regionsWeakCache[position]?.get() ?: defaultValue()
        }
    }

    private inline fun getOrPutWeak(position: LocalTileRegion, defaultValue: () -> MapArea?): MapArea? {
        return getLoadedOrCached(position)
            ?: run {
                val value = defaultValue()
                if (value != null)
                    regionsWeakCache[position] = WeakReference(value)
                value
            }
    }

//    fun getRegionForWrite(position: LocalTileRegion) = regions.getOrPut(position) { MapArea(position) }

    fun getRegionForWriteAndLoad(position: LocalTileRegion) = getOrPut(position) { MapArea(position).apply { if (file.exists()) load() } }

    fun getLoadedRegionForRead(position: LocalTileRegion) = getLoadedOrCached(position)

    fun peekRegion(position: LocalTileRegion): MapArea? = getOrPutWeak(position) {
        if (getFile(position).exists())
            MapArea(position).apply { load() }
        else null
    }

    suspend fun periodicCleanup() {
        cleanupRegions()
        cleanupEmptyWeakRefs()
    }

    fun LocalTileBlock.distanceTo(pos: Vec3d) = hypot(pos.x - x, pos.z - z)

    private suspend fun cleanupRegions() = withContext(Dispatchers.Default) {
        val regionsToCleanup = (currentWorld as? CurrentWorld)?.player?.pos?.let { playerPos ->
            regions.keys.filter {
                unloadDistance < hypot(playerPos.x - it.x, playerPos.z - it.z)
            }
        } ?: regions.keys.toList()
        regionsToCleanup.map { async { unload(it) } }.forEach { it.await() }
    }

    private fun cleanupEmptyWeakRefs() {
        val toFree = regionsWeakCache.filter { it.value.get() == null }
        for (entry in toFree) {
            regionsWeakCache.remove(entry.key)
        }
    }

    inline fun getRegionForTilesRendering(position: LocalTileRegion): MapArea? {
        return peekRegion(position)
    }


    //TODO move this function to CurrentWorld
    fun getFile(position: LocalTileRegion) = currentWorld.mapTilesPath.resolve("Region_${position.x}_${position.z}.whymap")

    //TODO move this function to CurrentWorld
    fun getThumbnailFile(position: LocalTileRegion) = currentWorld.mapThumbnailsPath.resolve("Region_${position.x}_${position.z}.png")

    fun saveAllAndClear() {
        runBlocking {
//            regions.values.parallelStream().forEach{launch { it.save() }} //TODO async saving should be possible with some locking?
            regions.values.map { async { it.save() } }.forEach { it.await() }
            regions.clear()
            regionsWeakCache.clear()
            LOGGER.debug("SAVED ALL TILES!")
            println("WhyMap: SAVED ALL TILES!")
        }
    }

}