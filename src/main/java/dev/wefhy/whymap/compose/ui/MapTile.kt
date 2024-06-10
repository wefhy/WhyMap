// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.compose.ui.ComposeConstants.scaleRange
import dev.wefhy.whymap.compose.ui.ComposeUtils.toLocalTileBlock
import dev.wefhy.whymap.compose.ui.ComposeUtils.toOffset
import dev.wefhy.whymap.utils.LocalTileBlock
import dev.wefhy.whymap.utils.LocalTileRegion
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.utils.WhyDispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapTileView(startPosition: LocalTileBlock) {
    val scope = rememberCoroutineScope()
    val tileRadius = 1
    val nTiles = 3
    var scale by remember { mutableStateOf(1f) }
    var center by remember { mutableStateOf(startPosition.toOffset()) }
    val block by remember { derivedStateOf { center.toLocalTileBlock() }} //startPosition - LocalTileBlock(offsetX.toInt(), offsetY.toInt())
    val centerTile = block.parent(TileZoom.RegionZoom)
    val minTile = centerTile - LocalTileRegion(tileRadius, tileRadius)
    val maxTile = centerTile + LocalTileRegion(tileRadius, tileRadius)
    val dontDispose = remember { mutableSetOf<LocalTileRegion>() }
    val images = remember { mutableStateMapOf<LocalTileRegion, ImageBitmap>() }

    dontDispose.removeAll { it.x !in minTile.x..maxTile.x || it.z !in minTile.z..maxTile.z }
    for (x in minTile.x..maxTile.x) {
        for (z in minTile.z..maxTile.z) {
            val tile = LocalTileRegion(x, z)
            if (tile in dontDispose) continue
            val index = tile.z.mod(nTiles) * nTiles + tile.x.mod(nTiles)
            LaunchedEffect(tile) {
                assert(tile !in images)
//                images.remove(tile) //TODO actually just return if already loaded. But this should be handled by dontDispose
                println("MapTileView LaunchedEffect, tile: $tile, index: $index")
                val image = withContext(WhyDispatchers.Render) {
                    activeWorld?.mapRegionManager?.getRegionForTilesRendering(tile) {
                        if (!isActive) return@getRegionForTilesRendering null.also { println("Cancel early 1")}
                        renderWhyImageNow().imageBitmap
                    }
                }
                if (!isActive) return@LaunchedEffect Unit.also { println("Cancel early 2")}
                image?.let {
                    images[tile] = it
                }
                dontDispose.add(tile)
            }
        }
    }

    Card(
        elevation = 8.dp
    ) {
//        val dpSize = with(LocalDensity.current) {
////            DpSize(t?.width?.toDp() ?: 1.dp, t?.height?.toDp() ?: 1.dp)
//            DpSize(image?.width?.toDp() ?: 1.dp, image?.height?.toDp() ?: 1.dp)
//        }

        Canvas(modifier = Modifier
            .size(DpSize(400.dp, 400.dp))
            .background(Color(0.1f, 0.1f, 0.1f))
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    center -= dragAmount / scale
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                val scrollDelta = it.changes.fold(Offset.Zero) { acc, c -> acc + c.scrollDelta }
                scale = (scale * (1 + scrollDelta.y / 10)).coerceIn(scaleRange)
            }
        ) {
//            drawRect(Color.Black, Offset(0f, 0f), Size(size.width, size.height))
            for (y in minTile.z .. maxTile.z) {
                for (x in minTile.x .. maxTile.x) {
                    val tile = LocalTileRegion(x, y)
                    val image = images[tile]
                    val drawOffset = tile.getStart()
                    image?.let { im ->
                        scale(scale) {
                            translate(size.width / 2, size.height / 2) {
                                translate( - center.x,  - center.y) {
                                    if (scale > 1) {
                                        drawImage(im, dstOffset = IntOffset(drawOffset.x, drawOffset.z), filterQuality = FilterQuality.None)
                                    } else {
                                        drawImage(im, topLeft = drawOffset.toOffset())
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
