// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.toOffset
import dev.wefhy.whymap.utils.LocalTileBlock

object ComposeUtils {
    fun LocalTileBlock.toOffset(): Offset = androidx.compose.ui.unit.IntOffset(x, z).toOffset()
    fun Offset.toLocalTileBlock(): LocalTileBlock = LocalTileBlock(x.toInt(), y.toInt())
}