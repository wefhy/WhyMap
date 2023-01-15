package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.config.WhyMapConfig.latestFileVersion
import dev.wefhy.whymap.config.WhyMapConfig.mappingsExportDir
import dev.wefhy.whymap.tiles.region.FileVersionManager.WhyMapFileVersion
import dev.wefhy.whymap.utils.mkDirsIfNecessary

object BlockMappingsManager {

    fun getMappings(): String {
        val mappings = MapArea.minecraftBlocks
        return mappings.joinToString("\n")
    }

    fun exportBlockMappings(): String {
        val data = getMappings()
        val file = mappingsExportDir.resolve(latestFileVersion.next.fileName)
        file.mkDirsIfNecessary()
        file.writeText(data, Charsets.UTF_8)
        return data
    }

    private fun blockMappingsForVersion(version: WhyMapFileVersion): List<String> {
        val classloader = javaClass.classLoader
        val resource = classloader.getResource("blockmappings/${version.fileName}")!!
        val mappings = resource.openStream().use {
            it.readAllBytes().toString(Charsets.UTF_8)
        }.split("\n")
        return mappings
    }

    fun getRemapLookup(version1: WhyMapFileVersion, version2: WhyMapFileVersion): List<Short> {
        return getRemapLookup(
            blockMappingsForVersion(version1),
            blockMappingsForVersion(version2)
        )
    }

    private fun getRemapLookup(mappings1: List<String>, mappings2: List<String>): List<Short> {
        return mappings1.map { mappings2.indexOf(it).toShort() }.map { if(it >= 0) it else mappings2.indexOf("block.minecraft.air").toShort() }
    }

    fun remap(data: List<Int>, remapLookUp: List<Int>): List<Int> {
        return data.map { remapLookUp[it] }
    }

    fun remap(data: List<Int>, mappings1: List<String>, mappings2: List<String>) {

    }
}