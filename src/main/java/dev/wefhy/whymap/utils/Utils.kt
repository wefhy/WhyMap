// Copyright (c) 2023 wefhy

@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalStdlibApi::class, ExperimentalContracts::class)

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.config.WhyMapConfig.logsDateFormatter
import dev.wefhy.whymap.config.WhyMapConfig.logsEntryTimeFormatter
import dev.wefhy.whymap.config.WhyMapConfig.pathForbiddenCharacters
import kotlinx.coroutines.sync.Semaphore
import net.minecraft.text.Text
import java.awt.image.*
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.LocalDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.InlineOnly
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

const val _1_255 = 1f / 255
const val _1_3 = 1f / 3
const val _1_2 = 1f / 2
const val bestHashConst = 92821
val rand = Random(0)

inline fun Double.roundToString(places: Int) = String.format("%.${places}f", this)
inline fun Float.roundToString(places: Int) = String.format("%.${places}f", this)

inline fun Double.roundTo(places: Int) = (this * 10.0.pow(places)).roundToInt() * 0.1.pow(places)

private inline fun Double._significant(places: Int) = (places - log10(this)).toInt().coerceAtLeast(0)

internal inline fun Double.significant(places: Int) = String.format("%.${_significant(places)}f", this)

internal inline fun Double.significantBy(max: Double, places: Int) = String.format("%.${max._significant(places)}f", this)

fun BufferedImage.getAverageColor(): Int { // This can only average up to 128x128 textures without integer overflow!!!
    val bytes = (data.dataBuffer as DataBufferByte).data
    val length = bytes.size
    var a = 0u
    var r = 0u
    var g = 0u
    var b = 0u
    for (i in 0..<length step 4) {
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
    for (i in 0..<length step 4) {
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

private val _depthShade = FloatArray(256) { _getDepthShade(it.toUByte()) }

internal inline fun getDepthShade(depth: UByte) = _depthShade[depth.toInt()]

inline fun _getDepthShade(depth: UByte): Float {
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

inline fun ByteArray.mapInPlace(transform: (Byte) -> Byte) {
    for (i in indices) {
        this[i] = transform(this[i])
    }
}

inline fun Array<ShortArray>.mapInPlace(transform: (Short) -> Short) {
    for (subArray in this) {
        subArray.mapInPlace(transform)
    }
}

inline fun Array<ByteArray>.mapInPlace(transform: (Byte) -> Byte) {
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

fun Raster.fillWithColor(color: Int) {
    val data = dataBuffer as DataBufferInt
    val pixels = data.data
    for (i in pixels.indices) {
        pixels[i] = color
    }
}

fun WritableRaster.fillWithColor2(color: Int) {
    println("${bounds.x} ${bounds.y} ${bounds.width} ${bounds.height}, ${minX} ${minY} ${width} ${height}")
    for (y in 0 until height) {
        for (x in 0 until width) {
            setPixel(x, y, intArrayOf(color, rand.nextInt(), rand.nextInt()))
        }
    }
}

inline fun File.useAtomicProxy(block: File.() -> Unit) {
    mkDirsIfNecessary()
    val tmp = File("$absolutePath.tmp").apply {
        createNewFile()
        deleteOnExit()
    }
    tmp.block()
//    tmp.renameTo(this)
    Files.move(tmp.toPath(), this.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
}

inline fun<T> Semaphore.tryAcquire(block: () -> T): T? {
    return if (tryAcquire()) {
        try {
            block()
        } finally {
            release()
        }
    } else {
        null
    }
}