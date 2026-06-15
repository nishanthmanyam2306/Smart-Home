package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.entities.*
import com.example.data.local.dao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        RoomEntity::class,
        DeviceEntity::class,
        AutomationEntity::class,
        ScheduleEntity::class,
        ActivityLogEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun deviceDao(): DeviceDao
    abstract fun automationDao(): AutomationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smarthome_gold_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate default data on DB creation
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val roomDao = database.roomDao()
                        val deviceDao = database.deviceDao()
                        val logDao = database.activityLogDao()
                        val settingDao = database.settingDao()
                        val scheduleDao = database.scheduleDao()
                        val automationDao = database.automationDao()

                        // Insert Default Settings
                        settingDao.insertSetting(SettingEntity("backend_url", "http://192.168.1.100:8000"))
                        settingDao.insertSetting(SettingEntity("mqtt_broker", "mqtt.eclipseprojects.io"))
                        settingDao.insertSetting(SettingEntity("system_alarm", "false"))
                        settingDao.insertSetting(SettingEntity("mock_mqtt_mode", "true"))

                        // Insert Default Rooms
                        val livingRoomId = roomDao.insertRoom(RoomEntity(name = "Living Room", iconName = "living_room")).toInt()
                        val masterBedroomId = roomDao.insertRoom(RoomEntity(name = "Master Bedroom", iconName = "bedroom")).toInt()
                        val kitchenId = roomDao.insertRoom(RoomEntity(name = "Kitchen", iconName = "kitchen")).toInt()

                        // Insert Default Devices for Living Room
                        val light1Id = deviceDao.insertDevice(DeviceEntity(
                            name = "Ceiling Chandelier",
                            type = "light",
                            roomId = livingRoomId,
                            status = true,
                            value = 80,
                            mqttPublishTopic = "smart_home/living_room/light/state",
                            mqttSubscribeTopic = "smart_home/living_room/light/cmd"
                        )).toInt()

                        val lockId = deviceDao.insertDevice(DeviceEntity(
                            name = "Main Gate Lock",
                            type = "lock",
                            roomId = livingRoomId,
                            status = false,
                            isLocked = true,
                            mqttPublishTopic = "smart_home/living_room/lock/state",
                            mqttSubscribeTopic = "smart_home/living_room/lock/cmd"
                        )).toInt()

                        val acId = deviceDao.insertDevice(DeviceEntity(
                            name = "Inverter Air Conditioner",
                            type = "ac",
                            roomId = livingRoomId,
                            status = false,
                            value = 24, // temp
                            mqttPublishTopic = "smart_home/living_room/ac/state",
                            mqttSubscribeTopic = "smart_home/living_room/ac/cmd"
                        )).toInt()

                        // Insert Default Devices for Bedroom
                        val fanId = deviceDao.insertDevice(DeviceEntity(
                            name = "Super Fan",
                            type = "fan",
                            roomId = masterBedroomId,
                            status = true,
                            value = 3, // speed
                            mqttPublishTopic = "smart_home/master_bedroom/fan/state",
                            mqttSubscribeTopic = "smart_home/master_bedroom/fan/cmd"
                        )).toInt()

                        val BedLightId = deviceDao.insertDevice(DeviceEntity(
                            name = "Comfy Table Light",
                            type = "light",
                            roomId = masterBedroomId,
                            status = false,
                            value = 50,
                            mqttPublishTopic = "smart_home/master_bedroom/light/state",
                            mqttSubscribeTopic = "smart_home/master_bedroom/light/cmd"
                        )).toInt()

                        // Insert Default Devices for Kitchen
                        val smartPlugId = deviceDao.insertDevice(DeviceEntity(
                            name = "Refrigerator Plug",
                            type = "plug",
                            roomId = kitchenId,
                            status = true,
                            mqttPublishTopic = "smart_home/kitchen/plug/state",
                            mqttSubscribeTopic = "smart_home/kitchen/plug/cmd"
                        )).toInt()

                        val smokeSensorId = deviceDao.insertDevice(DeviceEntity(
                            name = "Gas Leak Sensor",
                            type = "sensor",
                            roomId = kitchenId,
                            status = false,
                            value = 22, // ppm level
                            mqttPublishTopic = "smart_home/kitchen/smoke/state"
                        )).toInt()

                        // Save Initial Schedules
                        scheduleDao.insertSchedule(ScheduleEntity(
                            name = "Auto Lock Gate",
                            deviceId = lockId,
                            timeStr = "22:00",
                            daysOfWeek = "MON,TUE,WED,THU,FRI,SAT,SUN",
                            action = "LOCK",
                            isActive = true
                        ))

                        scheduleDao.insertSchedule(ScheduleEntity(
                            name = "Morning Brighter Lights",
                            deviceId = light1Id,
                            timeStr = "06:30",
                            daysOfWeek = "MON,TUE,WED,THU,FRI",
                            action = "ON",
                            isActive = true
                        ))

                        // Save Initial Automations
                        automationDao.insertAutomation(AutomationEntity(
                            name = "Gas Alarm Auto Off Plug",
                            triggerSourceType = "sensor",
                            triggerDeviceName = "Gas Leak Sensor",
                            triggerCondition = "> 100",
                            actionDeviceId = smartPlugId,
                            actionCommand = "TURN_OFF",
                            isActive = true
                        ))

                        // Insert logs
                        logDao.insertLog(ActivityLogEntity(tag = "SYSTEM", message = "Smart Home application database initialized."))
                        logDao.insertLog(ActivityLogEntity(tag = "SECURITY", message = "Main Gate lock status: LOCKED (Secure)."))
                        logDao.insertLog(ActivityLogEntity(tag = "INFO", message = "Table light default schedules loaded successfully."))
                    }
                }
            }
        }
    }
}
