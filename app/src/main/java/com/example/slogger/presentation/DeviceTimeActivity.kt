package com.example.slogger.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.example.slogger.R

class DeviceTimeActivity : AppCompatActivity() {

    private lateinit var year: NumberPicker
    private lateinit var month: NumberPicker
    private lateinit var day: NumberPicker
    private lateinit var hour: NumberPicker
    private lateinit var minute: NumberPicker
    private lateinit var second: NumberPicker

    private lateinit var panelType: String


    private val formatter : NumberPicker.Formatter = NumberPicker.Formatter {
        String.format("%02d",it)
    }

    private fun getDate(): String {
        val yy = String.format("%02d", year.value)
        var mm = String.format("%02d", month.value)
        var dd = String.format("%02d", day.value)

        return "$yy-$mm-$dd"
    }

    private fun getTime(): String {
        val hh = String.format("%02d", hour.value)
        var mm = String.format("%02d", minute.value)
        var ss = String.format("%02d", second.value)

        return "$hh:$mm:$ss"
    }

    private fun genDate(year: Int, month: Int, day: Int): Int {
        return (2000 + year) * 10000 + month * 100 + day
    }

    private fun genTimestamp(hour: Int, minute: Int, second: Int): Int {
        return hour * 3600 + minute * 60 + second
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_time)

        // "StartTime" or "EndTime"
        panelType = intent.getStringExtra("Tag").toString()
        //Log.d("Debug", "panelType: $panelType")

        var yyyymmdd = intent.getIntExtra("Date",0)
        var timestamp = intent.getIntExtra("Timestamp",0)

        year = findViewById(R.id.year)
        year.minValue=24
        year.maxValue=30
        year.value = com.example.sloggerlib.getYear(yyyymmdd) % 100

        year.setFormatter (formatter )

        month = findViewById(R.id.month)
        month.minValue = 1
        month.maxValue = 12
        month.value = com.example.sloggerlib.getMonth(yyyymmdd)
        month.setFormatter (formatter )

        day = findViewById(R.id.day)
        day.minValue = 1
        day.maxValue = 31

        day.value = com.example.sloggerlib.getDay(yyyymmdd)
        day.setFormatter (formatter )

        hour = findViewById(R.id.hour)
        hour.minValue=0
        hour.maxValue=23
        hour.value = com.example.sloggerlib.getHour(timestamp)
        hour.setFormatter (formatter )

        minute = findViewById(R.id.minute)
        minute.minValue = 0
        minute.maxValue = 59
        minute.value = com.example.sloggerlib.getMinute(timestamp)
        minute.setFormatter (formatter )

        second = findViewById(R.id.second)
        second.minValue = 0
        second.maxValue = 59
        second.value = com.example.sloggerlib.getSecond(timestamp)
        second.setFormatter(formatter)

        /* Todo
        month.setOnScrollListener {
            if (month.value <= 7) {
                if (month.value % 2 == 0) {
                    day.maxValue = 30
                }
            } else {
                if (month.value % 2 == 1) {
                    day.maxValue = 30
                }
            }

            // Logic to deal with February
            if (month.value == 2) {
                day.maxValue = 28
                if (year.value % 400 == 0) {
                    day.maxValue = 29
                } else if ((year.value % 4 == 0) && (year.value % 100 != 0)) {
                    day.maxValue = 29
                }
            }
        }
         */

        var saveButton = findViewById<Button>(R.id.timeSaveButton)

        saveButton.setOnClickListener{
            var res = Intent()

            res.putExtra("Tag", panelType)

            val d = genDate(year.value, month.value, day.value)
            res.putExtra("Date", d)

            var t = genTimestamp(hour.value, minute.value, second.value)
            res.putExtra("Timestamp", t)

            setResult(Activity.RESULT_OK, res)
            finish()
        }

    }

}