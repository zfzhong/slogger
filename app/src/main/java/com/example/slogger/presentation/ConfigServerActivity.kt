package com.example.slogger.presentation


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.slogger.R
import org.w3c.dom.Text

class ConfigServerActivity : AppCompatActivity() {
    private lateinit var baseURLText: TextView
    private lateinit var suffixURLText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_server)

        // Base URL
        val baseURL = intent.getStringExtra("BaseURL").toString()
        baseURLText = findViewById(R.id.baseURL)
        baseURLText.text = baseURL

        // Suffix URL
        val suffixURL = intent.getStringExtra("SuffixURL").toString()
        suffixURLText = findViewById(R.id.suffixURL)
        suffixURLText.text = suffixURL

        // event listener
        var saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener{
            var res = Intent()

            res.putExtra("BaseURL", baseURLText.text.toString())
            res.putExtra("SuffixURL", suffixURLText.text.toString())

            setResult(Activity.RESULT_OK, res)
            finish()
        }
    }
}