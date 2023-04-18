// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import me.shedaniel.clothconfig2.api.AbstractConfigEntry
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.text.Text
import kotlin.reflect.KProperty

sealed class SettingsEntry<T: Any>(default: T) {

    private var _value: T

    init {
        _value = default
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return _value
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        _value = value
    }

    abstract fun toConfigEntry(): AbstractConfigEntry<T>

    class SliderEntry(
        val min: Int,
        val max: Int,
        val default: Int
    ): SettingsEntry<Int>(default) {
        private var value by this

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            super.setValue(thisRef, property, value.coerceIn(min..max))
        }

        override fun toConfigEntry(): AbstractConfigEntry<Int> {
            return ConfigEntryBuilder.create().startIntSlider(Text.literal("slider"), value, min, max)
                .setSaveConsumer { value = it }
                .setTextGetter { Text.literal("$it%") }
                .setDefaultValue(default)
                .build()
        }
    }

    class BooleanEntry(
        val default: Boolean
    ): SettingsEntry<Boolean>(default) {
        private var value by this

        override fun toConfigEntry(): AbstractConfigEntry<Boolean> {
            return ConfigEntryBuilder.create().startBooleanToggle(Text.literal("hello"), value)
                .setSaveConsumer { value = it }
                .setDefaultValue(default)
                .build()
        }
    }
}