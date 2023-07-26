// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.CurrentWorld
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess
import kotlinx.serialization.Serializable

@Serializable
class AreaStatistics private constructor(
    val blockCounter: List<Pair<String, Int>>,
    val overlayCounter: List<Pair<String, Int>>,
    val biomeCounter: List<Pair<String, Int>>
) {
//    context(dev.wefhy.whymap.CurrentWorldProvider<dev.wefhy.whymap.WhyWorld>)
    constructor(currentWorld: CurrentWorld,vararg statistics: EncodedRegionStatistics) : this(
        blockCounter = statistics
            .flatMap { it.blockCounter.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.sum() }
            .mapKeys { BlockQuickAccess.decodeBlock(it.key).block.name.string }
            .toList()
            .sortedByDescending { it.second },
        overlayCounter = statistics
            .flatMap { it.overlayCounter.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.sum() }
            .mapKeys { BlockQuickAccess.decodeBlock(it.key).block.name.string }
            .toList()
            .sortedByDescending { it.second },
        biomeCounter = statistics
            .flatMap { it.biomeCounter.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.sum() }
            .mapKeys { currentWorld.biomeManager.biomeGetName(
                currentWorld.biomeManager.decodeBiome(it.key)
            ) }
            .toList()
            .sortedByDescending { it.second }
    )

    override fun toString(): String {
        val biomeSum = biomeCounter.sumOf { it.second }
        return """
Note: Undiscovered area is shown as Acacia Button / Bandlands (or other alphabedically first block/biome)
Blocks: 
    ${blockCounter.joinToString("\n    ") { "${it.first}: ${it.second}" }}
Overlays:
    ${overlayCounter.joinToString("\n    ") { "${it.first}: ${it.second}" }}
Biomes:
    ${biomeCounter.joinToString("\n    ") { "${it.first}: ${it.second * 100 / biomeSum}%" }}
        """.trimIndent()
    }
}
