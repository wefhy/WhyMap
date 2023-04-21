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
class SettingsEntry<T: Any>(val default: T) {

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
            var value by this
            addEntry {
                startBooleanToggle(Text.literal(name), default)
                    .setSaveConsumer { value = it }
                    .setDefaultValue(default)
                    .build()
            }
            return this
        }

        fun SettingsEntry<Int>.addSlider(name: String, min: Int = 0, max: Int = 100): SettingsEntry<Int> {
            var value by this
            addEntry {
                startIntSlider(Text.literal(name), value, min, max)
                    .setTextGetter { Text.literal("$it%") }
                    .setSaveConsumer { value = it }
                    .setDefaultValue(default)
                    .build()
            }
            return this
        }

        fun SettingsEntry<Double>.addSlider(name: String, min: Double = 0.0, max: Double = 100.0, resolution: Int = 100): SettingsEntry<Double> {
            with(MappingContext(resolution, min, max)) {
                val signifPlaces = log10(resolution - 1f).toInt() + 1
                var value by this@addSlider
                addEntry {
                    startIntSlider(Text.literal(name), value.mapToInt, 0, resolution)
                        .setTextGetter { Text.literal(it.mapToDouble.significantBy(max, signifPlaces)) }
                        .setSaveConsumer { value = it.mapToDouble }
                        .setDefaultValue(default.mapToInt)
                        .build()
                }
            }
            return this
        }

        internal fun<T : Enum<T>> SettingsEntry<T>.addToggle(name: String): SettingsEntry<T> {
            var value by this
            addEntry {
                startEnumSelector(Text.literal(name), value.javaClass, value)
                    .setSaveConsumer { value = it }
                    .setDefaultValue(default)
                    .build()
            }
            return this
        }
    }
}

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