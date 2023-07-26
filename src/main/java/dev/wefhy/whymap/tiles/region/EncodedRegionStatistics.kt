// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.region

class EncodedRegionStatistics(
    val blockCounter: Map<Short, Int>,
    val overlayCounter: Map<Short, Int>,
    val biomeCounter: Map<Byte, Int>,
) {
}