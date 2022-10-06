package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.config.WhyMapConfig.latestFileVersion
import dev.wefhy.whymap.config.WhyMapConfig.tileMetadataSize
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

    enum class WhyMapFileVersion(val i: Short) {
        Unknown(0), Version1(1);

        val isCurrent: Boolean
            get() = this == latestFileVersion

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
            private fun forVersionNumber(i: Short) = values().find { it.i == i } ?: Unknown

            fun recognizeVersion(metadata: WhyMapMetadata): WhyMapFileVersion {
                val buffer = ByteBuffer.wrap(metadata.data)
                val version = buffer.short
                val c1 = Random(version.toInt()).nextInt()
                val c2 = version.inv()
                if (c1 != buffer.int || c2 != buffer.short) return Unknown
                return WhyMapFileVersion.forVersionNumber(version)
            }
        }
    }


}