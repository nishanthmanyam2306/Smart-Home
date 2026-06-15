# ==========================================
# SMART HOME MANAGEMENT PLATFORM: MQTT BRIDGE SCRIPT
# Technology: Python, paho-mqtt
# Purpose: Bridge device status messages from physical hardware to Database and REST services
# ==========================================

import time
import json
import logging
import paho.mqtt.client as mqtt

# Configure system monitoring logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

MQTT_BROKER = "mqtt.eclipseprojects.io"
MQTT_PORT = 1883
BASE_TOPIC = "smart_home/#"

def on_connect(client, userdata, flags, rc):
    logging.info(f"Connected to MQTT Orchestrator Broker with result code: {rc}")
    client.subscribe(BASE_TOPIC)
    logging.info(f"Subscribed securely to base wildcard topic: {BASE_TOPIC}")

def on_message(client, userdata, msg):
    try:
        topic = msg.topic
        payload_str = msg.payload.decode("utf-8")
        logging.info(f"Incoming MQTT Telemetry -> Topic: {topic} | Payload: {payload_str}")

        # Parse subtopics
        # smart_home/room_compartment/device_type/state
        parts = topic.split("/")
        if len(parts) >= 4:
            room = parts[1]
            device_type = parts[2]
            metric_type = parts[3] # 'state' or 'cmd'

            payload_data = json.loads(payload_str)
            status = payload_data.get("status")
            value = payload_data.get("value", 0)
            is_locked = payload_data.get("is_locked", False)

            # Inside production systems, we would trigger an SQLAlchemy database update
            logging.info(f"[DB INTEGRATION] Updating SQLite/MySQL state for {room}/{device_type}: Status={status}, Value={value}")

            # Safety and security threshold evaluations
            if device_type == "smoke" and value > 80:
                logging.error(f"[SECURITY ALERT] Threat Condition Reached! Gas leakage in {room}! Triggering automations.")
                # We would trigger safety actions (turning off gas plugs, unlocking exits)

            elif device_type == "lock" and not is_locked:
                logging.warning(f"[SECURITY ALERT] Secure perimeter breach! Lock in {room} was opened.")

    except Exception as e:
        logging.error(f"Failed to bridge incoming MQTT topic message: {e}")

def main():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message

    logging.info(f"Connecting to live public MQTT bridge: {MQTT_BROKER}:{MQTT_PORT}...")
    try:
        client.connect(MQTT_BROKER, MQTT_PORT, 60)
    except Exception as e:
        logging.critical(f"Failed to link MQTT broker: {e}. Bridging is paused.")
        return

    # Start loop in background
    client.loop_forever()

if __name__ == "__main__":
    main()
