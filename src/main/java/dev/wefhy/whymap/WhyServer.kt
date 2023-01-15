// Copyright (c) 2022 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.communication.OnlinePlayer
import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.tiles.region.BlockMappingsManager.exportBlockMappings
import dev.wefhy.whymap.tiles.region.BlockMappingsManager.getMappings
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.utils.ImageWriter.encodePNG
import dev.wefhy.whymap.waypoints.OnlineWaypoint
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import java.awt.image.BufferedImage

object WhyServer {
    fun host() {
        embeddedServer(CIO, port = 7542) {
            install(ContentNegotiation) {
                json(Json {
//                        prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(CORS) {//TODO CORS is only for quick frontend testing, it isn't needed on production build
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowHeader(HttpHeaders.AccessControlAllowHeaders)
                allowHeader(HttpHeaders.ContentType)
                allowHeader(HttpHeaders.AccessControlAllowOrigin)
                exposeHeader(HttpHeaders.AccessControlAllowHeaders)
                exposeHeader(HttpHeaders.ContentType)
                exposeHeader(HttpHeaders.AccessControlAllowOrigin)
                allowCredentials = true
                anyHost()
            }
            routing {
                serverRouting()
            }
        }.start(wait = true)
    }

    fun Routing.serverRouting() {
        get("/") {
            call.respondRedirect("/index.html")
//                        call.respondHtml {
//                            body {
//                                a(href = "/map.html") {
//                                    +"DefaultMap"
//                                }
//                                a(href = "/export") {
//                                    +"Export loaded regions"
//                                }
//                            }
//                        }
        }
        get("/block/{x}/{z}") {
            activeWorld ?: return@get call.respondText("World not loaded!")
            val x = call.parameters["x"]?.toInt() ?: return@get call.respondText("Can't parse request")
            val z = call.parameters["z"]?.toInt() ?: return@get call.respondText("Can't parse request")
            val block = LocalTile.Block(x, z)
            activeWorld?.mapRegionManager?.getRegionForTilesRendering(block.parent(TileZoom.RegionZoom)) {
                call.respond(
                    getBlockInfo(block)
                )
            } ?: return@get call.respondText("Block unavailable")
        }
        get("/customRegion/{s}/{x}/{z}") {
            activeWorld ?: return@get call.respondText("World not loaded!")
            val x = call.parameters["x"]?.toInt() ?: return@get call.respondText("Can't parse request")
            val z = call.parameters["z"]?.toInt() ?: return@get call.respondText("Can't parse request")
            val s = call.parameters["s"]?.toInt() ?: return@get call.respondText("Can't parse request")
            activeWorld?.mapRegionManager?.getRegionForTilesRendering(LocalTile.Region(x, z)) {
                val bitmap = withContext(Dispatchers.IO) { getCustomRender(s) }
                call.respondOutputStream(contentType = WhyMapMod.contentType) {
                    withContext(Dispatchers.IO) {
                        encodePNG(bitmap) //TODO test other formats performance and quality
                    }
                }
            } ?: return@get call.respondText("Region unavailable")

        }
        get("/blockMappings") {
            return@get call.respondText(getMappings())
        }
        get("/exportBlockMappings") {
            return@get call.respondText(exportBlockMappings())
        }
        get("/tiles/{s}/{x}/{z}") {//TODO parse dimension
            activeWorld ?: return@get call.respondText("World not loaded!")
            val x = call.parameters["x"]?.toInt() ?: return@get call.respondText("Can't parse request")
            val z = call.parameters["z"]?.toInt() ?: return@get call.respondText("Can't parse request")
            val s = call.parameters["s"]?.toInt() ?: return@get call.respondText("Can't parse request")

            val bitmap: BufferedImage = when (s) {
                WhyMapConfig.regionZoom, -WhyMapConfig.regionZoom -> {
                    val regionTile = if (s >= 0) MapTile(x, z, TileZoom.RegionZoom).toLocalTile() else LocalTile(
                        x,
                        z,
                        TileZoom.RegionZoom
                    )
                    if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
                        "Requested tile: ${
                            Pair(
                                x,
                                z
                            )
                        }, scale: $s, region: $regionTile"
                    )
                    activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
                        withContext(Dispatchers.IO) { getRendered() }
                    } ?: return@get call.respondText("Region unavailable")

                }

                WhyMapConfig.chunkZoom, -WhyMapConfig.chunkZoom -> {
                    val chunkTile = if (s >= 0) MapTile(x, z, TileZoom.ChunkZoom).toLocalTile() else LocalTile(
                        x,
                        z,
                        TileZoom.ChunkZoom
                    )
                    if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
                        "Requested tile: ${
                            Pair(
                                x,
                                z
                            )
                        }, scale: $s, chunk: $chunkTile"
                    )
                    withContext(Dispatchers.IO) { activeWorld?.experimentalTileGenerator?.getTile(chunkTile.chunkPos) }
                        ?: return@get call.respondText("Chunk unavailable")
                }

                WhyMapConfig.thumbnailZoom, -WhyMapConfig.thumbnailZoom -> {
                    val thumbnailTile = if (s >= 0) MapTile(x, z, TileZoom.ThumbnailZoom).toLocalTile() else LocalTile(
                        x,
                        z,
                        TileZoom.ThumbnailZoom
                    )
                    if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
                        "Requested tile: ${
                            Pair(
                                x,
                                z
                            )
                        }, scale: $s, thumbnail: $thumbnailTile"
                    )
//                                withContext(Dispatchers.IO) { RegionThumbnailer.getTile(MapTile(x, z, TileZoom.ThumbnailZoom)) } ?: return@get call.respondText("Thumbnail unavailable")
                    val byteOutputStream = activeWorld?.thumbnailsManager?.getThumbnail(thumbnailTile)
                        ?: return@get call.respondText("Thumbnail unavailable")
                    call.respondOutputStream(contentType = ContentType.Image.JPEG) {
                        withContext(Dispatchers.IO) {
                            write(byteOutputStream.toByteArray())
                        }
                    }
                    return@get
                }

                else -> return@get call.respondText("Unsupported scale!")
            }
            call.respondOutputStream(contentType = WhyMapMod.contentType) {
                withContext(Dispatchers.IO) {
                    encodePNG(bitmap) //TODO test other formats performance and quality
                    /**
                     * Weird error can happen very rarely. Is it like out of memory?
                     * (in theory output stream provided by Ktor shouldn't have a length limit?)
                     *
                     * java.lang.IndexOutOfBoundsException: null
                     * at javax.imageio.stream.FileCacheImageOutputStream.seek(FileCacheImageOutputStream.java:170)
                     * [...]
                     * at dev.wefhy.whymap.utils.ImageWriter.encodePNG(ImageWriter.kt:50) ~[main/:?]
                     * at dev.wefhy.whymap.WhyServer$serverRouting$4$1$1.invokeSuspend(WhyServer.kt:133) ~[main/:?]
                     */
                }
            }
        }
//                    get("/regionheight/{x}/{z}") {
//                        activeWorld ?: return@get call.respondText("World not loaded!")
//                        val x = call.parameters["x"]?.toInt() ?: return@get call.respondText("Can't parse request")
//                        val z = call.parameters["z"]?.toInt() ?: return@get call.respondText("Can't parse request")
//                        val region = activeWorld?.mapRegionManager?.getLoadedRegionForRead(LocalTile.Region(x, z)) ?: return@get call.respondText("Region unavailable")
//                        call.respondText { region.heightMap.joinToString(","){it.joinToString(",")} }
//                    }

//                    get("/export") {
//                        MinecraftClient.getInstance().world ?: return@get call.respondText("World not loaded!")
//                        withContext(Dispatchers.Default) {
//                            activeWorld?.provider?.run {
//                                InteractiveMapExporter().exportRegions(
//                                    activeWorld.mapRegionManager.loadedRegions,
//                                    true
//                                )
//                            } ?: call.respond("Unavailable!") //todo respond error code
//                            call.respondText("Done!")
//                        }
//                    }

        static("/") {
            staticBasePackage = "web"
            resources(".")
        }
//                    val classloader = javaClass.classLoader
//                    val resource = classloader.getResource("web")
//                    val webDirectory = File(resource.toURI())
//                    Files.walk(webDirectory.toPath()).forEach {
//                        val file = it.toFile()
//                        val relativePath = file.relativeTo(webDirectory).path
//                        get("/$relativePath") {
//                            call.respondFile(file)
//                        }
//                    }
        get("/waypoints") {
            call.respond(
                activeWorld?.waypoints?.onlineWaypoints ?: listOf()
            )
        }
        get("/player") {
            val player =
                MinecraftClient.getInstance().player ?: run { call.respondText("Player does not exist"); return@get }
            val position = player.pos
            val onlinePlayer = OnlinePlayer(
                player.displayName.string,
                OnlinePlayer.PlayerPosition(
                    position.x.roundTo(1),
                    position.y.roundTo(1),
                    position.z.roundTo(1)
                ),
                player.yaw.toDouble()
            )
            call.respond(
                onlinePlayer
            )
        }
        get("/data") {
            call.respondText(
                Block.STATE_IDS.joinToString(", ") { it.block.translationKey }
            )
        }
        get("/datasize") {
            call.respondText(
                Block.STATE_IDS.size().toString()
            )
        }
        get("/hello") {
            call.respondText("hello world")
        }
        post("/waypoint") {
            val waypoint = call.receive<OnlineWaypoint>()
            WhyMapMod.LOGGER.debug("Received waypoint: ${waypoint.name}, ${waypoint.pos}")
            activeWorld?.waypoints?.add(waypoint) ?: call.respond(HttpStatusCode.ServiceUnavailable)
            call.respond(HttpStatusCode.OK)
        }
        post("/importWaypoints") {
            val waypointsFile = call.receiveText()
            if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.debug(waypointsFile)
            activeWorld?.waypoints?.import(waypointsFile) ?: call.respond(HttpStatusCode.ServiceUnavailable)
            call.respond(HttpStatusCode.OK)
        }
    }
}