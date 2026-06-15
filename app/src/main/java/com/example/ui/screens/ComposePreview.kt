package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.local.entities.ActivityLogEntity
import com.example.data.local.entities.DeviceEntity
import com.example.data.local.entities.RoomEntity
import com.example.ui.theme.MyApplicationTheme

@Preview(name = "Login Screen - Access Mode", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun LoginScreenAccessPreview() {
    MyApplicationTheme {
        LoginRegisterScreenContent(
            authError = null,
            isAuthenticating = false,
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
            onAutofill = {}
        )
    }
}

@Preview(name = "Login Screen - Register Mode", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun LoginScreenRegisterPreview() {
    MyApplicationTheme {
        LoginRegisterScreenContent(
            authError = "Simulated invalid credentials block.",
            isAuthenticating = false,
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
            onAutofill = {}
        )
    }
}

@Preview(name = "Dashboard Screen - All Devices", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DashboardAllDevicesPreview() {
    val mockRooms = listOf(
        RoomEntity(id = 1, name = "Living Room", iconName = "living_room"),
        RoomEntity(id = 2, name = "Master Bedroom", iconName = "bedroom"),
        RoomEntity(id = 3, name = "Kitchen Area", iconName = "kitchen")
    )

    val mockDevices = listOf(
        DeviceEntity(id = 101, name = "Main Chandelier", type = "light", roomId = 1, status = true, value = 80),
        DeviceEntity(id = 102, name = "HVAC Climate Control", type = "ac", roomId = 1, status = true, value = 22),
        DeviceEntity(id = 103, name = "Security Front Door", type = "lock", roomId = 1, status = false, isLocked = true),
        DeviceEntity(id = 104, name = "Exhaust Fan Node", type = "fan", roomId = 3, status = false, value = 0),
        DeviceEntity(id = 105, name = "Coffee Maker Socket", type = "plug", roomId = 3, status = true, value = 0),
        DeviceEntity(id = 106, name = "Environment Laser Sensor", type = "sensor", roomId = 2, status = true, value = 42)
    )

    val mockLogs = listOf(
        ActivityLogEntity(id = 1, timestamp = System.currentTimeMillis() - 5000, tag = "INFO", message = "System orchestrator node active."),
        ActivityLogEntity(id = 2, timestamp = System.currentTimeMillis() - 15000, tag = "SECURITY", message = "Front lock securely locked."),
        ActivityLogEntity(id = 3, timestamp = System.currentTimeMillis() - 30000, tag = "ALERT", message = "Extreme heat detected in Kitchen kitchen_temp_sensor: 31°C"),
        ActivityLogEntity(id = 4, timestamp = System.currentTimeMillis() - 45000, tag = "SYSTEM", message = "MQTT bridging established.")
    )

    MyApplicationTheme {
        DashboardScreenContent(
            rooms = mockRooms,
            devices = mockDevices,
            logs = mockLogs,
            selectedRoomId = null,
            onSelectRoom = {},
            onToggleDevice = {},
            onAdjustDeviceValue = { _, _ -> },
            onToggleDoorLock = {},
            onSimulateSensor = {}
        )
    }
}

@Preview(name = "Dashboard Screen - Living Room Selected", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DashboardLivingRoomSelectedPreview() {
    val mockRooms = listOf(
        RoomEntity(id = 1, name = "Living Room", iconName = "living_room"),
        RoomEntity(id = 2, name = "Master Bedroom", iconName = "bedroom"),
        RoomEntity(id = 3, name = "Kitchen Area", iconName = "kitchen")
    )

    val mockDevices = listOf(
        DeviceEntity(id = 101, name = "Main Chandelier", type = "light", roomId = 1, status = true, value = 80),
        DeviceEntity(id = 102, name = "HVAC Climate Control", type = "ac", roomId = 1, status = true, value = 22),
        DeviceEntity(id = 103, name = "Security Front Door", type = "lock", roomId = 1, status = false, isLocked = true),
        DeviceEntity(id = 104, name = "Exhaust Fan Node", type = "fan", roomId = 3, status = false, value = 0),
        DeviceEntity(id = 105, name = "Coffee Maker Socket", type = "plug", roomId = 3, status = true, value = 0),
        DeviceEntity(id = 106, name = "Environment Laser Sensor", type = "sensor", roomId = 2, status = true, value = 42)
    )

    val mockLogs = listOf(
        ActivityLogEntity(id = 1, timestamp = System.currentTimeMillis() - 5000, tag = "INFO", message = "System orchestrator node active."),
        ActivityLogEntity(id = 2, timestamp = System.currentTimeMillis() - 15000, tag = "SECURITY", message = "Front lock securely locked.")
    )

    MyApplicationTheme {
        DashboardScreenContent(
            rooms = mockRooms,
            devices = mockDevices,
            logs = mockLogs,
            selectedRoomId = 1,
            onSelectRoom = {},
            onToggleDevice = {},
            onAdjustDeviceValue = { _, _ -> },
            onToggleDoorLock = {},
            onSimulateSensor = {}
        )
    }
}
