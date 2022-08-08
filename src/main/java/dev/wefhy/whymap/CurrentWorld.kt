// Copyright (c) 2022 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.config.WhyMapConfig.cleanupInterval
import dev.wefhy.whymap.config.WhyMapConfig.modPath
import dev.wefhy.whymap.config.WhyMapConfig.thumbnailZoom
import dev.wefhy.whymap.mixin.DimensionTypeAccess
import dev.wefhy.whymap.tiles.BiomeCurrentWorldManager
import dev.wefhy.whymap.tiles.BiomeManager
import dev.wefhy.whymap.tiles.BiomeOfflineManager
import dev.wefhy.whymap.tiles.details.ExperimentalTileGenerator
import dev.wefhy.whymap.tiles.region.MapRegionManager
import dev.wefhy.whymap.tiles.thumbnails.RegionThumbnailer
import dev.wefhy.whymap.tiles.thumbnails.ThumbnailsManager
import dev.wefhy.whymap.waypoints.Waypoints
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.util.WorldSavePath
import net.minecraft.world.dimension.DimensionType
import java.io.Closeable

class CurrentWorld(val mc: MinecraftClient) : WhyWorld(), Closeable {
    //    val mc: MinecraftClient = MinecraftClient.getInstance()
    val session = mc.game.currentSession!!
    val world = mc.world!!
    val player = mc.player!!
    val dimension = world.dimension
    override val provider = CurrentWorldProvider(this)
    override val biomeManager by lazy { with(provider) { BiomeCurrentWorldManager() } }

    override val name: String =
        if (session.isRemoteServer) "Multiplayer_" + mc.currentServerEntry!!.address else mc.server!!.getSavePath(
            WorldSavePath.ROOT
        ).parent.fileName.toString()

    //    val alternativeName: String = mc.server!!.saveProperties.levelName
    val dimensionCoordinateScale = world.dimension.coordinateScale

    override val dimensionName = when (dimension) {
        DimensionTypeAccess.getOverworld() -> "Overworld"
        DimensionTypeAccess.getNether() -> "Nether"
        DimensionTypeAccess.getEnd() -> "End"
        else -> customDimensionName(dimension)
    }

    val waypoints = with(provider) { Waypoints() }

    val periodicCleanupJob = GlobalScope.launch {
        while (true) {
            delay(cleanupInterval * 1000L)
            mapRegionManager.periodicCleanup()
        }
    }

    init {
        waypoints.load()
    }

    fun saveAll() {
        waypoints.save()
        mapRegionManager.saveAllAndClear()
    }

    companion object {
        val instance: CurrentWorld? = null

        fun customDimensionName(dimension: DimensionType): String {
            val values = booleanArrayOf(
                dimension.isBedWorking,
                dimension.isUltrawarm,
                dimension.isNatural,
                dimension.isPiglinSafe,
                dimension.isRespawnAnchorWorking
            )
            return "CustomDimension-${values.joinToString("") { if (it) "1" else "0" }}-${dimension.coordinateScale}-${dimension.height}-${dimension.logicalHeight}"
        }
    }

    override fun close() {
        periodicCleanupJob.cancel()
        saveAll()
    }
}

class OfflineWorld(override val name: String, override val dimensionName: String) : WhyWorld(), Closeable {
    override val provider = CurrentWorldProvider(this)
    override val biomeManager = BiomeOfflineManager()

    override fun close() {
    }

}

abstract class WhyWorld {
    abstract val name: String
    abstract val dimensionName: String
    abstract val provider: CurrentWorldProvider<WhyWorld>
    abstract val biomeManager: BiomeManager

    val worldPath by lazy { modPath.resolve(name).resolve(dimensionName) }
    val mapTilesPath by lazy { worldPath.resolve("tiles") }
    val mapThumbnailsPath by lazy { worldPath.resolve("thumbnails").resolve("$thumbnailZoom") }
    val mapRegionManager by lazy { with(provider) { MapRegionManager() } }
    val regionThumbnailer by lazy { with(provider) { RegionThumbnailer() } }
    val experimentalTileGenerator by lazy { with(provider) { ExperimentalTileGenerator() } }
    val thumbnailsManager by lazy { with(provider) { ThumbnailsManager() } }
}

class CurrentWorldProvider<out T : WhyWorld>(val currentWorld: T)