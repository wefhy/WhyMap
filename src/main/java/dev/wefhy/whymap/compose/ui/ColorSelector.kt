// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.compose.utils.ComposeUtils

@Composable
fun ColorSelector(modifier: Modifier = Modifier, onSelect: (Color) -> Unit) {
    val availableColors = ComposeUtils.goodColors
    Card(elevation = 8.dp, modifier = modifier) {
        LazyHorizontalGrid(GridCells.Fixed(2), Modifier.padding(4.dp)) {
            items(availableColors.size) {
                ColorButton(color = availableColors[it], onSelect = onSelect)
            }
        }
    }
}

@Composable
fun ColorButton(color: Color, onSelect: (Color) -> Unit) {
    Card(Modifier.requiredSize(40.dp, 40.dp).padding(4.dp).clickable { onSelect(color) }, elevation = 4.dp) {
        Box(modifier = Modifier.background(color))
    }
}

@Preview
@Composable
private fun ColorSelectorPreview() {
    ColorSelector {}
}