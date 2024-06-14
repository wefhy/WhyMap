// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose

import androidx.compose.ui.graphics.Canvas
import net.minecraft.client.gui.DrawContext
import java.io.Closeable

internal abstract class Renderer: Closeable {
    abstract fun render(drawContext: DrawContext, tickDelta: Float, block: (Canvas) -> Unit)
    abstract fun invalidate()
    abstract fun onSizeChange(width: Int, height: Int)
}