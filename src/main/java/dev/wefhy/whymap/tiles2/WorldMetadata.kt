// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles2

import kotlinx.serialization.Serializable

@Serializable
data class Worlds(
    val worlds: List<WorldMetadata>,
)

@Serializable
data class WorldMetadata(
    val worldName: String,
    val dimensions: List<DimensionMetadata>,
//    val dimensionName: String,
//    val dimensionNames: List<String>,

)

@Serializable
data class DimensionMetadata(
    val dimensionName: String,
    val dimensionType: WhyDimensionType,
    val lastBlockMapping: String, //TODO maybe put mappings class here nad custom serializer?
    val lastBiomeMappings: String,
)

enum class WhyDimensionType {
    OVERWORLD,
    NETHER,
    END,
    UNKNOWN,
}
