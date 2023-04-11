package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.config.WhyMapConfig.tileMetadataSize

object FileVersionManager {

    @JvmInline
    value class WhyMapMetadata(val data: ByteArray) {
        init {
            assert(data.size == tileMetadataSize)
        }
    }

    sealed class WhyMapFileVersion(val i: Short) {

        class Custom(i: Short): WhyMapFileVersion(i)


        val fileName: String
            get() = "$i.blockmap"

        val next: WhyMapFileVersion
            get() = Custom((i + 1).toShort())

        companion object {
            object Unknown: WhyMapFileVersion(0)
            object Version1: WhyMapFileVersion(1)
            object Version2: WhyMapFileVersion(2)

        }
    }


}