// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import kotlinx.serialization.Serializable

@Serializable
class ThreeJsMesh(
//    val x: Float,
//    val y: Float,
    val vertices: List<Float>,
    val faces: List<Int>,
    val uvs: List<Float>
)