// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.runtime.*
import dev.wefhy.whymap.config.UserSettings
import dev.wefhy.whymap.config.WhyUserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MapViewModel(scope: CoroutineScope) {
    var isDark = MutableStateFlow(WhyUserSettings.generalSettings.theme == UserSettings.Theme.DARK)
    init {
        scope.launch {
            isDark.collectLatest {
                WhyUserSettings.generalSettings.theme = if (it) UserSettings.Theme.DARK else UserSettings.Theme.LIGHT
            }
        }
    }
}