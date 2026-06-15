package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkSurface
import com.example.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: HomeViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            if (currentScreen != "login") {
              BottomNavBar(
                activeScreen = currentScreen,
                onNavigate = { screen -> viewModel.setScreen(screen) }
              )
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(SlateDarkBackground)
              .padding(innerPadding)
          ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
              when (screen) {
                "login" -> LoginRegisterScreen(viewModel = viewModel)
                "dashboard" -> DashboardScreen(viewModel = viewModel)
                "rooms" -> RoomManagementScreen(viewModel = viewModel)
                "device_control" -> DeviceControlScreen(viewModel = viewModel)
                "automations" -> AutomationScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
                else -> DashboardScreen(viewModel = viewModel)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun BottomNavBar(
  activeScreen: String,
  onNavigate: (String) -> Unit
) {
  NavigationBar(
    containerColor = SlateDarkSurface,
    tonalElevation = 8.dp,
    modifier = Modifier
      .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
      .testTag("bottom_nav_bar")
  ) {
    NavigationBarItem(
      selected = activeScreen == "dashboard",
      onClick = { onNavigate("dashboard") },
      label = { Text("Home", color = if (activeScreen == "dashboard") CyberCyan else Color.White) },
      icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard", tint = if (activeScreen == "dashboard") CyberCyan else Color.LightGray) },
      colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF1E2838))
    )

    NavigationBarItem(
      selected = activeScreen == "rooms",
      onClick = { onNavigate("rooms") },
      label = { Text("Rooms", color = if (activeScreen == "rooms") CyberCyan else Color.White) },
      icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Rooms", tint = if (activeScreen == "rooms") CyberCyan else Color.LightGray) },
      colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF1E2838))
    )

    NavigationBarItem(
      selected = activeScreen == "device_control",
      onClick = { onNavigate("device_control") },
      label = { Text("Nodes", color = if (activeScreen == "device_control") CyberCyan else Color.White) },
      icon = { Icon(Icons.Default.Sensors, contentDescription = "Devices", tint = if (activeScreen == "device_control") CyberCyan else Color.LightGray) },
      colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF1E2838))
    )

    NavigationBarItem(
      selected = activeScreen == "automations",
      onClick = { onNavigate("automations") },
      label = { Text("Automations", color = if (activeScreen == "automations") CyberCyan else Color.White) },
      icon = { Icon(Icons.Default.Schedule, contentDescription = "Automations", tint = if (activeScreen == "automations") CyberCyan else Color.LightGray) },
      colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF1E2838))
    )

    NavigationBarItem(
      selected = activeScreen == "settings",
      onClick = { onNavigate("settings") },
      label = { Text("Console", color = if (activeScreen == "settings") CyberCyan else Color.White) },
      icon = { Icon(Icons.Default.Settings, contentDescription = "Console", tint = if (activeScreen == "settings") CyberCyan else Color.LightGray) },
      colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF1E2838))
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}
