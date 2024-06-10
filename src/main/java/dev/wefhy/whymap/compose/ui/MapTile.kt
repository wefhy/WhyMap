// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import dev.wefhy.whymap.utils.LocalTileBlock
import dev.wefhy.whymap.utils.LocalTileRegion
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.utils.WhyDispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapTileView(startPosition: LocalTileBlock) {
    val tileRadius = 1
    val nTiles = 3
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    val paint = Paint().apply {
        filterQuality = FilterQuality.None
    }
    val block = startPosition - LocalTileBlock(offsetX.toInt(), offsetY.toInt())
    val centerTile = block.parent(TileZoom.RegionZoom)
    val minTile = centerTile - LocalTileRegion(tileRadius, tileRadius)
    val maxTile = centerTile + LocalTileRegion(tileRadius, tileRadius)
    val dontDispose = remember { mutableSetOf<LocalTileRegion>() }
    val images: SnapshotStateList<ImageBitmap?> = remember { mutableStateListOf(null, null, null, null, null, null, null, null, null) }


    for (x in minTile.x..maxTile.x) {
        for (z in minTile.z..maxTile.z) {
            val tile = LocalTileRegion(x, z)
            if (tile in dontDispose) continue
            val index = tile.z.mod(nTiles) * nTiles + tile.x.mod(nTiles)
            images[index] = null
            LaunchedEffect(tile) {
                println("MapTileView LaunchedEffect, tile: $tile, index: $index")
//                if (tile in dontDispose) return@LaunchedEffect
                withContext(WhyDispatchers.Render) {
                    activeWorld?.mapRegionManager?.getRegionForTilesRendering(tile) {
                        if (!isActive) return@getRegionForTilesRendering Unit.also { println("Cancel early 1")}
                        val image = renderWhyImageNow().imageBitmap
                        if (!isActive) return@getRegionForTilesRendering Unit.also { println("Cancel early 2")}
                        images[index] = image
                        dontDispose.add(tile)
                    }
                }
            }
        }
        dontDispose.removeAll { it.x !in minTile.x..maxTile.x || it.z !in minTile.z..maxTile.z }
    }
    Column {
//        Text("Tile $tile")
//        val t: WhyTiledImage? = tile?.renderWhyImageNow()
//        val image = t?.imageBitmap
//        val dpSize = with(LocalDensity.current) {
////            DpSize(t?.width?.toDp() ?: 1.dp, t?.height?.toDp() ?: 1.dp)
//            DpSize(image?.width?.toDp() ?: 1.dp, image?.height?.toDp() ?: 1.dp)
//        }


//        WrappedCanvas(modifier = Modifier.size(dpSize)) { size ->
        Canvas(modifier = Modifier.size(DpSize(400.dp, 400.dp)).clipToBounds().pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x / scale
                offsetY += dragAmount.y / scale
            }
        }.onPointerEvent(PointerEventType.Scroll) {
            val scrollDelta = it.changes.fold(Offset.Zero) { acc, c -> acc + c.scrollDelta }
            scale *= 1 + scrollDelta.y / 10
        }
        ) {
//            println("MapTileView Canvas recompose, image: $image")
//            drawRect(Rect(Offset(0f, 0f), Size(size.width, size.height)), Paint().apply { color = Color.Black })
//            t?.drawTiledImage()
            drawRect(Color.Black, Offset(0f, 0f), Size(size.width, size.height))
            for (y in minTile.z .. maxTile.z) {
                for (x in minTile.x .. maxTile.x) {
                    val index = y.mod(nTiles) * nTiles + x.mod(nTiles)
                    val image = images[index]
                    val drawOffset = LocalTileRegion(x, y, TileZoom.RegionZoom).getCenter() - startPosition
                    image?.let { im ->
                        translate(offsetX * scale, offsetY * scale) {
                            scale(scale) {
                                val drawX = drawOffset.x + 350
                                val drawY = drawOffset.z + 350
                                if (scale > 1) {
                                    drawImage(im, dstOffset = IntOffset(drawX.toInt(), drawY.toInt()), filterQuality = FilterQuality.None)
                                } else {
                                    drawImage(im, topLeft = Offset(drawX.toFloat(), drawY.toFloat()))
                                }
                            }
                        }
                    }
                }
            }
//            drawIntoCanvas { canvas ->
//                with(canvas) {
//                    t?.drawTiledImage()
//                }
//            }
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
