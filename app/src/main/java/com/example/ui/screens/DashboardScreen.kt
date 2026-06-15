package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DeviceEntity
import com.example.data.local.entities.RoomEntity
import com.example.data.local.entities.ActivityLogEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun DashboardScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()
    val selectedRoomId by viewModel.selectedRoomId.collectAsState()

    DashboardScreenContent(
        rooms = rooms,
        devices = devices,
        logs = logs,
        selectedRoomId = selectedRoomId,
        onSelectRoom = { roomId -> viewModel.selectRoom(roomId) },
        onToggleDevice = { device -> viewModel.toggleDevice(device) },
        onAdjustDeviceValue = { device, value -> viewModel.adjustDeviceValue(device, value) },
        onToggleDoorLock = { device -> viewModel.toggleDoorLock(device) },
        onSimulateSensor = { device -> viewModel.simulateSensorTrigger(device, (60..120).random()) },
        modifier = modifier
    )
}

@Composable
fun DashboardScreenContent(
    rooms: List<RoomEntity>,
    devices: List<DeviceEntity>,
    logs: List<ActivityLogEntity>,
    selectedRoomId: Int?,
    onSelectRoom: (Int?) -> Unit,
    onToggleDevice: (DeviceEntity) -> Unit,
    onAdjustDeviceValue: (DeviceEntity, Int) -> Unit,
    onToggleDoorLock: (DeviceEntity) -> Unit,
    onSimulateSensor: (DeviceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Derived dashboard counts
    val onlineCount = devices.count { it.connectionStatus == "online" }
    val activeDevicesCount = devices.count { it.status }
    val securityLocksCount = devices.count { it.type == "lock" && it.isLocked }
    val securityUnlockedCount = devices.count { it.type == "lock" && !it.isLocked }

    // Check for extreme alerts (e.g., Gas level high)
    val criticalAlert = devices.find { it.type == "sensor" && it.value > 80 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Welcome and Header Area
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Smart Sync Terminal Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGrey
                    )
                }

                // Mini Indicator Badge
                Surface(
                    color = NeonEmerald.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeonEmerald, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURE",
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Critical Safety Alarm banner
        if (criticalAlert != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SafetyCoral.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SafetyCoral)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Threat Alert",
                            tint = SafetyCoral,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CRITICAL METRIC EXCEEDED",
                                color = SafetyCoral,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Device '${criticalAlert.name}' registered extreme value: ${criticalAlert.value} ppm! Immediate audit required.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Stats Summary Boxes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Devices
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Default.Power, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Active Devices", style = MaterialTheme.typography.labelSmall, color = MutedGrey)
                        Text(
                            text = "$activeDevicesCount / ${devices.size}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                // Security locks
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = if (securityUnlockedCount > 0) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (securityUnlockedCount > 0) SafetyCoral else NeonEmerald
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Security locks", style = MaterialTheme.typography.labelSmall, color = MutedGrey)
                        Text(
                            text = "${securityLocksCount} Locks",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (securityUnlockedCount > 0) SafetyCoral else NeonEmerald
                        )
                    }
                }
            }
        }

        // Room Row Selection
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Rooms",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedRoomId == null,
                            onClick = { onSelectRoom(null) },
                            label = { Text("All Devices") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = SlateDarkSurface,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedRoomId == null,
                                selectedBorderColor = CyberCyan,
                                borderColor = CardBorderColor
                            )
                        )
                    }
                    items(rooms) { room ->
                        FilterChip(
                            selected = selectedRoomId == room.id,
                            onClick = { onSelectRoom(room.id) },
                            label = { Text(room.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = SlateDarkSurface,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedRoomId == room.id,
                                selectedBorderColor = CyberCyan,
                                borderColor = CardBorderColor
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = getRoomIcon(room.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Devices Feed
        item {
            Text(
                text = "Devices Control Grid",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        val filteredDevices = if (selectedRoomId == null) {
            devices
        } else {
            devices.filter { it.roomId == selectedRoomId }
        }

        if (filteredDevices.isEmpty()) {
            item {
                Surface(
                    color = SlateDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = MutedGrey, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No devices in this compartment.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Switch rooms or go to Rooms section to configure and register a new node.",
                            color = MutedGrey,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Group devices to render inside a nice responsive dashboard list
            items(filteredDevices) { device ->
                DeviceDashboardRow(
                    device = device,
                    onToggle = { onToggleDevice(device) },
                    onAdjustValue = { value -> onAdjustDeviceValue(device, value) },
                    onToggleLock = { onToggleDoorLock(device) },
                    onSimulateSensor = { onSimulateSensor(device) }
                )
            }
        }

        // Activity Feed Logs section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "System telemetry feed (Live)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${logs.size} log entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGrey
                )
            }
        }

        val recentLogs = logs.take(6)
        if (recentLogs.isEmpty()) {
            item {
                Text(
                    text = "No log activity detected yet.",
                    color = MutedGrey,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            items(recentLogs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small log badge
                    val badgeColor = when (log.tag) {
                        "SECURITY" -> SafetyCoral
                        "SYSTEM" -> CyberCyan
                        "ALERT" -> Color.Yellow
                        else -> NeonEmerald
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(badgeColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "[${log.tag}]",
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(72.dp)
                    )
                    Text(
                        text = log.message,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceDashboardRow(
    device: DeviceEntity,
    onToggle: () -> Unit,
    onAdjustValue: (Int) -> Unit,
    onToggleLock: () -> Unit,
    onSimulateSensor: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (device.status) SlateDarkSurface else Color(0xFF13171F)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = if (device.status) CyberCyan.copy(alpha = 0.4f) else CardBorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val iconColor = if (device.status) {
                        when (device.type) {
                            "lock" -> SafetyCoral
                            else -> CyberCyan
                        }
                    } else {
                        MutedGrey
                    }
                    Icon(
                        imageVector = getDeviceIcon(device.type, device.status),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1E2838), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = device.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (device.connectionStatus == "online") NeonEmerald else SafetyCoral,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${device.type.uppercase()} • ${device.connectionStatus.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedGrey
                            )
                        }
                    }
                }

                // Interactive control on dashboard
                if (device.type == "lock") {
                    Button(
                        onClick = onToggleLock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (device.isLocked) Color(0xFF263238) else SafetyCoral,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (device.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (device.isLocked) "LOCKED" else "UNLOCK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else if (device.type == "sensor") {
                    Button(
                        onClick = onSimulateSensor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2838),
                            contentColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "SIMULATE ${device.value}ppm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Switch(
                        checked = device.status,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = MutedGrey,
                            uncheckedTrackColor = Color(0xFF233045)
                        )
                    )
                }
            }

            // Slider values for dimmer light, thermostat temperature or fan speed if active
            if (device.status && (device.type == "ac" || device.type == "light" || device.type == "fan")) {
                Spacer(modifier = Modifier.height(12.dp))
                val label = when (device.type) {
                    "ac" -> "Target Thermostat: ${device.value}°C"
                    "fan" -> "Speed Level: ${device.value} of 5"
                    else -> "Brightness Level: ${device.value}%"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(150.dp)
                    )

                    val range = when (device.type) {
                        "ac" -> 16f..30f
                        "fan" -> 1f..5f
                        else -> 10f..100f
                    }
                    Slider(
                        value = device.value.toFloat(),
                        onValueChange = { onAdjustValue(it.toInt()) },
                        valueRange = range,
                        colors = SliderDefaults.colors(
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = Color(0xFF1E2838),
                            thumbColor = CyberCyan
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun getDeviceIcon(type: String, status: Boolean): ImageVector {
    return when (type.lowercase()) {
        "light" -> Icons.Default.Lightbulb
        "lock" -> if (status) Icons.Default.LockOpen else Icons.Default.Lock
        "ac" -> Icons.Default.AcUnit
        "tv" -> Icons.Default.Tv
        "fan" -> Icons.Default.Toys
        "plug" -> Icons.Default.Power
        "sensor" -> Icons.Default.Sensors
        else -> Icons.Default.Info
    }
}

fun getRoomIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "living_room" -> Icons.Default.Home
        "bedroom" -> Icons.Default.Bed
        "kitchen" -> Icons.Default.Kitchen
        "bathroom" -> Icons.Default.Bathtub
        "meeting" -> Icons.Default.MeetingRoom
        else -> Icons.Default.MeetingRoom
    }
}
