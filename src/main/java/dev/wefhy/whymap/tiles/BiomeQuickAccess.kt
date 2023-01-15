// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles

import dev.wefhy.whymap.CurrentWorld
import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.utils.Color
import dev.wefhy.whymap.utils.FloatColor
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.biome.Biome

context(CurrentWorldProvider<CurrentWorld>)
class BiomeCurrentWorldManager : BiomeManager() {
    private val biomeRegistry = currentWorld.world.registryManager.get(RegistryKeys.BIOME)
    private val biomeNameMap = biomeRegistry.entrySet.associate { it.key.value.path to it.value }
    private val biomeNameMapRev = biomeRegistry.entrySet.associate { it.value to it.key.value.path }
    private val biomeIdMap = biomeNameMap.entries.sortedBy { it.key }.map { it.value }.toTypedArray()
    private val biomeIdMapRev = biomeNameMap.entries.sortedBy { it.key }.withIndex().associate { it.value.value to it.index.toByte() }
    private val fastLookupBiomeFoliage = biomeIdMap.map { Color(it.foliageColor).toFloatColor() }
    private val fastLookupBiomeWaterColor = biomeIdMap.map { Color(it.waterColor) }
    private val plains = biomeNameMap["plains"]!!

    private val netherBiomeMap = mapOf(
        "wastes" to 0x801500,
        "crimson" to 0xd42032,
        "warped" to 0x0ea186,
        "soul" to 0x3d2611,
        "basalt" to 0x787878
    )

    private val fastLookupBiomeFoliageExtras = biomeIdMap.map<Biome, FloatColor> { biome ->
        val name = biomeGetName(biome)
        val nether = netherBiomeMap.keys.find { name.contains(it) }
        if (nether == null)
            Color(biome.foliageColor).toFloatColor()
        else
            Color(netherBiomeMap[nether]!!).toFloatColor()
    }

    override fun biomeGetName(biome: Biome): String = biomeNameMapRev[biome]!!
    override fun encodeBiome(biome: Biome): Byte = biomeIdMapRev[biome]!!
    override fun decodeBiome(id: Byte): Biome = biomeIdMap[id.toInt()]
    //    override fun decodeBiomeFoliage(id: Byte): MapArea.FloatColor = fastLookupBiomeFoliage[id.toInt()]
    override fun decodeBiomeFoliage(id: Byte): FloatColor = fastLookupBiomeFoliageExtras[id.toInt()]
    override fun decodeBiomeWaterColor(id: Byte): Color = fastLookupBiomeWaterColor[id.toInt()]
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

    override fun decodeBiomeFoliage(id: Byte): FloatColor {
        TODO("Not yet implemented")
    }

    override fun decodeBiomeWaterColor(id: Byte): Color {
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
    abstract fun decodeBiomeFoliage(id: Byte): FloatColor
    abstract fun decodeBiomeWaterColor(id: Byte): Color
    abstract fun isPlains(biome: Biome): Boolean
}