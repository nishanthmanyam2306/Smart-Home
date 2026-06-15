/*
==========================================================
SMART HOME MANAGEMENT PLATFORM: ESP32 RELEADS / DIGITAL LOCK MODULE
Target hardware: ESP32 DevKit V1, Multi-relay board, Solenoid Lock
Purpose: Secure WiFi MQTT client controller to drive physical hardware
==========================================================
*/

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// WiFi Configuration parameters
const char* ssid = "Aurora_Home_WiFi";
const char* password = "aurora_cyber_key";

// MQTT broker parameters
const char* mqtt_broker = "mqtt.eclipseprojects.io";
const int mqtt_port = 1883;

// GPIO Relay Allocations
#define RELAY_LIGHT_PIN  18  // Drives ceiling light bulb relay
#define RELAY_LOCK_PIN   19  // Solenoid Lock command line (LOW = Safe Lock, HIGH = Open Solenoid)

WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Device MQTT Topics
const char* status_topic = "smart_home/living_room/lock/state";
const char* command_topic = "smart_home/living_room/lock/cmd";

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to local WiFi Access Point: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("Network connected successfully!");
  Serial.print("Assigned IP Address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Incoming MQTT Message arrived [");
  Serial.print(topic);
  Serial.print("] ");

  // Parse JSON payloads using ArduinoJson
  StaticJsonDocument<256> doc;
  DeserializationError error = deserializeJson(doc, payload, length);

  if (error) {
    Serial.print("JSON deserialization failure: ");
    Serial.println(error.c_str());
    return;
  }

  // Handle Lock Commands
  if (strcmp(topic, command_topic) == 0) {
    bool status = doc["status"]; // True if unlocked
    bool is_locked = doc["is_locked"]; // True if locked

    if (is_locked) {
      digitalWrite(RELAY_LOCK_PIN, LOW); // Solenoid inactive (Locked)
      Serial.println("[SECURITY DEADBOLT] Locked safely.");
    } else {
      digitalWrite(RELAY_LOCK_PIN, HIGH); // Pull solenoid latch (Unlocked)
      Serial.println("[SECURITY DEADBOLT] Unlocked - Solenoid Active.");
    }

    // Publish state confirmation telemetry out
    publish_telemetry(!is_locked, is_locked);
  }
}

void publish_telemetry(bool status, bool is_locked) {
  StaticJsonDocument<256> doc;
  doc["status"] = status;
  doc["is_locked"] = is_locked;
  doc["uptime_ms"] = millis();

  char buffer[256];
  serializeJson(doc, buffer);
  
  mqttClient.publish(status_topic, buffer);
  Serial.print("Published telemetry: ");
  Serial.println(buffer);
}

void reconnect_mqtt() {
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT terminal connection...");
    // Create custom unique client ID
    String clientId = "ESP32_Orchestration_Client_" + String(random(0xffff), HEX);
    
    if (mqttClient.connect(clientId.c_str())) {
      Serial.println("CONNECTED!");
      // Re-subscribe to incoming commands
      mqttClient.subscribe(command_topic);
    } else {
      Serial.print("Connection failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" | Retrying in 5 seconds.");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  
  // Output Pins Initialization
  pinMode(RELAY_LIGHT_PIN, OUTPUT);
  pinMode(RELAY_LOCK_PIN, OUTPUT);
  
  // High state keeps relays initially closed (Safe posture)
  digitalWrite(RELAY_LIGHT_PIN, LOW);
  digitalWrite(RELAY_LOCK_PIN, LOW);

  setup_wifi();
  
  mqttClient.setServer(mqtt_broker, mqtt_port);
  mqttClient.setCallback(callback);
}

void loop() {
  if (!mqttClient.connected()) {
    reconnect_mqtt();
  }
  mqttClient.loop();
  
  // Keep alive timer (publish status telemetry every 10 seconds)
  static unsigned long last_telemetry_time = 0;
  if (millis() - last_telemetry_time > 10000) {
    last_telemetry_time = millis();
    // Read current physical pins
    bool currentLockState = digitalRead(RELAY_LOCK_PIN) == HIGH;
    publish_telemetry(currentLockState, !currentLockState);
  }
}
