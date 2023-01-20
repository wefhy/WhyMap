// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import kotlinx.serialization.Serializable

object WorldEventQueue: UpdateQueue<WorldEventQueue.WorldEvent>() {

    override val capacity: Long = 600L //seconds

    @Serializable
    enum class WorldEvent {
        DimensionChange,
        LeaveWorld,
        EnterWorld,
        PlayerDeath
    }
}