// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import dev.wefhy.whymap.utils.LocalTileRegion
import kotlinx.serialization.Serializable

object RegionUpdateQueue : UpdateQueue<RegionUpdateQueue.RegionUpdatePosition>() { //TODO separate for every world / dimension?

    override val capacity = 60L

    @Serializable
    data class RegionUpdatePosition(val x: Int, val z: Int)

    private fun addUpdate(x: Int, z: Int) {
        addUpdate(RegionUpdatePosition(x, z))
    }

    internal fun addUpdate(tile: LocalTileRegion) {
        addUpdate(tile.x, tile.z)
    }
}