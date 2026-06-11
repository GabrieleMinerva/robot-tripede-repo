# Technical analysis

## Main architecture

```text
Samsung Galaxy S20
  - Android app
  - UI dashboard
  - Voice input/output
  - Camera access only when explicitly needed
  - OpenAI API wrapper
  - Privacy filter
  - BLE client

Bluetooth Low Energy

ESP32 DevKit
  - BLE GATT server
  - IMU reader
  - STOP button reader
  - LED controller
  - Future motor controller interface
```

## Android app modules

Suggested modules/classes:

- `MainActivity`
- `RobotDashboardViewModel`
- `BleRobotRepository`
- `Esp32Telemetry`
- `RobotCommand`
- `CommandValidator`
- `PrivacyFilter`
- `AssistantClient`
- `MockAssistantClient`
- `OpenAiAssistantClient` later
- `TextToSpeechService`

## ESP32 modules

Suggested firmware modules:

- BLE service setup
- command characteristic
- telemetry characteristic
- IMU service
- STOP button service
- LED service
- command validator

## Communication format

Prefer structured JSON during prototyping. A compact binary protocol may be considered only later.

## First implementation rule

All movement commands are simulation-only until the motor phase starts.
