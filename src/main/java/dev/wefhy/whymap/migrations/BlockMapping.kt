// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.utils.parseHex
import java.io.File
import java.nio.ByteBuffer
import kotlin.experimental.inv
import kotlin.random.Random

sealed class BlockMapping(val hash: String) {
    protected val classloader: ClassLoader = javaClass.classLoader
    var isCurrent = false

    abstract val mapping: List<String>

    open fun getMetadataArray(): ByteArray = hash.parseHex()

    class ExternalMapping(val file: File) : BlockMapping(file.nameWithoutExtension) {
        override val mapping: List<String> by lazy {
            file.readLines(Charsets.UTF_8)
        }
    }

    class LoadedMapping(hash: String, override val mapping: List<String>) : BlockMapping(hash)

    companion object {
        lateinit var WhyMapBeta: InternalMapping
    }

    open class InternalMapping(val version: Short, hash: String) : BlockMapping(hash) {

        override val mapping: List<String> by lazy {
            val resource = classloader.getResource("blockmappings/${version}.blockmap")!!
            resource.openStream().use {
                it.readAllBytes()
            }.toString(Charsets.UTF_8).split("\n")
        }

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
            val arr = ByteArray(WhyMapConfig.tileMetadataSize)
            getMetadataHead().copyInto(arr)
            return arr
        }
    }
}