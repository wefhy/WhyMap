// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import java.nio.ByteBuffer


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
    private const val fileVersion = 1

    fun encodeMetadata(biomeMapping: BiomeMapping, blockMapping: BlockMapping): ByteArray {
        val arr = ByteArray(64)
        val buffer = ByteBuffer.wrap(arr)
        buffer.putInt(fileVersion)
        buffer.putInt(fileVersion.inv())
        buffer.putLong(System.currentTimeMillis()) // TODO: use last meaningful update from MapArea (so don't include version changes)
        buffer.put(blockMapping.hash.toByteArray())
        buffer.put(biomeMapping.hash.toByteArray())
        buffer.flip()
        return arr
    }

    fun decodeMetadata(arr: ByteArray): WhyMapMetadata? {
        if (arr.size != 64) return null

        val buffer = ByteBuffer.wrap(arr)
        val fileVersion = buffer.int
        val fileVersionInverted = buffer.int
        if (fileVersion != fileVersionInverted.inv()) return null
        val lastUpdate = buffer.long
        buffer.alignedSlice(16)
        val blockMapping = buffer.slice(16, 16).toString()
        val biomeMapping = buffer.slice(32, 16).toString()
        return WhyMapMetadata(fileVersion, blockMapping, biomeMapping, lastUpdate)
    }

    class WhyMapMetadata(
        val fileVersion: Int,
        val blockMapHash: String,
        val biomeMapHash: String,
        val lastUpdate: Long
    ) {
        val fileVersionInverted: Int = fileVersion.inv()
    }
}