# AGENTS.md - Robot tripede Android + ESP32

## Project goal

Build a small indoor semi-dragging triped robot prototype.

The robot uses:

- Samsung Galaxy S20 as the main intelligent interface.
- ESP32 as the physical safety and sensor controller.
- Bluetooth Low Energy for communication between Android and ESP32.
- OpenAI API for online reasoning and assistant responses.
- Strict privacy filtering before any data is sent to external APIs.

The robot is currently not motorized. Movement commands must be simulated until the motor subsystem is explicitly added.

## Hardware architecture

Samsung Galaxy S20 responsibilities:

- User interface.
- Voice input.
- Text output.
- Speech output.
- Camera input.
- Wi-Fi connectivity.
- OpenAI API calls.
- Privacy filtering.
- High-level command generation.
- BLE communication with ESP32.

ESP32 responsibilities:

- Read IMU data.
- Read physical STOP button.
- Drive status LEDs.
- Expose BLE service.
- Reject unsafe commands.
- Later: control servos/motors.

## Current hardware

- Samsung Galaxy S20.
- ESP32 DevKit.
- IMU via I2C, preferably BNO085/BNO086/BNO055 or MPU6050.
- Physical STOP button.
- Red/green/blue status LEDs.
- Breadboard and Dupont wires.
- Power bank for ESP32.
- Robot body based on an electrical junction box.
- Smartphone mounted on top of the robot body.

## Safety rules

Codex must never implement direct uncontrolled motor movement.

All physical commands must pass through a safety layer.

Movement-related functions must remain mocked/simulated until the user explicitly starts the motor phase.

The STOP button has absolute priority.

If STOP is active:

- ESP32 must reject all movement commands.
- Android app must show STOP state.
- LED state must indicate STOP/error.

All commands sent from Android to ESP32 must be structured and validated.

No free-form natural language should be passed directly to movement execution.

## Privacy rules

Never stream continuous video to OpenAI.

Never stream continuous raw audio to OpenAI.

Prefer text commands over raw audio.

Send images only when explicitly required by a feature.

Before calling OpenAI API:

- remove unnecessary personal data;
- summarize the environment instead of sending raw camera data whenever possible;
- send the smallest useful payload;
- separate user conversation from hardware telemetry.

Do not hardcode API keys.

API keys must be loaded from secure configuration.

## App architecture

Initial Android app should provide:

- main dashboard;
- BLE connection state;
- ESP32 telemetry;
- IMU status;
- STOP status;
- voice command input;
- text response output;
- text-to-speech output;
- OpenAI API wrapper;
- privacy filter;
- command validator;
- mocked movement command panel.

Preferred implementation:

- Kotlin.
- Android Studio project.
- MVVM architecture.
- BLE service/repository separated from UI.
- OpenAI client separated from UI.
- Safety validator separated from OpenAI client.

## ESP32 firmware architecture

ESP32 firmware should provide:

- BLE GATT service.
- Telemetry characteristic.
- Command characteristic.
- STOP button reading.
- IMU reading.
- LED control.
- JSON or compact structured messages.

Initial commands:

- ping
- get_status
- set_led
- reset_error
- simulate_move_forward
- simulate_stop

Movement commands must remain simulation-only.

## BLE protocol principles

Android sends structured commands.

ESP32 returns structured telemetry.

Example Android to ESP32:

```json
{
  "type": "set_led",
  "color": "blue",
  "mode": "blink"
}
```

Example ESP32 to Android:

```json
{
  "stop": false,
  "imu": {
    "tilt": "stable",
    "fall_detected": false
  },
  "ble": "connected",
  "status": "ready"
}
```

## Coding rules

Keep code simple and modular.

Do not introduce complex frameworks unless necessary.

Prefer small testable classes.

Add comments only where useful.

Do not mix UI, BLE, OpenAI API, and safety logic in the same class.

Do not commit secrets.

Use environment/config placeholders for API keys.

## First milestone

Build a minimal Android app that:

1. opens on Samsung S20;
2. shows a dashboard;
3. connects to ESP32 over BLE;
4. receives fake or real telemetry;
5. displays STOP status;
6. can send LED commands;
7. accepts a typed command;
8. calls a mocked AI service first;
9. later swaps mocked AI with OpenAI API;
10. never sends raw camera/video by default.
