package com.example.data.network

import retrofit2.http.*
import okhttp3.ResponseBody

// FastAPI request/response models
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "user"
)

data class RegisterResponse(
    val id: Int,
    val email: String,
    val role: String,
    val is_active: Boolean,
    val msg: String = ""
)

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val email: String,
    val role: String
)

data class RoomNetworkModel(
    val id: Int? = null,
    val name: String,
    val icon_name: String
)

data class DeviceNetworkModel(
    val id: Int? = null,
    val name: String,
    val type: String,
    val room_id: Int,
    val status: Boolean,
    val value: Int = 0,
    val connection_status: String = "online",
    val is_locked: Boolean = false,
    val mqtt_publish_topic: String = "",
    val mqtt_subscribe_topic: String = ""
)

data class SyncResponse(
    val status: String,
    val message: String
)

interface ApiService {

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @FormUrlEncoded
    @POST("/api/v1/auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("/api/v1/rooms")
    suspend fun getRooms(@Header("Authorization") token: String): List<RoomNetworkModel>

    @POST("/api/v1/rooms")
    suspend fun createRoom(
        @Header("Authorization") token: String,
        @Body room: RoomNetworkModel
    ): RoomNetworkModel

    @DELETE("/api/v1/rooms/{id}")
    suspend fun deleteRoom(
        @Header("Authorization") token: String,
        @Path("id") roomId: Int
    ): ResponseBody

    @GET("/api/v1/devices")
    suspend fun getDevices(@Header("Authorization") token: String): List<DeviceNetworkModel>

    @POST("/api/v1/devices")
    suspend fun createDevice(
        @Header("Authorization") token: String,
        @Body device: DeviceNetworkModel
    ): DeviceNetworkModel

    @PUT("/api/v1/devices/{id}")
    suspend fun updateDevice(
        @Header("Authorization") token: String,
        @Path("id") deviceId: Int,
        @Body device: DeviceNetworkModel
    ): DeviceNetworkModel

    @DELETE("/api/v1/devices/{id}")
    suspend fun deleteDevice(
        @Header("Authorization") token: String,
        @Path("id") deviceId: Int
    ): ResponseBody
}
