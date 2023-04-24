// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.libs.whysettings

import dev.wefhy.whymap.utils.MappingContext
import dev.wefhy.whymap.utils.mapToDouble
import dev.wefhy.whymap.utils.mapToInt
import dev.wefhy.whymap.utils.significantBy
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.text.Text
import kotlin.math.log10
import kotlin.reflect.KProperty

context(WhySettingsCategory)
open class SettingsEntry<T: Any>(val default: T) {

    private var _value: T = default

    private var entryBuilder: ConfigCategory.() -> Unit = {}

    init {
        entrie.add(this)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return _value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        _value = value
    }

    open var guiValue
        get() = _value
        set(value) { _value = value }

    context(ConfigCategory)
    fun addToCategory() {
        return this@ConfigCategory.entryBuilder()
    }


    private fun addEntry(block: ConfigEntryBuilder.() -> AbstractConfigListEntry<*>) {
        entryBuilder = {
            val builder = ConfigEntryBuilder.create()
            val e = block(builder)
            addEntry(e)
        }
    }

    companion object {
        fun SettingsEntry<Boolean>.addToggle(name: String): SettingsEntry<Boolean> {
            addEntry {
                startBooleanToggle(Text.literal(name), guiValue)
                    .setSaveConsumer { guiValue = it }
                    .setDefaultValue(default)
                    .build()
            }
            return this
        }

        fun SettingsEntry<Int>.addSlider(name: String, min: Int = 0, max: Int = 100): SettingsEntry<Int> {
            addEntry {
                startIntSlider(Text.literal(name), guiValue, min, max)
                    .setTextGetter { Text.literal("$it%") }
                    .setSaveConsumer { guiValue = it }
                    .setDefaultValue(default)
                    .build()
            }
            return this
        }

        fun SettingsEntry<Double>.addSlider(name: String, min: Double = 0.0, max: Double = 100.0, resolution: Int = 100): SettingsEntry<Double> {
            with(MappingContext(resolution, min, max)) {
                val signifPlaces = log10(resolution - 1f).toInt() + 1
                addEntry {
                    startIntSlider(Text.literal(name), guiValue.mapToInt, 0, resolution)
                        .setTextGetter { Text.literal(it.mapToDouble.significantBy(max, signifPlaces)) }
                        .setSaveConsumer { guiValue = it.mapToDouble }
                        .setDefaultValue(default.mapToInt)
                        .build()
                }
            }
            return this
        }

        internal fun<T : Enum<T>> SettingsEntry<T>.addToggle(name: String): SettingsEntry<T> {
            addEntry {
                startEnumSelector(Text.literal(name), guiValue.javaClass, guiValue)
                    .setSaveConsumer { guiValue = it }
                    .setDefaultValue(default)
                    .build()
            }
            return this
        }
    }
}

context(WhySettingsCategory)
class VetoableSettingsEntry<T: Any>(default: T, val allow: (T) -> Boolean): SettingsEntry<T>(default) {

    override var guiValue: T
        get() = super.guiValue
        set(value) {
            if (allow(value)) {
                super.guiValue = value
            }
        }
}

context(WhySettingsCategory)
class CustomSetSettingsEntry<T: Any>(default: T, val set: (proposedValue: T, actuallySet: () -> Unit) -> Unit): SettingsEntry<T>(default) {
    override var guiValue: T
        get() = super.guiValue
        set(value) {
            if (guiValue == value) return
            set(value) {
                super.guiValue = value
            }
        }
}

//context(WhySettingsCategory)
//class ConfirmScreenSettingsEntry<T: Any>(default: T, confirmMessage: String): SettingsEntry<T>(default) {
//    override var guiValue: T
//        get() = super.guiValue
//        set(value) {
//            if (guiValue == value) return
//            with(MinecraftClient.getInstance()) {
//                WhyConfirmScreen(
//                    "Experimental minimap",
//                    "This minimap is experimental and may cause crashes. Are you sure you want to enable it?"
//                ) {
//                    if (it) {
//                        super.guiValue = value
//                    }
//                }.show()
//            }
//        }
//}

//fun main() {
//
//
//    val doubles = listOf(
//        0.0000000234,
//        0.000324, // 6
//        0.0234, //4
//        0.234, // 3
//        1.3245, // 2
//        7.2345, // 1
//        134.523445, // 0
//        23453412.4235, // 0
//        23415134562345.254 // 0
//    )
////    println(String.format("Answer : %.3f", d))
//    println(doubles.joinToString("\n") { "${it._significant(3).toString()} -> ${log(it, 10.0)} -> ${it.significant(3).toString()}" })
//
//}