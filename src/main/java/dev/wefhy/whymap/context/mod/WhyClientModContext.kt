// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.context.mod

import net.minecraft.client.MinecraftClient

class WhyClientModContext(
    val mc: MinecraftClient
): WhyModContext {
    val player
        get() = mc.player
    val playerName
        get() = player?.gameProfile?.name
    val displayName
        get() = player?.displayName?.string
}