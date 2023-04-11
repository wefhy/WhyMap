// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.tiles.region.BlockMappingsManager.calculateHash
import dev.wefhy.whymap.utils.toHex

sealed class WhyFileVersion(val version: Short?, val data: List<String>, val hash: String) {

    abstract val fileName: String

    class Unknown : WhyFileVersion(null, emptyList(), "") {
        override val fileName: String
            get() = "0.blockmap"
    }

    class Internal(version: Short, data: List<String>, hash: String = data.calculateHash().toHex()) : WhyFileVersion(version, data, hash) {
        override val fileName: String
            get() = "${hash}.blockmap"
    }

    class External(data: List<String>, hash: String = data.calculateHash().toHex()) : WhyFileVersion(null, data, hash) {
        override val fileName: String
            get() = "${version}.blockmap"
    }

    companion object {

    }
}