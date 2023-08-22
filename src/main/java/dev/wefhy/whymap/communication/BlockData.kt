// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.communication

@kotlinx.serialization.Serializable
class BlockData(
    val block :String,
    val overlay :String,
    val biome :String,
    val height :Short,
    val depth :UByte,
    val light: UByte
)