# Chintu Car v1 Complete

Upload all files to the ROOT of a GitHub repository.
Then GitHub -> Actions -> Build Chintu Car APK -> Run workflow.
Download artifact: ChintuCar-debug-apk.

The app currently includes the control UI and development MQTT connection.
MQTT broker: tcp://broker.hivemq.com:1883
Control topic: chintu-car/control
Telemetry topic: chintu-car/telemetry

The live map is a UI placeholder until GNSS telemetry and a map SDK are connected.
The displayed speed is a UI prototype until actual telemetry arrives.

For a physical vehicle, replace the public broker with an authenticated private broker,
use encryption, and implement an ESP32 watchdog that stops motors if commands stop arriving.
