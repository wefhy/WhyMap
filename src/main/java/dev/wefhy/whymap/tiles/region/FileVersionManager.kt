package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.config.WhyMapConfig.tileMetadataSize
import dev.wefhy.whymap.tiles.region.BlockMappingsManager.currentVersion
import java.nio.ByteBuffer
import kotlin.experimental.inv
import kotlin.random.Random

object FileVersionManager {

    @JvmInline
    value class WhyMapMetadata(val data: ByteArray) {
        init {
            assert(data.size == tileMetadataSize)
        }
    }

    sealed class WhyMapFileVersion(val i: Short) {

        class Custom(i: Short): WhyMapFileVersion(i)

        val isCurrent: Boolean
            get() = this == currentVersion

        val isUnknown: Boolean
            get() = this == Unknown

        val fileName: String
            get() = "$i.blockmap"

        val next: WhyMapFileVersion
            get() = Custom((i + 1).toShort())

        fun getMetadataHead(): ByteArray {
            val arr = ByteArray(8)
            val buffer = ByteBuffer.wrap(arr)
            buffer.putShort(i)
            buffer.putInt(Random(i.toInt()).nextInt())
            buffer.putShort(i.inv())
            buffer.flip()
            return arr
        }

        fun getMetadataArray(): ByteArray {
            val arr = ByteArray(tileMetadataSize)
            getMetadataHead().copyInto(arr)
            return arr
        }

        companion object {
            object UserDefined: WhyMapFileVersion(-1)
            object Unknown: WhyMapFileVersion(0)
            object Version1: WhyMapFileVersion(1)
            object Version2: WhyMapFileVersion(2)

            val latest = Version2

            private val existingVersions = listOf(
                Unknown,
                Version1,
                Version2
            )
            private fun forVersionNumber(i: Short) = existingVersions.find { it.i == i } ?: Unknown

            fun recognizeVersion(metadata: WhyMapMetadata): WhyMapFileVersion {
                val buffer = ByteBuffer.wrap(metadata.data)
                val version = buffer.short
                val c1 = Random(version.toInt()).nextInt()
                val c2 = version.inv()
                if (c1 != buffer.int || c2 != buffer.short) return Unknown
                return forVersionNumber(version)
            }
        }
    }


}