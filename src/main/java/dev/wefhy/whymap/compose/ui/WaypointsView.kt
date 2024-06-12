// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wefhy.whymap.waypoints.CoordXYZ
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class WaypointEntry(
    val waypointId: Int,
    val name: String,
    val color: Color,
    val distance: Float,
    val coords: CoordXYZ,
    val date: Date? = null,
    val waypointType: Type? = null
) {

    enum class Type {
        SPAWN, DEATH, TODO, HOME, SIGHTSEEING
    }
}

@Composable
fun WaypointEntryView(waypointEntry: WaypointEntry, modifier: Modifier = Modifier) {
    val dateFormatter = SimpleDateFormat("HH:mm, EEE, MMM d", Locale.getDefault())
    Card(modifier = modifier.fillMaxWidth(), elevation = 8.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(waypointEntry.color.copy(alpha = 0.25f))
                .clipToBounds()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row {
                    Text(text = waypointEntry.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${waypointEntry.waypointType ?: ""}",
                            modifier = Modifier.align(Alignment.CenterEnd),
                            fontStyle = FontStyle.Italic,
                            fontSize = 17.sp
                        )
                    }
                }

                Text(text = "${waypointEntry.distance}m", fontSize = 16.sp)
                Text(text = waypointEntry.date?.let {dateFormatter.format(it)} ?: "Now", color = Color.Gray, fontSize = 14.sp)
            }
            val c = waypointEntry.coords
            Text(
                text = "${c.x}, ${c.y}, ${c.z}",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        waypointEntry.color
//                        Color.Blue
                    )
                    .padding(6.dp),
                color = if (waypointEntry.color.luminance() > 0.5f) Color.Black else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clickable { /*TODO*/ }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun WaypointsView(waypoints: List<WaypointEntry>, onRefresh: () -> Unit, onClick: (WaypointEntry) -> Unit = {}, onHover: (WaypointEntry, Boolean) -> Unit = {_, _ -> }) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        onRefresh()
        delay(100)
        refreshing = false
    }

    val state = rememberPullRefreshState(refreshing, ::refresh)

    Box(Modifier.pullRefresh(state).clipToBounds()) {
        LazyColumn(
            modifier = Modifier.width(270.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 16.dp),
        ) {
            items(waypoints) { wp ->
                WaypointEntryView(wp, Modifier.clickable {
                    onClick(wp)
                }.onPointerEvent(PointerEventType.Enter) {
                    onHover(wp, true)
                }.onPointerEvent(PointerEventType.Exit) {
                    onHover(wp, false)
                })
            }
        }

        PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
    }
}

private val viewEntry = WaypointEntry(
    waypointId = 2137,
    name = "Hello",
    color = Color.Red,
    distance = 123.57f,
    date = Date(),
    coords = CoordXYZ(1, 2, 3),
)


@Preview
@Composable
fun Preview() {
    WaypointEntryView(
        viewEntry
    )
}

@Preview
@Composable
fun Preview2() {
    WaypointsView(
        listOf(viewEntry, viewEntry, viewEntry), {}
    )
}