package com.example.slogger.presentation

import android.content.Context
import android.util.Log
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.io.FileWriter;
import java.io.IOException;





class DebugLogger(val parentDir: File){
    //private val TAG = "DebugLogger"
    private val LOG_FILE_NAME = "app_log.txt"
    private val logFile = File(parentDir, LOG_FILE_NAME)

    fun logDebug(tag: String, message: String) {
        Log.d(tag, message)

        // Append the log message to a file
        writeToFile(message)
    }

    private fun writeToFile(message: String) {
        try {
            val t = LocalDateTime.now()
            val writer = FileWriter(logFile, true)
            writer.append("$t -> $message\n")
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            Log.e("DebugLogger", "Error writing to log file: ${e.message}")
        }
    }

    // Replace with the appropriate method to get the external files directory
    // This can be done within an Activity, Fragment, or Application context
    private fun getExternalFilesDir(): File {
        // Example: return context.getExternalFilesDir(null) ?: File("")
        // Make sure to handle the null case appropriately
        TODO("Replace this with the appropriate implementation")
    }
}

