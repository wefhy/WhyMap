// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import kotlinx.serialization.Serializable

object RegionUpdateQueue : UpdateQueue<RegionUpdateQueue.RegionUpdatePosition>() { //TODO separate for every world / dimension?

    override val capacity = 60L

    @Serializable
    data class RegionUpdatePosition(val x: Int, val z: Int)

    internal fun addUpdate(x: Int, z: Int) {
        addUpdate(RegionUpdatePosition(x, z))
    }
}