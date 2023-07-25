// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.config.WhyUserSettings
import dev.wefhy.whymap.utils.WhyRuntime.isClothConfigInstalled
import dev.wefhy.whymap.utils.plus
import net.minecraft.client.gui.screen.NoticeScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text

object ConfigEntryPoint {

    fun getConfigScreen(parent: Screen? = null): Screen {
        return if (isClothConfigInstalled) {
//            ClothConfig().buildConfig(parent)
            WhyUserSettings.buildClothConfig(parent)
        } else noticeScreen
    }

    private const val clothConfigLink = "https://modrinth.com/mod/cloth-config"
    private val clothConfigClickableLink = Text.literal(clothConfigLink).apply {
        style = style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, clothConfigLink)).withUnderline(true)
    }


    private val configClickableLink = Text.literal(WhyMapConfig.configFile.absolutePath).apply {
        style = style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_FILE, WhyMapConfig.configFile.absolutePath)).withUnderline(true)
    }

    private val text = Text.literal("You can download it from ") + clothConfigClickableLink + Text.literal("\nOr you can configure whymap using the config file at \n") + configClickableLink

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