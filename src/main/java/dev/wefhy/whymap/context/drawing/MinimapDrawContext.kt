// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.context.drawing

import dev.wefhy.whymap.config.UserSettings
import net.minecraft.client.util.math.MatrixStack

class MinimapDrawContext(
    val matrixStack: MatrixStack,
    val mode: UserSettings.MapMode,
    val position: UserSettings.MinimapPosition,
    private val size: Float,
    val scale: Float,
    val padding: Float,
) {
    val radius
        get() = size / 2f * scale
}