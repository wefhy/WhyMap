// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.ui.hud.lines

import dev.wefhy.whymap.ui.hud.HudLine

class DynamicLine(private val getText: () -> String?) : HudLine() {

    override val text: String
        get() = getText()?.also { visible = true } ?: "".also { visible = false }
}