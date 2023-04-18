// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import me.shedaniel.clothconfig2.api.AbstractConfigEntry

class TheConfig {

    val slider = SettingsEntry.SliderEntry(0, 100, 50)
    val bool = SettingsEntry.BooleanEntry(true)

    fun toConfigEntries(): List<AbstractConfigEntry<out Any>> {
        val list = listOf(
            slider,
            bool
        )
        return list.map { it.toConfigEntry() }
    }
}