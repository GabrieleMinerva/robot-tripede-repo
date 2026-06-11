# Privacy and safety

## Privacy

The app must use a privacy-by-design approach.

Default behavior:

- no continuous video upload;
- no continuous audio upload;
- no raw sensor dump to external services;
- no API key hardcoded in source code;
- minimal payload sent to OpenAI API.

Preferred payload:

- user command as text;
- optional summarized environment;
- optional single image only when needed and explicitly triggered;
- ESP32 telemetry summary.

## Safety

OpenAI responses must never directly control hardware.

The app must convert assistant output into a limited command set.

The ESP32 must validate commands again.

STOP button has priority over everything.

If STOP is pressed, movement commands must be ignored.

## Command validation

The Android app must validate commands before sending them to ESP32.

The ESP32 must validate commands again before executing them.

Free-form text must never be sent directly to a hardware execution layer.
