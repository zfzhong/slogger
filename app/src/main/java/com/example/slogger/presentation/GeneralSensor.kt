package com.example.slogger.presentation


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.io.File
import java.lang.ref.WeakReference

open class GeneralSensor (
    private val context: Context,
    private val sensorManager: SensorManager,
    private val type: Int,
    private val deviceName: String,
    private val protocol: String,
    private val expId: String,
    private val freq: Int,
    private val maxRecordInFile: Int,
    private val batchSize: Int
) {
    // Configure wakeup sensors
    //private var sensor = sensorManager.getDefaultSensor(type, true)!!
    private var sensor = sensorManager.getDefaultSensor(type)!!


    private lateinit var fileHandler: File

    private var filename = ""
    private var fileSeqNum = 1
    private var currRecordCount = 0

    private var isRunning = false

    // write once every 50 records
    private val write2fileMaxCount = 50
    // buffer the writing records
    private var buffString = ""

    private fun getSensorTypeName(): String {
        if (type == Sensor.TYPE_HEART_RATE) {
            return "Heart"
        } else if (type == Sensor.TYPE_GYROSCOPE) {
            return "Gyro"
        } else if (type == Sensor.TYPE_ACCELEROMETER) {
            return "Accel"
        } else if (type == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
            return "Presence" // Fitbit convention
        }

        return "Unspecified"
    }

    open fun getValues(event: SensorEvent): String {
        // Timestamp in nanoseconds
        val t = event.timestamp

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        return "$t,$x,$y,$z\n"
    }

    private val listener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == type) {

                val s = getValues(event)

                if (currRecordCount == 0) {
                    // 1. Get current wall clock timestamp.
                    val millis = System.currentTimeMillis().toString()

                    // 2. Create a new file for storing logging data.
                    filename = genLogFileName(
                        deviceName,
                        protocol,
                        getSensorTypeName(),
                        freq,
                        fileSeqNum,
                        expId,
                        millis)

                    // open the new file
                    fileHandler = File(context.filesDir, filename)
                }

                write2File(s)

                currRecordCount += 1

                // The following code might never have change to flush out some records which stay
                // in the 'buffSting' and their total number is less than 'write2fileMaxCount'.
//                if (currRecordCount % write2fileMaxCount == 0) {
//                    write2File(buffString)
//                    buffString = ""
//                }


                if (currRecordCount >= maxRecordInFile) {
                    currRecordCount = 0
                    fileSeqNum += 1
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            return
        }
    }

    private fun write2File(data: String) {
        try {
            fileHandler.appendText(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun isRunning(): Boolean {
        return isRunning
    }

    public fun start() {
        if (isRunning) {
            throw java.lang.Exception("The Accelerometer is running! Can't start it again!")
        }

        isRunning = true

        try {
            Log.d("Debug", "start/reg sensor ${getSensorTypeName()}")
            // register event to start accelerometer
            sensorManager.registerListener(
                listener,
                sensor,
                //getSensorMode(freq)
                (1000/freq)*1000,
                (1000/freq)*1000*batchSize
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun reset() {
        Log.d("Debug","Unregister ${getSensorTypeName()}")

        if (isRunning) {
            // Unregister sensor events
            sensorManager.unregisterListener(listener, sensor)

            // reset parameters
            // Any other parameters? How about experiment Id?

            filename = ""
            fileSeqNum = 1
            currRecordCount = 0
            isRunning = false
        }
    }

}