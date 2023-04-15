// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import java.io.File

interface BiomeMapping: DataMapping {
    context(CurrentWorldProvider<WhyWorld>)
    val isCurrent: Boolean
        get() = this == currentWorld.mappingsManager.currentBiomeMapping
    class ExternalMapping(file: File): DataMapping.ExternalMapping(file), BiomeMapping
    class LoadedMapping(hash: String, mapping: List<String>): DataMapping.LoadedMapping(hash, mapping), BiomeMapping
    class InternalMapping(version: Short, hash: String): DataMapping.InternalMapping(version, hash), BiomeMapping {
        override val folderName = "biomemappings"
        override val fileExtension = "biomemap"
    }
    companion object {
        lateinit var LegacyBiomeMapping: InternalMapping
    }
}