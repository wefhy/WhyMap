// Copyright (c) 2023 wefhy

@file:Suppress("NOTHING_TO_INLINE")

package dev.wefhy.whymap.whygraphics

import dev.wefhy.whymap.utils._1_2
import dev.wefhy.whymap.utils._1_255
import dev.wefhy.whymap.utils.coerceIn0255

class WhyColor(val r: Float, val g: Float, val b: Float, val a: Float = 1f) {
    override fun toString(): String {
        return "WhyColor(r=$r, g=$g, b=$b, a=$a)"
    }
    companion object {

        val Transparent = WhyColor(0f, 0f, 0f, 0f)
        val White = WhyColor(1f, 1f, 1f, 1f)
        val Gray = WhyColor(.5f, .5f, .5f, .5f)
        val Black = WhyColor(0f, 0f, 0f, 1f)
        val Red = WhyColor(1f, 0f, 0f, 1f)
        val Green = WhyColor(0f, 1f, 0f, 1f)
        val Blue = WhyColor(0f, 0f, 1f, 1f)

        fun fromInts(r: Int, g: Int, b: Int, a: Int): WhyColor {
            return WhyColor(r * _1_255, g * _1_255, b * _1_255, a * _1_255)
        }

        fun fromGray(gray: Int) = fromGray(gray * _1_255)

        fun fromGray(gray: Float): WhyColor {
            return WhyColor(gray, gray, gray)
        }

        fun fromInts(r: Int, g: Int, b: Int) = WhyColor(r * _1_255, g * _1_255, b * _1_255)

        fun fromRGB(rgb: Int): WhyColor {
            return WhyColor(
                ((rgb shr 16) and 0xFF) * _1_255,
                ((rgb shr 8) and 0xFF) * _1_255,
                (rgb and 0xFF) * _1_255
            )
        }

        fun fromRGBWithAlpha(rgb: Int, alpha: Float): WhyColor {
            return WhyColor(
                ((rgb shr 16) and 0xFF) * _1_255,
                ((rgb shr 8) and 0xFF) * _1_255,
                (rgb and 0xFF) * _1_255,
                alpha
            )
        }

        fun fromRGBA(rgba: Int): WhyColor {
            return WhyColor(
                ((rgba shr 24) and 0xFF) * _1_255,
                ((rgba shr 16) and 0xFF) * _1_255,
                ((rgba shr 8) and 0xFF) * _1_255,
                (rgba and 0xFF) * _1_255
            )
        }

//        fun fromARGB(argb: Int): WhyColor {
//            return WhyColor(
//                ((argb shr 16) and 0xFF) * _1_255,
//                ((argb shr 8) and 0xFF) * _1_255,
//                (argb and 0xFF) * _1_255,
//                ((argb shr 24) and 0xFF) * _1_255
//            )
//        }
    }
}

val WhyColor.intR
    inline get() = (r * 255).toInt().coerceIn0255()
val WhyColor.intG
    inline get() = (g * 255).toInt().coerceIn0255()
val WhyColor.intB
    inline get() = (b * 255).toInt().coerceIn0255()
val WhyColor.intA
    inline get() = (a * 255).toInt().coerceIn0255()
val WhyColor.intRGB
    inline get() = (intR shl 16) or (intG shl 8) or intB
val WhyColor.intBGR
    inline get() = (intB shl 16) or (intG shl 8) or intR
val WhyColor.intRGBA
    inline get() = (intR shl 24) or (intG shl 16) or (intB shl 8) or intA
val WhyColor.intARGB
    inline get() = (intA shl 24) or (intR shl 16) or (intG shl 8) or intB

val WhyColor.floatArray
    get() = floatArrayOf(r, g, b, a)
val WhyColor.floatArrayRGB
    get() = floatArrayOf(r, g, b)
val WhyColor.intArrayRGBA
    get() = intArrayOf(intR, intG, intB, intA)
val WhyColor.intArrayRGB
    get() = intArrayOf(intR, intG, intB)

fun WhyColor.withAlpha(alpha: Float) = WhyColor(r, g, b, alpha)

fun WhyColor.rop(scale: WhyColor, offset: WhyColor) = alphaOverlaidWith(scale) + offset
fun WhyColor.rop2(scale: WhyColor, offset: WhyColor) = alphaOverlaidWith(scale + offset)

fun WhyColor.rop(overlay: WhyColor, scale: WhyColor, offset: WhyColor) = alphaOverlaidWith(overlay * scale + offset)
fun ropComponent(overlay: WhyColor, scale: WhyColor, offset: WhyColor) = overlay * scale + offset
fun WhyColor.rop2(overlay: WhyColor, scale: WhyColor, offset: WhyColor) = alphaOverlaidWith((overlay + offset) * scale)

fun WhyColor.intoFloatArrayRGBA(array: FloatArray) {
    array[0] = r
    array[1] = g
    array[2] = b
    array[3] = a
}
fun WhyColor.intoFloatArrayRGB(array: FloatArray) {
    array[0] = r
    array[1] = g
    array[2] = b
}
fun WhyColor.intoIntArrayRGBA(array: IntArray) {
    array[0] = intR
    array[1] = intG
    array[2] = intB
    array[3] = intA
}
fun WhyColor.intoIntArrayRGB(array: IntArray) {
    array[0] = intR
    array[1] = intG
    array[2] = intB
}

/** Note: alpha is averaged */
inline operator fun WhyColor.plus(other: WhyColor) = WhyColor(r + other.r, g + other.g, b + other.b, (a + other.a) * _1_2)
inline operator fun WhyColor.plus(other: Float) = WhyColor(r + other, g + other, b + other, a)
inline operator fun WhyColor.plus(other: Int) = this + (other * _1_255)
//inline operator fun WhyColor.plus(other: WhyColor) = WhyColor((r + other.r).coerceAtMost(1f), (g + other.g).coerceAtMost(1f), (b + other.b).coerceAtMost(1f), (a + other.a) * _1_2)
//inline operator fun WhyColor.plus(other: Float) = WhyColor((r + other).coerceAtMost(1f), (g + other).coerceAtMost(1f), (b + other).coerceAtMost(1f), a)
//inline operator fun WhyColor.plus(other: Int) = WhyColor((r + other * _1_255).coerceAtMost(1f), (g + other * _1_255).coerceAtMost(1f), (b + other * _1_255).coerceAtMost(1f), a)
/** Note: alpha is multiplied */
inline operator fun WhyColor.times(other: WhyColor) = WhyColor(r * other.r, g * other.g, b * other.b, a * other.a)
/** Note: alpha is untouched */
inline operator fun WhyColor.times(multiplier: Float) = WhyColor(r * multiplier, g * multiplier, b * multiplier, a)
//inline operator fun WhyColor.times(multiplier: Float) = WhyColor((r * multiplier).coerceAtMost(1f), (g * multiplier).coerceAtMost(1f), (b * multiplier).coerceAtMost(1f), a)
/** Note: alpha is averaged */
inline infix fun WhyColor.mix(other: WhyColor) = WhyColor((r + other.r) * _1_2, (g + other.g) * _1_2, (b + other.b) * _1_2, (a + other.a) * _1_2)
inline fun WhyColor.mixWeight(o: WhyColor, w: Float): WhyColor {
    val x = 1f - w
    return WhyColor(
        (r * w + o.r * x),
        (g * w + o.g * x),
        (b * w + o.b * x),
        (a * w + o.a * x)
    )
}

inline infix fun WhyColor.alphaOverlaidWith(o: WhyColor) = o alphaOver this

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