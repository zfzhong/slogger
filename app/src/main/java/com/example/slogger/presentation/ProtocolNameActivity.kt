package com.example.slogger.presentation


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.slogger.R
import org.w3c.dom.Text

class ProtocolNameActivity : AppCompatActivity() {
    private lateinit var editText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_protocol_name)

        // set the view
        val name = intent.getStringExtra("Protocol").toString().trim()
        editText = findViewById(R.id.protocolNameEditText)
        editText.text = name

        // event listener
        var saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            var res = Intent()
            res.putExtra("Protocol", editText.text.toString().trim())
            setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}