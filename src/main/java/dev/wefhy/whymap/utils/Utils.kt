// Copyright (c) 2022 wefhy

@file:Suppress("NOTHING_TO_INLINE")

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.config.WhyMapConfig.logsDateFormatter
import dev.wefhy.whymap.config.WhyMapConfig.logsEntryTimeFormatter
import dev.wefhy.whymap.utils.ObfuscatedLogHelper.i
import net.minecraft.text.Text
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.time.LocalDateTime
import kotlin.math.pow
import kotlin.math.roundToInt

const val _1_255 = 1f / 255
const val _1_3 = 1f / 3

inline fun Double.roundTo(places: Int) = (this * 10.0.pow(places)).roundToInt() * 0.1.pow(places)

fun BufferedImage.getAverageColor(): Int { // This can only average up to 128x128 textures without integer overflow!!!
    val bytes = (data.dataBuffer as DataBufferByte).data
    val length = bytes.size
    var a = 0u
    var r = 0u
    var g = 0u
    var b = 0u
    for (i in 0 until length step 4) {
        val _a = bytes[i + 0].toUByte()
        a += _a
        r += bytes[i + 3].toUByte() * _a
        g += bytes[i + 2].toUByte() * _a
        b += bytes[i + 1].toUByte() * _a
    }
    val divider = (length * 255 / 4).toUInt()
    if (a == 0u) a = 1u
    return bytesToInt(
        r = r / a,
        g = g / a,
        b = b / a,
        a = a / divider
    )
}

fun BufferedImage.getAverageLeavesColor(): Int { // This can only average up to 128x128 textures without integer overflow!!!
    val bytes = (data.dataBuffer as DataBufferByte).data
    val length = bytes.size
    var a = 0u
    var r = 0u
    var g = 0u
    var b = 0u
    for (i in 0 until length step 4) {
        val _a = bytes[i + 0].toUByte()
        a += _a
        r += bytes[i + 3].toUByte()
        g += bytes[i + 2].toUByte()
        b += bytes[i + 1].toUByte()
    }
    if (a == 0u) a = 1u
    val divider = (length / 4).toUInt()
    return bytesToInt(
        r = r / divider,
        g = g / divider,
        b = b / divider,
        a = a / divider
    )
}

val currentDateString
    get() = LocalDateTime.now().format(logsDateFormatter)

val currentLogEntryTimeString
    get() = LocalDateTime.now().format(logsEntryTimeFormatter)

inline fun bytesToInt(r: UInt, g: UInt, b: UInt, a: UInt): Int {
    return (a.coerceIn0255().toInt() shl 24) or (r.coerceIn0255().toInt() shl 16) or (g.coerceIn0255()
        .toInt() shl 8) or b.coerceIn0255().toInt()
}

inline fun bytesToInt(r: Int, g: Int, b: Int, a: Int): Int {
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

inline fun bytesToInt(r: Byte, g: Byte, b: Byte, a: Byte): Int {
    return (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
}

inline fun File.mkDirsIfNecessary() {
    if (!parentFile.exists())
        parentFile.mkdirs()
}


inline fun getDepthShade(depth: Byte): Float { //TODO use lookup table
    val tmp1 = (1 - depth * 0.02f).coerceAtLeast(0f)
    return 1 - tmp1 * tmp1 * _1_3
}

inline fun Int.coerceIn0255() = coerceIn(0, 255)

inline fun UInt.coerceIn0255() = coerceIn(0u, 255u)

inline operator fun Text.plus(other: Text): Text = copy().append(other)

inline fun ShortArray.mapInPlace(transform: (Short) -> Short) {
    for (i in this.indices) {
        this[i] = transform(this[i])
    }
}

inline fun Array<ShortArray>.mapInPlace(transform: (Short) -> Short) {
    for (subArray in this) {
        subArray.mapInPlace(transform)
    }
}


