# Slogger

Slogger records raw sensor data on Android devices for research use: a Wear OS
app for smartwatches, a companion app for Android tablets and phones, and the
shared library both are built on.

It is meant for studies that need the *raw* signal — timestamped accelerometer,
gyroscope, heart rate, off-body and Bluetooth proximity samples written to CSV —
rather than the summarised activity metrics consumer wearables usually expose.
Recording runs unattended on a schedule, survives the screen turning off, and
uploads finished files to a server you nominate.

Slogger has been used to collect data for studies at the University of South
Carolina and Iowa State University.

---

## Contents

- [What it records](#what-it-records)
- [Modules](#modules)
- [Requirements](#requirements)
- [Building](#building)
- [Configuration](#configuration)
- [Sampling rates](#sampling-rates)
- [Output files](#output-files)
- [Uploading](#uploading)
- [Scheduled recording](#scheduled-recording)
- [Permissions](#permissions)
- [Data handling](#data-handling)
- [Support](#support)

---

## What it records

| Stream | Source | Notes |
|---|---|---|
| Accelerometer | `TYPE_ACCELEROMETER` | raw, including gravity |
| Linear acceleration | `TYPE_LINEAR_ACCELERATION` | the platform's own gravity-removed stream, recorded at the accelerometer rate; on by default |
| Gyroscope | `TYPE_GYROSCOPE` | |
| Heart rate | `TYPE_HEART_RATE` | watch only |
| Off-body / presence | `TYPE_LOW_LATENCY_OFFBODY_DETECT` | watch only; tells you when the device was not being worn |
| Bluetooth proximity | BLE scan or advertise | for device-to-device proximity between participants or rooms |

Linear acceleration is recorded alongside the raw accelerometer on purpose: it
lets the fusion performed offline during analysis be checked against the
platform's own estimate. A fused estimate nobody recorded cannot be compared
afterwards. Turn it off if the extra file per session is not wanted.

Composite sensors are synthesised by the platform and a given watch may simply
not offer one. Slogger treats a missing sensor as absent rather than fatal.

---

## Modules

| Module | Application ID | Runs on |
|---|---|---|
| `app` | `com.application.slogger` | Wear OS watches — Google Pixel Watch, Samsung Galaxy Watch |
| `tablet` | `com.application.tablet` | Android tablets and phones |
| `sloggerlib` | — | shared library: sensors, scheduling, file handling, BLE, upload |

The watch and tablet apps are thin user interfaces over `sloggerlib`, so both
write identical file formats and speak to the same server. The tablet build
records motion and Bluetooth but not heart rate or off-body, which are watch
sensors.

---

## Publications

Slogger has been used to collect or extract the data analysed in the following
peer-reviewed studies. Each names the application in its methods.

**Finnegan OL, White JW III, Armstrong B, Adams EL, Burkart S, Beets MW,
Nelakuditi S, Zhong Z, Yang H, Kiely KP, Ghosal R, Fairclough SJ, Welk GJ,
Weaver RG.** The ability of monitor-independent movement summary units,
Euclidean norm minus one, and mean amplitude deviation to harmonize
accelerometry data across research-grade and consumer wearable devices during
simulated free-living physical activity in children. *Journal for the
Measurement of Physical Behaviour* 2025;8(1). doi:10.1123/jmpb.2025-0002

> "Slogger is a sensor tracking application that our team developed to access
> and record underlying accelerometry and heart rate data from Fitbit devices."

**Duhamahoro J, Weaver RG, Finnegan OL, Zhong Z, Nelakuditi S, Berg E, Welk GJ.**
Evaluating device-agnostic properties of raw data preprocessing metrics in
physical activity assessment. *Measurement in Physical Education and Exercise
Science* 2026.

> "Collecting and extracting data from the watch was facilitated by a custom
> application called the 'Slogger' ... designed to log raw data from
> Accelerometer, Gyroscope, and Heart Rate Sensor locally with configurable
> frequencies."

Data for this study were collected under the Free-Living Study for Health
(FLASH) protocol at Iowa State University.

**Weaver RG, White JW, Finnegan O, Yang H, Zhong Z, Kiely K, Jones C, Tong Y,
Nelakuditi S, Ghosal R, Brown DE, Pate R, Welk GJ, de Zambotti M, Wang Y,
Burkart S, Adams EL, Armstrong B, Beets MW.** Predicting sleep and sleep stage
in children using actigraphy and heartrate via a long short-term memory deep
learning algorithm: a performance evaluation. *Journal of Sleep Research*
2026;35(1):e70149. doi:10.1111/jsr.70149

> "For Fitbit, the research team developed a custom application (Slogger). This
> application leveraged the Fitbit application programming interface to record
> and export the raw actigraphy data collected via Fitbit."

Raw actigraphy was recorded from the Fitbit Sense at 50 Hz against overnight
polysomnography in 238 children.

### A note on versions

Studies collecting on the **Fitbit Sense** used the earlier Fitbit-platform
version of this application, whose source is at
[zfzhong/Slog](https://github.com/zfzhong/Slog). Studies collecting on **Wear OS
watches or Android tablets** used the version in this repository. The recording
logic and the upload server are the same across both; some papers refer to
either as "Slogger".

---

## Requirements

- Android Studio (Gradle 8.7, Kotlin, Jetpack Compose)
- Watch app: Wear OS, `minSdk 31` (Android 12)
- Tablet app: `minSdk 30` (Android 11)
- `compileSdk 34`

## Building

```bash
git clone git@github.com:zfzhong/slogger.git
cd slogger
./gradlew :app:assembleRelease        # watch
./gradlew :tablet:assembleRelease     # tablet / phone
```

Or open the project in Android Studio and run the `app` or `tablet`
configuration against a connected device.

---

## Configuration

All settings are made on the device itself and stored as a config file in the
app's private storage.

| Setting | Meaning |
|---|---|
| Device name | Identifier for this unit, e.g. `PIX014`. Becomes the first field of every filename. |
| Protocol | Label for the recording condition, e.g. `Sleep`, `PA`. Second field of every filename. |
| Start / end date and time | When recording begins and ends (see [Scheduled recording](#scheduled-recording)). |
| Accelerometer / gyroscope / heart / off-body rate | Per-sensor sampling rate, or off. |
| Log linear acceleration | Whether to record the gravity-removed stream. Default on. |
| BLE mode | `SCAN`, `ADVERTISE` or `OFF`. |
| BLE scan / rest interval | Seconds scanning, then seconds idle. Default 20 / 20. |
| BLE filter | Only record advertisements whose device name matches. |
| Batch size | Records buffered before each write to disk. |
| Server URL | Base URL that finished files are uploaded to. |

**Set your own server URL before collecting data.** The value compiled in is a
development host and is not a place to send study data.

---

## Sampling rates

Rates are chosen by name. The underlying Android delay constants are advisory:
the actual rate varies by device, and the real sampling interval should be
recovered from the timestamps rather than assumed.

| Setting | Motion sensors | Heart rate / off-body |
|---|---|---|
| `Off` | not recorded | not recorded |
| `Normal` | ~5 Hz | ~1 Hz |
| `UI` | ~16 Hz | — |
| `Game` | ~50 Hz | — |
| `Fastest` | ~100 Hz or above | — |

---

## Output files

Files are written to the app's private storage and named:

```
<device>_<protocol>_<sensor>_<rate>_<seq>_<sessionId>_<created>.csv

PIX014_Sleep_Heart_50_1_1698278400157_1698278400254.csv
```

| Field | Meaning |
|---|---|
| `device` | device name from the configuration |
| `protocol` | protocol label from the configuration |
| `sensor` | `Accel`, `Linear`, `Gyro`, `Heart`, `Presence` or `BLE` |
| `rate` | configured rate |
| `seq` | file sequence number within the session, from 1 |
| `sessionId` | wall-clock milliseconds when the recording session started — shared by every file of a session |
| `created` | wall-clock milliseconds when this particular file was opened |

A new file is started every 6000 records, so a session produces a numbered
series per sensor rather than one large file.

### Columns

No header row is written.

| Sensor | Columns |
|---|---|
| `Accel`, `Linear`, `Gyro` | `timestamp, x, y, z` |
| `Heart` | `timestamp, bpm` |
| `Presence` | `timestamp, value` |
| `BLE` | `timestamp_ms, index, device_name, mac_address, rssi` |

**Timestamps differ between streams.** Sensor rows carry the Android sensor
event timestamp in **nanoseconds**, measured on the device's monotonic clock,
which does not correspond to wall-clock time. BLE rows carry **wall-clock
milliseconds**. To place a sensor session on the wall clock, anchor it with the
`sessionId` and `created` values in the filename, which are wall-clock
milliseconds. Doing this correctly matters when data from several devices is
combined.

---

## Uploading

Finished files are uploaded as `multipart/form-data` POSTs to
`<server URL>/cmii/upload/`. Files remain on the device until the upload is
acknowledged, so recording is unaffected by being out of network range.

> **`allowInsecureTls`**
> A configuration flag exists to skip certificate verification, for a legacy
> host whose certificate the Android trust store rejects. It disables **all**
> TLS checking, not merely one exception. Leave it off for any server with a
> valid certificate, and do not enable it for a server receiving study data.

---

## Scheduled recording

Recording is driven by exact alarms (`setExactAndAllowWhileIdle`) at the
configured start and end times, so a device can be handed to a participant
already configured and will begin and end on its own. Sampling runs in a
foreground service with a persistent notification, which is what keeps Android
from suspending collection when the screen is off.

Battery optimisation should be disabled for the app on devices used for
multi-day collection.

---

## Permissions

The watch app requests `BODY_SENSORS` and `ACTIVITY_RECOGNITION` for heart rate
and motion; `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT` and
location for Bluetooth proximity — Android requires location permission for any
BLE scan, and Slogger does not record location; `FOREGROUND_SERVICE` and
`WAKE_LOCK` to keep recording with the screen off; `SCHEDULE_EXACT_ALARM` for
timed sessions; and `INTERNET` for upload.

---

## Data handling

Slogger writes raw sensor data to the device's private storage and, when a
server is configured, uploads it there. It applies no encryption of its own, at
rest or beyond the transport. Devices, the receiving server, and the retention
and access arrangements around both are the responsibility of the study, and
should be described in its ethics or IRB submission before collection begins.

---

## Support

Slogger is research software, written and maintained by Zifei (David) Zhong.
Issues and pull requests are welcome. If you are running it for a study and hit
something the tool was not built for — a new device, a different protocol, an
unfamiliar output format — please open an issue rather than working around it.

