// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.config.WhyMapConfig.metadataSize
import dev.wefhy.whymap.utils.parseHex
import dev.wefhy.whymap.utils.toHex
import io.ktor.util.*
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
    private const val fileVersion = 2

    fun encodeMetadata(blockMapping: BlockMapping, biomeMapping: BiomeMapping): ByteArray {
        val arr = ByteArray(metadataSize)
        val buffer = ByteBuffer.wrap(arr)
        buffer.putInt(fileVersion)
        buffer.putInt(fileVersion.inv())
        buffer.putLong(System.currentTimeMillis()) // TODO: use last meaningful update from MapArea (so don't include version changes)
        buffer.put(blockMapping.hash.parseHex())
        buffer.put(biomeMapping.hash.parseHex())
        buffer.flip()
        return arr
    }

    fun decodeMetadata(arr: ByteArray): WhyMapMetadata? {
        if (arr.size != metadataSize) return null

        val buffer = ByteBuffer.wrap(arr)
        val fileVersion = buffer.int
        val fileVersionInverted = buffer.int
        if (fileVersion != fileVersionInverted.inv()) return null
        val lastUpdate = buffer.long
        val blockMapping = buffer.slice(16, 16).moveToByteArray().toHex()
        val biomeMapping = buffer.slice(32, 16).moveToByteArray().toHex()
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