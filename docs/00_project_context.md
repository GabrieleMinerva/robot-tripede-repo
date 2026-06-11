# Project context

This project is a prototype for a small indoor triped robot.

The first version does not use Raspberry Pi because of price and availability constraints.

The selected architecture uses a Samsung Galaxy S20 as the main smart interface and an ESP32 as the physical controller.

The robot must work only when connected to home Wi-Fi.

The user wants strict privacy, so the app must avoid sending unnecessary audio/video/image data to external APIs.

The robot will later move using two active legs and one passive rear support point, but the current phase focuses only on app, BLE, IMU, STOP button and LED feedback.
