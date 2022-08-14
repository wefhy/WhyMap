// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.tiles.region.MapAreaAccess.LoadPriority.*
import dev.wefhy.whymap.tiles.thumbnails.LazyThumbnail
import dev.wefhy.whymap.tiles.thumbnails.RenderedThumbnailProvider
import dev.wefhy.whymap.utils.LocalTileRegion
import kotlinx.coroutines.delay
import net.minecraft.util.math.Vec3d
import java.awt.image.BufferedImage
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.hypot

context (CurrentWorldProvider<WhyWorld>)
class MapAreaAccess private constructor(val position: LocalTileRegion) : RenderedThumbnailProvider {

    override var wasUpdated: Boolean = false

    private var loadedRegion: MapArea? = null

    private var cachedRegion: WeakReference<MapArea> = WeakReference(null)

    private inline val getRegion
        get() = loadedRegion ?: cachedRegion.get()

    private val operationPending = AtomicBoolean(false)

    private val file
        get() = currentWorld.getFile(position)

    val fileExists
        get() = file.exists()

    val isLoaded
        get() = loadedRegion != null || cachedRegion.get() != null

    private suspend inline fun <T> withLock(block: () -> T): T {
        while (!operationPending.compareAndSet(false, true)) {
            delay(10)
        }
        return block().also { operationPending.set(false) }
    }

//    private val saveActionPending = AtomicBoolean(false)
//    private val loadActionsPending = AtomicInteger(0)
//
//    private suspend inline fun<T> withLoadLock(block: () -> T): T  {
//        loadActionsPending.getAndIncrement()
//        while (saveActionPending.get()) {
//            delay(10)
//        }
//        return block().also {
//            loadActionsPending.getAndDecrement()
//        }
//    }
//
//    private suspend inline fun<T> withSaveLock(block: () -> T): T  {
//        while (!saveActionPending.compareAndSet(false, true)) {
//            delay(10)
//        }
//        while (!loadActionsPending.equals(0)) {
//            delay(10)
//        }
//        return block().also { saveActionPending.set(false) }
//    }

    private suspend fun load() = withLock {
        if (fileExists) {

        }
    }

    internal suspend inline fun <T> withLoaded(loadPriority: LoadPriority, block: MapArea.() -> T): T? = withLock {
        val region: MapArea? = when (loadPriority) {
            PEEK_IF_LOADED -> getRegion
            KEEP_IF_LOADED -> getRegion.also { loadedRegion = it }
            LOAD_AND_PEEK -> getRegion ?: MapArea.GetIfExists(position)
            LOAD_AND_KEEP -> (getRegion ?: MapArea.GetIfExists(position)).also { loadedRegion = it }
//            GET_FOR_WRITE -> getRegion ?: MapArea.GetForWrite(position).also { loadedRegion = it }
        }
        return@withLock region?.block()
    }

    internal suspend inline fun <T> withLoadedForWrite(block: MapArea.() -> T): T = withLock {
        val region = (getRegion ?: MapArea.GetForWrite(position)).also { loadedRegion = it }
        wasUpdated = true
        return@withLock region.block()
    }

    suspend fun unload() = withLock {
        loadedRegion?.let { loadedRegion ->
            loadedRegion.save()
            cachedRegion = WeakReference(loadedRegion)
        }
    }

    suspend inline fun clean(playerPos: Vec3d?) {
        if (playerPos == null || WhyMapConfig.unloadDistance < hypot(playerPos.x - position.x, playerPos.z - position.z))
            unload()
    }

    enum class LoadPriority() {
        PEEK_IF_LOADED,
        KEEP_IF_LOADED,
        LOAD_AND_PEEK,
        LOAD_AND_KEEP,
//        GET_FOR_WRITE,
    }

    companion object {
        context(CurrentWorldProvider<WhyWorld>)
        fun GetIfExists(position: LocalTileRegion) = if (currentWorld.getFile(position).exists()) MapAreaAccess(position) else null

        context(CurrentWorldProvider<WhyWorld>)
        fun GetForWrite(position: LocalTileRegion) = MapAreaAccess(position)
    }



    override suspend fun getThumbnail(): BufferedImage? {
        return withLoaded(PEEK_IF_LOADED) {
            getAndSaveThumbnail()
        } ?: getFileThumbnail(position)?.getThumbnail()
        ?: withLoaded(LOAD_AND_PEEK) {
            getAndSaveThumbnail()
        }
    }

    private fun getFileThumbnail(position: LocalTileRegion): RenderedThumbnailProvider? {
        return if (currentWorld.getThumbnailFile(position).exists())
            LazyThumbnail(position)
        else null
    }
}