// Copyright (c) 2022-2023 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.WhyMapMod.Companion.forceWipeCache
import dev.wefhy.whymap.WhyServer.serverRouting
import dev.wefhy.whymap.communication.OnlinePlayer
import dev.wefhy.whymap.config.UserSettings.ExposeHttpApi
import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.config.WhyMapConfig.portRange
import dev.wefhy.whymap.config.WhyUserSettings
import dev.wefhy.whymap.events.*
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.utils.ImageWriter.encode
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
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import java.awt.image.BufferedImage

fun Application.myApplicationModule() {

    val exposeHttpApiSetting = WhyUserSettings.serverSettings.exposeHttpApi
    if (exposeHttpApiSetting == ExposeHttpApi.DISABLED)
        return
    install(ContentNegotiation) {
        json(Json {
//                        prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(CORS) {
        allowCredentials = true
        when (exposeHttpApiSetting) {
            ExposeHttpApi.LOCALHOST_ONLY -> {
                allowHost("localhost")
                allowHost("127.0.0.1")
            }
            ExposeHttpApi.EVERYWHERE -> {
                anyHost()
            }
            ExposeHttpApi.DEBUG -> {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.AccessControlAllowHeaders)
                allowHeader(HttpHeaders.ContentType)
                allowHeader(HttpHeaders.AccessControlAllowOrigin)
                exposeHeader(HttpHeaders.AccessControlAllowHeaders)
                exposeHeader(HttpHeaders.ContentType)
                exposeHeader(HttpHeaders.AccessControlAllowOrigin)
            }
            ExposeHttpApi.DISABLED -> println("Dear Kotlin Devs, this branch is unreachable, why do I need it?")
        }
    }
    routing {
        serverRouting(

        )
    }
}

object WhyServer {
    private const val parsingError: String = "Can't parse request"

    fun host() {
        for (p in portRange) {
            try {
                WhyMapConfig.port = p
                println("Trynig to run WhyMap server on port $p...")
                val host = when(WhyUserSettings.serverSettings.exposeHttpApi) {
                    ExposeHttpApi.DISABLED -> return
                    ExposeHttpApi.LOCALHOST_ONLY -> "localhost"
                    ExposeHttpApi.EVERYWHERE -> "0.0.0.0"
                    ExposeHttpApi.DEBUG -> "0.0.0.0"
                }

                embeddedServer(CIO, port = p, module = Application::myApplicationModule, host = host).start(wait = true)
                break
            } catch (e: Throwable) {
                println("Failed to run server on port $p. Trying on next one.")
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun PipelineContext<Unit, ApplicationCall>.getParams(vararg paramName: String): IntArray? {
        return paramName.map { call.parameters[it]?.toInt() ?: return null.also {
            println(
                call.parameters.toMap().map { (k, v) -> "$k: $v" }.joinToString(
                    ", ",
                    "Failed to parse request: "
                )
            )
        } }.toIntArray()
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
                val bitmap = getCustomRender(s)
                call.respondOutputStream(contentType = WhyMapMod.contentType) {
                    withContext(WhyDispatchers.Encoding) {
                        encodePNG(bitmap) //TODO test other formats performance and quality
                    }
                }
            } ?: return@get call.respondText("Region unavailable")

        }
        get("/blockMappings") {
            call.respondText(activeWorld?.mappingsManager?.blockMappingsJoined.toString())
        }
        get("/lastRegionUpdates/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L
            call.respond(RegionUpdateQueue.getLatestUpdates(threshold))
        }
        get("/lastChunkUpdates/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L
            call.respond(ChunkUpdateQueue.getLatestUpdates(threshold))
        }
        get("/lastThumbnailUpdates/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L
            call.respond(ThumbnailUpdateQueue.getLatestUpdates(threshold))
        }
        get("/worldEvents/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L
            call.respond(WorldEventQueue.getLatestUpdates(threshold))
        }
        get("/featureUpdates/{threshold}") {
            val threshold = call.parameters["threshold"]?.toLong() ?: 0L //TODO this is not error handling for NumberFormatException!
            call.respond(FeatureUpdateQueue.getLatestUpdates(threshold))
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
                        getRendered()
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
                    activeWorld?.experimentalTileGenerator?.getTile(chunkTile.chunkPos)
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
//                                withContext(WhyDispatchers.Render) { RegionThumbnailer.getTile(MapTile(x, z, TileZoom.ThumbnailZoom)) } ?: return@get call.respondText("Thumbnail unavailable")
                    val byteOutputStream = activeWorld?.thumbnailsManager?.getThumbnail(thumbnailTile)
                        ?: return@get call.respondText("Thumbnail unavailable")
                    return@get call.respondOutputStream(contentType = ContentType.Image.JPEG) {
                        withContext(WhyDispatchers.IO) {
                            write(byteOutputStream.toByteArray())
                        }
                    }
                }

                else -> return@get call.respondText("Unsupported scale!")
            }
            call.respondOutputStream(contentType = WhyMapMod.contentType) {
                withContext(WhyDispatchers.Encoding) {
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

            val deaths = call.request.queryParameters["deaths"].toBoolean()
            call.respond(
                activeWorld?.waypoints?.let {
                    if (deaths) it.onlineWaypoints else it.onlineWaypointsWithoutDeaths
                } ?: listOf()
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
        get("/forceWipeCache") {
            forceWipeCache()
        }
        get("/reloadTileWithBlock/{x}/{z}") {
            val (x, z) = getParams("x", "z") ?: return@get call.respondText(parsingError)
            val tile = MapTile(x, z, TileZoom.BlockZoom).toLocalTile().parent(TileZoom.RegionZoom)
            //todo reload zoomed in tile?
            activeWorld?.mapRegionManager?.apply {
                unloadRegion(tile)
            }
            RegionUpdateQueue.addUpdate(tile)

            /*?.getRegionForTilesRendering(tile) {
                getRendered()
            } ?: return@get call.respondText("Region unavailable")*/
        }
        val activeRenders = Semaphore(2)


        get("/exportArea/{x1}/{z1}/{x2}/{z2}/{format}") {
            activeRenders.tryAcquire {
                val limit = 200
                //TODO add option to select scale
                val (x1, z1, x2, z2) = getParams("x1", "z1", "x2", "z2") ?: return@get call.respondText(parsingError)
                val formatName = call.parameters["format"] ?: return@get call.respondText("Format not specified")
                val format = ImageFormat.values().find { it.matchesExtension(formatName) } ?: return@get call.respondText("Format not supported")
                val blockArea = RectArea(
                    LocalTile.Block(x1, z1),
                    LocalTile.Block(x2, z2)
                )
                println("Exporting $blockArea")
                println("Regions: ${blockArea.parent(TileZoom.RegionZoom).list()}")
                val regionArea = blockArea.parent(TileZoom.RegionZoom)
                if (regionArea.size > limit) return@get call.respondText("Too big area! Area would need to render ${regionArea.size} regions, limit is $limit regions.")
                val image = BufferedImage(
                    regionArea.blockArea().sizeX,
                    regionArea.blockArea().sizeZ,
                    BufferedImage.TYPE_INT_RGB
                )
                val raster = image.raster
                val renderJobs = regionArea.list().map { regionTile ->
                    launch(WhyDispatchers.Render) {
                        println("Rendering $regionTile, " +
                                activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
                                    renderWhyImageNow().writeInto(
                                        raster,
                                        regionTile.getStart().x - regionArea.blockArea().start.x,
                                        regionTile.getStart().z - regionArea.blockArea().start.z
                                    )
                                })
                    }
                }
                renderJobs.joinAll()
                val cropped = raster.createWritableChild(
                    blockArea.start.x - regionArea.blockArea().start.x,
                    blockArea.start.z - regionArea.blockArea().start.z,
                    blockArea.sizeX,
                    blockArea.sizeZ,
                    0,
                    0,
                    null
                ).run {
                    BufferedImage(image.colorModel, this, image.isAlphaPremultiplied, null)
                }


                call.respondOutputStream(contentType = format.contentType) {
                    withContext(WhyDispatchers.Encoding) {
                        encode(cropped, format)
                    }
                }
            } ?: return@get call.respondText("Too many renders in progress")

        }
        post("/waypoint") {
            val waypoint = call.receive<OnlineWaypoint>()
            WhyMapMod.LOGGER.debug("Received waypoint: ${waypoint.name}, ${waypoint.pos}")
            activeWorld?.waypoints?.add(waypoint) ?: call.respond(HttpStatusCode.ServiceUnavailable)
            call.respond(HttpStatusCode.OK)
        }
        delete("/waypoint") {
            println("deleting waypoint")
//            println(call.receive<String>())
            val waypoint = call.receive<OnlineWaypoint>()
            WhyMapMod.LOGGER.debug("Deleting waypoint: ${waypoint.name}, ${waypoint.pos}")
            activeWorld?.waypoints?.remove(waypoint) ?: call.respond(HttpStatusCode.ServiceUnavailable)
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