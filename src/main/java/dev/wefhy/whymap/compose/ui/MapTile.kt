// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.utils.LocalTileRegion


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapTileView(regionTile: LocalTileRegion) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    val paint = Paint().apply {
        filterQuality = FilterQuality.None
    }

//    var tile: MapArea? by remember { mutableStateOf(null) }
    var image: ImageBitmap? by remember { mutableStateOf(null) }
    println("MapTileView recompose, image: $image, regionTile: $regionTile")
    LaunchedEffect(regionTile) {
        println("MapTileView LaunchedEffect")
        activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
            println("MapTileView LaunchedEffect getRegionForTilesRendering, tile: ${this@getRegionForTilesRendering}")
            val tile = this@getRegionForTilesRendering
            image = tile.renderWhyImageNow().imageBitmap
        }
    }


    Column {
//        Text("Tile $tile")
//        val t: WhyTiledImage? = tile?.renderWhyImageNow()
//        val image = t?.imageBitmap
        val dpSize = with(LocalDensity.current) {
//            DpSize(t?.width?.toDp() ?: 1.dp, t?.height?.toDp() ?: 1.dp)
            DpSize(image?.width?.toDp() ?: 1.dp, image?.height?.toDp() ?: 1.dp)
        }


//        WrappedCanvas(modifier = Modifier.size(dpSize)) { size ->
        Canvas(modifier = Modifier.size(dpSize).clipToBounds().pointerInput(Unit) {
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
            println("MapTileView Canvas recompose, image: $image")
//            drawRect(Rect(Offset(0f, 0f), Size(size.width, size.height)), Paint().apply { color = Color.Black })
//            t?.drawTiledImage()
            drawRect(Color.Black, Offset(0f, 0f), Size(size.width, size.height))
            image?.let { im ->
                scale(scale) {
//                    drawImage(im, topLeft = Offset(offsetX, offsetY))
                    drawImage(im, dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()), filterQuality = FilterQuality.None)
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
