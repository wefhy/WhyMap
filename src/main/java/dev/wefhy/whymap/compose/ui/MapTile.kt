// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        Text("Tile $tile")
        Canvas(modifier = Modifier.fillMaxSize()) {
            println("MapTileView Canvas recompose, tile: $tile")
            val t: WhyTiledImage? = tile?.renderWhyImageNow()
//            drawRect(Color.Magenta, Offset(0f, 0f), Size(size.width, size.height))
            t?.drawTiledImage()
        }

    }
}