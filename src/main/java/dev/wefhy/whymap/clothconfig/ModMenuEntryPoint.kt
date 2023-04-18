// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader

@Environment(EnvType.CLIENT)
class ModMenuEntryPoint : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
//        if (FabricLoader.getInstance().isModLoaded("cloth-config2"))
        if (FabricLoader.getInstance().isModLoaded("cloth-config"))
            ClothConfig().buildConfig(parent)
        else
            parent
        //TODO display warning if cloth-config is not installed

    }
}
