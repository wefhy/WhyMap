// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.gui

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.text.Text


class WhyInputScreen(title: String, message: String, callback: (Boolean, String) -> Unit) {

    private val input: String
        get() = confirmScreen.inputText

    private val confirmScreen = InputScreen({
        callback(it, input)
        close()
    }, Text.of(title), Text.of(message), Text.of("Yes"), Text.of("No"))

    private fun close() {
        confirmScreen.close()
    }

    context(MinecraftClient)
    suspend fun show() {
        withContext(asCoroutineDispatcher()) {
            setScreenAndRender(confirmScreen)
        }
    }
}