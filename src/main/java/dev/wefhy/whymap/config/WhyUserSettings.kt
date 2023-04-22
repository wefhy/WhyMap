// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.config

import dev.wefhy.whymap.WhyMapClient
import dev.wefhy.whymap.libs.whysettings.SettingsEntry
import dev.wefhy.whymap.libs.whysettings.SettingsEntry.Companion.addSlider
import dev.wefhy.whymap.libs.whysettings.SettingsEntry.Companion.addToggle
import dev.wefhy.whymap.libs.whysettings.WhySettings
import dev.wefhy.whymap.libs.whysettings.WhySettingsCategory

object WhyUserSettings: WhySettings() {
    val generalSettings = GeneralSettingsCategory().register()
    val mapSettings = MapSettingsCategory().register()
    val serverSettings = ServerSettingsCategory().register()

    fun load(userSettings: UserSettings) {
        generalSettings.mapScale = userSettings.mapScale
        generalSettings.displayHud = userSettings.displayHud
        mapSettings.minimapPosition = userSettings.minimapPosition
        mapSettings.minimapMode = userSettings.minimapMode
        serverSettings.exposeHttpApi = userSettings.exposeHttpApi
    }

    fun save(): UserSettings {
        return UserSettings(
            mapScale = generalSettings.mapScale,
            displayHud = generalSettings.displayHud,
            minimapPosition = mapSettings.minimapPosition,
            minimapMode = mapSettings.minimapMode,
            exposeHttpApi = serverSettings.exposeHttpApi,
        )
    }
}

class GeneralSettingsCategory : WhySettingsCategory("General") {
    var mapScale by SettingsEntry(1.0).addSlider("Map scale", 0.5, 2.0)
    var displayHud by SettingsEntry(true).addToggle("Display HUD")
}

class MapSettingsCategory: WhySettingsCategory("Map") {
    var minimapPosition by SettingsEntry(UserSettings.MinimapPosition.TOP_LEFT).addToggle("Minimap position")
    var minimapMode by SettingsEntry(WhyMapClient.MapMode.NORTH_LOCKED).addToggle("Minimap mode")
}

class ServerSettingsCategory: WhySettingsCategory("Server") {
    var exposeHttpApi by SettingsEntry(UserSettings.ExposeHttpApi.LOCALHOST_ONLY).addToggle("Expose HTTP API")
}