# BLE protocol

## Goal

Use Bluetooth Low Energy between Samsung S20 and ESP32.

The ESP32 exposes a BLE GATT service.

The Android app connects as BLE client.

## Initial characteristics

Suggested service name:

```text
RobotTripedeService
```

Suggested characteristics:

- `command`: Android writes commands to ESP32.
- `telemetry`: ESP32 notifies Android with telemetry updates.
- `status`: Android reads current status.

UUIDs are not finalized yet.

## Android to ESP32 commands

### Ping

```json
{
  "type": "ping"
}
```

### Set LED

```json
{
  "type": "set_led",
  "color": "blue",
  "mode": "blink"
}
```

### Get status

```json
{
  "type": "get_status"
}
```

### Simulated movement

```json
{
  "type": "simulate_move_forward",
  "speed": "slow",
  "duration_ms": 500
}
```

Movement remains simulated.

## ESP32 to Android telemetry

```json
{
  "stop": false,
  "imu": {
    "tilt": "stable",
    "fall_detected": false,
    "accel": {
      "x": 0.0,
      "y": 0.0,
      "z": 9.8
    }
  },
  "ble": "connected",
  "status": "ready"
}
```
