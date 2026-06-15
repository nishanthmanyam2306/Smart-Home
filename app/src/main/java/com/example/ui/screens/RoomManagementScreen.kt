package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RoomEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val devices by viewModel.devices.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var roomName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("living_room") }

    val iconChoices = listOf(
        Pair("living_room", "Living Room"),
        Pair("bedroom", "Bedroom"),
        Pair("kitchen", "Kitchen"),
        Pair("bathroom", "Bathroom"),
        Pair("meeting", "Office")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Room Compartments",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = "Manage physical layout zones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGrey
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_room_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                    .background(SlateDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MutedGrey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Rooms Registered", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Tap the ADD button above to configure a custom section.",
                        color = MutedGrey,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms) { room ->
                    val roomDeviceCount = devices.count { it.roomId == room.id }
                    val activeInRoom = devices.count { it.roomId == room.id && it.status }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectRoom(room.id); viewModel.setScreen("dashboard") }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = getRoomIcon(room.iconName),
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF1B2636), RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.deleteRoom(room.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = SafetyCoral.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = room.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$roomDeviceCount devices connected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedGrey
                                )
                            }
                            
                            if (activeInRoom > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = NeonEmerald.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$activeInRoom node(s) ON",
                                        color = NeonEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add room
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Configure Room Compartment", fontWeight = FontWeight.Bold, color = Color.White) },
            containerColor = Color(0xFF131A26),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Register a new logical container inside the local database environment.", color = MutedGrey, fontSize = 12.sp)

                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Compartment Name", color = MutedGrey) },
                        modifier = Modifier.fillMaxWidth().testTag("add_room_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF233045),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Column {
                        Text("Visual Icon Indicator", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            lazyRowItems(iconChoices) { choice ->
                                val selected = selectedIconName == choice.first
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            color = if (selected) CyberCyan.copy(alpha = 0.15f) else Color(0xFF1E2838),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) CyberCyan else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedIconName = choice.first }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getRoomIcon(choice.first),
                                        contentDescription = choice.second,
                                        tint = if (selected) CyberCyan else MutedGrey,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (roomName.isNotBlank()) {
                            viewModel.addRoom(roomName, selectedIconName)
                            roomName = ""
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_room_button")
                ) {
                    Text("REGISTER Compartment", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = MutedGrey)
                }
            }
        )
    }
}
