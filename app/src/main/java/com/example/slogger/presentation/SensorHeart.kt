package com.example.slogger.presentation

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import java.lang.ref.WeakReference

class SensorHeart(
    context: Context,
    sensorManager: SensorManager,
    type: Int,
    deviceName: String,
    expId: String,
    freq: Int,
    maxRecordInFile: Int
) : GeneralSensor(
    context,
    sensorManager,
    type,
    deviceName,
    expId,
    freq,
    maxRecordInFile)
{
    // The heart rate sensor differs from Accelerometer/Gyroscope in
    // the logging file format. We need the following function to get
    // heart rate readings.
    override fun getValues(event: SensorEvent): String {
        // Timestamp in nanoseconds
        val t = event.timestamp

        val x = event.values[0]

        return "$t,$x\n"
    }
}