// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

class FloatColor(val r: Float, val g: Float, val b: Float) {
    operator fun times(o: FloatColor) = FloatColor(
        r * o.r,
        g * o.g,
        b * o.b
    )
}
val FloatColor.floatArray
    get() = floatArrayOf(r, g, b, 1f)