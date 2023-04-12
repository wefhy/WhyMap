// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.utils.parseHex
import java.io.File

interface DataMapping {
    val hash: String
    val mapping: List<String>

    fun getMetadataArray(): ByteArray = hash.parseHex()

    abstract class ExternalMapping(val file: File) : DataMapping {
        override val hash = file.nameWithoutExtension
        override val mapping: List<String> by lazy {
            file.readLines(Charsets.UTF_8)
        }
    }

    abstract class LoadedMapping(override val hash: String, override val mapping: List<String>) : DataMapping

    abstract class InternalMapping(val version: Short, override val hash: String) : DataMapping {
        protected abstract val folderName: String
        protected abstract val fileExtension: String
        protected val classloader: ClassLoader = javaClass.classLoader

        override val mapping: List<String> by lazy {
            val resource = classloader.getResource("$folderName/$version.$fileExtension")!!
            resource.openStream().use {
                it.readAllBytes()
            }.toString(Charsets.UTF_8).split("\n")
        }
    }
}