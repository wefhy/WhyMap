// Copyright (c) 2023 wefhy

@file:Suppress("NOTHING_TO_INLINE")

package dev.wefhy.whymap.whygraphics

import dev.wefhy.whymap.utils._1_2
import dev.wefhy.whymap.utils._1_255
import dev.wefhy.whymap.utils.coerceIn0255

abstract class WhyIntARGB {
    abstract val intARGB: Int
}

typealias FastWhyColor = FloatArray



val FastWhyColor.a: Float
    inline get() = this[0]
val FastWhyColor.r: Float
    inline get() = this[1]
val FastWhyColor.g: Float
    inline get() = this[2]
val FastWhyColor.b: Float
    inline get() = this[3]

val FastWhyColor.intA
    inline get() = (a * 255).toInt()
val FastWhyColor.intR
    inline get() = (r * 255).toInt()
val FastWhyColor.intG
    inline get() = (g * 255).toInt()
val FastWhyColor.intB
    inline get() = (b * 255).toInt()

val FastWhyColor.intARGB
    inline get() = (intA shl 24) or (intR shl 16) or (intG shl 8) or intB

fun FastWhyColor.toWhyColor(): WhyColor = WhyColor(r, g, b, a)

inline fun WhyColor.toFastWhyColor(): FastWhyColor = floatArrayOf(a, r, g, b)

class WhyColor(
    @JvmField val r: Float,
    @JvmField val g: Float,
    @JvmField val b: Float,
    @JvmField val a: Float = 1f
) {
//    val intABGR by lazy { _intABGR }
    override fun toString(): String {
        return "WhyColor(r=$r, g=$g, b=$b, a=$a)"
    }
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

        fun fromGray(gray: Int): WhyColor {
            val fl = gray * _1_255
            return WhyColor(fl, fl, fl)
        }

        fun fromInts(r: Int, g: Int, b: Int) = WhyColor(r * _1_255, g * _1_255, b * _1_255)

        fun fromRGB(rgb: Int): WhyColor {
            return WhyColor(
                ((rgb shr 16) and 0xFF) * _1_255,
                ((rgb shr 8) and 0xFF) * _1_255,
                (rgb and 0xFF) * _1_255
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

        fun fromABGR(abgr: Int): WhyColor {
            return WhyColor(
                (abgr and 0xFF) * _1_255,
                ((abgr shr 8) and 0xFF) * _1_255,
                ((abgr shr 16) and 0xFF) * _1_255,
                ((abgr shr 24) and 0xFF) * _1_255
            )
        }

        fun fromARGB(argb: Int): WhyColor {
            return WhyColor(
                ((argb shr 16) and 0xFF) * _1_255,
                ((argb shr 8) and 0xFF) * _1_255,
                (argb and 0xFF) * _1_255,
                ((argb shr 24) and 0xFF) * _1_255
            )
        }
    }
}

val WhyColor.intR
    inline get() = (r * 255f).toInt().coerceIn0255() //TODO create CoercedWhyColor? Might be a lot more efficient for some operations
val WhyColor.intG
    inline get() = (g * 255f).toInt().coerceIn0255()
val WhyColor.intB
    inline get() = (b * 255f).toInt().coerceIn0255()
val WhyColor.intA
    inline get() = (a * 255f).toInt().coerceIn0255()
val WhyColor.intRGB
    inline get() = (intR shl 16) or (intG shl 8) or intB
val WhyColor.intBGR
    inline get() = (intB shl 16) or (intG shl 8) or intR
val WhyColor.intRGBA
    inline get() = (intR shl 24) or (intG shl 16) or (intB shl 8) or intA
val WhyColor.intARGB
    inline get() = (intA shl 24) or (intR shl 16) or (intG shl 8) or intB
val WhyColor.intABGR
    inline get() = (intA shl 24) or (intB shl 16) or (intG shl 8) or intR

private const val mulA = 255f * 255f * 255f * 255f
private const val mulR = 255f * 255f * 255f
private const val mulG = 255f * 255f
private const val mulB = 255f

private val WhyColor.fastA
    inline get() = (a * mulA).coerceIn(0f, mulA).toInt()
private val WhyColor.fastR
    inline get() = (r * mulR).coerceIn(0f, mulR).toInt()
private val WhyColor.fastG
    inline get() = (g * mulG).coerceIn(0f, mulG).toInt()
private val WhyColor.fastB
    inline get() = (b * mulB).coerceIn(0f, mulB).toInt()

private const val maskA = 0xFF000000.toInt()
private const val maskR = 0x00FF0000.toInt()
private const val maskG = 0x0000FF00.toInt()
private const val maskB = 0x000000FF.toInt()

internal val WhyColor.fastIntARGB
    inline get() = (fastA and maskA) or (fastR and maskR) or (fastG and maskG) or (fastB and maskB)


val WhyColor.floatArray
    get() = floatArrayOf(r, g, b, a)
val WhyColor.floatArrayRGB
    get() = floatArrayOf(r, g, b)
val WhyColor.intArrayRGBA
    get() = intArrayOf(intR, intG, intB, intA)
val WhyColor.intArrayRGB
    get() = intArrayOf(intR, intG, intB)

fun intoFloatArrayRGBA(color: WhyColor, array: FloatArray) {
    array[0] = color.r
    array[1] = color.g
    array[2] = color.b
    array[3] = color.a
}
fun intoFloatArrayRGB(color: WhyColor, array: FloatArray) {
    array[0] = color.r
    array[1] = color.g
    array[2] = color.b
}
fun intoIntArrayRGBA(color: WhyColor, array: IntArray) {
    array[0] = color.intR
    array[1] = color.intG
    array[2] = color.intB
    array[3] = color.intA
}
fun intoIntArrayRGB(color: WhyColor, array: IntArray) {
    array[0] = color.intR
    array[1] = color.intG
    array[2] = color.intB
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