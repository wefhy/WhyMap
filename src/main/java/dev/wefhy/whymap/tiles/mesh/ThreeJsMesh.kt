// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import kotlinx.serialization.Serializable

@Serializable
class ThreeJsMesh(
    val vertices: List<Float>,
    val indices: List<Int>,
    val uvs: List<Float>,
): ThreeJsObject()