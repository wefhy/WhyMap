// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.config.WhyMapConfig
import java.io.File
import java.nio.ByteBuffer
import kotlin.experimental.inv
import kotlin.random.Random

sealed interface BlockMapping: DataMapping {
    context(CurrentWorldProvider<WhyWorld>)
    val isCurrent: Boolean
        get() = this == currentWorld.mappingsManager.currentBlockMapping
    class ExternalMapping(file: File): DataMapping.ExternalMapping(file), BlockMapping
    class LoadedMapping(hash: String, mapping: List<String>): DataMapping.LoadedMapping(hash, mapping), BlockMapping
    class InternalMapping(version: Short, hash: String): DataMapping.InternalMapping(version, hash), BlockMapping {
        override val folderName = "blockmappings"
        override val fileExtension = "blockmap"

        private fun getMetadataHead(): ByteArray {
            val arr = ByteArray(8)
            val buffer = ByteBuffer.wrap(arr)
            buffer.putShort(version)
            buffer.putInt(Random(version.toInt()).nextInt())
            buffer.putShort(version.inv())
            buffer.flip()
            return arr
        }

        override fun getMetadataArray(): ByteArray {
            val arr = ByteArray(WhyMapConfig.legacyMetadataSize)
            getMetadataHead().copyInto(arr)
            return arr
        }
    }
    companion object {
        lateinit var WhyMapBeta: InternalMapping
    }
}