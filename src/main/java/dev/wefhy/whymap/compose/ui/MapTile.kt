// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition.PlatformDefault.x
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.compose.ui.ComposeConstants.scaleRange
import dev.wefhy.whymap.compose.utils.ComposeUtils.goodBackground
import dev.wefhy.whymap.compose.utils.ComposeUtils.toImageBitmap
import dev.wefhy.whymap.compose.utils.ComposeUtils.toLocalTileBlock
import dev.wefhy.whymap.compose.utils.ComposeUtils.toOffset
import dev.wefhy.whymap.config.WhyMapConfig.tileResolution
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.utils.ImageWriter.encodeJPEG
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream

enum class MapControl {
    User, Target
}

private suspend fun renderDetail(tile: LocalTileChunk) = withContext(WhyDispatchers.Render) {
//    val bufferedImage = activeWorld?.experimentalTileGenerator?.getTile(tile.chunkPos) ?: return@withContext null //TODO render directly to compose canvas!
//    bufferedImage.toImageBitmap(intermediate = ImageFormat.JPEG)
    activeWorld?.experimentalTileGenerator?.getComposeTile(tile)
}
private suspend fun renderRegion(tile: LocalTileRegion) = withContext(WhyDispatchers.Render) {
    activeWorld?.mapRegionManager?.getRegionForTilesRendering(tile) {
        if (!isActive) return@getRegionForTilesRendering null.also { println("Cancel early 1") }
        renderWhyImageNow().imageBitmap
    }
}
private suspend fun renderThumbnail(tile: LocalTileThumbnail) = withContext(WhyDispatchers.Render) {
    activeWorld?.thumbnailsManager?.getThumbnail(tile)?.let {
        try {
            Image.makeFromEncoded(it.toByteArray()).toComposeImageBitmap()
        } catch (e: Throwable) {
            null
        }
    }
}
//private suspend inline fun<reified T : TileZoom> render(tile: LocalTile<T>): ImageBitmap? = when (T::class) {
//    TileZoom.RegionZoom::class -> renderRegion(tile as LocalTileRegion)
//    TileZoom.ThumbnailZoom::class -> renderThumbnail(tile as LocalTileThumbnail)
//    TileZoom.ChunkZoom::class -> renderDetail(tile as LocalTileChunk)
//    else -> throw IllegalArgumentException("Unsupported zoom level: ${T::class.simpleName}")
//}

private suspend inline fun<reified T : TileZoom> render(tile: LocalTile<T>): ImageBitmap? = when (tile.zoom.zoom) {
    TileZoom.RegionZoom.zoom -> renderRegion(tile as LocalTileRegion)
    TileZoom.ThumbnailZoom.zoom -> renderThumbnail(tile as LocalTileThumbnail)
    TileZoom.ChunkZoom.zoom -> renderDetail(tile as LocalTileChunk)
    else -> throw IllegalArgumentException("Unsupported zoom level: ${T::class.simpleName}")
}




@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapTileView(startPosition: LocalTileBlock, waypoints: List<WaypointEntry> = emptyList(), hovered: WaypointEntry?, updateCount: Int = 0) {
    //TODO layers - on max zoom just color the tile if the region file exists
    var mapControl by remember { mutableStateOf(MapControl.Target) }
    var animationTarget by remember { mutableStateOf(startPosition) }
    remember(updateCount) {
        mapControl = MapControl.Target
        animationTarget = startPosition
    }
    val animationCenter by animateOffsetAsState(animationTarget.toOffset(), animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    var scale by remember { mutableStateOf(1f) }
    var center by remember { mutableStateOf(startPosition.toOffset()) }
    remember(animationCenter, mapControl) {
        if (mapControl == MapControl.Target) {
            center = animationCenter
        }
    }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val zoom = remember(scale, canvasSize) {
        val tilesPerCanvas = canvasSize.maxDimension / tileResolution.toFloat()
        val tilesPerVisibleCanvas = tilesPerCanvas / scale / 5 //5 is number of tiles visible on the screen
        when(tilesPerVisibleCanvas) {
            in 0f..0.05f -> TileZoom.ChunkZoom
            in 0.05f.. 1.1f -> TileZoom.RegionZoom
            else -> TileZoom.ThumbnailZoom
        }
//        when {
//            scale < 0.5 -> TileZoom.ThumbnailZoom
//            scale < 16 -> TileZoom.RegionZoom
//            else -> TileZoom.ChunkZoom
//        }
    }
    val tileRadius = when(zoom) {
        TileZoom.ChunkZoom -> 4
        TileZoom.RegionZoom -> 2
        TileZoom.ThumbnailZoom -> 3
        else -> 0
    }
    val nTiles = tileRadius * 2 + 1
    val block by remember { derivedStateOf { center.toLocalTileBlock() } } //startPosition - LocalTileBlock(offsetX.toInt(), offsetY.toInt())
    val centerTile = block.parent(zoom)
    val minTile = centerTile - LocalTile(tileRadius, tileRadius, zoom)
    val maxTile = centerTile + LocalTile(tileRadius, tileRadius, zoom)
    val dontDispose = remember { mutableSetOf<LocalTile<out TileZoom>>() }
    val images = remember { mutableStateMapOf<LocalTile<out TileZoom>, ImageBitmap>() }
    dontDispose.removeAll { it.x !in minTile.x..maxTile.x || it.z !in minTile.z..maxTile.z }
    for (tile in (minTile..maxTile).toList().sortedByDescending { it distanceTo centerTile }) {
        if (tile in dontDispose) continue
        val index = tile.z.mod(nTiles) * nTiles + tile.x.mod(nTiles)
        LaunchedEffect(tile) {
            assert(tile !in images)
//                images.remove(tile) //TODO actually just return if already loaded. But this should be handled by dontDispose
            println("MapTileView LaunchedEffect, tile: $tile, index: $index")
            val image = render(tile)
            if (!isActive) return@LaunchedEffect Unit.also { println("Cancel early 2") }
            image?.let {
                images[tile] = it
            }
            dontDispose.add(tile)
        }
    }

    Box {
        Card(
            elevation = 8.dp
        ) {
//        val dpSize = with(LocalDensity.current) {
////            DpSize(t?.width?.toDp() ?: 1.dp, t?.height?.toDp() ?: 1.dp)
//            DpSize(image?.width?.toDp() ?: 1.dp, image?.height?.toDp() ?: 1.dp)
//        }

            Canvas(modifier = Modifier
//                .size(DpSize(400.dp, 400.dp))
                .fillMaxSize()
//                .background(Color(0.1f, 0.1f, 0.1f))
                .background(Color.Black)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        center -= dragAmount / scale
                        animationTarget = center.toLocalTileBlock()
                        mapControl = MapControl.User
                    }
                }
                .onPointerEvent(PointerEventType.Scroll) {
                    val scrollDelta = it.changes.fold(Offset.Zero) { acc, c -> acc + c.scrollDelta }
                    scale = (scale * (1 + scrollDelta.y / 10)).coerceIn(scaleRange)
                }
            ) {
                canvasSize = size
                val filterQuality = if (scale > 1) FilterQuality.None else FilterQuality.Low
                val res = (tileResolution / zoom.scale).toInt()
                scale(scale) {
                    translate(size.width / 2, size.height / 2) {
                        translate(-center.x, -center.y) {
                            for (tile in minTile..maxTile) {
                                val image = images[tile]
                                val drawOffset = tile.getStart()
                                image?.let { im ->
                                    drawImage(im, dstOffset = IntOffset(drawOffset.x, drawOffset.z), dstSize = IntSize(res, res), filterQuality = filterQuality)
                                }
                            }
                            waypoints.sortedBy { it == hovered }.forEach {
                                val offset = it.coords.toLocalBlock().toOffset() + Offset(0.5f, 0.5f)
                                val size = if (it == hovered) 16f else 8f
                                val outlineWidth = if (it == hovered) 5f else 2f
                                drawCircle(
                                    color = it.color,
                                    radius = size / scale,
                                    center = offset,
                                    style = Fill
                                )
                                drawCircle(
                                    color = it.color.goodBackground(),
                                    radius = size / scale,
                                    center = offset,
                                    style = Stroke(outlineWidth / scale)
                                )
                            }
                            val player = activeWorld?.player?: return@scale
                            val playerPos = player.pos
                            val playerYaw = player.yaw
                            val offset = Offset(playerPos.x.toFloat(), playerPos.z.toFloat())
                            translate(offset.x, offset.y) {
                                rotate(playerYaw, pivot = Offset(0f, 0f)) {
                                    drawPath(
                                        path = Path().apply {
                                            val size = 16f / scale
                                            moveTo(0f, 0f)
                                            lineTo(-size, -size)
                                            lineTo(0f, 1.5f*size)
                                            lineTo(size, -size)
                                            close()
                                        },
                                        color = Color.Red,
                                        style = Fill
                                    )
                                }
                            }
                        }
                    }
                }
            }
            //center icon
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Center",
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).size(32.dp).clip(
                    CircleShape).clickable {
                    val player = activeWorld?.player?: return@clickable
                    val playerPos = player.pos
                    animationTarget = Offset(playerPos.x.toFloat(), playerPos.z.toFloat()).toLocalTileBlock()
                    mapControl = MapControl.Target
                }.background(MaterialTheme.colors.background).padding(4.dp)
            )
        }
    }
}
@Composable
fun CachedCanvas(modifier: Modifier = Modifier, block: DrawScope.() -> Unit) {
    Spacer(modifier = Modifier.fillMaxSize().drawWithCache {
        println("CachedCanvas recompose")
        onDrawWithContent {
            block()
        }
    })
}

@Composable
fun WrappedCanvas(modifier: Modifier = Modifier, block: Canvas.(Size) -> Unit) {
    Canvas(modifier) {
//        this.drawIntoCanvas {
//
//        }
        val image = ImageBitmap(
            size.width.toInt(),
            size.height.toInt(),
            ImageBitmapConfig.Argb8888)
        val canvas = Canvas(image)
        canvas.block(size)
        drawImage(image)
    }
}
