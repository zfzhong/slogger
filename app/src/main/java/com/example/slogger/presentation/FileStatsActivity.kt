package com.example.slogger.presentation


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.slogger.R
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
            //var res = Intent()
            //res.putExtra("FileStats", editText.text.toString())
            //setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}