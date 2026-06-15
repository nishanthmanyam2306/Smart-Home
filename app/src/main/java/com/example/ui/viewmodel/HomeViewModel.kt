package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.repository.HomeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepository(application)

    // User authentication flows
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    // Core home data flows
    val rooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automations: StateFlow<List<AutomationEntity>> = repository.allAutomations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleEntity>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<List<SettingEntity>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen navigation
    // "login", "dashboard", "rooms", "device_control", "automations", "logs", "settings"
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Selected room filter (null means show all devices on dashboard)
    private val _selectedRoomId = MutableStateFlow<Int?>(null)
    val selectedRoomId: StateFlow<Int?> = _selectedRoomId.asStateFlow()

    init {
        // Hydrate current logged user on startup
        viewModelScope.launch {
            val user = repository.getActiveUser()
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = "dashboard"
            }
        }
    }

    // AUTH ACTIONS
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "Email and Password cannot be empty."
            return
        }

        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null

            // Try remote API first, then fall back to local DB sandbox mode
            val user = repository.loginRemote(email, password)
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = "dashboard"
            } else {
                // local fallback credentials
                val existing = repository.getActiveUser()
                if (existing != null && existing.email == email) {
                    _currentUser.value = existing
                    _currentScreen.value = "dashboard"
                } else {
                    // Create simulated local account
                    val newUser = repository.localLogin(email, "jwt_mock_token_key", "admin")
                    _currentUser.value = newUser
                    _currentScreen.value = "dashboard"
                }
            }
            _isAuthenticating.value = false
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "Email and Password cannot be empty."
            return
        }

        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null

            val success = repository.registerRemote(email, password)
            if (success) {
                // Auto login upon registration
                val newUser = repository.localLogin(email, "jwt_mock_token_key", "user")
                _currentUser.value = newUser
                _currentScreen.value = "dashboard"
            } else {
                // Local simulation registration style
                val newUser = repository.localLogin(email, "jwt_mock_token_key", "user")
                _currentUser.value = newUser
                _currentScreen.value = "dashboard"
            }
            _isAuthenticating.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            _currentScreen.value = "login"
        }
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun selectRoom(roomId: Int?) {
        _selectedRoomId.value = roomId
    }

    // ROOM MANAGEMENT
    fun addRoom(name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createRoom(name, icon)
        }
    }

    fun deleteRoom(id: Int) {
        viewModelScope.launch {
            repository.deleteRoom(id)
            if (_selectedRoomId.value == id) {
                _selectedRoomId.value = null
            }
        }
    }

    // DEVICE CONTROL & MANAGEMENT
    fun addDevice(name: String, type: String, roomId: Int, initialValue: Int = 0) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addDevice(name, type, roomId, initialValue)
        }
    }

    fun toggleDevice(device: DeviceEntity) {
        viewModelScope.launch {
            val updated = device.copy(status = !device.status)
            repository.updateDeviceState(updated)
        }
    }

    fun adjustDeviceValue(device: DeviceEntity, newValue: Int) {
        viewModelScope.launch {
            val updated = device.copy(value = newValue)
            repository.updateDeviceState(updated)
        }
    }

    fun toggleDoorLock(device: DeviceEntity) {
        viewModelScope.launch {
            repository.setLockState(device, !device.isLocked)
        }
    }

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch {
            repository.deleteDevice(device)
        }
    }

    // SCHEDULING
    fun createSchedule(name: String, deviceId: Int, time: String, days: String, action: String) {
        if (name.isBlank() || time.isBlank()) return
        viewModelScope.launch {
            repository.addSchedule(name, deviceId, time, days, action)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun toggleSchedule(schedule: ScheduleEntity, active: Boolean) {
        viewModelScope.launch {
            repository.toggleScheduleActive(schedule, active)
        }
    }

    // AUTOMATION
    fun createAutomation(
        name: String,
        sourceType: String,
        sourceName: String,
        condition: String,
        targetDeviceId: Int,
        command: String
    ) {
        if (name.isBlank() || sourceName.isBlank()) return
        viewModelScope.launch {
            repository.addAutomation(name, sourceType, sourceName, condition, targetDeviceId, command)
        }
    }

    fun deleteAutomation(automation: AutomationEntity) {
        viewModelScope.launch {
            repository.deleteAutomation(automation)
        }
    }

    fun toggleAutomation(automation: AutomationEntity, active: Boolean) {
        viewModelScope.launch {
            repository.toggleAutomationActive(automation, active)
        }
    }

    // SETTINGS
    fun saveConfigValue(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // SIMULATED SENSOR METRICS INCREMENT ROUTINE
    fun simulateSensorTrigger(sensor: DeviceEntity, newValue: Int) {
        viewModelScope.launch {
            val updated = sensor.copy(value = newValue, status = newValue > 80) // ON / Alert status if PPM smokes > 80
            repository.updateDeviceState(updated)
        }
    }
}
