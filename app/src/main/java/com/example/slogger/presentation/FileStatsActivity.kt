package com.example.slogger.presentation


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.slogger.R
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class FileStatsActivity : AppCompatActivity() {
    private lateinit var editText: TextView
    private lateinit var configParams: com.example.sloggerlib.ConfigParams
    private var configFile = "config.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_stats)

        // set the view
        //val data = intent.getStringExtra("FileStats").toString()
        var accelFileCount = 0
        var gyroFileCount = 0
        var heartFileCount = 0
        var bodyFileCount = 0

        val files = filesDir.listFiles()
        for (file in files!!) {
            if (file.name.contains("Accel")) {
                accelFileCount += 1
            } else if (file.name.contains("Gyro")) {
                gyroFileCount += 1
            } else if (file.name.contains("Heart")) {
                heartFileCount += 1
            } else if (file.name.contains("Presence")) {
                bodyFileCount += 1
            }
         }

        val data = "A:$accelFileCount G:$gyroFileCount H:$heartFileCount B:$bodyFileCount"
        editText = findViewById(R.id.fileStatsTextView)
        editText.text = data

        loadConfigFile()

        // event listener
        var saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            var res = Intent()
            setResult(Activity.RESULT_OK, res)

            val alertDialogBuilder = AlertDialog.Builder(this@FileStatsActivity)

            if (configParams.lastUploadedCount < accelFileCount + gyroFileCount + heartFileCount + bodyFileCount + 1) {
                alertDialogBuilder.setMessage("Delete? Some files are not uploaded!")
            } else {
                alertDialogBuilder.setMessage("Delete all files?")
            }

            alertDialogBuilder.setPositiveButton("Yes") { _,_ ->
                val localFiles = filesDir.listFiles()
                for (file in localFiles!!) {
                    if (file.name != "config.txt") {
                        file.delete()
                    }
                }
                configParams.lastUploadedCount = 0
                saveConfigFile()

                Toast.makeText(this@FileStatsActivity, "Deletion completed", Toast.LENGTH_SHORT).show()
                finish()
            }
            alertDialogBuilder.setNegativeButton("Cancel") {_,_ ->
                Toast.makeText(this@FileStatsActivity, "Deletion canceled", Toast.LENGTH_SHORT).show()
            }

            val alertDialogBox = alertDialogBuilder.create()
            alertDialogBox.show()
        }
    }

    fun deleteAllFiles() {
        var files = filesDir.listFiles()
        for (file in files!!) {
            if (file.name != "config.txt") {
                file.delete()
            }
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

    private fun loadConfigFile() {
        val file = File(filesDir, configFile)
        configParams = if (file.exists()) {
            val s = file.bufferedReader().readLine()
            Json.decodeFromString(s)
        } else {
            com.example.sloggerlib.ConfigParams("None")
        }
    }
}