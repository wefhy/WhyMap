// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import dev.wefhy.whymap.waypoints.CoordXYZ
import dev.wefhy.whymap.waypoints.OnlineWaypoint

object FeatureUpdateQueue: UpdateQueue<OnlineWaypoint>() {
    override val capacity = 60L
}