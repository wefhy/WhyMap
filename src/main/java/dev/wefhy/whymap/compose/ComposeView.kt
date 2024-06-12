// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.scene.MultiLayerComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.utils.Accessors.clientWindow
import dev.wefhy.whymap.utils.WhyDispatchers
import dev.wefhy.whymap.utils.WhyDispatchers.launchOnMain
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.gui.DrawContext
import org.jetbrains.skiko.MainUIDispatcher
import java.awt.Component
import java.io.Closeable
import java.util.concurrent.Executors
import java.awt.event.KeyEvent as AwtKeyEvent

@OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
open class ComposeView(
    width: Int,
    height: Int,
    private val density: Density = Density(2f),
    private val content: @Composable () -> Unit
) : Closeable {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawSingleThreadDispatcher = MainUIDispatcher.limitedParallelism(1)
    protected val singleThreadDispatcher = rawSingleThreadDispatcher +
            CoroutineExceptionHandler { _, throwable -> println(throwable) }
    private var invalidated = true
    private val screenScale = 2 //TODO This is Macbook specific
    private var width by mutableStateOf(width)
    private var height by mutableStateOf(height)
    private val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val directRenderer: Renderer = DirectRenderer(width * screenScale, height * screenScale)
    private val boxedContent: @Composable () -> Unit
        get() = {
//            val width by ::width.asFlow().collectAsState(0)
//            val height by ::height.asFlow().collectAsState(0)
//            with(LocalDensity.current) { //TODO this causes the crash lol xD
//                val dpWidth = outputWidth.toDp()
//                val dpHeight = outputHeight.toDp()
//                println("DP: $dpWidth, $dpHeight")
//                Box(Modifier.size(dpWidth, dpHeight)) {
//                Box(Modifier.size(dpWidth, dpHeight).background(Color(0x77000077.toInt()))) {
            Box(Modifier.size(width.dp * screenScale / density.density, height.dp * screenScale / density.density)) {
                content()
            }
//            }
        }

    //TODO use ImageComposeScene, seems more popular?
    private val scene = MultiLayerComposeScene(coroutineContext = WhyDispatchers.MainDispatcher, density = density) {
//    private val scene = MultiLayerComposeScene(coroutineContext = singleThreadDispatcher, density = density) {
//    private val scene = MultiLayerComposeScene(coroutineContext = coroutineContext, density = density) {
//    private val scene = SingleLayerComposeScene(coroutineContext = coroutineContext, density = density) {
        invalidated = true
    }

    init {
        scene.setContent(boxedContent)
    }

    private inline fun onComposeThread(crossinline block: () -> Unit) = launchOnMain {
        block()
    }

    private fun Offset.toComposeCoords(): Offset {
        return this * clientWindow.scaleFactor.toFloat()
    }

    fun passLMBClick(x: Float, y: Float) = onComposeThread {
        scene.sendPointerEvent(
            eventType = PointerEventType.Press,
            Offset(x, y).toComposeCoords(),
        )
    }

    fun passMouseMove(x: Float, y: Float) = onComposeThread {
        scene.sendPointerEvent(
            eventType = PointerEventType.Move,
            Offset(x, y).toComposeCoords(),
        )
    }

    fun passLMBRelease(x: Float, y: Float) = onComposeThread {
        scene.sendPointerEvent(
            eventType = PointerEventType.Release,
            Offset(x, y).toComposeCoords()
        )
    }

    fun passScroll(x: Float, y: Float, scrollX: Float, scrollY: Float) = onComposeThread {
        scene.sendPointerEvent(
            eventType = PointerEventType.Scroll,
            Offset(x, y).toComposeCoords(),
            scrollDelta = Offset(scrollX, scrollY),
        )
    }

//    private fun getAwtKeyEvent(key: Int, action: Int, modifiers: Int): KeyEvent {
//        val k = Key(key)
//        return java.awt.event.KeyEvent(
//            scene,
//            action,
//            System.currentTimeMillis(),
//            modifiers,
//            key,
//            k.toString().first()
//        ).let {
//            KeyEvent(k, KeyEventType.KeyDown, codePoint = key)
//        }
//    }
    val dummy = object : Component() {}

    private fun createKeyEvent(awtId: Int, time: Long, awtMods: Int, key: Int, char: Char, location: Int) = KeyEvent(
        AwtKeyEvent(dummy, awtId, time, awtMods, key, char, location)
    )

    private fun remapKeycode(key: Int, char: Char): Int {
        return when (key) {
            0x0 -> char.toInt()
            else -> key
        }
    }


    fun passKeyPress(key: Int, action: Int, modifiers: Int) = onComposeThread {
//        scene.sendKeyEvent(androidx.compose.ui.input.key.KeyEvent(AwtKeyEvent.KEY_TYPED, System.nanoTime() / 1_000_000, getAwtMods(), remapKeycode(key, char), 0.toChar(), AwtKeyEvent.KEY_LOCATION_STANDARD))
//        scene.sendKeyEvent(KeyEvent(AwtKeyEvent.KEY_TYPED, System.nanoTime() / 1_000_000, getAwtMods(), remapKeycode(key, char), 0.toChar(), AwtKeyEvent.KEY_LOCATION_STANDARD))
//        val time = System.nanoTime() / 1_000_000
//        val kmod = action//getAwtMods()
//        val char = Key(key).toString().first()
//        val native1 = createKeyEvent(AwtKeyEvent.KEY_PRESSED, time, kmod, remapKeycode(key, char), 0.toChar(), AwtKeyEvent.KEY_LOCATION_STANDARD)
//        val native2 = createKeyEvent(AwtKeyEvent.KEY_TYPED, time, kmod, 0, char, AwtKeyEvent.KEY_LOCATION_UNKNOWN)
//        val k = Key(key)
//        val event1 = KeyEvent(k, KeyEventType.KeyDown, codePoint = key, nativeEvent = native1)
//        scene.sendKeyEvent(event1)
//        val event2 = KeyEvent(k, KeyEventType.Unknown, codePoint = key, nativeEvent = native2)
//        scene.sendKeyEvent(event2)
        val event = KeyEvent(Key(key), KeyEventType.KeyDown, codePoint = key)
        scene.sendKeyEvent(event)//getAwtKeyEvent(key, action, modifiers)))
    }

    fun passKeyRelease(key: Int, action: Int, modifiers: Int) = onComposeThread {
        val event = KeyEvent(Key(key), KeyEventType.KeyUp, codePoint = key)
        scene.sendKeyEvent(event)//getAwtKeyEvent(key, action, modifiers)))
    }

    var isRendering = false

    fun render(drawContext: DrawContext, tickDelta: Float) {
//        println("Trying to start rendering on thread ${Thread.currentThread().name}!")
        if (isRendering) throw Exception("Already rendering!")
        isRendering = true
//        if (!invalidated) return Unit.also {
//            println("Cancelled rendering on thread ${Thread.currentThread().name}!")
//            isRendering = false
//        }
        width = clientWindow.width
        height = clientWindow.height
        directRenderer.onSizeChange(
            width * screenScale,
            height * screenScale
        )
        directRenderer.render(drawContext, tickDelta) { glCanvas ->
            /**
             * So the problem is
             *  - scene.render needs to run on minecraft render thread
             *  - but it also needs to run on the same thread as the scene
             *  - scene under the hood probably uses `val MainUIDispatcher: CoroutineDispatcher get() = SwingDispatcher`
             *  For some reason, this only happens
             *
             *  The problem is in GlobalSnapshotManager.ensureStarted - it uses swing thread to consume events
             */

            try {
//                println("Rendering START!")
                scene.render(glCanvas, System.nanoTime())
//                println("Rendered END!")
                invalidated = false
            } catch (e: Exception) {
                e.printStackTrace()
                scene.setContent(boxedContent)
            }
        }
//        println("Finished rendering on thread ${Thread.currentThread().name}!")
        isRendering = false
    }

    override fun close() {
        directRenderer.close()
        scene.close()
    }
}