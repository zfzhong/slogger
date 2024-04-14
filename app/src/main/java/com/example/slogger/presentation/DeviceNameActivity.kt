package com.example.slogger.presentation


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.slogger.R
import org.w3c.dom.Text

class DeviceNameActivity : AppCompatActivity() {
    private lateinit var editText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_name)

        // set the view
        val name = intent.getStringExtra("DeviceName").toString().trim()
        editText = findViewById(R.id.deviceNameEditText)
        editText.text = name

        // event listener
        var saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            var res = Intent()
            res.putExtra("DeviceName", editText.text.toString().trim())
            setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}