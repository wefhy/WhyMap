// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.wefhy.whymap.config.WhyMapConfig.configFile
import dev.wefhy.whymap.utils.plus
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.NoticeScreen
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
class ModMenuEntryPoint : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
//        if (FabricLoader.getInstance().isModLoaded("cloth-config2"))
        if (FabricLoader.getInstance().isModLoaded("cloth-config"))
            ClothConfig().buildConfig(parent)
        else
            noticeScreen
    }

    val modrinthLink = "https://modrinth.com/mod/cloth-config"
    val modrinthClickableLink = Text.literal(modrinthLink).apply {
        style = style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, modrinthLink)).withUnderline(true)
    }

    val configClickableLink = Text.literal(configFile.absolutePath).apply {
        style = style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_FILE, configFile.absolutePath)).withUnderline(true)
    }

    val text = Text.literal("You can download it from ") + modrinthClickableLink + Text.literal("\nOr you can configure whymap using the config file at \n") + configClickableLink

    private val noticeScreen =
        NoticeScreen(
            ::closeNoticeScreen,
            Text.literal("Cloth Config required to configure WhyMap"),
            text
        )

    private fun closeNoticeScreen() {
        noticeScreen.close()
    }
}
