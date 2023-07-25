// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.gui

import com.google.common.collect.ImmutableList
import net.minecraft.client.gui.screen.DialogScreen
import net.minecraft.text.Text

class WhyDialog(
    title: String,
    message: String,
    buttons: List<Pair<String,() -> Unit>>
) : DialogScreen(
    Text.literal(title),
    listOf(Text.literal(message)),
    ImmutableList.copyOf(
        buttons.map { (text, callback) -> DialogScreen.ChoiceButton(Text.literal(text)) {callback() } }
    )
) {

}