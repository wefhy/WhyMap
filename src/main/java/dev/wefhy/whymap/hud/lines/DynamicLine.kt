// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.hud.lines

import dev.wefhy.whymap.hud.HudLine

class DynamicLine(private val getText: () -> String?) : HudLine() {

    override val text: String
        get() = getText()?.also { visible = true } ?: "".also { visible = false }
}