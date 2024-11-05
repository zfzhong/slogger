package com.application.slogger.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.application.slogger.R

class BatchSizeActivity : AppCompatActivity() {
    private lateinit var editText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_size)

        // set the view
        val batchSize = intent.getIntExtra("BatchSize", 1)
        editText = findViewById(R.id.batchSizeEditText)
        editText.text = batchSize.toString().trim()

        // event listener
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            val res = Intent()
            res.putExtra("BatchSize", editText.text.toString().trim())
            setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}