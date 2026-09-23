package com.application.slogger.presentation


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.application.slogger.R

class BleFilterActivity : AppCompatActivity() {
    private lateinit var editText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_filter)

        // set the view
        val filterNames = intent.getStringExtra("ScanFilter").toString().trim()
        editText = findViewById(R.id.bleFilterEditText)
        editText.text = filterNames

        // event listener
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            val res = Intent()
            res.putExtra("ScanFilter", editText.text.toString().trim())
            setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}
