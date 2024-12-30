// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.migrations

import dev.wefhy.whymap.migrations.MappingsManager
import org.junit.jupiter.api.Test
import java.net.URL

/**
 * This test class should check whether there are any missing migrations
 * It should scan existing block mappings
 */


class MissingMigrations {
    @Test
    fun `read existing mappings`() {
        val mappings = getMappings("blockmappings.txt")
        println(mappings)

        val fullMappings = allBlockMappings
        println(fullMappings)
    }

    @Test
    fun `check full integrity`() {
        val all = allBlockMappings.values.toList()
        for (i in all.indices) {
            for (j in i + 1 until all.size) {
                checkIntegrity("${i-1}", all[i], "$i", all[j])
            }
        }
    }

    @Test
    fun `check incremental integrity`() {
        val all = allBlockMappings.values.toList()
        for (i in 1 until all.size) {
            checkIntegrity("${i-1}", all[i - 1], "$i", all[i])
        }
    }

    private fun checkIntegrity(name1: String, map1: BlockMapping, name2: String, map2: BlockMapping) {
        val map1Mappings: List<String> = map1.mapping
        val map2Mappings: List<String> = map2.mapping

        //check whether there's anything in map1 that's not in map2
        val missingMappings = map1Mappings.filter { it !in map2Mappings }
        if (missingMappings.isNotEmpty()) {
            println("Missing mappings in $name1(${map1.hash}) compared to $name2(${map2.hash}): $missingMappings")
        } else {
            println("No missing mappings in $name1(${map1.hash}) compared to $name2(${map2.hash})")
        }
    }



    companion object {
        private val classLoader = Companion::class.java.classLoader
        private val externalBlockMappings = listOf<BlockMapping>()
        private val externalBiomeMappings = listOf<BiomeMapping>()
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
        private val allBlockMappings: MutableMap<String, BlockMapping> = (externalBlockMappings + internalBlockMappings).associateBy { it.hash }.toMutableMap()

        private val allBiomeMappings: MutableMap<String, BiomeMapping> = (externalBiomeMappings + internalBiomeMappings).associateBy { it.hash }.toMutableMap()


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
    }
}