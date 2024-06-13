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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.compose.ui.ComposeConstants.scaleRange
import dev.wefhy.whymap.compose.ui.ComposeUtils.toLocalTileBlock
import dev.wefhy.whymap.compose.ui.ComposeUtils.toOffset
import dev.wefhy.whymap.config.WhyMapConfig.storageTileBlocks
import dev.wefhy.whymap.config.WhyMapConfig.tileResolution
import dev.wefhy.whymap.utils.*
import dev.wefhy.whymap.utils.ImageWriter.encodeJPEG
import dev.wefhy.whymap.utils.ImageWriter.encodePNG
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.IntBuffer
import javax.imageio.ImageIO

enum class MapControl {
    User, Target
}

private suspend fun renderDetail(tile: LocalTileChunk) = withContext(WhyDispatchers.Render) {
    val bufferedImage = activeWorld?.experimentalTileGenerator?.getTile(tile.chunkPos) ?: return@withContext null //TODO render directly to compose canvas!
    val stream = ByteArrayOutputStream()
    stream.encodeJPEG(bufferedImage)
    try {
        Image.makeFromEncoded(stream.toByteArray()).toComposeImageBitmap()
    } catch (e: Throwable) {
        null
    }
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
    val scope = rememberCoroutineScope()
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
    val zoom = when {
        scale < 0.5 -> TileZoom.ThumbnailZoom
        scale < 16 -> TileZoom.RegionZoom
        else -> TileZoom.ChunkZoom
    }
    val tileRadius = when(zoom) {
        TileZoom.ChunkZoom -> 4
        else -> 2
    }
    val nTiles = tileRadius * 2 + 1
    val block by remember { derivedStateOf { center.toLocalTileBlock() } } //startPosition - LocalTileBlock(offsetX.toInt(), offsetY.toInt())
    val centerTile = block.parent(zoom)
    val minTile = centerTile - LocalTile(tileRadius, tileRadius, zoom)
    val maxTile = centerTile + LocalTile(tileRadius, tileRadius, zoom)
    val dontDispose = remember { mutableSetOf<LocalTile<out TileZoom>>() }
    val images = remember { mutableStateMapOf<LocalTile<out TileZoom>, ImageBitmap>() }

    dontDispose.removeAll { it.x !in minTile.x..maxTile.x || it.z !in minTile.z..maxTile.z }
    for (x in minTile.x..maxTile.x) {
        for (z in minTile.z..maxTile.z) {
            val tile = LocalTile(x, z, zoom)
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
                .background(Color(0.1f, 0.1f, 0.1f))
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
                scale(scale ) {
                    translate(size.width / 2, size.height / 2) {
                        translate(-center.x, -center.y) {
                            for (y in minTile.z..maxTile.z) {
                                for (x in minTile.x..maxTile.x) {
                                    val tile = LocalTile(x, y, zoom)
                                    val image = images[tile]
                                    val drawOffset = tile.getStart()
                                    val res = (tileResolution / zoom.scale).toInt()
                                    image?.let { im ->
                                        if (scale > 1) {
//                                            drawImage(im, dstOffset = IntOffset(drawOffset.x, drawOffset.z), dstSize = IntSize((im.width / zoom.scale).toInt(), (im.height / zoom.scale).toInt()), filterQuality = FilterQuality.None)
                                            drawImage(im, dstOffset = IntOffset(drawOffset.x, drawOffset.z), dstSize = IntSize(res, res), filterQuality = FilterQuality.None)
                                        } else {
//                                            drawImage(im, dstOffset = IntOffset(drawOffset.x, drawOffset.z), dstSize = IntSize((im.width / zoom.scale).toInt(), (im.height / zoom.scale).toInt()), filterQuality = FilterQuality.Low)
                                            drawImage(im, dstOffset = IntOffset(drawOffset.x, drawOffset.z), dstSize = IntSize(res, res), filterQuality = FilterQuality.Low)
                                        }
                                    }
                                }
                            }
                            waypoints.forEach {
                                val offset = it.coords.toLocalBlock().toOffset() + Offset(0.5f, 0.5f)
                                val size = if (it == hovered) 16f else 8f
                                drawCircle(
                                    color = it.color,
                                    radius = size / scale,
                                    center = offset,
                                    style = Fill
                                )
                                if (it == hovered) {
                                    drawCircle(
                                        color = if (it.color.luminance() > 0.5f) Color.Black else Color.White,
                                        radius = size / scale,
                                        center = offset,
                                        style = Stroke(4f / scale)
                                    )
                                }
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
