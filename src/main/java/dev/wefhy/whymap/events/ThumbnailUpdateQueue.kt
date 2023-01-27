// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import kotlinx.serialization.Serializable

object ThumbnailUpdateQueue : UpdateQueue<ThumbnailUpdateQueue.ThumbnailUpdatePosition>() {

    override val capacity = 300L //seconds

    @Serializable
    data class ThumbnailUpdatePosition(val x: Int, val z: Int)

    internal fun addUpdate(x: Int, z: Int) {
        addUpdate(ThumbnailUpdatePosition(x, z))
    }
}