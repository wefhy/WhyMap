// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.wefhy.whymap.clothconfig.ConfigEntryPoint.getConfigScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
class ModMenuEntryPoint : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(::getConfigScreen)
}
