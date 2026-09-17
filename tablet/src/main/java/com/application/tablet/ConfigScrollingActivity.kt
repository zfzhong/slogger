package com.application.tablet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.application.sloggerlib.ConfigParams
import com.application.sloggerlib.freq2mode
import com.application.sloggerlib.mode2freq
import com.application.sloggerlib.str2blemode
import com.application.sloggerlib.str2blescanpower
import com.application.sloggerlib.blescanpower2str
import com.application.sloggerlib.str2bleadmode
import com.application.sloggerlib.bleadmode2str
import com.application.sloggerlib.str2bleadpower
import com.application.sloggerlib.bleadpower2str
import com.application.tablet.R
import com.application.tablet.databinding.ActivityConfigScrollingBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream


class ConfigScrollingActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var configFile = "config.txt"
    private lateinit var configParams: ConfigParams

    private lateinit var binding: ActivityConfigScrollingBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.Theme_Slogger)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config_scrolling)


        binding = ActivityConfigScrollingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        // Load configurations
        loadConfigFile()

        val deviceName = findViewById<TextView>(R.id.id_device_name)
        deviceName.text = configParams.deviceName

        val protocol = findViewById<TextView>(R.id.id_protocol)
        protocol.text = configParams.protocol

        // Where the logs go. It was only ever a default compiled into
        // ConfigParams, so a tablet could record perfectly and have nowhere to
        // send the files - which is exactly how this was found.
        val serverUrl = findViewById<TextView>(R.id.id_server_url)
        serverUrl.text = configParams.baseURL

        val startTime = findViewById<TextView>(R.id.id_start_time)
        val sdate = configParams.getStartDate()
        val stime = configParams.getStartTime()
        startTime.text = "$sdate $stime"

        val endTime = findViewById<TextView>(R.id.id_end_time)
        val edate = configParams.getEndDate()
        val etime = configParams.getEndTime()
         endTime.text = "$edate $etime"

        val bleFilterDeviceNames = findViewById<TextView>(R.id.id_ble_scan_filter_device_names)
        bleFilterDeviceNames.text = configParams.bleFilterDeviceNames

        val accelSpinner: Spinner = findViewById(R.id.id_accel_spinner)
        val gyroSpinner: Spinner = findViewById(R.id.id_gyro_spinner)
        val bleSpinner: Spinner = findViewById(R.id.id_ble_spinner)


        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.accel_mode,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            Log.d("debug", "adapter")
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner.
            accelSpinner.adapter = adapter
            accelSpinner.onItemSelectedListener = this

            // set selection
            val accelMode = freq2mode(configParams.accelFreq)
            val pos = adapter.getPosition(accelMode)
            accelSpinner.setSelection(pos)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.gyro_mode,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            Log.d("debug", "adapter")
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner.
            gyroSpinner.adapter = adapter
            gyroSpinner.onItemSelectedListener = this

            // set selection
            val gyroMode = freq2mode(configParams.gyroFreq)
            val pos = adapter.getPosition(gyroMode)
            gyroSpinner.setSelection(pos)
        }


        ArrayAdapter.createFromResource(
            this,
            R.array.ble_mode,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            Log.d("debug", "adapter")
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner.
            bleSpinner.adapter = adapter
            bleSpinner.onItemSelectedListener = this

            // set selection
            val bleMode = configParams.bleMode.toString()
            Log.d("Debug", "BleMode: $bleMode")

            val pos = adapter.getPosition(bleMode)
            bleSpinner.setSelection(pos)
        }

        bleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val mode = parent?.getItemAtPosition(position)?.toString() ?: ""
                updateBleConfigVisibility(mode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // Apply visibility immediately based on the loaded selection
        updateBleConfigVisibility(configParams.bleMode.toString())

        val scanPowerSpinner: Spinner = findViewById(R.id.id_ble_scan_power_spinner)
        val adModeSpinner: Spinner = findViewById(R.id.id_ble_ad_mode_spinner)
        val adPowerSpinner: Spinner = findViewById(R.id.id_ble_ad_power_spinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.ble_scan_power,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            scanPowerSpinner.adapter = adapter
            scanPowerSpinner.onItemSelectedListener = this

            val pos = adapter.getPosition(blescanpower2str(configParams.bleScanPower))
            scanPowerSpinner.setSelection(pos)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.ble_ad_mode,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adModeSpinner.adapter = adapter
            adModeSpinner.onItemSelectedListener = this

            val pos = adapter.getPosition(bleadmode2str(configParams.bleAdMode))
            adModeSpinner.setSelection(pos)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.ble_ad_power,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adPowerSpinner.adapter = adapter
            adPowerSpinner.onItemSelectedListener = this

            val pos = adapter.getPosition(bleadpower2str(configParams.bleAdPower))
            adPowerSpinner.setSelection(pos)
        }

        val saveButton = findViewById<Button>(R.id.id_save)
        saveButton.setOnClickListener {
            handleSaveButtonClick()
        }

        val deleteButton = findViewById<Button>(R.id.id_delete)
        deleteButton.setOnClickListener {
            handleDeleteButtonClick()
        }
    }

    private fun updateBleConfigVisibility(bleModeStr: String) {
        val scanGroup = findViewById<View>(R.id.id_ble_scan_config_group)
        val adGroup = findViewById<View>(R.id.id_ble_ad_config_group)

        when (bleModeStr) {
            "SCAN" -> {
                scanGroup.visibility = View.VISIBLE
                adGroup.visibility = View.GONE
            }
            "ADVERTISE" -> {
                scanGroup.visibility = View.GONE
                adGroup.visibility = View.VISIBLE
            }
            else -> {
                scanGroup.visibility = View.GONE
                adGroup.visibility = View.GONE
            }
        }
    }

    private fun handleDeleteButtonClick() {
        Log.d("debug", "Delete button clicked")
        val localFiles = filesDir.listFiles()
        for (file in localFiles!!) {
            if (file.name != "config.txt") {
                file.delete()
            }
        }
        configParams.lastUploadedCount = 0
        saveConfigFile()

        Toast.makeText(this@ConfigScrollingActivity, "Files deleted.", Toast.LENGTH_SHORT).show()
        //finish()
    }

    /**
     * Tidy what was typed into something uploadable, or null to keep what was
     * already there.
     *
     * A bare host is the likely typo and the expensive one: HttpController
     * builds baseURL + suffixURL, so "example.org" would silently produce
     * "example.org/cmii/upload/" and every upload would fail with nothing on
     * screen to say why. A blank box means "leave it alone" rather than
     * "upload nowhere", because clearing a field by accident should not
     * quietly detach a tablet from the server mid-study.
     */
    private fun normaliseServer(raw: String): String? {
        var v = raw.trim()
        if (v.isEmpty()) return null
        if (!v.startsWith("http://") && !v.startsWith("https://")) v = "https://$v"
        return v.trimEnd('/')
    }

    private fun handleSaveButtonClick() {
        Log.d("debug", "Save Button Clicked")
        val deviceName = findViewById<TextView>(R.id.id_device_name).text.toString()
        val protocol = findViewById<TextView>(R.id.id_protocol).text.toString()
        val serverUrl = findViewById<TextView>(R.id.id_server_url).text.toString()

        val accelFreq = findViewById<Spinner>(R.id.id_accel_spinner).selectedItem.toString()
        val gyroFreq = findViewById<Spinner>(R.id.id_gyro_spinner).selectedItem.toString()
        val ble = findViewById<Spinner>(R.id.id_ble_spinner).selectedItem.toString()

        val startTime = findViewById<TextView>(R.id.id_start_time).text.toString()
        val endTime = findViewById<TextView>(R.id.id_end_time).text.toString()

        Log.d("debug", "$deviceName, $protocol, $accelFreq, $startTime, $endTime, $ble")

        val bleScanFilterDeviceNames = findViewById<TextView>(R.id.id_ble_scan_filter_device_names).text.toString()

        val bleScanPower = findViewById<Spinner>(R.id.id_ble_scan_power_spinner).selectedItem.toString()
        val bleAdMode = findViewById<Spinner>(R.id.id_ble_ad_mode_spinner).selectedItem.toString()
        val bleAdPower = findViewById<Spinner>(R.id.id_ble_ad_power_spinner).selectedItem.toString()

        configParams.accelFreq = mode2freq(accelFreq, "Accel")
        configParams.gyroFreq = mode2freq(gyroFreq, "Gyro")
        configParams.bleMode = str2blemode(ble)
        configParams.bleScanPower = str2blescanpower(bleScanPower)
        configParams.bleAdMode = str2bleadmode(bleAdMode)
        configParams.bleAdPower = str2bleadpower(bleAdPower)
        configParams.bleFilterDeviceNames = bleScanFilterDeviceNames

        configParams.deviceName = deviceName
        configParams.protocol = protocol
        normaliseServer(serverUrl)?.let { configParams.baseURL = it }

        var tokens = startTime.split(Regex("\\s+"))

        // get date
        var yymmdd = tokens[0].split("-")
        configParams.startDate = genDate(yymmdd[0].toInt(), yymmdd[1].toInt(), yymmdd[2].toInt())

        // get timestamp
        var ts = tokens[1].split(":")
        configParams.startTimestamp = genTimestamp(ts[0].toInt(), ts[1].toInt(), ts[2].toInt())


        tokens = endTime.split(Regex("\\s+"))

        // get date
        yymmdd = tokens[0].split("-")
        configParams.endDate = genDate(yymmdd[0].toInt(), yymmdd[1].toInt(), yymmdd[2].toInt())

        // get timestamp
        ts = tokens[1].split(":")
        configParams.endTimestamp = genTimestamp(ts[0].toInt(), ts[1].toInt(), ts[2].toInt())

        saveConfigFile()
        finish()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //TODO("Not yet implemented")
        Log.d("debug", "onItemSelected")

        val item = parent!!.getItemAtPosition(position).toString()
        // Showing selected spinner item
        Log.d("debug", "selected $item")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //TODO("Not yet implemented")
        Log.d("debug", "nothing selected")
    }

    private fun loadConfigFile() {
        // We have to specify the 'fileDir', otherwise it will cause error.
        // This wasted me about 2 hours to debug.
        val file = File(filesDir, configFile)
        configParams = if (file.exists()) {
            val s = file.bufferedReader().readLine()
            Log.d("debug", s)
            Json.decodeFromString(s)
        } else {
            ConfigParams("None")
        }
    }

    private fun broadcastConfigChange() {
        //Log.d("debug", "broadcastConfigChange")
        val broadcastIntent = Intent("config_change")
        broadcastIntent.putExtra("Message", "ConfigChanged")
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    private fun saveConfigFile() {
        //Log.d("debug", "saveConfig $msg")
        val file = File(filesDir, configFile)
        try {
            val s = Json.encodeToString(configParams)

            FileOutputStream(file).use {
                it.write(s.toByteArray())
            }
            // Not broadcasting for quick and dirty hack
            //broadcastConfigChange()
        } catch (e: Exception) {
            Log.d("error", e.toString())
        }
    }

    private fun genDate(year: Int, month: Int, day: Int): Int {
        return year * 10000 + month * 100 + day
    }

    private fun genTimestamp(hour: Int, minute: Int, second: Int): Int {
        return hour * 3600 + minute * 60 + second
    }
}