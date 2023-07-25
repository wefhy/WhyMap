// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import kotlinx.serialization.Serializable

object ChunkUpdateQueue : UpdateQueue<ChunkUpdateQueue.ChunkUpdatePosition>() { //TODO separate for every world / dimension?

    override val capacity = 20L //seconds

    @Serializable
    data class ChunkUpdatePosition(val x: Int, val z: Int)

    internal fun addUpdate(x: Int, z: Int) {
        addUpdate(ChunkUpdatePosition(x, z))
    }
}