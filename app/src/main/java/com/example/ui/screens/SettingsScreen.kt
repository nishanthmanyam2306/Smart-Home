package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()

    var backendUrl by remember { mutableStateOf("http://192.168.1.100:8000") }
    var mqttBroker by remember { mutableStateOf("mqtt.eclipseprojects.io") }
    var forceMqttLocal by remember { mutableStateOf(true) }

    // Hydrate fields based on settings on start
    LaunchedEffect(settings) {
        val u = settings.find { it.keyStr == "backend_url" }?.valueStr
        if (u != null) backendUrl = u
        
        val m = settings.find { it.keyStr == "mqtt_broker" }?.valueStr
        if (m != null) mqttBroker = m

        val l = settings.find { it.keyStr == "mock_mqtt_mode" }?.valueStr
        if (l != null) forceMqttLocal = l.toBoolean()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming header
        item {
            Column {
                Text(
                    text = "System Console",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Text(
                    text = "Configure local environment nodes and endpoints",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGrey
                )
            }
        }

        // Segment 1: Logged Credentials
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1E2838), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = currentUser?.email ?: "Guest Operator",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Role: ${currentUser?.role?.uppercase() ?: "USER"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedGrey
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyCoral.copy(alpha = 0.15f), contentColor = SafetyCoral),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("logout_btn")
                    ) {
                        Text("LOGOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Segment 2: Network configurations (FastAPI + MQTT)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Orchestrator Endpoints", fontWeight = FontWeight.Bold, color = Color.White)

                    OutlinedTextField(
                        value = backendUrl,
                        onValueChange = {
                            backendUrl = it
                            viewModel.saveConfigValue("backend_url", it)
                        },
                        label = { Text("FastAPI REST Endpoint URL", color = MutedGrey) },
                        modifier = Modifier.fillMaxWidth().testTag("backend_url_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF233045),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = mqttBroker,
                        onValueChange = {
                            mqttBroker = it
                            viewModel.saveConfigValue("mqtt_broker", it)
                        },
                        label = { Text("MQTT Broker IP/Host", color = MutedGrey) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF233045),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Sandboxed Fallback", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Operate internally if REST endpoints are offline.", color = MutedGrey, fontSize = 11.sp)
                        }
                        
                        Switch(
                            checked = forceMqttLocal,
                            onCheckedChange = {
                                forceMqttLocal = it
                                viewModel.saveConfigValue("mock_mqtt_mode", it.toString())
                            },
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

        // Segment 3: Audit Trails (Full log list)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historical System Audit Trail",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = { viewModel.clearLogHistory() }) {
                    Text("CLEAR ALL", color = SafetyCoral, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Text("No historical event logs inside room partitions.", color = MutedGrey, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(logs) { log ->
                val badgeColor = when (log.tag) {
                    "SECURITY" -> SafetyCoral
                    "SYSTEM" -> CyberCyan
                    "ALERT" -> Color.Yellow
                    else -> NeonEmerald
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111622)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(badgeColor, RoundedCornerShape(100f)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "[${log.tag}]",
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.width(72.dp)
                        )
                        Text(
                            text = log.message,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
