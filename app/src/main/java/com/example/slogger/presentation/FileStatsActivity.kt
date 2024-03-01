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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.w3c.dom.Text

class FileStatsActivity : AppCompatActivity() {
    private lateinit var editText: TextView
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
        for (file in files) {
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

        // event listener
        var saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            var res = Intent()
            setResult(Activity.RESULT_OK, res)

            val alertDialogBuilder = AlertDialog.Builder(this@FileStatsActivity)
            alertDialogBuilder.setMessage("Delete all files?")
            alertDialogBuilder.setPositiveButton("Yes") { _,_ ->
                var files = filesDir.listFiles()
                for (file in files!!) {
                    if (file.name != "config.txt") {
                        file.delete()
                    }
                }
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
}