package com.application.sloggerlib

import android.content.Context
import android.hardware.SensorManager

/**
 * The platform's own gravity-free acceleration, logged beside the raw
 * accelerometer rather than instead of it.
 *
 * Written as <device>_<protocol>_LAccel_<rate>_<seq>_<expId>_<wallMs>.csv, so
 * it sorts and parses exactly like the accelerometer's own files.
 *
 * Android synthesises TYPE_LINEAR_ACCELERATION by fusing the accelerometer
 * with the gyroscope, which is the same job the analysis does offline. Keeping
 * both lets one check the other: if they disagree, one of the two is wrong and
 * the disagreement is visible instead of silent.
 *
 * It is a companion stream, not a replacement. Vendors implement the fusion
 * differently and an OS update can change it with no announcement, so the raw
 * accelerometer and gyroscope remain the record of what was measured and this
 * remains a cross-check.
 */
class SensorLinearAccel(
    context: Context,
    sensorManager: SensorManager,
    type: Int,
    deviceName: String,
    protocol: String,
    expId: String,
    freq: Int,
    maxRecordInFile: Int,
    batchSize: Int,
    isWearable: Boolean
) : GeneralSensor(
    context,
    sensorManager,
    type,
    deviceName,
    protocol,
    expId,
    freq,
    maxRecordInFile,
    batchSize,
    isWearable)
{}
