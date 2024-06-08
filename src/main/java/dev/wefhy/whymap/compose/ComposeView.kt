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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.scene.SingleLayerComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.utils.Accessors.clientWindow
import dev.wefhy.whymap.utils.WhyDispatchers.launchOnMain
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.gui.DrawContext
import java.io.Closeable
import java.util.concurrent.Executors

@OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)
open class ComposeView(
    width: Int,
    height: Int,
    private val density: Density = Density(2f),
    private val content: @Composable () -> Unit
) : Closeable {
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
    private val scene = SingleLayerComposeScene(coroutineContext = coroutineContext, density = density)

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

    fun render(drawContext: DrawContext, tickDelta: Float) {
        width = clientWindow.width
        height = clientWindow.height
        directRenderer.onSizeChange(
            width * screenScale,
            height * screenScale
        )
        directRenderer.render(drawContext, tickDelta) { glCanvas ->
            try {
                scene.render(glCanvas, System.nanoTime())
            } catch (e: Exception) {
                e.printStackTrace()
                scene.setContent(boxedContent)
            }
        }
    }

    override fun close() {
        directRenderer.close()
        scene.close()
    }
}