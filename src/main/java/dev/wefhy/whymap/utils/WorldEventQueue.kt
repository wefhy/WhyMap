// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import kotlinx.serialization.Serializable

object WorldEventQueue: UpdateQueue<WorldEventQueue.WorldEvent>() {

    @Serializable
    enum class WorldEvent {
        DimensionChange,
        LeaveWorld,
        EnterWorld,
        PlayerDeath
    }
}