// Copyright (c) 2023 wefhy

@file:Suppress("NOTHING_TO_INLINE")

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.config.WhyMapConfig.logsDateFormatter
import dev.wefhy.whymap.config.WhyMapConfig.logsEntryTimeFormatter
import dev.wefhy.whymap.config.WhyMapConfig.pathForbiddenCharacters
import net.minecraft.text.Text
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.Closeable
import java.io.File
import java.time.LocalDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.*
import kotlin.math.pow
import kotlin.math.roundToInt

const val _1_255 = 1f / 255
const val _1_3 = 1f / 3
const val _1_2 = 1f / 2

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

inline val currentDateString
    get() = LocalDateTime.now().format(logsDateFormatter)

inline val currentLogEntryTimeString
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

inline fun File.mkDirsIfNecessary(): File {
    if (!parentFile.exists())
        parentFile.mkdirs()
    return this
}

inline fun getDepthShade(depth: UByte): Float { //TODO use lookup table
    val tmp1 = (1 - depth.toInt() * 0.02f).coerceAtLeast(0f)
    return 1 - tmp1 * tmp1 * _1_3
}

inline fun Int.coerceIn0255() = coerceIn(0, 255)

inline fun UInt.coerceIn0255() = coerceIn(0u, 255u)

inline operator fun Text.plus(other: Text): Text = copy().append(other)

inline fun ShortArray.mapInPlace(transform: (Short) -> Short) {
    for (i in indices) {
        this[i] = transform(this[i])
    }
}

inline fun Array<ShortArray>.mapInPlace(transform: (Short) -> Short) {
    for (subArray in this) {
        subArray.mapInPlace(transform)
    }
}

inline fun unixTime() = System.currentTimeMillis() / 1000

val String.sanitizedPath
    get() = filter { it !in pathForbiddenCharacters }.trim()
//
//@JvmInline
//value class OverflowArray<T>(val array: ArrayList<T>) {
//    operator fun get(index: Int) = array[index.rem(array.size)]
//    operator fun set(index: Int, value: T) {
//        array[index.mod(array.size)] = value
//    }
//}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@OptIn(ExperimentalContracts::class)
@InlineOnly
inline fun <T : Closeable?, R> T.useWith(block: T.() -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return use { it.block() }
}

@RequiresOptIn("This call might be expensive, consider using direct array access")
annotation class ExpensiveCall

fun String.parseHex() = chunked(2).map { Integer.valueOf(it, 16).toByte() }.toByteArray()

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(): String {
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v = get(j).toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

inline fun<reified T : Enum<T>> Enum<T>.nextValue(): Enum<T> {
    val values = javaClass.enumConstants
    return values[(this.ordinal + 1) % values.size]
}

fun<A,B> memoize(block: (A) -> B): (A) -> B {
    val cache = mutableMapOf<A, B>()
    return { cache.getOrPut(it) { block(it) } }
}
