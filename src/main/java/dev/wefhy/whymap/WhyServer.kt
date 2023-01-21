// Copyright (c) 2022-2023 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.WhyServer.serverRouting
import dev.wefhy.whymap.communication.OnlinePlayer
import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.events.TileUpdateQueue
import dev.wefhy.whymap.events.WorldEventQueue
import dev.wefhy.whymap.tiles.mesh.MeshGenerator
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
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import java.awt.image.BufferedImage

fun Application.myApplicationModule() {
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
}

object WhyServer {
    private const val parsingError: String = "Can't parse request"

    fun host() {
        embeddedServer(CIO, port = 7542, module = Application::myApplicationModule).start(wait = true)
    }

    fun PipelineContext<Unit, ApplicationCall>.getParams(vararg paramName: String): IntArray? {
        return paramName.map { call.parameters[it]?.toInt() ?: return null }.toIntArray()
    }

    inline fun withActiveWorld(block: (CurrentWorld) -> Unit): CurrentWorld? {
        return activeWorld?.also {
            block(it)
        }
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
            val (x, z) = getParams("x", "z") ?: return@get call.respondText(parsingError)
            val block = LocalTile.Block(x, z)
            activeWorld?.mapRegionManager?.getRegionForTilesRendering(block.parent(TileZoom.RegionZoom)) {
                call.respond(
                    getBlockInfo(block)
                )
            } ?: return@get call.respondText("Block unavailable")
        }
        get("/customRegion/{s}/{x}/{z}") {
            activeWorld ?: return@get call.respondText("World not loaded!")
            val (x, z, s) = getParams("x", "z", "s") ?: return@get call.respondText(parsingError)
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
            call.respondText(getMappings())
        }
        get("/exportBlockMappings") {
            call.respondText(exportBlockMappings())
        }
        get("/lastUpdates/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L
            call.respond(TileUpdateQueue.getLatestUpdates(threshold))
        }
        get("/worldEvents/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L
            call.respond(WorldEventQueue.getLatestUpdates(threshold))
        }
        get("/textureAtlas") {
            val bitmap = with(activeWorld?.provider) {
                if (this == null) return@get call.respondText("World not loaded!")
                withContext(Dispatchers.Default) { TextureAtlas.textureAtlas }
            }
            call.respondOutputStream(contentType = WhyMapMod.contentType) {
                withContext(Dispatchers.IO) {
                    encodePNG(bitmap)
                }
            }
        }
//        get("/3d/tiles/15/{x}/{z}") {
//            activeWorld ?: return@get call.respondText("World not loaded!")
//            val (x, z, s) = getParams("x", "z", "s") ?: return@get call.respondText(parsingError)
//            val chunkTile = if (s >= 0)
//                MapTile(x, z, TileZoom.ChunkZoom).toLocalTile()
//            else
//                LocalTile(x, z, TileZoom.ChunkZoom)
//
//            if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
//                "Requested tile: ${Pair(x, z)}, scale: $s, chunk: $chunkTile"
//            )
//            val bitmap: BufferedImage = withContext(Dispatchers.IO) { activeWorld?.experimentalTileGenerator?.getTile(chunkTile.chunkPos) }
//                ?: return@get call.respondText("Chunk unavailable")
//
//
//
//        }
        get("/3d/tiles/{s}/{x}/{z}") {
            activeWorld ?: return@get call.respondText("World not loaded!")
            val (x, z, s) = getParams("x", "z", "s") ?: return@get call.respondText(parsingError)
            if ((s != 17) && (s != -17)) return@get call.respondText("unsupportedZoomLevel")
            val regionTile = if (s >= 0)
                MapTile(x, z, TileZoom.RegionZoom).toLocalTile()
            else
                LocalTile(x, z, TileZoom.RegionZoom)
            val result = activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
                MeshGenerator.getBlenderPythonMesh()
            } ?: return@get call.respondText("Chunk unavailable")
            call.respondText(result)


        }
        get("/regionheight/{x}/{z}/{s}") {
            activeWorld ?: return@get call.respondText("World not loaded!")
            val (x, z, s) = getParams("x", "z", "s") ?: return@get call.respondText(parsingError)
            val regionTile = if (s >= 0)
                MapTile(x, z, TileZoom.RegionZoom).toLocalTile()
            else
                LocalTile(x, z, TileZoom.RegionZoom)
            val heightMap = activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
                heightMap
            }
//            val region = activeWorld?.mapRegionManager?.getLoadedRegionForRead(LocalTile.Region(x, z)) ?: return@get call.respondText("Region unavailable")
//            call.respondText { region.heightMap.joinToString(","){it.joinToString(",")} }
        }











        //TODO all tile requests should be cancellable
        get("/tiles/{s}/{x}/{z}") {//TODO parse dimension
            activeWorld ?: return@get call.respondText("World not loaded!")
            val (x, z, s) = getParams("x", "z", "s") ?: return@get call.respondText(parsingError)

            val bitmap: BufferedImage = when (s) {
                WhyMapConfig.regionZoom, -WhyMapConfig.regionZoom -> {
                    val regionTile = if (s >= 0)
                        MapTile(x, z, TileZoom.RegionZoom).toLocalTile()
                    else
                        LocalTile(x, z, TileZoom.RegionZoom)

                    if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
                        "Requested tile: ${Pair(x, z)}, scale: $s, region: $regionTile"
                    )
                    activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
                        withContext(Dispatchers.IO) { getRendered() }
                    } ?: return@get call.respondText("Region unavailable")

                }

                WhyMapConfig.chunkZoom, -WhyMapConfig.chunkZoom -> {
                    val chunkTile = if (s >= 0)
                        MapTile(x, z, TileZoom.ChunkZoom).toLocalTile()
                    else
                        LocalTile(x, z, TileZoom.ChunkZoom)

                    if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
                        "Requested tile: ${Pair(x, z)}, scale: $s, chunk: $chunkTile"
                    )
                    withContext(Dispatchers.IO) { activeWorld?.experimentalTileGenerator?.getTile(chunkTile.chunkPos) }
                        ?: return@get call.respondText("Chunk unavailable")
                }

                WhyMapConfig.thumbnailZoom, -WhyMapConfig.thumbnailZoom -> {
                    val thumbnailTile = if (s >= 0)
                        MapTile(x, z, TileZoom.ThumbnailZoom).toLocalTile()
                    else
                        LocalTile(x, z, TileZoom.ThumbnailZoom)

                    if (WhyMapConfig.DEV_VERSION) WhyMapMod.LOGGER.info(
                        "Requested tile: ${Pair(x, z)}, scale: $s, thumbnail: $thumbnailTile"
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

                    /**
                     * And another one in file Nodes.md as `Ex 2`
                     */
                }
            }
        }

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