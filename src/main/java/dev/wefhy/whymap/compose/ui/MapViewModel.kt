// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.wefhy.whymap.config.UserSettings
import dev.wefhy.whymap.config.WhyUserSettings
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapViewModel {
    var isDark = MutableStateFlow(WhyUserSettings.generalSettings.theme == UserSettings.Theme.DARK)
    init {
        GlobalScope.launch {
            isDark.collectLatest {
                WhyUserSettings.generalSettings.theme = if (it) UserSettings.Theme.DARK else UserSettings.Theme.LIGHT
            }
        }
    }
}