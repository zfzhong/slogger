package com.example.slogger.presentation


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.slogger.R

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.FileOutputStream

class ConfigListActivity : AppCompatActivity() {
    private var configFile = "config.txt"
    private lateinit var configParams: ConfigParams
    private lateinit var nameView:TextView
    private lateinit var startTimeView: TextView
    private lateinit var endTimeView: TextView
    private lateinit var accelView: TextView
    private lateinit var gyroView: TextView

    private val deviceNameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data
                val name =  data?.getStringExtra("Name").toString()
                configParams.deviceName = name
                setNameView()

                saveConfigFile()
            }
        }

    private val startTimeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data
                val type = data?.getStringExtra("Type").toString()
                val d = data?.getLongExtra("Date", 0)!!
                val t = data?.getLongExtra("Timestamp", 0)!!

                Log.d("Debug", "$type, $d, $t")
                if (type == "Start") {
                    configParams.startDate = d
                    configParams.startTimestamp = t
                    setStartTimeView()
                }
                if (type == "End") {
                    configParams.endDate = d
                    configParams.endTimestamp = t
                    setEndTimeView()
                }
                saveConfigFile()
            }
        }

    private val sensorFreqResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data

                val type = data?.getStringExtra("Type").toString()
                if (type == "Accel") {
                    configParams.accelFreq = data?.getIntExtra("Freq", 0)!!
                    setAccelView()
                }
                if (type == "Gyro") {
                    configParams.gyroFreq = data?.getIntExtra("Freq", 0)!!
                    //configParams.heartFreq = data?.getIntExtra("HeartFreq", 0)!!
                    setGyroView()
                }
                saveConfigFile()
            }
        }

    /*
    private val httpXferResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data
                configParams.xferLink = data?.getStringExtra("XferLink").toString()

                setEndTimeView()
            }
        }
     */

    private fun loadConfigFile() {
        // We have to specify the 'fileDir', otherwise it will cause error.
        // This wasted me about 2 hours to debug.
        val file = File(filesDir, configFile)
        configParams = if (file.exists()) {
            val s = file.bufferedReader().readLine()
            Json.decodeFromString(s)
        } else {
            ConfigParams("None")
        }
    }

    private fun saveConfigFile() {
        var file = File(filesDir, configFile)
        try {
            val s = Json.encodeToString(configParams)

            FileOutputStream(file).use {
                it.write(s.toByteArray())
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
        }
    }

    private fun setNameView() {
        val name = configParams.deviceName
        nameView.text = "Name: $name"
    }

    private fun setStartTimeView() {
        val startDate = configParams.getStartDate()
        val startTime = configParams.getStartTime()
        startTimeView.text = "Start: $startDate $startTime"
    }

    private fun setAccelView() {
        val accelFreq = configParams.accelFreq

        accelView.text = "Accel: $accelFreq Hz"
    }
    private fun setGyroView() {
        val gyroFreq = configParams.gyroFreq

        gyroView.text = "Gyro: $gyroFreq Hz"
    }

    private fun setEndTimeView() {
        val endDate = configParams.getEndDate()
        val endTime = configParams.getEndTime()
        endTimeView.text = " End: $endDate $endTime"
    }

    private fun setViews() {
        nameView = findViewById(R.id.deviceName)
        startTimeView = findViewById(R.id.startTime)
        endTimeView = findViewById(R.id.endTime)
        accelView = findViewById(R.id.sensorAccel)
        gyroView = findViewById(R.id.sensorGyro)


        setNameView()
        setStartTimeView()
        setEndTimeView()
        setAccelView()
        setGyroView()

        nameView.setOnClickListener {
            var intent = Intent(this, DeviceNameActivity::class.java)
            intent.putExtra("Name", configParams.deviceName)
            deviceNameResultLauncher.launch(intent)
        }

        startTimeView.setOnClickListener {
            var intent = Intent(this, DeviceTimeActivity::class.java)

            intent.putExtra("Type", "Start")
            intent.putExtra("Year", configParams.getStartYear())
            intent.putExtra("Month", configParams.getStartMonth())
            intent.putExtra("Day", configParams.getStartDay())

            intent.putExtra("Hour", configParams.getStartHour())
            intent.putExtra("Minute", configParams.getStartMinute())
            intent.putExtra("Second", configParams.getStartSecond())

            startTimeResultLauncher.launch(intent)
        }

        endTimeView.setOnClickListener {
            var intent = Intent(this, DeviceTimeActivity::class.java)

            intent.putExtra("Type", "End")
            intent.putExtra("Year", configParams.getEndYear())
            intent.putExtra("Month", configParams.getEndMonth())
            intent.putExtra("Day", configParams.getEndDay())

            intent.putExtra("Hour", configParams.getEndHour())
            intent.putExtra("Minute", configParams.getEndMinute())
            intent.putExtra("Second", configParams.getEndSecond())

            startTimeResultLauncher.launch(intent)
        }

        accelView.setOnClickListener {
            var intent = Intent(this, SensorFreqActivity::class.java)
            intent.putExtra("Type", "Accel")
            intent.putExtra("Freq", configParams.accelFreq)
            //intent.putExtra("HeartFreq", configParams.heartFreq)
            sensorFreqResultLauncher.launch(intent)
        }

        gyroView.setOnClickListener {
            var intent = Intent(this, SensorFreqActivity::class.java)
            intent.putExtra("Type", "Gyro")
            intent.putExtra("Freq", configParams.gyroFreq)
            //intent.putExtra("HeartFreq", configParams.heartFreq)
            sensorFreqResultLauncher.launch(intent)
        }
        /*
        xferView.setOnClickListener {
            var intent = Intent(this, HttpXferActivity::class.java)
            intent.putExtra("XferLink", xferView.text.toString())
            httpXferResultLauncher.launch(intent)
        }*/

        /*
        var button = findViewById<Button>(R.id.confirmButton)
        button.setOnClickListener{
            saveConfigFile()
            finish()
        }
         */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_list)

        loadConfigFile()
        setViews()
    }
}
