// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.gui

import it.unimi.dsi.fastutil.booleans.BooleanConsumer
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

class InputScreen(callback: BooleanConsumer?, title: Text?, message: Text?, yesText: Text?, noText: Text?) : ConfirmScreen(callback, title, message, yesText, noText) {

    var inputText = ""

    override fun init() {
        super.init()
        val textFieldWidget = TextFieldWidget(textRenderer, width/2 - 50, 50, 100, 20, Text.of("waypoint name"))
        textFieldWidget.setChangedListener { inputText = it }
        addChild(textFieldWidget)
    }
}