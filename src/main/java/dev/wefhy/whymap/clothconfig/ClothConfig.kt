// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.clothconfig

import dev.wefhy.whymap.config.UserSettings
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.properties.Delegates

class ClothConfig {
    fun buildConfig(parent: Screen?): Screen {
        val configBuilder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("WhyMap"))
            .setDoesConfirmSave(false)
            .setSavingRunnable {  }
            .setTransparentBackground(true)

        val general = configBuilder.getOrCreateCategory(Text.literal("General"))
        val map = configBuilder.getOrCreateCategory(Text.literal("Map"))
        val server = configBuilder.getOrCreateCategory(Text.literal("Server"))

//        general.addEntry(
//            ConfigEntryBuilder.create().startBooleanToggle(ConfigFactory.lang("show_big_map"), ConfigFactory.modConfig.getBoolean("show_big_map"))
//                .setSaveConsumer(Consumer<Boolean> { `val`: Boolean? -> ConfigFactory.modConfig.setBoolean("show_big_map", `val`) })
//                .setDefaultValue(ConfigFactory.modConfig.getDefault("show_big_map") as Boolean)
//                .build()
//        )
        var a by Delegates.observable(true) { _, _, _ ->  }

        general.addEntry(
            ConfigEntryBuilder.create().startBooleanToggle(Text.literal("hello"), a)
                .setSaveConsumer { a = it }
                .setDefaultValue(false)
                .build()
        )

        general.addEntry(
//            ConfigEntryBuilder.create().startIntSlider(Text.literal("slider"), 0, 100, 50)
            ConfigEntryBuilder.create().startIntSlider(Text.literal("slider"), 50, 0, 100)
                .setSaveConsumer {  }
                .setTextGetter { Text.literal("slide $it hey") }
                .setDefaultValue(50)
                .build()
        )

        server.addEntry(
            ConfigEntryBuilder.create().startEnumSelector(Text.literal("enum"), UserSettings.ExposeHttpApi::class.java, UserSettings.ExposeHttpApi.DEBUG)
                .setSaveConsumer {  }
                .setDefaultValue(UserSettings.ExposeHttpApi.DEBUG)
                .build()
        )

        server.addEntry(
            ConfigEntryBuilder.create().startStrField(Text.literal("str"), "hello")
                .setSaveConsumer {  }
                .setDefaultValue("hello")
                .build()
        )

        server.addEntry(
            ConfigEntryBuilder.create().startStrList(Text.literal("str list"), listOf("hello", "world"))
                .setSaveConsumer {  }
                .setDefaultValue(listOf("hello", "world"))
                .build()
        )

        server.addEntry(
            ConfigEntryBuilder.create().startSelector(Text.literal("selector"), arrayOf("hello", "world"), 0)
                .setSaveConsumer {  }
                .setDefaultValue(0)
                .build()
        )
        return configBuilder.build()

//            .setAlwaysShowTabs()
    }
}