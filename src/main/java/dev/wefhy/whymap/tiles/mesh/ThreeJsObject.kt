// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.mesh

import kotlinx.serialization.*

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("ThreeJsObject")
open class ThreeJsObject(
    @EncodeDefault
    var posX: Float = 0f,
    @EncodeDefault
    var posY: Float = 0f,
    val children: List<@Polymorphic ThreeJsObject>? = null,
) {

}