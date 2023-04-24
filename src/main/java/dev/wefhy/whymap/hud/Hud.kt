// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.hud

import dev.wefhy.whymap.hud.lines.DynamicLine
import dev.wefhy.whymap.hud.lines.StaticLine
import dev.wefhy.whymap.whygraphics.WhyColor
import net.minecraft.client.util.math.MatrixStack

open class Hud {

    val lines = mutableListOf<HudLine>()

    fun addLine(line: HudLine) {
        lines.add(line)
    }

    fun addLine(text: String, color: WhyColor = WhyColor.White) {
        lines.add(StaticLine(text, color))
    }

    fun addLine(text: () -> String?) {
        lines.add(DynamicLine(text))
    }

    context(HudContext)
    fun draw() {
        matrixStack.push()
        lines.sortedByDescending { it.priority }.forEachIndexed { i, line ->
            if (line.visible) {
                val height = line.draw()
                matrixStack.translate(0f, height, 0f)
            }
        }
        matrixStack.pop()
    }

    class HudContext(
        val matrixStack: MatrixStack,
        val defaultColor: WhyColor
    )
}