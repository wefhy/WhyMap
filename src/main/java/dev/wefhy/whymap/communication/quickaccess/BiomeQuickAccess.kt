// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.communication.quickaccess

import dev.wefhy.whymap.CurrentWorld
import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.whygraphics.WhyColor
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.biome.Biome

context(CurrentWorldProvider<CurrentWorld>)
class BiomeCurrentWorldManager : BiomeManager() {
    private val biomeRegistry = currentWorld.world.registryManager.get(RegistryKeys.BIOME)
    private val biomeNameMap = biomeRegistry.entrySet.associate { it.key.value.path to it.value }
    private val biomeNameMapRev = biomeRegistry.entrySet.associate { it.value to it.key.value.path }
    private val biomeIdMap = biomeNameMap.entries.sortedBy { it.key }.map { it.value }.toTypedArray()
    val biomeNameList = biomeNameMap.keys.sorted()
    private val biomeIdMapRev = biomeNameMap.entries.sortedBy { it.key }.withIndex().associate { it.value.value to it.index.toByte() }
    private val fastLookupBiomeFoliage = biomeIdMap.map { WhyColor.fromRGB(it.foliageColor) }
    private val fastLookupBiomeWaterColor = biomeIdMap.map { WhyColor.fromRGB(it.waterColor) } //TODO maybe add some alpha here already?
    private val plains = biomeNameMap["plains"]!!

    private val netherBiomeMap = mapOf(
        "wastes" to 0x801500,
        "crimson" to 0xd42032,
        "warped" to 0x0ea186,
        "soul" to 0x3d2611,
        "basalt" to 0x787878
    )

    private val fastLookupBiomeFoliageExtras = biomeIdMap.map<Biome, WhyColor> { biome ->
        val name = biomeGetName(biome)
        val nether = netherBiomeMap.keys.find { name.contains(it) }
        if (nether == null)
            WhyColor.fromRGB(biome.foliageColor)
        else
            WhyColor.fromRGB(netherBiomeMap[nether]!!)
    } //TODO maybe add some alpha here already?

    override fun biomeGetName(biome: Biome): String = biomeNameMapRev[biome]!!
    override fun encodeBiome(biome: Biome): Byte = biomeIdMapRev[biome]!!
    override fun decodeBiome(id: Byte): Biome = biomeIdMap[id.toUByte().toInt()]
    //    override fun decodeBiomeFoliage(id: Byte): MapArea.FloatColor = fastLookupBiomeFoliage[id.toInt()]
    override fun decodeBiomeFoliage(id: Byte): WhyColor = fastLookupBiomeFoliageExtras[id.toUByte().toInt()]
    override fun decodeBiomeWaterColor(id: Byte): WhyColor = fastLookupBiomeWaterColor[id.toUByte().toInt()]
    override fun isPlains(biome: Biome) = biome == plains
}

class BiomeOfflineManager : BiomeManager() {
    override fun biomeGetName(biome: Biome): String {
        TODO("Not yet implemented")
    }

    override fun encodeBiome(biome: Biome): Byte {
        TODO("Not yet implemented")
    }

    override fun decodeBiome(id: Byte): Biome {
        TODO("Not yet implemented")
    }

    override fun decodeBiomeFoliage(id: Byte): WhyColor {
        TODO("Not yet implemented")
    }

    override fun decodeBiomeWaterColor(id: Byte): WhyColor {
        TODO("Not yet implemented")
    }

    override fun isPlains(biome: Biome): Boolean {
        TODO("Not yet implemented")
    }

}

abstract class BiomeManager { //Can't be interface for performance reasons (called for each pixel)!
    abstract fun biomeGetName(biome: Biome): String
    abstract fun encodeBiome(biome: Biome): Byte
    abstract fun decodeBiome(id: Byte): Biome
    abstract fun decodeBiomeFoliage(id: Byte): WhyColor
    abstract fun decodeBiomeWaterColor(id: Byte): WhyColor
    abstract fun isPlains(biome: Biome): Boolean
}