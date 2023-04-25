// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.context.mod

import net.minecraft.server.MinecraftServer

class WhyServerModContext(
    val ms: MinecraftServer
): WhyModContext {
    val players
        get() = ms.playerManager.playerList
    val playerNames
        get() = players.map { it.gameProfile.name }
    val playerPositions
        get() = players.map { it.pos }
    val isSingle = ms.isSingleplayer
}