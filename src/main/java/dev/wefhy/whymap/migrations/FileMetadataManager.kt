// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.config.WhyMapConfig.metadataSize
import dev.wefhy.whymap.utils.parseHex
import dev.wefhy.whymap.utils.toHex
import io.ktor.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * metadata format:
 * 4 bytes: file version
 * 4 bytes: file version inverted
 * 8 bytes: unix timestamp of last meaningful update
 * 16 bytes: block mapping hash
 * 16 bytes: biome mapping hash
 * 16 bytes: reserved for future use
 */
object FileMetadataManager {
    private const val FILE_VERSION = 3
    private val byteOrder = when(ByteOrder.nativeOrder()) {
        ByteOrder.BIG_ENDIAN -> ByteOrderEnum.BIG_ENDIAN
        ByteOrder.LITTLE_ENDIAN -> ByteOrderEnum.LITTLE_ENDIAN
        else -> ByteOrderEnum.NATIVE
    }

    enum class ByteOrderEnum(val i: Byte, val order: ByteOrder) {
        NATIVE(0, ByteOrder.nativeOrder()),
        BIG_ENDIAN(1, ByteOrder.BIG_ENDIAN),
        LITTLE_ENDIAN(2, ByteOrder.LITTLE_ENDIAN),
//        OTHER(3)

    }

    fun encodeMetadata(blockMapping: BlockMapping, biomeMapping: BiomeMapping): ByteArray {
        val arr = ByteArray(metadataSize)
        val buffer = ByteBuffer.wrap(arr)
        buffer.putInt(FILE_VERSION)
        buffer.putInt(FILE_VERSION.inv())
        buffer.putLong(System.currentTimeMillis()) // TODO: use last meaningful update from MapArea (so don't include version changes)
        buffer.put(blockMapping.hash.parseHex())
        buffer.put(biomeMapping.hash.parseHex())
        buffer.put(byteOrder.i)
        buffer.flip()
        return arr
    }

    fun decodeMetadata(arr: ByteArray): WhyMapMetadata? {
        if (arr.size != metadataSize) return null

        val buffer = ByteBuffer.wrap(arr)
        val fileVersion = buffer.int // 0
        val fileVersionInverted = buffer.int // 4
        if (fileVersion != fileVersionInverted.inv()) return null
        val lastUpdate = buffer.long // 8
        val blockMapping = buffer.slice(16, 16).moveToByteArray().toHex()
        val biomeMapping = buffer.slice(32, 16).moveToByteArray().toHex()
        val byteOrder = ByteOrderEnum.entries.find { it.i == buffer.get(48) } ?: ByteOrderEnum.NATIVE
        return WhyMapMetadata(fileVersion, blockMapping, biomeMapping, lastUpdate, byteOrder)
    }

    class WhyMapMetadata(
        val fileVersion: Int,
        val blockMapHash: String,
        val biomeMapHash: String,
        val lastUpdate: Long,
        val byteOrder: ByteOrderEnum
    ) {
        val fileVersionInverted: Int = fileVersion.inv()
    }
}