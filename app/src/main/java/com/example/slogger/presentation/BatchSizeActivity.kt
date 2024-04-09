package com.example.slogger.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slogger.R

class BatchSizeActivity : AppCompatActivity() {
    private lateinit var editText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_size)

        // set the view
        val batchSize = intent.getIntExtra("BatchSize", 1)
        editText = findViewById(R.id.batchSizeEditText)
        editText.text = batchSize.toString()

        // event listener
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            val res = Intent()
            res.putExtra("BatchSize", editText.text.toString())
            setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}