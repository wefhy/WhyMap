// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.minecraftBlocks
import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.config.WhyMapConfig.customMappingsDir
import dev.wefhy.whymap.utils.mkDirsIfNecessary
import dev.wefhy.whymap.utils.toHex
import java.net.URL
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.experimental.inv
import kotlin.random.Random

class MappingsManager(
    private val blockMappings: List<String> = minecraftBlocks.toList(),
    private val biomeMappings: List<String>
) {

    companion object {
        private val md = MessageDigest.getInstance("MD5")
        private val fileWithCurrentBlockMapVersion = customMappingsDir.resolve("current-block")
        private val fileWithCurrentBiomeMapVersion = customMappingsDir.resolve("current-biome")
        private val classLoader = Companion::class.java.classLoader

        private val internalBlockMappings: List<BlockMapping.InternalMapping> =
            getMappings("blockmappings.txt")
                .map { (version, hash) ->
                    BlockMapping.InternalMapping(version.toShort(), hash)
                }
                .also {
                    it.find { it.version == 0.toShort() }?.let { BlockMapping.WhyMapBeta = it }
                }

        private val internalBiomeMappings: List<BiomeMapping.InternalMapping> =
            getMappings("biomemappings.txt")
                .map { (version, hash) ->
                    BiomeMapping.InternalMapping(version.toShort(), hash)
                }
                .also {
                    it.find { it.version == 0.toShort() }?.let { BiomeMapping.LegacyBiomeMapping = it }
                }

        private fun getMappings(resourceName: String): List<List<String>> =
            classLoader
                .getResource(resourceName)
                ?.getMappings()
                ?: emptyList()

        private fun URL.getMappings(): List<List<String>> = openStream()
            .use { it.readAllBytes() }
            .toString(Charsets.UTF_8)
            .lines()
            .map {
                it.split("=")/*.let { (version, hash) ->
                    BlockMapping.InternalMapping(version.toShort(), hash)
                }*/
            }.filter { it.size == 2 }

        private val externalBlockMappings: List<BlockMapping.ExternalMapping> = customMappingsDir
            .listFiles { file -> file.extension == "blockmap" }
            ?.map { file -> BlockMapping.ExternalMapping(file) }
            ?: emptyList()

        private val externalBiomeMappings: List<BiomeMapping.ExternalMapping> = customMappingsDir
            .listFiles { file -> file.extension == "biomemap" }
            ?.map { file -> BiomeMapping.ExternalMapping(file) }
            ?: emptyList()

        //TODO this needs to be tested whether internal mappings have priority in the map!
        private val allBlockMappings: MutableMap<String, BlockMapping> = (externalBlockMappings + internalBlockMappings).associateBy { it.hash }.toMutableMap()

        private val allBiomeMappings: MutableMap<String, BiomeMapping> = (externalBiomeMappings + internalBiomeMappings).associateBy { it.hash }.toMutableMap()

        private fun findInternalBlockMapping(version: Short): BlockMapping? = internalBlockMappings.find { it.version == version }

        private fun findInternalBiomeMapping(version: Short): BiomeMapping? = internalBiomeMappings.find { it.version == version }

        fun recognizeLegacyVersion(metadata: WhyMapLegacyMetadata): BlockMapping? {
            val buffer = ByteBuffer.wrap(metadata.data)
            val version = buffer.short
            val c1 = Random(version.toInt()).nextInt()
            val c2 = version.inv()
            if (c1 == buffer.int && c2 == buffer.short) {
                return findInternalBlockMapping(version) ?: allBlockMappings[metadata.data.toHex()]
            }
            return allBlockMappings[metadata.data.toHex()]
        }

        fun recognizeAllMappings(metadata: WhyMapLegacyMetadata): Pair<BlockMapping?, BiomeMapping?> {
            val buffer = ByteBuffer.wrap(metadata.data)
            val version = buffer.short

            val blockMapping = findInternalBlockMapping(version) ?: allBlockMappings[metadata.data.toHex()]
            val biomeMapping = findInternalBiomeMapping(version) ?: allBiomeMappings[metadata.data.toHex()]
            return blockMapping to biomeMapping
        }

        fun getRemapLookup(mapping1: DataMapping, mapping2: DataMapping): List<Short> {
            return getRemapLookup(
                mapping1.mapping,
                mapping2.mapping
            )
        }

        private fun getRemapLookup(mappings1: List<String>, mappings2: List<String>): List<Short> {
            //TODO memoize mappings
            //TODO binary search? Are they always sorted?
            //TODO separate function for biomes and blocks
            val air = mappings2.indexOf("block.minecraft.air").toShort().takeIf { it >= 0 } ?: mappings2.indexOf("plains").toShort()
            return mappings1.map { mappings2.indexOf(it).toShort() }.map { if (it >= 0) it else air }
        }

        fun remap(data: List<Int>, remapLookUp: List<Int>): List<Int> {
            return data.map { remapLookUp[it] }
        }

        fun List<String>.calculateHash(): ByteArray {
            return joinToString("\n").calculateHash()
        }

        private fun String.calculateHash(): ByteArray {
            return toByteArray(Charsets.UTF_8).calculateHash()
        }

        private fun ByteArray.calculateHash(): ByteArray {
            return md.digest(this)
        }
    }

    init {
        fileWithCurrentBlockMapVersion.delete()
    }

    val blockMappingsJoined = blockMappings.joinToString("\n")
    val biomeMappingsJoined = biomeMappings.joinToString("\n")

    val currentBlockMapping: BlockMapping = blockMappingsJoined.calculateHash().toHex().let { currentHash ->
        (allBlockMappings[currentHash] ?: createNewCustomBlockMappings(currentHash, blockMappings)).also {
            BlockMapping.current = it
            if (it !is BlockMapping.InternalMapping) {
                fileWithCurrentBlockMapVersion.writeText(it.hash, Charsets.UTF_8)
            }
        }
    }

    val currentBiomeMapping: BiomeMapping = biomeMappingsJoined.calculateHash().toHex().let { currentHash ->
        (allBiomeMappings[currentHash] ?: createNewCustomBiomeMappings(currentHash, biomeMappings)).also {
            BiomeMapping.current = it
            if (it !is BiomeMapping.InternalMapping) {
                fileWithCurrentBiomeMapVersion.writeText(it.hash, Charsets.UTF_8)
            }
        }
    }

    val unsupportedAntiNPEBlockRemapLookup = currentBlockMapping.getCurrentRemapLookup()
    val unsupportedAntiNPEBiomeRemapLookup = currentBiomeMapping.getCurrentRemapLookup()

    val metadata = FileMetadataManager.encodeMetadata(
        currentBlockMapping,
        currentBiomeMapping,
    )

    private fun createNewCustomBlockMappings(hash: String, mappings: List<String>): BlockMapping {
        val file = customMappingsDir.resolve("$hash.blockmap")
        file.mkDirsIfNecessary().writeText(blockMappingsJoined, Charsets.UTF_8)
        return BlockMapping.LoadedMapping(hash, mappings).also {
            allBlockMappings[hash] = it
        }
    }

    private fun createNewCustomBiomeMappings(hash: String, mappings: List<String>): BiomeMapping {
        val file = customMappingsDir.resolve("$hash.biomemap")
        file.mkDirsIfNecessary().writeText(biomeMappingsJoined, Charsets.UTF_8)
        return BiomeMapping.LoadedMapping(hash, mappings).also {
            allBiomeMappings[hash] = it
        }
    }

    internal inline fun getCurrentRemapLookup(mapping: BlockMapping): List<Short> = mapping.getCurrentRemapLookup()

    internal inline fun getCurrentRemapLookup(mapping: BiomeMapping): List<Short> = mapping.getCurrentRemapLookup()

    private fun BlockMapping.getCurrentRemapLookup(): List<Short> {
        return getRemapLookup(
            this,
            currentBlockMapping
        )
    }

    private fun BiomeMapping.getCurrentRemapLookup(): List<Short> {
        return getRemapLookup(
            this,
            currentBiomeMapping
        )
    }

    fun getMappings(metadata: FileMetadataManager.WhyMapMetadata): MappingsSet {
        val blockMapping = allBlockMappings[metadata.blockMapHash]
        val biomeMapping = allBiomeMappings[metadata.biomeMapHash]
        return MappingsSet(blockMapping, biomeMapping)
        //todo if either is null, tile should be not marked as modified (so if it's not visited, it won't be saved). TBH it's already kinda handled at the end of MapArea.load()
        //todo if either is null, you could use AI to guess most common blocks correctly
    }

//    fun getCurrentRemapLookups(metadata: FileMetadataManager.WhyMapMetadata): Pair<List<Short>?, List<Short>?> {
//        val blockMapping = allBlockMappings[metadata.blockMapHash]
//        val biomeMapping = allBiomeMappings[metadata.biomeMapHash]
//        return blockMapping?.getCurrentRemapLookup() to biomeMapping?.getCurrentRemapLookup()
//    }

    enum class UnsupportedBlockMappingsBehavior {
        DISABLE_WRITE,
        DONT_CONVERT_OLD,
        EMBED_MAPPINGS,
        EXTERNAL_MAPPINGS,
        SAVE_ONLY_VANILLA,
    }

    @JvmInline
    value class WhyMapLegacyMetadata(val data: ByteArray) {
        init {
            assert(data.size == WhyMapConfig.legacyMetadataSize)
        }
    }

    data class MappingsSet(
        val blockMappings: BlockMapping?,
        val biomeMappings: BiomeMapping?,
    )
}