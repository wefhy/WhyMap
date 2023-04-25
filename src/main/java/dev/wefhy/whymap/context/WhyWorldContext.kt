// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.context

import dev.wefhy.whymap.config.WhyMapConfig

abstract class WhyWorldContext(val worldName: String) {

    open val worldDisplayName = worldName
    val worldPath = WhyMapConfig.modPath.resolve(worldName)
}

context(dev.wefhy.whymap.context.mod.WhyModContext)
class WhyOnlineWorldContext(worldName: String): WhyWorldContext(worldName) {

}


class WhyOfflineWorldContext(worldName: String) : WhyWorldContext(worldName) {

}
