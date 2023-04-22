// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.libs.whysettings

import dev.wefhy.whymap.utils.WhyRuntime.isClothConfigInstalled
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

abstract class WhySettings() {
    //was working on 12:01:30

    private val categories: MutableList<WhySettingsCategory> = mutableListOf()

    fun buildClothConfig(parent: Screen?): Screen {
        require(isClothConfigInstalled)
        val configBuilder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("WhyMap"))
            .setDoesConfirmSave(false)
            .setSavingRunnable {  }
            .setTransparentBackground(true)
        with(configBuilder) {
            categories.forEach { category ->
                category.buildClothSettings()
            }
        }
        return configBuilder.build()
    }

    fun<T: WhySettingsCategory> T.register(): T {
        categories.add(this)
        return this
    }
}

//@Serializable
//context(WhySettings)
abstract class WhySettingsCategory(val name: String) {
//    val clothConfigCategory: ConfigCategory? = null

//    @Transient
    val entrie: MutableList<SettingsEntry<*>> = mutableListOf()
//    init {
//        categories.add(this) this crashes kotlin compiler if added with context(WhySettings)
//    }


    context(ConfigBuilder)
    fun buildClothSettings( ) {
        val category = getOrCreateCategory(Text.literal(name))
        with(category) {
            entrie.forEach { entry ->
                entry.addToCategory()
            }
        }
    }
}