// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
open class ThreeJsObject(
    var posX: Float = 0f,
    var posY: Float = 0f,
    val children: List<@Polymorphic ThreeJsObject>? = null,
) {

}