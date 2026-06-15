package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val token: String,
    val role: String = "user",
    val isActive: Boolean = true
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String
)

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "light", "fan", "ac", "lock", "plug", "tv", "sensor"
    val roomId: Int,
    var status: Boolean, // ON/OFF
    var value: Int = 0,  // e.g., speed, brightness, temperature
    var connectionStatus: String = "online", // "online", "offline"
    var isLocked: Boolean = false, // for doors
    val mqttPublishTopic: String = "",
    val mqttSubscribeTopic: String = ""
)

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerSourceType: String, // "sensor", "time", "manual"
    val triggerDeviceName: String, // e.g. "living_room_temp_sensor"
    val triggerCondition: String,  // "> 28", "< 18", "on_smoke"
    val actionDeviceId: Int,       // Target device to run
    val actionCommand: String,     // "TURN_ON", "TURN_OFF", "SET_TEMP_22", "LOCK"
    val isActive: Boolean = true
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val deviceId: Int,
    val timeStr: String, // "18:30"
    val daysOfWeek: String, // "MON,TUE,WED,THU,FRI,SAT,SUN"
    val action: String, // "ON", "OFF"
    val isActive: Boolean = true
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String, // "INFO", "ALERT", "SECURITY", "SYSTEM"
    val message: String
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val keyStr: String,
    val valueStr: String
)
