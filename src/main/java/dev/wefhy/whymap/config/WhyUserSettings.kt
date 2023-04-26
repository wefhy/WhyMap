// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.config

import dev.wefhy.whymap.WhyMapClient
import dev.wefhy.whymap.gui.WhyConfirmScreen
import dev.wefhy.whymap.libs.whysettings.CustomSetSettingsEntry
import dev.wefhy.whymap.libs.whysettings.SettingsEntry
import dev.wefhy.whymap.libs.whysettings.SettingsEntry.Companion.addColorPicker
import dev.wefhy.whymap.libs.whysettings.SettingsEntry.Companion.addSlider
import dev.wefhy.whymap.libs.whysettings.SettingsEntry.Companion.addToggle
import dev.wefhy.whymap.libs.whysettings.WhySettings
import dev.wefhy.whymap.libs.whysettings.WhySettingsCategory
import dev.wefhy.whymap.whygraphics.WhyColor
import dev.wefhy.whymap.whygraphics.intARGB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient

object WhyUserSettings: WhySettings() {
    val generalSettings = GeneralSettingsCategory().register()
    val mapSettings = MapSettingsCategory().register()
    val serverSettings = ServerSettingsCategory().register()

    fun load(userSettings: UserSettings) {
        mapSettings.mapScale = userSettings.mapScale
        generalSettings.displayHud = userSettings.displayHud
        mapSettings.minimapPosition = userSettings.minimapPosition
        mapSettings.minimapMode = userSettings.minimapMode
        serverSettings.exposeHttpApi = userSettings.exposeHttpApi
        mapSettings.forceExperimentalMinmap = userSettings.forceExperimentalMinmap
        generalSettings.hudColor = WhyColor.fromARGB(userSettings.hudColor)
    }

    fun save(): UserSettings {
        return UserSettings(
            mapScale = mapSettings.mapScale,
            displayHud = generalSettings.displayHud,
            minimapPosition = mapSettings.minimapPosition,
            minimapMode = mapSettings.minimapMode,
            exposeHttpApi = serverSettings.exposeHttpApi,
            forceExperimentalMinmap = mapSettings.forceExperimentalMinmap,
            hudColor = generalSettings.hudColor.intARGB
        )
    }
}

class GeneralSettingsCategory : WhySettingsCategory("General") {
    var displayHud by SettingsEntry(true).addToggle("Display HUD")
    var hudColor by SettingsEntry(WhyColor.White).addColorPicker("HUD color")
}

class MapSettingsCategory: WhySettingsCategory("Map") {
    var forceExperimentalMinmap by CustomSetSettingsEntry(
        false
    ) { proposedValue, actuallySet ->
        if (proposedValue) {
            GlobalScope.launch {
                with(MinecraftClient.getInstance()) {
                    WhyConfirmScreen(
                        "Experimental minimap",
                        "This minimap is experimental and WILL cause crashes if used more than a few minutes. Are you sure you want to enable it?"
                    ) {
                        if (it) {
                            actuallySet()
                        }
                    }.show()
                }
            }
        } else {
            actuallySet()
        }
    }.addToggle("Force Minimap (experimental, will cause crashes)")
    var minimapPosition by SettingsEntry(UserSettings.MinimapPosition.TOP_LEFT).addToggle("Minimap position")
    var minimapMode by SettingsEntry(WhyMapClient.MapMode.NORTH_LOCKED).addToggle("Minimap mode")
    var mapScale by SettingsEntry(1.0).addSlider("Map scale", 0.5, 2.0)
}

class ServerSettingsCategory: WhySettingsCategory("Server") {
    var exposeHttpApi by SettingsEntry(UserSettings.ExposeHttpApi.LOCALHOST_ONLY).addToggle("Expose HTTP API (RESTART REQUIRED)")
}

class HudEntrySettings(
    val name: String,
    val visible: Boolean,
    val color: WhyColor,
    val priority: Int,
)