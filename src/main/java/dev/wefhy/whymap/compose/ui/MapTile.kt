// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.tiles.region.MapArea
import dev.wefhy.whymap.utils.LocalTileRegion
import dev.wefhy.whymap.whygraphics.WhyTiledImage


@Composable
fun MapTileView(regionTile: LocalTileRegion) {

    var tile: MapArea? by remember { mutableStateOf(null) }
    println("MapTileView recompose, tile: $tile, regionTile: $regionTile")
    LaunchedEffect(regionTile) {
        println("MapTileView LaunchedEffect")
        activeWorld?.mapRegionManager?.getRegionForTilesRendering(regionTile) {
            println("MapTileView LaunchedEffect getRegionForTilesRendering, tile: ${this@getRegionForTilesRendering}")
            tile = this@getRegionForTilesRendering
        }
    }


    Column {
//        Text("Tile $tile")
        val t: WhyTiledImage? = tile?.renderWhyImageNow()

        val dpSize = with(LocalDensity.current) {
            DpSize(t?.width?.toDp() ?: 1.dp, t?.height?.toDp() ?: 1.dp)
        }


        WrappedCanvas(modifier = Modifier.size(dpSize)) { size ->
            println("MapTileView Canvas recompose, tile: $tile")
            drawRect(Rect(Offset(0f, 0f), Size(size.width, size.height)), Paint().apply { color = Color.Black })
            t?.drawTiledImage()
//            drawRect(Color.Magenta, Offset(0f, 0f), Size(size.width, size.height))
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
