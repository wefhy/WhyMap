package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.config.WhyMapConfig.latestFileVersion
import dev.wefhy.whymap.config.WhyMapConfig.mappingsExportDir
import dev.wefhy.whymap.tiles.region.FileVersionManager.WhyMapFileVersion
import dev.wefhy.whymap.utils.mkDirsIfNecessary

object BlockMappingsManager {

    fun exportBlockMappings() {
        val mappings = MapArea.minecraftBlocks
        val data = mappings.joinToString("\n")
        val filename = versionFileName(latestFileVersion)
        val file = mappingsExportDir.resolve(filename)
        file.mkDirsIfNecessary()
        file.writeText(data, Charsets.UTF_8)
    }

    private fun blockMappingsForVersion(version: WhyMapFileVersion): List<String> {
        val classloader = javaClass.classLoader
        val resource = classloader.getResource("blockmappings/${versionFileName(version)}")!!
        val mappings = resource.openStream().use {
            it.readAllBytes().toString(Charsets.UTF_8)
        }.split("\n")
        return mappings
    }

    fun getRemapLookup(version1: WhyMapFileVersion, version2: WhyMapFileVersion): List<Int> {
        return getRemapLookup(
            blockMappingsForVersion(version1),
            blockMappingsForVersion(version2)
        )
    }

    private fun getRemapLookup(mappings1: List<String>, mappings2: List<String>): List<Int> {
        return mappings1.map { mappings2.indexOf(it) }.map { if(it >= 0) it else mappings2.indexOf("block.minecraft.air") }
    }

    fun remap(data: List<Int>, remapLookUp: List<Int>): List<Int> {
        return data.map { remapLookUp[it] }
    }

    fun remap(data: List<Int>, mappings1: List<String>, mappings2: List<String>) {

    }

    private fun versionFileName(version: WhyMapFileVersion) = "${version.i}.blockmap"
}