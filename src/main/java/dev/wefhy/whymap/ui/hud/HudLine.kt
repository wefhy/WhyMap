// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.ui.hud

import dev.wefhy.whymap.whygraphics.WhyColor
import dev.wefhy.whymap.whygraphics.intARGB
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack

abstract class HudLine {

    context(MatrixStack)
    fun draw(): Float {
        return if (visible) {
            val minecraft = MinecraftClient.getInstance()
            val textRenderer = minecraft.textRenderer
            push()
            scale(scale, scale, scale)
            textRenderer.drawWithShadow(this@MatrixStack, text, 0f, 0f, color.intARGB)
            pop()
            height
        } else 0f
    }

    abstract val text: String
    open val color: WhyColor = WhyColor.White
    open val scale: Float = 0.8f
    open val priority: Int = 0
    var visible: Boolean = true
    open val height: Float
        get() = scale * 12f
}