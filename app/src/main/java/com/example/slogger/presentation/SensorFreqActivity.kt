package com.example.slogger.presentation


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import com.example.slogger.R

class SensorFreqActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DropDownTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_freq)

        val titleView = findViewById<TextView>(R.id.title)
        val autoSensorView = findViewById<AutoCompleteTextView>(R.id.sensor)

        val type = intent.getStringExtra("Type").toString()
        val freq  = intent.getIntExtra("Freq", 0)
        val mode = freq2mode(freq)

        if (type == "Accel") {
            titleView.text = "Accelerometer"
            // set dropdown menu for Accel
            val freqs1 = resources.getStringArray(R.array.accel_freqs)
            val arrayAdapter1 = ArrayAdapter(this, R.layout.dropdown_item, freqs1)

            autoSensorView.setAdapter(arrayAdapter1)
            autoSensorView.setText(mode, false)
        }

        if (type == "Gyro") {
            titleView.text = "Gyroscope"
            // set dropdown menu for Gyro
            val freqs2 = resources.getStringArray(R.array.gyro_freqs)
            val arrayAdapter2 = ArrayAdapter(this, R.layout.dropdown_item, freqs2)

            autoSensorView.setAdapter(arrayAdapter2)
            autoSensorView.setText(mode, false)
        }

        val button = findViewById<Button>(R.id.freqButton)
        button.setOnClickListener {
            var res = Intent()

            res.putExtra("Type", type)

            val mode = autoSensorView.text.toString()
            val freq = mode2freq(mode)
            res.putExtra("Freq", freq)
            setResult(Activity.RESULT_OK, res)

            finish()
        }
    }
}