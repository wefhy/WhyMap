// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.gui

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text


class WhyConfirmScreen(title: String, message: String, callback: (Boolean) -> Unit) {
    private val confirmScreen = ConfirmScreen({
        callback(it)
        close()
    }, Text.of(title), Text.of(message), Text.of("Yes"), Text.of("No"))

    var parent: Screen? = null

    private fun close() {
        MinecraftClient.getInstance().openScreen(parent)
//        confirmScreen.close()
    }

    context(MinecraftClient)
    suspend fun show() {
        withContext(asCoroutineDispatcher()) {
//            currentScreen = confirmScreen
            parent = currentScreen
            openScreen(confirmScreen)
        }
    }
}