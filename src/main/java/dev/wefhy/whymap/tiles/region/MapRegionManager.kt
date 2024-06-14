// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.CurrentWorld
import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.tiles.region.MapAreaAccess.LoadPriority.*
import dev.wefhy.whymap.tiles.thumbnails.EmptyThumbnailProvider
import dev.wefhy.whymap.tiles.thumbnails.RenderedThumbnailProvider
import dev.wefhy.whymap.utils.LocalTileRegion
import dev.wefhy.whymap.utils.WhyDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.client.MinecraftClient
import java.util.concurrent.ConcurrentHashMap

context(CurrentWorldProvider<WhyWorld>)
class MapRegionManager {

    private val regionLoaders = ConcurrentHashMap<LocalTileRegion, MapAreaAccess>()
//    private val lock = getLockForName(activeWorld!!.name + activeWorld!!.dimensionName)

    /**
    This is copy of library function but as it uses ConcurrentHashMap, it correctly solves nullability
    (library defined getOrPut is generic and doesn't take into account that Concurrent implementation can't hold null values)
     */
    inline fun <K, V> ConcurrentHashMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
        // Do not use computeIfAbsent on JVM8 as it would change locking behavior
        return this[key] ?: defaultValue().let { default -> this.putIfAbsent(key, default) ?: default }
    }

//    private suspend fun<T : MapArea?> queueRegionAccessEvent(priority: Unit, event: () -> T): T {
//
//    }

    internal suspend inline fun <T> getRegionForWriteAndLoad(position: LocalTileRegion, block: MapArea.() -> T): T {
        val regionLoader = regionLoaders.getOrPut(position) { MapAreaAccess.GetForWrite(position) }
        return regionLoader.withLoadedForWrite {
            block()
        }
    }

    suspend fun <T> getLoadedRegionForRead(position: LocalTileRegion, block: MapArea.() -> T?): T? {
        val regionLoader = regionLoaders.getOrPut(position) { MapAreaAccess.GetIfExists(position) ?: return null }
        return regionLoader.withLoaded(PEEK_IF_LOADED) {
            block()
        }
    }

    suspend fun periodicCleanup() {
        cleanupRegions()
        cleanupEmptyWeakRefs()
    }

    private suspend fun cleanupRegions() = withContext(WhyDispatchers.LowPriority) {
        // TODO make sure this runs on correct dispatcher to avoid context switching
        val playerPos = MinecraftClient.getInstance()?.player?.pos
        regionLoaders.values.map { launch { it.clean(playerPos) } }.forEach { it.join() }
    }

    private fun cleanupEmptyWeakRefs() {
        // TODO can this be done thread safe? Does this even matter?
        // Keeping it would be at worst a few thousand kilobytes of data but might make map access times longer
    }

    internal suspend inline fun <T> getRegionForTilesRendering(position: LocalTileRegion, block: MapArea.() -> T): T? {
        //Even though this is ConcurrentMap, the function here can be invoked even if not needed - this will create additional instance of MapAreaAccess
        //ComputeIfAbsent would be better but then there's no way to cancel the computation if file does not exist
        val regionLoader = regionLoaders.getOrPut(position) { MapAreaAccess.GetIfExists(position) ?: return null }
        return regionLoader.withLoaded(LOAD_AND_PEEK) {
            block()
        }
    }

    internal suspend inline fun <T> getRegionForMinimapRendering(position: LocalTileRegion, block: MapArea.() -> T): T? {
        val regionLoader = regionLoaders.getOrPut(position) { MapAreaAccess.GetIfExists(position) ?: return null }
        return regionLoader.withLoaded(LOAD_AND_KEEP) {
            block()
        }
    }

    fun saveAllAndClear() {
        runBlocking {
            regionLoaders.values.map { async { it.unload() } }.forEach { it.await() }
            regionLoaders.clear()
            LOGGER.debug("SAVED ALL TILES!")
            println("WhyMap: SAVED ALL TILES!")
        }
    }

    fun unloadRegion(position: LocalTileRegion) {
        runBlocking {
            regionLoaders[position]?.unload()
            regionLoaders.remove(position)
        }
    }

    fun getRegionLoaderForThumbnailRendering(position: LocalTileRegion): RenderedThumbnailProvider {
        return regionLoaders.getOrPut(position) { MapAreaAccess.GetIfExists(position) ?: return EmptyThumbnailProvider }
        //TODO use optionals?
    }
//
//    companion object {
//        private val locks = mutableSetOf<String>()
//
//        fun getLockForName(name: String): Any {
//            locks.add(name)
//            return locks.find { it == name }!!
//        }
//    }

}