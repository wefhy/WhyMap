// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.config.WhyMapConfig.currentWorldName
import dev.wefhy.whymap.config.WhyMapConfig.webExportDirectory
import dev.wefhy.whymap.utils.TileZoom.ChunkZoom
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

context(CurrentWorldProvider<WhyWorld>)
class InteractiveMapExporter {
    suspend fun exportRegions(positions: List<LocalTileRegion>, detailedMap: Boolean) {
        webExportDirectory.resolve(currentWorldName).apply {
            copyWeb()
            val nRegions = positions.size
            for ((i, position) in positions.withIndex()) {
                exportRegion(position)
                if (detailedMap) {
                    position.toList(ChunkZoom).flatten().forEach {
                        exportChunk(it)
                    }
                }
                LOGGER.debug("Exported region ${i + 1}/$nRegions")
            }
//            runBlocking { TODO async exporting will be hard but I guess possible with some locking?
//                positions.mapIndexed { i, region ->
//                    async {
//                        exportRegion(region)
//                        if (detailedMap) {
//                            region.toChunks().flatten().forEach {
//                                exportChunk(it)
//                            }
//                        }
//                        LOGGER.debug("Exported region $i/$nRegions")
//                    }
//                }.map { it.await() }
//            }
            LOGGER.debug("Finished exporting!")
        }
    }

    context(File)
    fun copyWeb() {
        LOGGER.debug("Copying web!")
        val files = listOf<String>(
            "map.html",
            "script.js",
            "style.css"
        )
        if (files.all { exportResource(it) }) {
            LOGGER.debug("Web copied!")
        } else {
            LOGGER.debug("Web failed to copy!")
        }

    }

    context(File)
    suspend fun exportRegion(position: LocalTileRegion): Boolean {
        return currentWorld.mapRegionManager.getRegionForTilesRendering(position) {
            val tile = position.toMapTile()
            val file = resolve("tiles").resolve(tile)
            file.parentFile.mkdirs()
            withContext(WhyDispatchers.Encoding) {
                ImageIO.write(
                    getRendered(),
                    "png",
                    file
                )
            }
        } ?: false

    }

    context(File)
    suspend fun exportChunk(position: LocalTileChunk): Boolean {
        val renderedChunk = currentWorld.experimentalTileGenerator.getTile(position.chunkPos) ?: return false
        val tile = position.toMapTile()
        val file = resolve("tiles")
            .resolve(tile.zoom.toString())
            .resolve(tile.x.toString())
            .resolve(tile.z.toString())
        file.parentFile.mkdirs()
        return withContext(WhyDispatchers.Encoding) {
            ImageIO.write(
                renderedChunk,
                "png",
                file
            )
        }
    }

    context(File)
    fun exportResource(resourceName: String): Boolean {
        LOGGER.debug("Will copy web rn")
        val resource = javaClass.classLoader.getResourceAsStream("web/$resourceName") ?: return false
//        val resource = javaClass.getResource("/absolute/path/of/source/in/jar/file") ?: return null
//        val inputFile = File(resource.toURI())
        val outputFile = resolve(resourceName)
        LOGGER.debug("Written ${outputFile.absolutePath}")
        Files.copy(resource, outputFile.toPath())
        return true
    }
}