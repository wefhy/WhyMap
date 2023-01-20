// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import kotlinx.serialization.Serializable


/**
 * Heavily optimized for this only purpose
 */
object TileUpdateQueue : UpdateQueue<TileUpdateQueue.ChunkUpdatePosition>() { //TODO separate for every world / dimension?
    //TODO add new areas to queue!
    @Serializable
    class ChunkUpdatePosition(val x: Int, val z: Int)

    internal fun addUpdate(x: Int, z: Int) {
        addUpdate(ChunkUpdatePosition(x, z))
    }
}