// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wefhy.whymap.WhyMapClient.Companion.kbModSettings
import dev.wefhy.whymap.WhyMapMod
import dev.wefhy.whymap.compose.ComposeView
import dev.wefhy.whymap.compose.styles.McTheme
import dev.wefhy.whymap.compose.styles.mcColors
import dev.wefhy.whymap.compose.styles.noctuaColors
import dev.wefhy.whymap.utils.Accessors.clientInstance
import dev.wefhy.whymap.utils.Accessors.clientWindow
import dev.wefhy.whymap.utils.LocalTileBlock
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class ConfigScreen : Screen(Text.of("Config")) {

    private val vm = MapViewModel()

    private val composeView = ComposeView(
        width = clientWindow.width,
        height = clientWindow.height,
        density = Density(2f)
    ) {
        var visible by remember { mutableStateOf(false) }
        val isDarkTheme by vm.isDark.collectAsState()
        McTheme(colors = if (isDarkTheme) mcColors else noctuaColors) { //todo change theme according to minecraft day/night or real life
            LaunchedEffect(Unit) {
                visible = true
            }
            AnimatedVisibility(visible, enter = scaleIn() + fadeIn()) {
                UI(vm)
            }
//            Scaffold {
//            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
//        super.render(context, mouseX, mouseY, delta)
        composeView.render(context, delta)
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        println("Mouse clicked at $mouseX, $mouseY")
        composeView.passLMBClick(mouseX.toFloat(), mouseY.toFloat())
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        composeView.passMouseMove(mouseX.toFloat(), mouseY.toFloat())
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        println("Mouse released at $mouseX, $mouseY")
        composeView.passLMBRelease(mouseX.toFloat(), mouseY.toFloat())
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
//        println("Mouse scrolled at $mouseX, $mouseY, $horizontalAmount, $verticalAmount")
        composeView.passScroll(mouseX.toFloat(), mouseY.toFloat(), horizontalAmount.toFloat(), verticalAmount.toFloat())
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (kbModSettings.matchesKey(keyCode, scanCode)) {
            println("Closing settings!")
            close()
            return true
        }
        composeView.passKeyPress(keyCode, scanCode, modifiers)
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        composeView.passKeyRelease(keyCode, scanCode, modifiers)
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun close() {
        composeView.close()
        super.close()
    }
}

private var i = 0

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UI(vm: MapViewModel) {
    var clicks by remember { mutableStateOf(0) }
    var showList by remember { mutableStateOf(true) }
    var showMap by remember { mutableStateOf(true) }
    Card(
        border = BorderStroke(1.dp, Color(0.05f, 0.1f, 0.2f)),
        elevation = 20.dp, modifier = Modifier/*.padding(200.dp, 0.dp, 0.dp, 0.dp)*/.padding(8.dp)
    ) {
        Box {
            Column(Modifier.background(MaterialTheme.colors.background)) {
                var showDropDown by remember { mutableStateOf(false) }
                TopAppBar({
                    Text("WhyMap")

                    //smaller text in cursive
                    Text("by wefhy", fontSize = 12.sp, modifier = Modifier.padding(4.dp).offset(0.dp, 4.dp), fontStyle = FontStyle.Italic)
                    DimensionDropDown()
//                    Spacer(Modifier.weight(1f))
//                    BetterDimensionDrop()
                }, actions = {
                    IconButton(
                        onClick = { showDropDown = true }) {
                        Icon(Icons.Filled.MoreVert, null)

                    }
                    DropdownMenu(
                        showDropDown, { showDropDown = false }
                        // offset = DpOffset((-102).dp, (-64).dp),
                    ) {
                        DropdownMenuItem(/*icon = {
                            Icon(
                                Icons.Filled.Home
                            )
                        },*/ onClick = {
                            showDropDown = false
                        }) { Text(text = "Drop down item") }
                    }
                })
                Row(Modifier.padding(8.dp)) {
                    var hovered by remember { mutableStateOf<WaypointEntry?>(null) }
                    var updateCount by remember { mutableStateOf(0) }
                    var center by remember { mutableStateOf(clientInstance.player?.pos ?: Vec3d.ZERO) }
                    val entries = remember { mutableStateListOf<WaypointEntry>() }
                    println("Recomposition ${i++}")
                    Column {
                        Text("Clicks: $clicks")
                        Button(onClick = { clicks++ }) {
                            Text("Click me!")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show List")
                            Switch(checked = showList, onCheckedChange = { showList = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Map")
                            Switch(checked = showMap, onCheckedChange = { showMap = it })
                        }
//                        Text("Hovered: ${hovered?.name ?: "None"}")
                    }

                    remember {
                        val waypoints = WhyMapMod.activeWorld?.waypoints?.waypoints ?: emptyList()
                        entries.addAll(waypoints.mapIndexed { i, it ->
                            WaypointEntry(
                                waypointId = i,
                                name = it.name,
                                color = it.color?.let {
                                    if (it.first() != '#') return@let null
                                    Color(it.drop(1).toInt(16)).copy(alpha = 1f)
                                } ?: Color.Black,
                                distance = clientInstance?.player?.pos?.distanceTo(it.location.toVec3d())?.toFloat() ?: 0f,
                                coords = it.location,
                            )
                        })
                    }

                    Spacer(Modifier.weight(0.000001f))

                    AnimatedVisibility(
                        showMap,
                        Modifier.weight(1f),
                        enter = expandIn(),
                        exit = shrinkOut()
                    ) {
                        MapTileView(LocalTileBlock(center), entries, hovered, updateCount)
                    }

                    AnimatedVisibility(
                        showList,
                        enter = expandIn(),
                        exit = shrinkOut()
                    ) {
                        WaypointsView(entries, {
                            println("Refresh!")
                        }, {
                            println("Clicked on ${it.name}, centering on ${it.coords}")
                            showMap = true
                            center = it.coords.toVec3d()
                            updateCount++
                        }, { entry, hovering ->
                            println("Hovering over ${entry.name}, hovering: $hovering")
                            if (hovering) {
                                hovered = entry
                            } else {
                                if (hovered == entry)
                                    hovered = null
                            }
                        })
                    }
                }
            }
            FloatingActionButton(onClick = { vm.isDark.value = !vm.isDark.value }, Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                val im = if (vm.isDark.value) Icons.TwoTone.ModeNight else Icons.TwoTone.WbSunny
                Icon(im, contentDescription = "Theme")
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BetterDimensionDrop() {
    val coffeeDrinks = arrayOf("OverWorld", "Nether", "End", "Neth/OW overlay")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(coffeeDrinks[0]) }

    Box(
        modifier = Modifier
//            .fillMaxWidth()
            .padding(4.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
//            TextField(
//                value = selectedText,
//                textStyle = MaterialTheme.typography.body1,
//                onValueChange = {},
//                readOnly = true,
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//                // modifier = Modifier.menuAnchor() TODO this will be required in material3
//            )
            val interactionSource = remember { MutableInteractionSource() }
            BasicTextField(
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp).merge(
                    TextStyle(color = TextFieldDefaults.textFieldColors().textColor(true).value)
                ),
                value = selectedText,
                onValueChange = {},
//                textStyle = TextStyle.Default.copy(fontSize = 18.sp),
                modifier = Modifier
//                    .background(
//                        color = colors.background,
//                        shape = TextFieldDefaults.TextFieldShape//RoundedCornerShape(13.dp)
//                    )
//                    .indicatorLine(
//                        enabled = enabled,
//                        isError = false,
//                        interactionSource = interactionSource,
//                        colors = TextFieldDefaults.outlinedTextFieldColors(),
//                        focusedIndicatorLineThickness = 0.dp,  //to hide the indicator line
//                        unfocusedIndicatorLineThickness = 0.dp //to hide the indicator line
//                    )
                    .height(42.dp),


//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp)
//                    .border(1.dp, Color.Black)
            ) {
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = selectedText,
                    enabled = true,
                    innerTextField = it,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(16.dp, 0.dp, 0.dp, 0.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                coffeeDrinks.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            selectedText = item
                            expanded = false
                        }
                    ) { Text(text = item) }
                }
            }
        }
    }
}

@Composable
fun DimensionDropDown() {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("OverWorld") }

    Box(
        modifier = Modifier.fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd).border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { expanded = !expanded }.padding(8.dp)) {
//            IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "More"
            )
//            }
            Text(selected)
        }


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                onClick = { selected = "OverWorld"; expanded = false }
            ) { Text("OverWorld") }
            DropdownMenuItem(
                onClick = { selected = "Nether"; expanded = false }
            ) { Text("Nether") }
            DropdownMenuItem(
                onClick = { selected = "End"; expanded = false }
            ) { Text("End") }
            DropdownMenuItem(
                onClick = { selected = "Neth/OW overlay"; expanded = false }
            ) { Text("Neth/OW overlay") }
        }
    }
}

@Preview
@Composable
private fun preview() {
    val vm = MapViewModel()
    MaterialTheme(colors = darkColors()) {
        Scaffold {
            UI(vm)
        }
    }
}