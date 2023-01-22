// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import kotlinx.serialization.Serializable


/**
 * Heavily optimized for this only purpose
 */
object TileUpdateQueue : UpdateQueue<TileUpdateQueue.ChunkUpdatePosition>() { //TODO separate for every world / dimension?
    //TODO add new areas to queue!
    @Serializable
    class ChunkUpdatePosition(val x: Int, val z: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ChunkUpdatePosition

            if (x != other.x) return false
            if (z != other.z) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + z
            return result
        }

        override fun toString(): String {
            return "CUP(x=$x, z=$z)"
        }
    }


    internal fun addUpdate(x: Int, z: Int) {
        addUpdate(ChunkUpdatePosition(x, z))
    }
}