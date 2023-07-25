// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

class MappingContext(
    val resolution: Int,
    val min: Double,
    val max: Double
)

context(MappingContext)
val Int.mapToDouble
    get() = (this.toDouble() / resolution) * (max - min) + min

context(MappingContext)
val Double.mapToInt
    get() = ((this - min) / (max - min) * resolution).toInt()