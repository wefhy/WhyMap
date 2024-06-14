// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.utils

import net.minecraft.client.MinecraftClient

object Accessors {
    inline val clientInstance get() = MinecraftClient.getInstance()
    inline val clientWindow get() = clientInstance.window
}