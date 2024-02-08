package com.example.slogger.presentation

import android.content.Context
import android.util.Log
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDateTime

class DebugLogger(
    private val fileHandler: File
) {
    fun log2File(s: String) {
        try {
            val t = LocalDateTime.now()

            Log.d("Debug", "$t -> $s")
            fileHandler.appendText("$t -> $s")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}