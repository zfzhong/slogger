package com.example.slogger.presentation

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import java.lang.ref.WeakReference

class SensorOffbody(
    context: Context,
    sensorManager: SensorManager,
    type: Int,
    deviceName: String,
    protocol: String,
    expId: String,
    freq: Int,
    maxRecordInFile: Int
) : GeneralSensor(
    context,
    sensorManager,
    type,
    deviceName,
    protocol,
    expId,
    freq,
    maxRecordInFile)
{
    // The Motion sensor differs from Accelerometer/Gyroscope in
    // the logging file format. We need the following function to get
    // offbody readings.
    override fun getValues(event: SensorEvent): String {
        // Timestamp in nanoseconds
        val t = event.timestamp

        val x = event.values[0]

        return "$t,$x\n"
    }
}