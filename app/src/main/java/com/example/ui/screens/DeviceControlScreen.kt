package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DeviceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("light") }
    var selectedRoomId by remember { mutableStateOf<Int?>(null) }
    var initialVal by remember { mutableStateOf(50) } // Default slider brightness

    val deviceTypes = listOf(
        Pair("light", "Smart Lightbulb"),
        Pair("fan", "Smart ceiling fan"),
        Pair("ac", "Climate Control (AC)"),
        Pair("lock", "Secure Deadbolt Lock"),
        Pair("plug", "Smart Wall Plug"),
        Pair("sensor", "Hazard/Temp Sensor")
    )

    // Set initial room if available
    LaunchedEffect(rooms) {
        if (selectedRoomId == null && rooms.isNotEmpty()) {
            selectedRoomId = rooms.first().id
        }
    }

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
                    text = "Hardware Nodes",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = "Control or provision physical IoT devices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGrey
                )
            }

            Button(
                onClick = { showAddDeviceDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_device_trigger_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PROVISION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (devices.isEmpty()) {
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
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MutedGrey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Devices Registered", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Click provisioning above to register custom room devices.",
                        color = MutedGrey,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(devices) { device ->
                    val roomName = rooms.find { it.id == device.roomId }?.name ?: "Unknown compartment"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = getDeviceIcon(device.type, device.status),
                                        contentDescription = null,
                                        tint = if (device.status) CyberCyan else MutedGrey,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1B2636), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(device.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Zone: $roomName • ${device.type.uppercase()}", style = MaterialTheme.typography.bodySmall, color = MutedGrey)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Delete node Button
                                    IconButton(onClick = { viewModel.deleteDevice(device) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete device", tint = SafetyCoral.copy(alpha = 0.8f))
                                    }

                                    if (device.type == "lock") {
                                        IconButton(onClick = { viewModel.toggleDoorLock(device) }) {
                                            Icon(
                                                imageVector = if (device.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                contentDescription = "Lock door toggle",
                                                tint = if (device.isLocked) SafetyCoral else NeonEmerald
                                            )
                                        }
                                    } else {
                                        Switch(
                                            checked = device.status,
                                            onCheckedChange = { viewModel.toggleDevice(device) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = CyberCyan,
                                                uncheckedThumbColor = MutedGrey,
                                                uncheckedTrackColor = Color(0xFF1E2838)
                                            )
                                        )
                                    }
                                }
                            }
                            
                            // Render additional controls if node is ON
                            if (device.status && (device.type == "ac" || device.type == "light" || device.type == "fan")) {
                                Divider(color = CardBorderColor, modifier = Modifier.padding(vertical = 12.dp))
                                
                                val parameterTitle = when (device.type) {
                                    "ac" -> "Climate temperature (Min 16 - Max 30): ${device.value}°C"
                                    "fan" -> "Fan rotation Speed step: ${device.value}"
                                    else -> "Light luminance value: ${device.value}%"
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(parameterTitle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    
                                    val rangeLimit = when (device.type) {
                                        "ac" -> 16f..30f
                                        "fan" -> 1f..5f
                                        else -> 10f..100f
                                    }

                                    Slider(
                                        value = device.value.toFloat(),
                                        onValueChange = { viewModel.adjustDeviceValue(device, it.toInt()) },
                                        valueRange = rangeLimit,
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = CyberCyan,
                                            thumbColor = CyberCyan,
                                            inactiveTrackColor = Color(0xFF1E2838)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showAddDeviceDialog = false },
            containerColor = Color(0xFF131A26),
            title = { Text("Provision Hardware Node", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Introduce secure MQTT/Rely nodes into active database partitions.", color = MutedGrey, fontSize = 12.sp)

                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = { Text("Node Name (e.g. Master AC)", color = MutedGrey) },
                        modifier = Modifier.fillMaxWidth().testTag("add_device_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF233045),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Select Node Type
                    Column {
                        Text("Node Category", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(140.dp)
                        ) {
                            items(deviceTypes) { type ->
                                val selected = selectedType == type.first
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = if (selected) CyberCyan.copy(alpha = 0.12f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) CyberCyan else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedType = type.first }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getDeviceIcon(type.first, false),
                                        contentDescription = null,
                                        tint = if (selected) CyberCyan else MutedGrey,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(type.second, color = if (selected) Color.White else MutedGrey, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Select Compartment Zone
                    Column {
                        Text("Target Zone compartment", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        if (rooms.isEmpty()) {
                            Text("Please construct a compartment room section first under Rooms tab.", color = SafetyCoral, fontSize = 11.sp)
                        } else {
                            var expandedRooms by remember { mutableStateOf(false) }
                            val activeRoomName = rooms.find { it.id == selectedRoomId }?.name ?: "Tap to choose compartment"
                            
                            Box {
                                Button(
                                    onClick = { expandedRooms = !expandedRooms },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(activeRoomName, color = Color.White)
                                }

                                DropdownMenu(
                                    expanded = expandedRooms,
                                    onDismissRequest = { expandedRooms = false },
                                    modifier = Modifier.background(Color(0xFF1C273C))
                                ) {
                                    rooms.forEach { room ->
                                        DropdownMenuItem(
                                            text = { Text(room.name, color = Color.White) },
                                            onClick = {
                                                selectedRoomId = room.id
                                                expandedRooms = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetRoom = selectedRoomId
                        if (deviceName.isNotBlank() && targetRoom != null) {
                            val initial = when (selectedType) {
                                "ac" -> 22
                                "fan" -> 3
                                else -> 80
                            }
                            viewModel.addDevice(deviceName, selectedType, targetRoom, initial)
                            deviceName = ""
                            showAddDeviceDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_device_btn")
                ) {
                    Text("REGISTER MODULE", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDeviceDialog = false }) {
                    Text("CANCEL", color = MutedGrey)
                }
            }
        )
    }
}
