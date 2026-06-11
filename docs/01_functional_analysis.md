# Functional analysis

## Actors

- User
- Android app on Samsung S20
- ESP32 controller
- OpenAI API
- Future motor subsystem

## Main functions

The Android app must:

- show robot status;
- connect to ESP32 via BLE;
- display IMU and STOP status;
- accept typed and later voice commands;
- call OpenAI API through a privacy wrapper;
- show the assistant response on screen;
- read the response aloud;
- send only validated structured commands to ESP32.

The ESP32 firmware must:

- expose BLE services;
- read IMU;
- read physical STOP button;
- manage LEDs;
- reject unsafe commands;
- provide telemetry to Android.

## Out of scope for current phase

- Real motor movement.
- Leg mechanics.
- Autonomous navigation.
- Continuous video streaming.
- Continuous audio streaming.
