package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.database.AppDatabase
import com.example.data.local.entities.*
import com.example.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

class HomeRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val roomDao = database.roomDao()
    private val deviceDao = database.deviceDao()
    private val automationDao = database.automationDao()
    private val scheduleDao = database.scheduleDao()
    private val activityLogDao = database.activityLogDao()
    private val settingDao = database.settingDao()

    // Read flows for reactive dynamic screen state!
    val allRooms: Flow<List<RoomEntity>> = roomDao.getAllRooms()
    val allDevices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()
    val allAutomations: Flow<List<AutomationEntity>> = automationDao.getAllAutomations()
    val allSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    val allLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()
    val allSettings: Flow<List<SettingEntity>> = settingDao.getAllSettingsFlow()

    private var cachedApiService: ApiService? = null
    private var cachedBaseUrl: String = ""

    private suspend fun getApiService(): ApiService? {
        val urlSetting = settingDao.getSetting("backend_url")
        val baseUrl = urlSetting?.valueStr ?: "http://192.168.1.100:8000"
        
        if (cachedApiService != null && cachedBaseUrl == baseUrl) {
            return cachedApiService
        }

        try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            cachedBaseUrl = baseUrl
            cachedApiService = retrofit.create(ApiService::class.java)
            return cachedApiService
        } catch (e: Exception) {
            Log.e("HomeRepository", "Failed to build Retrofit with base url: $baseUrl", e)
            return null
        }
    }

    // AUTH ACTIONS
    suspend fun localLogin(email: String, token: String, role: String): UserEntity {
        val user = UserEntity(email = email, token = token, role = role)
        userDao.clearUsers()
        userDao.insertUser(user)
        addLog("SYSTEM", "User $email logged in successfully.")
        return user
    }

    suspend fun getActiveUser(): UserEntity? {
        return userDao.getCurrentUser()
    }

    suspend fun logout() {
        val currentUser = userDao.getCurrentUser()
        if (currentUser != null) {
            addLog("SYSTEM", "User ${currentUser.email} logged out.")
        }
        userDao.clearUsers()
    }

    suspend fun registerRemote(email: String, password: String): Boolean {
        return try {
            val api = getApiService() ?: return false
            val response = api.register(RegisterRequest(email, password))
            response.is_active
        } catch (e: Exception) {
            Log.e("HomeRepository", "Registration remote endpoint failed, using local registration fallback", e)
            false
        }
    }

    suspend fun loginRemote(email: String, password: String): UserEntity? {
        return try {
            val api = getApiService() ?: return null
            val response = api.login(email, password)
            localLogin(response.email, response.access_token, response.role)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Login remote endpoint failed, falling back to local credentials", e)
            null
        }
    }

    // ROOM MANAGEMENT
    suspend fun createRoom(name: String, icon: String): Int {
        val room = RoomEntity(name = name, iconName = icon)
        val newId = roomDao.insertRoom(room).toInt()
        
        // Remote Sync (Optional/Fire & Forget)
        val activeUser = getActiveUser()
        if (activeUser != null) {
            try {
                val api = getApiService()
                api?.createRoom("Bearer ${activeUser.token}", RoomNetworkModel(id = newId, name = name, icon_name = icon))
            } catch (e: Exception) {
                Log.d("HomeRepository", "Room sync offline: ${e.message}")
            }
        }
        addLog("SYSTEM", "Room '$name' created successfully.")
        return newId
    }

    suspend fun deleteRoom(id: Int) {
        val activeUser = getActiveUser()
        if (activeUser != null) {
            try {
                val api = getApiService()
                api?.deleteRoom("Bearer ${activeUser.token}", id)
            } catch (e: Exception) {
                Log.d("HomeRepository", "Room delete sync offline: ${e.message}")
            }
        }
        roomDao.deleteRoomById(id)
        deviceDao.deleteDevicesByRoom(id) // Cascade delete devices
        addLog("SYSTEM", "Room deleted (ID: $id) with all contained devices.")
    }

    // DEVICE MANAGEMENT & CONTROL
    suspend fun addDevice(name: String, type: String, roomId: Int, initialValue: Int = 0): Int {
        val topicPrefix = "smart_home/${name.lowercase().replace(" ", "_")}"
        val device = DeviceEntity(
            name = name,
            type = type,
            roomId = roomId,
            status = false,
            value = initialValue,
            isLocked = type == "lock",
            mqttPublishTopic = "$topicPrefix/state",
            mqttSubscribeTopic = "$topicPrefix/cmd"
        )
        val newId = deviceDao.insertDevice(device).toInt()

        val activeUser = getActiveUser()
        if (activeUser != null) {
            try {
                val api = getApiService()
                api?.createDevice("Bearer ${activeUser.token}", DeviceNetworkModel(
                    id = newId,
                    name = name,
                    type = type,
                    room_id = roomId,
                    status = false,
                    value = initialValue,
                    is_locked = type == "lock",
                    mqtt_publish_topic = "$topicPrefix/state",
                    mqtt_subscribe_topic = "$topicPrefix/cmd"
                ))
            } catch (e: Exception) {
                Log.d("HomeRepository", "Device sync offline: ${e.message}")
            }
        }
        addLog("DEVICE", "Added new device: $name (Type: ${type.uppercase()}).")
        return newId
    }

    suspend fun updateDeviceState(device: DeviceEntity) {
        deviceDao.updateDevice(device)
        
        val activeUser = getActiveUser()
        if (activeUser != null) {
            try {
                val api = getApiService()
                api?.updateDevice("Bearer ${activeUser.token}", device.id, DeviceNetworkModel(
                    id = device.id,
                    name = device.name,
                    type = device.type,
                    room_id = device.roomId,
                    status = device.status,
                    value = device.value,
                    connection_status = device.connectionStatus,
                    is_locked = device.isLocked,
                    mqtt_publish_topic = device.mqttPublishTopic,
                    mqtt_subscribe_topic = device.mqttSubscribeTopic
                ))
            } catch (e: Exception) {
                Log.d("HomeRepository", "Device update state sync offline: ${e.message}")
            }
        }
        
        // MQTT Publish Simulation
        addLog("DEVICE", "State changed - ${device.name}: ${if (device.status) "ON" else "OFF"}${if (device.type == "ac") " (${device.value}°C)" else if (device.type == "fan") " (Speed ${device.value})" else if (device.type == "light") " (${device.value}%)" else ""}")
        
        // Look for matching automation rules
        evaluateAutomations(device)
    }

    suspend fun deleteDevice(device: DeviceEntity) {
        val activeUser = getActiveUser()
        if (activeUser != null) {
            try {
                val api = getApiService()
                api?.deleteDevice("Bearer ${activeUser.token}", device.id)
            } catch (e: Exception) {
                Log.d("HomeRepository", "Device delete sync offline: ${e.message}")
            }
        }
        deviceDao.deleteDevice(device)
        addLog("DEVICE", "Device '${device.name}' removed.")
    }

    // LOCKS
    suspend fun setLockState(device: DeviceEntity, lock: Boolean) {
        if (device.type == "lock") {
            device.isLocked = lock
            device.status = !lock // status is true when unlocked, false when locked
            updateDeviceState(device)
            addLog("SECURITY", "Door '${device.name}' is now ${if (lock) "LOCKED 🔒 (Secure)" else "UNLOCKED 🔓 (Attention)"}")
        }
    }

    // AUTOMATION MANAGEMENT
    suspend fun addAutomation(
        name: String,
        sourceType: String,
        sourceName: String,
        condition: String,
        targetDeviceId: Int,
        command: String
    ): Int {
        val automation = AutomationEntity(
            name = name,
            triggerSourceType = sourceType,
            triggerDeviceName = sourceName,
            triggerCondition = condition,
            actionDeviceId = targetDeviceId,
            actionCommand = command,
            isActive = true
        )
        val id = automationDao.insertAutomation(automation).toInt()
        addLog("SYSTEM", "New Automation Rule Saved: '$name'")
        return id
    }

    suspend fun deleteAutomation(automation: AutomationEntity) {
        automationDao.deleteAutomation(automation)
        addLog("SYSTEM", "Automation rule '${automation.name}' removed.")
    }

    suspend fun toggleAutomationActive(automation: AutomationEntity, active: Boolean) {
        val updated = automation.copy(isActive = active)
        automationDao.updateAutomation(updated)
        addLog("SYSTEM", "Automation '${automation.name}' is now ${if (active) "ENABLED" else "DISABLED"}")
    }

    // SCHEDULES
    suspend fun addSchedule(name: String, deviceId: Int, time: String, days: String, action: String): Int {
        val schedule = ScheduleEntity(
            name = name,
            deviceId = deviceId,
            timeStr = time,
            daysOfWeek = days,
            action = action,
            isActive = true
        )
        val id = scheduleDao.insertSchedule(schedule).toInt()
        addLog("SYSTEM", "Created Schedule '$name' for Device #$deviceId at $time.")
        return id
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
        addLog("SYSTEM", "Schedule '${schedule.name}' deleted.")
    }

    suspend fun toggleScheduleActive(schedule: ScheduleEntity, active: Boolean) {
        val updated = schedule.copy(isActive = active)
        scheduleDao.updateSchedule(updated)
        addLog("SYSTEM", "Schedule '${schedule.name}' is now ${if (active) "ENABLED" else "DISABLED"}")
    }

    // LOGS
    suspend fun addLog(tag: String, message: String) {
        activityLogDao.insertLog(ActivityLogEntity(tag = tag, message = message))
    }

    suspend fun clearLogs() {
        activityLogDao.clearAllLogs()
    }

    // SETTINGS
    suspend fun saveSetting(key: String, value: String) {
        settingDao.insertSetting(SettingEntity(keyStr = key, valueStr = value))
        addLog("SYSTEM", "Config updated: $key = $value")
    }

    suspend fun getSettingValue(key: String): String? {
        return settingDao.getSetting(key)?.valueStr
    }

    // BACKGROUND AUTOMATION ENGINE SIMULATOR
    private suspend fun evaluateAutomations(triggerDevice: DeviceEntity) {
        val activeAutomations = allAutomations.firstOrNull() ?: emptyList()
        val allDevs = allDevices.firstOrNull() ?: emptyList()

        for (aut in activeAutomations) {
            if (!aut.isActive) continue

            // Sensor Automations (Trigger: Temp Sensor change)
            if (aut.triggerSourceType == "sensor" && triggerDevice.type == "sensor") {
                if (triggerDevice.name == aut.triggerDeviceName) {
                    val condValue = aut.triggerCondition.substring(2).trim().toIntOrNull() ?: continue
                    val isGreater = aut.triggerCondition.startsWith(">")
                    val isLess = aut.triggerCondition.startsWith("<")

                    var isActionTriggered = false
                    if (isGreater && triggerDevice.value > condValue) {
                        isActionTriggered = true
                    } else if (isLess && triggerDevice.value < condValue) {
                        isActionTriggered = true
                    }

                    if (isActionTriggered) {
                        // Find action device
                        val actionDevice = allDevs.find { it.id == aut.actionDeviceId }
                        if (actionDevice != null) {
                            addLog("SECURITY", "Automation trigger matching rule '${aut.name}' fulfilled! Running action command.")
                            
                            when (aut.actionCommand) {
                                "TURN_ON" -> {
                                    if (!actionDevice.status) {
                                        actionDevice.status = true
                                        deviceDao.updateDevice(actionDevice)
                                        addLog("DEVICE", "Auto action executed: Turned ON ${actionDevice.name}")
                                    }
                                }
                                "TURN_OFF" -> {
                                    if (actionDevice.status) {
                                        actionDevice.status = false
                                        deviceDao.updateDevice(actionDevice)
                                        addLog("DEVICE", "Auto action executed: Turned OFF ${actionDevice.name}")
                                    }
                                }
                                "LOCK" -> {
                                    if (!actionDevice.isLocked) {
                                        actionDevice.isLocked = true
                                        actionDevice.status = false
                                        deviceDao.updateDevice(actionDevice)
                                        addLog("SECURITY", "Auto action executed: LOCKED ${actionDevice.name}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
