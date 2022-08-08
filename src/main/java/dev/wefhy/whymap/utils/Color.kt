// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

import kotlin.math.roundToInt
import kotlin.math.sqrt

class Color(_r: Int, _g: Int, _b: Int) {
    val r = _r
    val g = _g
    val b = _b

    constructor(int: Int) : this(
        (int shr (8 * 2)) and 0xff,
        (int shr (8 * 1)) and 0xff,
        (int shr (8 * 0)) and 0xff
    )

    operator fun plus(o: Color): Color {
        return Color(r + o.r, g + o.g, b + o.b)
    }

    operator fun plus(o: Int): Color {
        return Color(r + o, g + o, b + o)
    }

    operator fun times(o: Float): Color {
        return Color((r * o).toInt(), (g * o).toInt(), (b * o).toInt())
    }

    fun toFloatColor() = FloatColor(
        r * _1_255,
        g * _1_255,
        b * _1_255
    )


    fun mix1(o: Color): Color {
        return Color(
            (r + o.r) / 2,
            (g + o.g) / 2,
            (b + o.b) / 2
        )
    }

    fun mix2(o: Color): Color {
        return Color(
            sqrt((r * o.r).toDouble()).roundToInt(),
            sqrt((g * o.g).toDouble()).roundToInt(),
            sqrt((b * o.b).toDouble()).roundToInt()
        )
    }

    operator fun times(o: Color): Color {
        return Color(
            r * o.r / 255,
            g * o.g / 255,
            b * o.b / 255
        )
    }

    operator fun times(o: FloatColor): Color {
        return Color(
            (r * o.r).toInt(),
            (g * o.g).toInt(),
            (b * o.b).toInt()
        )
    }

    fun mixWeight(o: Color, w: Float): Color {
        val x = 1 - w
        return Color(
            (r * w + o.r * x).toInt(),
            (g * w + o.g * x).toInt(),
            (b * w + o.b * x).toInt()
        )
    }

    fun toInt(): Int = (r.coerceIn0255() shl 16) + (g.coerceIn0255() shl 8) + b.coerceIn0255()

}
