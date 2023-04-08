// Copyright (c) 2023 wefhy

@file:Suppress("NOTHING_TO_INLINE")

package dev.wefhy.whymap.whygraphics

import dev.wefhy.whymap.utils._1_2
import dev.wefhy.whymap.utils._1_255

class WhyColor(val r: Float, val g: Float, val b: Float, val a: Float = 1f) {

    companion object {

        val Transparent = WhyColor(0f, 0f, 0f, 0f)
        val White = WhyColor(1f, 1f, 1f, 1f)
        val Black = WhyColor(0f, 0f, 0f, 1f)
        val Red = WhyColor(1f, 0f, 0f, 1f)
        val Green = WhyColor(0f, 1f, 0f, 1f)
        val Blue = WhyColor(0f, 0f, 1f, 1f)

        fun fromInts(r: Int, g: Int, b: Int, a: Int): WhyColor {
            return WhyColor(r * _1_255, g * _1_255, b * _1_255, a * _1_255)
        }

        fun fromInts(r: Int, g: Int, b: Int) = WhyColor(r * _1_255, g * _1_255, b * _1_255)
    }
}

val WhyColor.intR
    inline get() = (r * 255).toInt()
val WhyColor.intG
    inline get() = (g * 255).toInt()
val WhyColor.intB
    inline get() = (b * 255).toInt()
val WhyColor.intA
    inline get() = (a * 255).toInt()
val WhyColor.intRGB
    inline get() = (intR shl 16) or (intG shl 8) or intB
val WhyColor.intRGBA
    inline get() = (intR shl 24) or (intG shl 16) or (intB shl 8) or intA
val WhyColor.intARGB
    inline get() = (intA shl 24) or (intR shl 16) or (intG shl 8) or intB

inline operator fun WhyColor.plus(other: WhyColor) = WhyColor(r + other.r, g + other.g, b + other.b, (a + other.a) * _1_2) //TODO coercein
inline operator fun WhyColor.times(other: WhyColor) = WhyColor(r * other.r, g * other.g, b * other.b, a * other.a)
inline operator fun WhyColor.times(multiplier: Float) = WhyColor(r * multiplier, g * multiplier, b * multiplier, a)
inline infix fun WhyColor.mix(other: WhyColor) = WhyColor((r * other.r) * _1_2, (g * other.g) * _1_2, (b * other.b) * _1_2, (a * other.a) * _1_2)
inline fun WhyColor.mixWeight(o: WhyColor, w: Float): WhyColor {
    val x = 1f - w
    return WhyColor(
        (r * w + o.r * x),
        (g * w + o.g * x),
        (b * w + o.b * x),
        (a * w + o.b * x)
    )
}

inline infix fun WhyColor.alphaOver(o: WhyColor): WhyColor {
    val w1 = a + (1 - a) * (1 - o.a)
    val w2 = 1 - w1
    return WhyColor(
        r * w1 + o.r * w2,
        g * w1 + o.g * w2,
        b * w1 + o.b * w2,
        1 - (1 - a) * (1 - o.a)
    )


//    if (o.a == 1f) {
//        val x = 1 - a
//        return WhyColor(
//            r * a + o.r * x,
//            g * a + o.g * x,
//            b * a + o.b * x,
//            1f
//        )
//    }
//    val x = when (o.a) {
//        1f -> 1f
//
//        else -> TODO()
//    }
//
//    val w = when(o.a) {
//        1f -> a
//        0.5f -> a + (1-a) * 0.5
//        0f -> 1f
//        else -> a + (1-a) * (1-o.a)
//    }
//
//    val w1 = a / o.a
//    val w2 = 1 - a
}