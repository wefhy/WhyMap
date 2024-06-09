// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class WaypointEntry(
    val name: String,
    val distance: Float,
    val waypointId: Int,
    val date: Date,
    val waypointStatus: Status,
    val waypointType: Type
) {
    enum class Status {
        NEW, REACHED, ARCHIVED
    }

    enum class Type {
        SPAWN, DEATH, TODO, HOME, SIGHTSEEING
    }
}

@Composable
fun WaypointEntryView(waypointEntry: WaypointEntry) {
    val dateFormatter = SimpleDateFormat("HH:mm, EEE, MMM d", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row {
                    Text(text = waypointEntry.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${waypointEntry.distance}m",
                            modifier = Modifier.align(Alignment.CenterEnd),
                            fontStyle = FontStyle.Italic,
                            fontSize = 17.sp
                        )
                    }
                }
                Text(text = dateFormatter.format(waypointEntry.date), fontSize = 16.sp)
                Text(text = waypointEntry.waypointId.toString(), color = Color.Gray, fontSize = 14.sp)
            }
            Text(
                text = "${waypointEntry.waypointStatus}/${waypointEntry.waypointType}",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Color.Blue
                    )
                    .padding(4.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WaypointsView(waypoints: List<WaypointEntry>, onRefresh: () -> Unit) {
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
//            contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 16.dp)
        ) {
            for (waypoint in waypoints) {
                Box(Modifier.padding(8.dp)) {
                    WaypointEntryView(waypoint)
                }
            }
        }
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(), //TODO removing this causes race condition instead of crash
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 16.dp)
//        ) {
//            items(waypoints) {
//                WaypointEntryView(it)
//            }
//        }

        PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
    }
}

val viewEntry = WaypointEntry(
    name = "Hello",
    distance = 123.57f,
    waypointId = 2137,
    date = Date(),
    waypointStatus = WaypointEntry.Status.NEW,
    waypointType = WaypointEntry.Type.TODO
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
        listOf(viewEntry, viewEntry, viewEntry)
    ){}
}