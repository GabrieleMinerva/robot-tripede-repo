# Robot Tripede - Android + ESP32

Repository iniziale per il progetto di robot tripede semi-strisciante.

## Idea del progetto

Il robot usa un Samsung Galaxy S20 come testa intelligente e un ESP32 come centralina fisica.

- Il Samsung gestisce interfaccia, voce, schermo, camera, Wi-Fi e chiamate OpenAI API.
- L'ESP32 gestisce IMU, pulsante STOP, LED e in futuro servi/motori.
- La comunicazione tra Samsung ed ESP32 avviene via Bluetooth Low Energy.
- La privacy è rigida: non si invia video continuo, audio grezzo continuo o dati non necessari.

## Struttura

```text
robot-tripede-repo/
├── AGENTS.md
├── README.md
├── docs/
├── android-app/
└── esp32-firmware/
```

## Prima milestone

Creare una app Android Kotlin per Samsung S20 che:

1. mostra una dashboard;
2. si collega all'ESP32 via BLE;
3. visualizza stato STOP e telemetria IMU;
4. invia comandi LED;
5. accetta un comando testuale;
6. usa inizialmente un servizio AI mockato;
7. non implementa ancora movimento reale.

## Nota per Codex

Prima di scrivere codice, leggere `AGENTS.md` e tutti i file in `docs/`.
