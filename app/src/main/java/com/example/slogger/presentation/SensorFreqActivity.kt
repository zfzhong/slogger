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
    public lateinit var freqs: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DropDownTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_freq)

        val titleView = findViewById<TextView>(R.id.title)
        val autoSensorView = findViewById<AutoCompleteTextView>(R.id.sensor)

        val tag = intent.getStringExtra("Tag").toString()
        val freq  = intent.getIntExtra("Freq", 0)

        if (tag == "AccelFreq") {
            titleView.text = "Accelerometer"
            freqs = resources.getStringArray(R.array.accel_freqs)
        } else if (tag == "GyroFreq") {
            titleView.text = "Gyroscope"
            freqs = resources.getStringArray(R.array.gyro_freqs)
        } else if (tag == "HeartFreq") {
            titleView.text = "Heart"
            freqs = resources.getStringArray(R.array.heart_freqs)
        } else if (tag == "OffBodyFreq") {
            titleView.text = "OffBody"
            freqs = resources.getStringArray(R.array.offbody_freqs)
        }

        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, freqs)
        autoSensorView.setAdapter(arrayAdapter)
        autoSensorView.setText(com.example.sloggerlib.freq2mode(freq), false)

        val button = findViewById<Button>(R.id.freqButton)
        button.setOnClickListener {
            var res = Intent()
            res.putExtra("Tag", tag)

            val mode = autoSensorView.text.toString()
            res.putExtra("Freq", com.example.sloggerlib.mode2freq(mode, tag))

            setResult(Activity.RESULT_OK, res)

            finish()
        }
    }
}