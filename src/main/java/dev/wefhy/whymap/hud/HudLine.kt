// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.hud

import dev.wefhy.whymap.whygraphics.WhyColor
import dev.wefhy.whymap.whygraphics.intARGB
import net.minecraft.client.MinecraftClient

abstract class HudLine {

    context(Hud.HudContext)
    fun draw(): Float {
        return if (visible) {
            val minecraft = MinecraftClient.getInstance()
            val textRenderer = minecraft.textRenderer
            matrixStack.push()
            matrixStack.scale(scale, scale, scale)
            textRenderer.drawWithShadow(matrixStack, text, 0f, 0f, (color ?: defaultColor).intARGB)
            matrixStack.pop()
            height
        } else 0f
    }

    abstract val text: String
    open val color: WhyColor? = null
    open val scale: Float = 0.8f
    open val priority: Int = 0
    var visible: Boolean = true
    open val height: Float
        get() = scale * 12f
}