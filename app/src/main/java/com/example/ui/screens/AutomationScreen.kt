package com.example.ui.screens

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
import com.example.data.local.entities.AutomationEntity
import com.example.data.local.entities.ScheduleEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val automations by viewModel.automations.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val devices by viewModel.devices.collectAsState()

    var activeTab by remember { mutableStateOf("schedules") } // "schedules" or "rules"
    var showCreateDialog by remember { mutableStateOf(false) }

    // Dialog state for Schedule
    var scheduleName by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("08:00") }
    var selectedDeviceSchedId by remember { mutableStateOf<Int?>(null) }
    var scheduleAction by remember { mutableStateOf("ON") }

    // Dialog state for Automation Rule
    var ruleName by remember { mutableStateOf("") }
    var triggerSensorName by remember { mutableStateOf("") }
    var triggerCondVal by remember { mutableStateOf("80") }
    var triggerOp by remember { mutableStateOf(">") }
    var selectedDeviceTargetId by remember { mutableStateOf<Int?>(null) }
    var actionCmdType by remember { mutableStateOf("TURN_ON") }

    // Set defaults when dialog displays
    LaunchedEffect(devices) {
        if (selectedDeviceSchedId == null && devices.isNotEmpty()) {
            selectedDeviceSchedId = devices.first().id
        }
        if (selectedDeviceTargetId == null && devices.isNotEmpty()) {
            selectedDeviceTargetId = devices.first().id
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
                    text = "Automations & Schedules",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = "Configure timers and rules-based logic",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGrey
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_automation_trigger_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CREATE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Custom segment control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141A24), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeTab = "schedules" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "schedules") Color(0xFF1E2838) else Color.Transparent,
                    contentColor = if (activeTab == "schedules") CyberCyan else MutedGrey
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedules", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { activeTab = "rules" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "rules") Color(0xFF1E2838) else Color.Transparent,
                    contentColor = if (activeTab == "rules") CyberCyan else MutedGrey
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Automation Rules", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Dynamic lists based on active category
        if (activeTab == "schedules") {
            if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                        .background(SlateDarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No schedules established.", color = MutedGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(schedules) { schedule ->
                        val targetDevName = devices.find { it.id == schedule.deviceId }?.name ?: "Unknown Module"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                            border = BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1B2636), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(schedule.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(
                                            text = "Trigger at ${schedule.timeStr} • Target: $targetDevName (${schedule.action})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedGrey
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { viewModel.deleteSchedule(schedule) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SafetyCoral.copy(alpha = 0.7f))
                                    }
                                    
                                    Switch(
                                        checked = schedule.isActive,
                                        onCheckedChange = { viewModel.toggleSchedule(schedule, it) },
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
                    }
                }
            }
        } else {
            // Automation Rules list
            if (automations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                        .background(SlateDarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No local automation rules programmed.", color = MutedGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(automations) { automation ->
                        val targetDevName = devices.find { it.id == automation.actionDeviceId }?.name ?: "Unknown Module"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                            border = BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1B2636), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(automation.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(
                                            text = "IF ${automation.triggerDeviceName} ${automation.triggerCondition} \nTHEN Execute: $targetDevName -> ${automation.actionCommand}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedGrey,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { viewModel.deleteAutomation(automation) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SafetyCoral.copy(alpha = 0.7f))
                                    }
                                    
                                    Switch(
                                        checked = automation.isActive,
                                        onCheckedChange = { viewModel.toggleAutomation(automation, it) },
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
                    }
                }
            }
        }
    }

    // Modal dialogue
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF131A26),
            title = {
                Text(
                    text = if (activeTab == "schedules") "Configure Schedule Timer" else "Program Automation Edge Rule",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (activeTab == "schedules") {
                        // Schedules creation parameters
                        OutlinedTextField(
                            value = scheduleName,
                            onValueChange = { scheduleName = it },
                            label = { Text("Schedule Label", color = MutedGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("schedule_name_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0xFF233045),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = scheduleTime,
                            onValueChange = { scheduleTime = it },
                            label = { Text("Time (e.g. 18:30)", color = MutedGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("schedule_time_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0xFF233045),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Action type
                        Column {
                            Text("Action Trigger", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                FilterChip(
                                    selected = scheduleAction == "ON",
                                    onClick = { scheduleAction = "ON" },
                                    label = { Text("Turn ON") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                                )
                                FilterChip(
                                    selected = scheduleAction == "OFF",
                                    onClick = { scheduleAction = "OFF" },
                                    label = { Text("Turn OFF") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                                )
                            }
                        }

                        // Target Device selection
                        Column {
                            Text("Target Device", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (devices.isEmpty()) {
                                Text("No target nodes available.", color = SafetyCoral, fontSize = 11.sp)
                            } else {
                                var expandedDevs by remember { mutableStateOf(false) }
                                val currentDevName = devices.find { it.id == selectedDeviceSchedId }?.name ?: "Choose device"
                                
                                Box {
                                    Button(
                                        onClick = { expandedDevs = !expandedDevs },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(currentDevName, color = Color.White)
                                    }

                                    DropdownMenu(
                                        expanded = expandedDevs,
                                        onDismissRequest = { expandedDevs = false },
                                        modifier = Modifier.background(Color(0xFF1C273C))
                                    ) {
                                        devices.forEach { dev ->
                                            DropdownMenuItem(
                                                text = { Text(dev.name, color = Color.White) },
                                                onClick = {
                                                    selectedDeviceSchedId = dev.id
                                                    expandedDevs = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Rules creation parameters
                        OutlinedTextField(
                            value = ruleName,
                            onValueChange = { ruleName = it },
                            label = { Text("Automation Rule Name", color = MutedGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("rule_name_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0xFF233045),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Trigger Source
                        OutlinedTextField(
                            value = triggerSensorName,
                            onValueChange = { triggerSensorName = it },
                            label = { Text("Triggering Sensor Model (e.g. Gas Leak Sensor)", color = MutedGrey) },
                            modifier = Modifier.fillMaxWidth().testTag("sensor_name_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0xFF233045),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Operator & Value
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("IF Value is", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            
                            Button(
                                onClick = { triggerOp = if (triggerOp == ">") "<" else ">" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = CyberCyan)
                            ) {
                                Text(triggerOp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedTextField(
                                value = triggerCondVal,
                                onValueChange = { triggerCondVal = it },
                                label = { Text("value", color = MutedGrey) },
                                modifier = Modifier.width(80.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = Color(0xFF233045),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // Target Device selection
                        Column {
                            Text("THEN Execute on:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (devices.isEmpty()) {
                                Text("No target nodes available.", color = SafetyCoral, fontSize = 11.sp)
                            } else {
                                var expandedDevsRule by remember { mutableStateOf(false) }
                                val currentDevName = devices.find { it.id == selectedDeviceTargetId }?.name ?: "Choose device"
                                
                                Box {
                                    Button(
                                        onClick = { expandedDevsRule = !expandedDevsRule },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(currentDevName, color = Color.White)
                                    }

                                    DropdownMenu(
                                        expanded = expandedDevsRule,
                                        onDismissRequest = { expandedDevsRule = false },
                                        modifier = Modifier.background(Color(0xFF1C273C))
                                    ) {
                                        devices.forEach { dev ->
                                            DropdownMenuItem(
                                                text = { Text(dev.name, color = Color.White) },
                                                onClick = {
                                                    selectedDeviceTargetId = dev.id
                                                    expandedDevsRule = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Action commands selection
                        Column {
                            Text("Action Command", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                FilterChip(
                                    selected = actionCmdType == "TURN_ON",
                                    onClick = { actionCmdType = "TURN_ON" },
                                    label = { Text("TURN ON") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                                )
                                FilterChip(
                                    selected = actionCmdType == "TURN_OFF",
                                    onClick = { actionCmdType = "TURN_OFF" },
                                    label = { Text("TURN OFF") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                                )
                                FilterChip(
                                    selected = actionCmdType == "LOCK",
                                    onClick = { actionCmdType = "LOCK" },
                                    label = { Text("LOCK DOOR") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyberCyan, selectedLabelColor = Color.Black)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (activeTab == "schedules") {
                            val targetDev = selectedDeviceSchedId
                            if (scheduleName.isNotBlank() && targetDev != null) {
                                viewModel.createSchedule(scheduleName, targetDev, scheduleTime, "MON,WED,FRI", scheduleAction)
                                scheduleName = ""
                                showCreateDialog = false
                            }
                        } else {
                            val targetDev = selectedDeviceTargetId
                            if (ruleName.isNotBlank() && triggerSensorName.isNotBlank() && targetDev != null) {
                                viewModel.createAutomation(ruleName, "sensor", triggerSensorName, "$triggerOp $triggerCondVal", targetDev, actionCmdType)
                                ruleName = ""
                                triggerSensorName = ""
                                showCreateDialog = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_automation_btn")
                ) {
                    Text("ACTIVATE RULE", color = CyberCyan, fontWeight = FontWeight.Bold)
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
