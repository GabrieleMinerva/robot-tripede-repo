# Development roadmap

## Phase 1 - Repository and app skeleton

- Create Android Kotlin project.
- Create dashboard UI.
- Add mock BLE repository.
- Add mock assistant client.
- Add privacy filter.
- Add command validator.

## Phase 2 - ESP32 firmware mock/real telemetry

- Create ESP32 firmware project.
- Expose BLE service.
- Send fake telemetry.
- Implement LED commands.
- Implement STOP button reading.

## Phase 3 - IMU integration

- Select IMU model.
- Wire IMU over I2C.
- Read IMU on ESP32.
- Send IMU telemetry to Android.

## Phase 4 - OpenAI API integration

- Add API client.
- Store API key securely.
- Send text-only commands first.
- Add strict privacy filter.
- Keep movement mocked.

## Phase 5 - Voice and speech output

- Add voice command input.
- Add text-to-speech output.
- Keep transcript visible on screen.

## Phase 6 - Future movement phase

Not part of current phase.

Only after user approval:

- choose servos/motors;
- design leg mechanics;
- add motor driver;
- implement hardware safety;
- test movements at low power.
