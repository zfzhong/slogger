package com.example.slogger.presentation


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
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

    private lateinit var recyclerView: WearableRecyclerView
    private lateinit var configList: MutableList<ConfigItem>
    private lateinit var configAdapter: ConfigAdapter

    private val deviceNameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data
                val name =  data?.getStringExtra("DeviceName").toString()
                configParams.deviceName = name
                //setNameView()

                configList[0].value = name

                configAdapter.notifyDataSetChanged()

                saveConfigFile()
            }
        }

    private val protocolNameResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data
                val protocol =  data?.getStringExtra("Protocol").toString()

                configParams.protocol = protocol
                configList[1].value = protocol
                configAdapter.notifyDataSetChanged()

                saveConfigFile()
            }
        }

    private val startEndTimeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result
                val data: Intent? = result.data
                val type = data?.getStringExtra("Tag").toString()
                val d = data?.getIntExtra("Date", 0)!!
                val t = data?.getIntExtra("Timestamp", 0)!!

                Log.d("Debug", "$type, $d, $t")
                configParams.startDate = d
                configParams.startTimestamp = t

                var i = 2
                if (type == "EndTime") {
                    i = 3
                }

                val startHour = String.format("%02d", getHour(t))
                val startMinute = String.format("%02d", getMinute(t))

                configList[i].value = configParams.getStartDate() + " $startHour:$startMinute "

                configAdapter.notifyDataSetChanged()

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
            intent.putExtra("Year", getYear(configParams.startDate))
            intent.putExtra("Month", getMonth(configParams.startDate))
            intent.putExtra("Day", getDay(configParams.startDate))

            intent.putExtra("Hour", getHour(configParams.startTimestamp))
            intent.putExtra("Minute", getMinute(configParams.startTimestamp))
            intent.putExtra("Second", getSecond(configParams.startTimestamp))

            startEndTimeResultLauncher.launch(intent)
        }

        endTimeView.setOnClickListener {
            var intent = Intent(this, DeviceTimeActivity::class.java)

            intent.putExtra("Type", "End")
            intent.putExtra("Year", getYear(configParams.endDate))
            intent.putExtra("Month", getMonth(configParams.endDate))
            intent.putExtra("Day", getDay(configParams.endDate))

            intent.putExtra("Hour", getHour(configParams.endTimestamp))
            intent.putExtra("Minute", getMinute(configParams.endTimestamp))
            intent.putExtra("Second", getSecond(configParams.endTimestamp))

            startEndTimeResultLauncher.launch(intent)
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
        setContentView(R.layout.activity_recyclerview)

        loadConfigFile()

        configList = ArrayList()

        val deviceName = ConfigItem("DeviceName", "Device", "")
        configList.add(deviceName)

        val protocol = ConfigItem("Protocol", "Protocol", "")
        configList.add(protocol)

        val startTime = ConfigItem("StartTime", "Start", "YYYY-MM-DD 00:00")
        configList.add(startTime)

        val endTime = ConfigItem("EndTime", "End", "YYYY-MM-DD 00:00")
        configList.add(endTime)

        val accelFreq = ConfigItem("AccelFreq", "Accel", "0")
        configList.add(accelFreq)

        val gyroFreq = ConfigItem("GyroFreq", "Gyro", "0")
        configList.add(gyroFreq)

        val heartFreq = ConfigItem("HeartFreq", "Heart", "0")
        configList.add(heartFreq)

        val fileStats = ConfigItem("FileStats", "Files", "0")
        configList.add(fileStats)

        configAdapter = ConfigAdapter(configList)

        configAdapter.onItemClick = {
            if (it.tag == "DeviceName") {
                var intent = Intent(this, DeviceNameActivity::class.java)
                intent.putExtra("Tag", it.tag)
                deviceNameResultLauncher.launch(intent)
            } else if (it.tag == "Protocol") {
                var intent = Intent(this, ProtocolNameActivity::class.java)
                intent.putExtra("Tag", it.tag)
                protocolNameResultLauncher.launch(intent)
            } else if (it.tag == "StartTime" || it.tag == "EndTime") {
                var intent = Intent(this, DeviceTimeActivity::class.java)
                intent.putExtra("Tag", it.tag)
                startEndTimeResultLauncher.launch(intent)
            }
        }

        recyclerView = findViewById(R.id.wearable_recycler_view)
        recyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            //layoutManager = WearableLinearLayoutManager(this@ConfigurationActivity)
            layoutManager = WearableLinearLayoutManager(this@ConfigListActivity, CustomScrollingLayoutCallback())
            adapter = configAdapter

        }

        //nameView = findViewById(R.id.deviceName)
        //setViews()
    }
}

/** How much icons should scale, at most.  */
private const val MAX_ICON_PROGRESS = 0.65f

class CustomScrollingLayoutCallback : WearableLinearLayoutManager.LayoutCallback() {

    private var progressToCenter: Float = 0f

    override fun onLayoutFinished(child: View, parent: RecyclerView) {
        child.apply {
            // Figure out % progress from top to bottom.
            val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
            val yRelativeToCenterOffset = y / parent.height + centerOffset

            // Normalize for center.
            progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)

            // Adjust to the maximum scale.
            progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS)

            scaleX = 1f
            scaleY = 1f

            if (progressToCenter > 0.3f) {
                scaleX = 1 - progressToCenter * progressToCenter
                //scaleY = 1 - progressToCenter * progressToCenter
            }
        }
    }
}
