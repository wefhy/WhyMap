#-keep,allowoptimization,allowobfuscation,allowshrinking class dev.wefhy.whymap.** { *; }
-keep class dev.wefhy.whymap.** { *; }
-ignorewarnings
-dontoptimize
-dontpreverify
-dontobfuscate

-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars  <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjars  <java.home>/jmods/java.management.jmod(!**.jar;!module-info.class)

-dontwarn net.minecraft.**
-dontwarn net.fabric.**
-dontwarn com.mojang.**
-dontwarn net.fabricmc.**
-dontwarn me.shedaniel.clothconfig2.**
-dontwarn org.w3c.dom.**
-dontwarn com.terraformersmc.modmenu.**

-keepattributes Exceptions,Signature,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-printmapping out.map
#-keep class kotlin.Metadata
#-keep class kotlin.jvm.functions.Function1 { *; }


-dontwarn **kotlinx.coroutines.**
-dontwarn **org.apache.commons.codec**
-dontwarn **slf4j**

-dontwarn javax.xml.parsers.**
-dontwarn javax.xml.transform.**
-dontwarn org.xml.sax.**
-dontwarn org.spongepowered.asm.**

-dontwarn it.unimi.dsi.fastutil.booleans.**
-dontwarn com.google.common.collect.**
-dontwarn org.joml.Matrix4f

