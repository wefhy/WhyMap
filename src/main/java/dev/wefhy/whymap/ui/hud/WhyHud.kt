// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.ui.hud

import dev.wefhy.whymap.utils.roundToString
import net.minecraft.client.MinecraftClient

class WhyHud(val mc: MinecraftClient) : Hud() {

    val world
        get() = mc.world
    val player
        get() = mc.player
    val biomeAccess
        get() = world?.biomeAccess

    private val Double.rc
        inline get() = roundToString(1)

    private val Float.rc
        inline get() = roundToString(1)

    init {
        addLine {
            val player = player ?: return@addLine null
            "X: ${player.x.rc}, Y: ${player.y.rc}, Z: ${player.z.rc}"
        }
        addLine {
            val player = player ?: return@addLine null
            "Yaw: ${player.yaw.rc}, Pitch: ${player.pitch.rc}"
        }

        addLine {
            "FPS: ${mc.currentFps}"
        }
        addLine {
            val biome = biomeAccess?.getBiome(mc.player?.blockPos) ?: return@addLine null
            "Biome: ${biome.key.get().value.path}"
        }
    }

}