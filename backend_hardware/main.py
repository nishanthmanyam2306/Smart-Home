# ==========================================
# SMART HOME MANAGEMENT PLATFORM: FASTAPI BACKEND
# Python FastAPI, SQLAlchemy with MySQL support, Pydantic, OAuth2 JWT
# ==========================================

import os
from datetime import datetime, timedelta
from typing import List, Optional
from fastapi import FastAPI, Depends, HTTPException, status, Form
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr
from jose import JWTError, jwt
from passlib.context import CryptContext

# Secret keys and cryptography configs
SECRET_KEY = os.getenv("JWT_SECRET_KEY", "b3dfa79109033ce54029198f161830219ca7b9696ee6da3dd5b9be6db32ec8fb")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="api/v1/auth/login")

app = FastAPI(
    title="Aurora Smart Home Gateway REST API",
    description="Enterprise-grade IoT management and user orchestration REST APIs.",
    version="1.0.0"
)

# Enable CORS for device and mobile interactions
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ------------------------------------------
# Crypto / Token Utilities
# ------------------------------------------
def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# ------------------------------------------
# Mock Mock-Database tables
# ------------------------------------------
class UserInDB(BaseModel):
    id: int
    email: str
    hashed_password: str
    role: str = "user"
    is_active: bool = True

class RoomInDB(BaseModel):
    id: int
    name: str
    icon_name: str

class DeviceInDB(BaseModel):
    id: int
    name: str
    type: str # 'light', 'fan', 'ac', 'lock', 'plug', 'sensor'
    room_id: int
    status: bool
    value: int = 0
    connection_status: str = "online"
    is_locked: bool = False
    mqtt_publish_topic: str = ""
    mqtt_subscribe_topic: str = ""

# Populate bootstrap mock tables
USERS_DB = [
    UserInDB(id=1, email="admin@smarthome.io", hashed_password=get_password_hash("admin123"), role="admin")
]
ROOMS_DB = []
DEVICES_DB = []

# ------------------------------------------
# Pydantic Schemas
# ------------------------------------------
class UserRegister(BaseModel):
    email: EmailStr
    password: str
    role: str = "user"

class UserOut(BaseModel):
    id: int
    email: EmailStr
    role: str
    is_active: bool

class Token(BaseModel):
    access_token: str
    token_type: str
    email: str
    role: str

class RoomIn(BaseModel):
    name: str
    icon_name: str

class RoomOut(BaseModel):
    id: int
    name: str
    icon_name: str

class DeviceIn(BaseModel):
    name: str
    type: str
    room_id: int
    status: bool
    value: int = 0
    is_locked: bool = False
    mqtt_publish_topic: str = ""
    mqtt_subscribe_topic: str = ""

class DeviceOut(BaseModel):
    id: int
    name: str
    type: str
    room_id: int
    status: bool
    value: int
    connection_status: str
    is_locked: bool
    mqtt_publish_topic: str
    mqtt_subscribe_topic: str

# ------------------------------------------
# OAuth Dependency Verification
# ------------------------------------------
async def get_current_user(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate active credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    user = next((u for u in USERS_DB if u.email == email), None)
    if user is None:
        raise credentials_exception
    return user

# ------------------------------------------
# API Endpoints
# ------------------------------------------

@app.post("/api/v1/auth/register", response_model=UserOut)
def register(user: UserRegister):
    if any(u.email == user.email for u in USERS_DB):
        raise HTTPException(status_code=400, detail="Terminal operator already enrolled.")
    
    new_user = UserInDB(
        id=len(USERS_DB) + 1,
        email=user.email,
        hashed_password=get_password_hash(user.password),
        role=user.role,
        is_active=True
    )
    USERS_DB.append(new_user)
    return UserOut(id=new_user.id, email=new_user.email, role=new_user.role, is_active=new_user.is_active)

@app.post("/api/v1/auth/login")
def login(username: str = Form(...), password: str = Form(...)):
    user = next((u for u in USERS_DB if u.email == username), None)
    if not user or not verify_password(password, user.hashed_password):
        raise HTTPException(status_code=400, detail="Incorrect email username or secure token password.")
    
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": user.email, "role": user.role}, expires_delta=access_token_expires
    )
    return {
        "access_token": access_token,
        "token_type": "bearer",
        "email": user.email,
        "role": user.role
    }

# --- ROOM COMPARTMENTS ---
@app.get("/api/v1/rooms", response_model=List[RoomOut])
def get_rooms(current_user: UserInDB = Depends(get_current_user)):
    return ROOMS_DB

@app.post("/api/v1/rooms", response_model=RoomOut)
def create_room(room: RoomIn, current_user: UserInDB = Depends(get_current_user)):
    new_id = len(ROOMS_DB) + 1
    new_room = RoomInDB(id=new_id, name=room.name, icon_name=room.icon_name)
    ROOMS_DB.append(new_room)
    return new_room

@app.delete("/api/v1/rooms/{id}")
def delete_room(id: int, current_user: UserInDB = Depends(get_current_user)):
    global ROOMS_DB, DEVICES_DB
    ROOMS_DB = [r for r in ROOMS_DB if r.id != id]
    DEVICES_DB = [d for d in DEVICES_DB if d.room_id != id] # Cascade delete devices
    return {"status": "ok", "message": "Room compartment and linked devices deleted."}

# --- DEVICES ---
@app.get("/api/v1/devices", response_model=List[DeviceOut])
def get_devices(current_user: UserInDB = Depends(get_current_user)):
    return DEVICES_DB

@app.post("/api/v1/devices", response_model=DeviceOut)
def create_device(device: DeviceIn, current_user: UserInDB = Depends(get_current_user)):
    new_id = len(DEVICES_DB) + 1
    new_device = DeviceInDB(
        id=new_id,
        name=device.name,
        type=device.type,
        room_id=device.room_id,
        status=device.status,
        value=device.value,
        connection_status="online",
        is_locked=device.is_locked,
        mqtt_publish_topic=device.mqtt_publish_topic,
        mqtt_subscribe_topic=device.mqtt_subscribe_topic
    )
    DEVICES_DB.append(new_device)
    return new_device

@app.put("/api/v1/devices/{id}", response_model=DeviceOut)
def update_device(id: int, updated: DeviceIn, current_user: UserInDB = Depends(get_current_user)):
    device = next((d for d in DEVICES_DB if d.id == id), None)
    if not device:
        raise HTTPException(status_code=404, detail="Requested device hardware node not found.")
    
    device.status = updated.status
    device.value = updated.value
    device.is_locked = updated.is_locked
    
    # Simulate MQTT trigger out (would be done by python-mqtt client)
    print(f"[MQTT CMD] Publish to {device.mqtt_subscribe_topic} -> Status: {device.status}, Val: {device.value}")
    
    return device

@app.delete("/api/v1/devices/{id}")
def delete_device(id: int, current_user: UserInDB = Depends(get_current_user)):
    global DEVICES_DB
    DEVICES_DB = [d for d in DEVICES_DB if d.id != id]
    return {"status": "ok", "message": "Module deleted from system partition."}
