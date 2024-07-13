package com.example.sloggerlib


import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.BufferedWriter
import java.io.File

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

    // This will cause crash if we run the code on macbook.
    // To test on macbook, we should change it to
    // private var sensor = sensorManager.getDefaultSensor(type)
    //

    //private var sensor = sensorManager.getDefaultSensor(type, true)!! // Wearable
    private var sensor = sensorManager.getDefaultSensor(type) // Macbook

    private lateinit var fileHandler: File

    private var filename = ""
    private var fileSeqNum = 1
    private var currRecordCount = 0

    private var isRunning = false

    // User bufferedWriter to write to files
    private var bufferedWriter: BufferedWriter? = null

    // Write once every 500 records
    //private val write2fileMaxCount = 500
    private val write2fileMaxCount = batchSize

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

                if (currRecordCount == 0) {
                    // 1. Get current wall clock timestamp.
                    val millis = System.currentTimeMillis().toString()

                    // 2. Create a new file for storing logging data.
                    filename = com.example.sloggerlib.genLogFileName(
                        deviceName,
                        protocol,
                        getSensorTypeName(),
                        freq,
                        fileSeqNum,
                        expId,
                        millis
                    )

                    // open the new file
                    fileHandler = File(context.filesDir, filename)
//                    try {
//                        fileHandler.createNewFile()
//                    } catch (e: IOException) {
//                        broadcastMessage("create file error: $filename")
//                    }

                    // close previous bufferedWriter
                    bufferedWriter?.flush()
                    bufferedWriter?.close()
                    bufferedWriter = fileHandler.bufferedWriter()
                }

                val s = getValues(event)
                //bufferedWriter.write(s)

                write2File(s)
                currRecordCount += 1

                if (currRecordCount % write2fileMaxCount == 0) {
                    bufferedWriter?.flush()
                }

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

    private fun flushBuffer() {
        bufferedWriter?.flush()
        bufferedWriter?.close()
    }

    private fun write2File(data: String) {
        try {
            bufferedWriter?.write(data)
        } catch (e: Exception) {
            broadcastMessage("writing to file error")
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
            // Flush buffer to file before stop working
            flushBuffer()

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

    private fun broadcastMessage(msg: String) {
        //Log.d("debug", "broadcastMessage")
        val broadcastIntent = Intent("sensor_logging")
        broadcastIntent.putExtra("Message", msg)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }

}