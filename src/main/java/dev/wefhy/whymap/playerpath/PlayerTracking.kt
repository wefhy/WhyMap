// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.playerpath

import net.minecraft.util.math.Vec3d

object PlayerTracking {

    var currentPath: PlayerPath? = null

    fun newPath(player: String) {
        currentPath = PlayerPath(player)
    }

    fun addEntry(pos: Vec3d) {
        currentPath?.addEntry(PlayerPathEntry(System.currentTimeMillis(), pos.x, pos.y, pos.z))
    }

    private fun save() {
        currentPath?.saveToFile()
    }

    fun endPath() {
        save()
        currentPath = null
    }
}