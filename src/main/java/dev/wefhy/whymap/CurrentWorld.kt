// Copyright (c) 2022 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.communication.quickaccess.BiomeCurrentWorldManager
import dev.wefhy.whymap.communication.quickaccess.BiomeManager
import dev.wefhy.whymap.communication.quickaccess.BiomeOfflineManager
import dev.wefhy.whymap.config.WhyMapConfig.cleanupInterval
import dev.wefhy.whymap.config.WhyMapConfig.logsPath
import dev.wefhy.whymap.config.WhyMapConfig.modPath
import dev.wefhy.whymap.config.WhyMapConfig.thumbnailZoom
import dev.wefhy.whymap.migrations.MappingsManager
import dev.wefhy.whymap.tiles.details.ExperimentalTileGenerator
import dev.wefhy.whymap.tiles.region.MapRegionManager
import dev.wefhy.whymap.tiles.thumbnails.RegionThumbnailer
import dev.wefhy.whymap.tiles.thumbnails.ThumbnailsManager
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.waypoints.Waypoints
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.util.WorldSavePath
import net.minecraft.world.dimension.DimensionType
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZOutputStream
import java.io.BufferedWriter
import java.io.Closeable
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class CurrentWorld(mc: MinecraftClient, overrideDimension: DimensionType? = null) : WhyWorld(), Closeable {
    val world = mc.world!!
    val player = mc.player!!
    private val dimension = overrideDimension ?: world.dimension!!
    override val dimensionScale = dimension.coordinateScale
    override val provider = CurrentWorldProvider(this)
    override val biomeManager by lazy { with(provider) { BiomeCurrentWorldManager() } }
    override val mappingsManager = MappingsManager(biomeMappings = biomeManager.biomeNameList)

    override val name: String =
        if (!mc.isConnectedToLocalServer) "Multiplayer_" + mc.currentServerEntry!!.address.sanitizedPath else mc.server!!.getSavePath(
            WorldSavePath.ROOT
        ).parent.fileName.toString()

    //    val alternativeName: String = mc.server!!.saveProperties.levelName
    val dimensionCoordinateScale = dimension.coordinateScale

    override val dimensionName = dimension.serialize()

    val waypoints = with(provider) { Waypoints() }

    private val periodicCleanupJob = GlobalScope.launch {//TODO this definitely shouldn't be launched from GlobalScope...
        while (true) {
            delay(cleanupInterval * 1000L)
            mapRegionManager.periodicCleanup()
        }
    }

    init {
        waypoints.load()
    }

    private fun saveAll() {
        waypoints.save()
        mapRegionManager.saveAllAndClear()
    }

    override fun close() {
        periodicCleanupJob.cancel()
        saveAll()
        super.close()
    }
}

class OfflineWorld(override val name: String, override val dimensionName: String) : WhyWorld() {
    override val provider = CurrentWorldProvider(this)
    override val biomeManager = BiomeOfflineManager()
    override val dimensionScale = TODO("Not yet implemented")
    override val mappingsManager = TODO("Needs another type of mapping manager")

    override fun close() {
        super.close()
    }
}

abstract class WhyWorld : Closeable {
    abstract val name: String
    abstract val dimensionName: String
    abstract val provider: CurrentWorldProvider<WhyWorld>
    abstract val biomeManager: BiomeManager
    abstract val dimensionScale: Double
    abstract val mappingsManager: MappingsManager

    val worldPath by lazy { modPath.resolve(name).resolve(dimensionName) }
    val mapTilesPath by lazy { worldPath.resolve("tiles") }
    val mapThumbnailsPath by lazy { worldPath.resolve("thumbnails").resolve("$thumbnailZoom") }
    val mapRegionManager by lazy { with(provider) { MapRegionManager() } }
    val regionThumbnailer by lazy { with(provider) { RegionThumbnailer() } }
    val experimentalTileGenerator by lazy { with(provider) { ExperimentalTileGenerator() } }
    val thumbnailsManager by lazy { with(provider) { ThumbnailsManager() } }

    private val sessionLogFile by lazy { logsPath.resolve("$currentDateString-$name-$dimensionName.log")}
    private val compressedFile by lazy { logsPath.resolve("$currentDateString-$name-$dimensionName.log.xz")}

    fun getFile(position: LocalTileRegion) = mapTilesPath.resolve("Region_${position.x}_${position.z}.whymap")

    fun getThumbnailFile(position: LocalTileRegion) = mapThumbnailsPath.resolve("Region_${position.x}_${position.z}.png")


    //    val logWriter = BufferedWriter(FileWriter(sessionLogFile, true))
    private val logWriter by lazy {
        sessionLogFile.mkDirsIfNecessary()
//        BufferedWriter(OutputStreamWriter(FileOutputStream(sessionLogFile, true), "UTF-8"), 128)
        BufferedWriter(OutputStreamWriter(XZOutputStream(FileOutputStream(compressedFile), LZMA2Options(3)), "UTF-8"))
    }

    fun writeToLog(message: String) {
        logWriter.append("$currentLogEntryTimeString: $message\n")
    }

    override fun close() {
        logWriter.append("\nStats:\n${ObfuscatedLogHelper.dumpStats()}\n")
        logWriter.append("\nObfuscation map:\n${ObfuscatedLogHelper.dumpMap()}\n")
        logWriter.close()

//        if (!compressedFile.exists()) {
//            compressedFile.parentFile.mkdirs()
//            compressedFile.createNewFile()
//        }
//        compressedFile.outputStream().use { fileOutputStream ->
//            XZOutputStream(fileOutputStream, LZMA2Options(3)).use { xz ->
//                xz.write(sessionLogFile.readBytes())
//            }
//        }
    }

}

class CurrentWorldProvider<out T : WhyWorld>(val currentWorld: T)