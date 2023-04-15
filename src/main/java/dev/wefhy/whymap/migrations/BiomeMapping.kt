// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import java.io.File

interface BiomeMapping: DataMapping {
    val isCurrent: Boolean
        get() = this == current
    class ExternalMapping(file: File): DataMapping.ExternalMapping(file), BiomeMapping
    class LoadedMapping(hash: String, mapping: List<String>): DataMapping.LoadedMapping(hash, mapping), BiomeMapping
    class InternalMapping(version: Short, hash: String): DataMapping.InternalMapping(version, hash), BiomeMapping {
        override val folderName = "biomemappings"
        override val fileExtension = "biomemap"
    }
    companion object {
        lateinit var current: BiomeMapping
        lateinit var LegacyBiomeMapping: InternalMapping
    }
}