# Hardware architecture

## Current hardware

- Samsung Galaxy S20, mounted on top of the robot body.
- ESP32 DevKit.
- IMU over I2C.
- Physical STOP button.
- RGB or separate red/green/blue LEDs.
- Breadboard and Dupont jumper wires.
- Power bank for ESP32.
- Electrical junction box as robot body.

## Samsung S20 role

The smartphone provides:

- display;
- touchscreen;
- camera;
- microphone;
- speaker;
- Wi-Fi;
- Bluetooth;
- compute power;
- OpenAI API access.

## ESP32 role

The ESP32 provides:

- GPIO;
- IMU acquisition;
- STOP button acquisition;
- LED status feedback;
- BLE GATT server;
- future servo/motor control.

## Initial wiring idea

```text
ESP32 3V3  -> IMU VCC, if module supports 3.3V
ESP32 GND  -> IMU GND, STOP GND, LED GND path
ESP32 SDA  -> IMU SDA
ESP32 SCL  -> IMU SCL
GPIO input -> STOP button
GPIO out   -> LEDs through resistors
```

Exact pins must be defined after selecting the ESP32 board.
