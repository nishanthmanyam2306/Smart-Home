# SMART HOME ORCHESTRATION PLATFORM: ARCHITECTURE & MANUALS
**Aurora Smart Home Enterprise Specifications Document**

---

## 1. REQUIREMENTS ANALYSIS
The Aurora Smart Home platform is designed as an offline-first, highly scalable, and secure microservice orchestration ecosystem. It bridges physical hardware layers (ESP32 microcontrollers with relay channels and security deadbolts) to local network routers, cloud-hosted MQTT message brokers, and enterprise FastAPI backends.

### Scalability Strategy
*   **Database Constraints**: Partitioning of historic telemetry `activity_logs` by timestamp range indexes.
*   **MQTT Load Distribution**: Client subscriptions are mapped to modular topics (e.g., `smart_home/{room_id}/{device_type}/{id}/state`) allowing clustering of MQTT Brokers.
*   **Offline-First Mobile Architecture**: Jetpack Compose client leverages local SQLite Room Database persistence to maintain absolute operational readiness if WAN endpoints fail, syncing transactions incrementally once network handshakes restore.

---

## 2. SYSTEM ARCHITECTURE
```
 ┌─────────────────────────────────────────────────────────┐
 │               Jetpack Compose Mobile App                │
 └────────────────────────────┬────────────────────────────┘
                              │
               HTTP REST      │      MQTT Publish/Subscribe
             (JSON / JWT)     │     (Binary Telemetry Packet)
                              ▼
┌─────────────────────────────┴─────────────────────────────┐
│                   LAN Network / Internet                  │
└──────┬─────────────────────────────────────────────┬──────┘
       │                                             │
       ▼                                             ▼
┌──────────────┐                              ┌──────────────┐
│  FastAPI     │                              │  Mosquitto   │
│  REST Server │                              │  MQTT Broker │
└──────┬───────┘                              └──────┬───────┘
       │                                             │
       ▼                                             ▼
┌──────────────┐                              ┌──────────────┐
│    MySQL     │                              │    ESP32     │
│  Database    │                              │  Controller  │
└──────────────┘                              └──────────────┘
```

### Flow of Commands
1.  **Direct Control**: User toggles a Light or Unlocks a Security Deadbolt on the mobile Android dashboard.
2.  **State Save & Publish**: The Android App immediately saves the instruction into the SQLite Room Database, appends a log to the historical telemetry audit trail, and issues an asynchronous PUT call to `/api/v1/devices/{id}`.
3.  **FastAPI Routing**: FastAPI updates the status inside the MySQL instance, issues a command payload structure via an internal MQTT bridge client onto the appropriate MQTT topic (e.g. `smart_home/living_room/lock/cmd`).
4.  **Hardware Activation**: The ESP32 is subscribed to `smart_home/living_room/lock/cmd`. Upon message arrival, it reads the JSON payload, checks authenticity, switches the GPIO pin digital signal to HIGH, opening the physical solenoid lock, and replies with a status update on the publishing topic.

---

## 3. DATABASE ARCHITECTURE (MySQL + Room)
Primary database entities include:
*   `users`: Security credential references with strict unique indexes on email and linked Role IDs.
*   `roles` / `permissions`: Standard Role-Based Access Control (RBAC) ensuring guest residents cannot execute override actions like unlocking doors or changing system broker settings.
*   `rooms`: Logical physical compartments (Living room, master bedroom, kitchen, terrace).
*   `devices`: Connected smart nodes with statuses, dimming/dimmer levels, connection metrics, and MQTT channel declarations.
*   `automations` / `schedules`: Edge-orchestrated timers and sensors-condition dependencies (e.g. If Smoke PPM telemetry is > 100, turn off Gas Plug relay and unlock front deadbolt).

---

## 4. API DOCUMENTATION
### 1. Authentication
*   **Endpoint**: `POST /api/v1/auth/login`
*   **Format**: `application/x-www-form-urlencoded`
*   **Form Parameters**: `username` (email) & `password`
*   **Response Header**: Bearer JWT Token
*   **JSON Response**:
    ```json
    {
      "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "token_type": "bearer",
      "email": "admin@smarthome.io",
      "role": "admin"
    }
    ```

### 2. Device Controls
*   **Endpoint**: `PUT /api/v1/devices/{id}`
*   **Security Header**: `Authorization: Bearer <access_token>`
*   **JSON Request Body**:
    ```json
    {
      "name": "Front Gate Solenoid",
      "type": "lock",
      "room_id": 1,
      "status": false,
      "value": 0,
      "is_locked": true,
      "mqtt_publish_topic": "smart_home/living_room/lock/state",
      "mqtt_subscribe_topic": "smart_home/living_room/lock/cmd"
    }
    ```

---

## 5. HARDWARE INTEGRATION & MQTT TOPICS
### Topic Matrix
*   **Commands (App to Node)**: `smart_home/{room}/{device_type}/cmd` (Drives relay state switches).
*   **Telemetry (Node to App)**: `smart_home/{room}/{device_type}/state` (Reports real-time battery levels, temperatures, or locks).

### Device Handshake Flow
1.  **Booting**: ESP32 boots up, joins SSID Wifi.
2.  **Handshake**: Connects to the Eclipse Mosquitto broker, registers a unique client identifier, publishes an online package structure `{"connection_status": "online"}` onto its state channel.
3.  **Active Routine**: Constantly checks physical sensor loops (like gas leak, thermostat pins). If anomalies exceed safety thresholds, it sends a payload to the broker instantly, bypassing polling delays.

---

## 6. DEPLOYMENT GUIDE (Docker Ecosystem Compose)
To launch the entire backend, DB schema, and Mosquitto broker:
1.  Verify Docker and Docker Compose are installed and running.
2.  Navigate to the `/backend_hardware` sub-directory.
3.  Deploy services in background daemon mode:
    ```bash
    docker-compose up -d
    ```
4.  Verify that containers have booted successfully:
    ```bash
    docker ps
    ```
5.  FastAPI Swagger Docs will be instantly interactable on:
    `http://localhost:8000/docs`
