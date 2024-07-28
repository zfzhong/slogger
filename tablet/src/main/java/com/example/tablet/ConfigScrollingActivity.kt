package com.example.tablet

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
import com.example.tablet.databinding.ActivityConfigScrollingBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream


class ConfigScrollingActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var configFile = "config.txt"
    private lateinit var configParams: com.example.sloggerlib.ConfigParams

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

        val startTime = findViewById<TextView>(R.id.id_start_time)
        val sdate = configParams.getStartDate()
        val stime = configParams.getStartTime()
        startTime.text = "$sdate $stime"

        val endTime = findViewById<TextView>(R.id.id_end_time)
        val edate = configParams.getEndDate()
        val etime = configParams.getEndTime()
         endTime.text = "$edate $etime"

        val spinner: Spinner = findViewById(R.id.id_accel_spinner)

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
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this

            // set selection
            val accelMode = com.example.sloggerlib.freq2mode(configParams.accelFreq)
            val pos = adapter.getPosition(accelMode)
            spinner.setSelection(pos)
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

    private fun handleSaveButtonClick() {
        Log.d("debug", "Save Button Clicked")
        val deviceName = findViewById<TextView>(R.id.id_device_name).text.toString()
        val protocol = findViewById<TextView>(R.id.id_protocol).text.toString()

        val accelFreq = findViewById<Spinner>(R.id.id_accel_spinner).selectedItem.toString()

        val startTime = findViewById<TextView>(R.id.id_start_time).text.toString()
        val endTime = findViewById<TextView>(R.id.id_end_time).text.toString()
        Log.d("debug", "$deviceName, $protocol, $accelFreq, $startTime, $endTime")

        configParams.accelFreq = com.example.sloggerlib.mode2freq(accelFreq, "Accel")

        configParams.deviceName = deviceName
        configParams.protocol = protocol

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
            com.example.sloggerlib.ConfigParams("None")
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