-- ==========================================
-- SMART HOME MANAGEMENT PLATFORM: DATABASE SCHEMA
-- Target Engine: MySQL 8.0+
-- Author: Elite Architecture and Security team
-- ==========================================

CREATE DATABASE IF NOT EXISTS smarthome_db;
USE smarthome_db;

-- ------------------------------------------
-- 1. Roles and Permissions (RBAC)
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codename VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------
-- 2. User Credentials and Profiles
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_email (email),
    FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------
-- 3. Room Compartments Hierarchy
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    icon_name VARCHAR(64) NOT NULL DEFAULT 'living_room',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------
-- 4. Device Categories and Nodes
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS device_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE, -- 'light', 'fan', 'ac', 'lock', 'plug', 'sensor'
    description VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS devices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type_id INT NOT NULL,
    room_id INT NOT NULL,
    status BOOLEAN DEFAULT FALSE, -- ON/OFF
    value INT DEFAULT 0, -- dimmer/temperature level
    connection_status VARCHAR(16) DEFAULT 'online', -- 'online', 'offline'
    is_locked BOOLEAN DEFAULT FALSE, -- specific to Deadbolt Locks
    mqtt_publish_topic VARCHAR(255) NULL,
    mqtt_subscribe_topic VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_device_room (room_id),
    FOREIGN KEY (type_id) REFERENCES device_types(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------
-- 5. Timers and Automation Engine Routines
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    device_id INT NOT NULL,
    time_str VARCHAR(8) NOT NULL, -- "HH:MM" format
    days_of_week VARCHAR(128) NOT NULL, -- Comma separated e.g., "MON,WED,FRI"
    action VARCHAR(16) NOT NULL, -- "ON", "OFF", "LOCK", "UNLOCK"
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS automations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    trigger_source_type VARCHAR(32) NOT NULL, -- "sensor", "time", "manual"
    trigger_device_name VARCHAR(128) NOT NULL, -- e.g., "living_room_temp_sensor"
    trigger_condition VARCHAR(32) NOT NULL, -- "> 28", "< 18"
    action_device_id INT NOT NULL,
    action_command VARCHAR(32) NOT NULL, -- "TURN_ON", "TURN_OFF", "LOCK"
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (action_device_id) REFERENCES devices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------
-- 6. Audit Trails, Logs & Alerts
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tag VARCHAR(32) NOT NULL, -- 'SYSTEM', 'SECURITY', 'ALERT', 'INFO'
    message VARCHAR(512) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_log_tag_time (tag, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------
-- Seed Initial Constraints Metadata
-- ------------------------------------------
INSERT IGNORE INTO roles (id, name, description) VALUES 
(1, 'admin', 'Root terminal administrator with full read-write orchestration privileges'),
(2, 'user', 'Resident user with standard room controlling widgets permissions');

INSERT IGNORE INTO device_types (id, name, description) VALUES
(1, 'light', 'Smart luminance bulb dimmer'),
(2, 'fan', 'High torque ceiling smart fan controller'),
(3, 'ac', 'Inverter air conditioning temperature module'),
(4, 'lock', 'High security electronic door lock'),
(5, 'plug', 'Wall mounting power relay switch'),
(6, 'sensor', 'Digital environmental telemetry sensors');
