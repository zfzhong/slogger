package com.example.slogger.presentation

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Call
import okhttp3.Callback
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

class HttpController(
    val activityReference: WeakReference<MainActivity>,
    var xferLink:String
) {
    //private val _XFER_URL = "https://weardatadl.com:8443/android_xfer/"
    //private val _XFER_URL = "http://192.168.1.214:8000/android_xfer/"


    private var numOfSentFiles = 0
    private var numOfFailedFiles = 0


    fun resetNumOfSentFiles() {
        numOfSentFiles = 0
        numOfFailedFiles = 0
    }

    public final suspend fun sendGetRequest() {
        // This function is mostly used for debugging network connection
        var client = OkHttpClient.Builder().build()

        // Define the URL you want to request
        //val url = "https://weardatadl.com:8443/api-auth/" // Replace with your API endpoint
        //var url = "http://192.168.1.214:8000/api-auth/"
        var url = "http://127.0.0.1:8000/api-auth/"
        //var url = "https://httpbin.org/get"

        // Create a GET request
        val request = Request.Builder()
            .url(url)
            .build()

        var tag = "HTTP-GET"
        Log.d(tag, "debug ok http")

        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseData = response.body?.string()
                // Handle the response data here
                Log.d(tag, "--> Success: $responseData")
            } else {
                // Handle the error response
                Log.d(tag, "--> failure")
            }
        } catch (e: Exception) {
            // Handle network or other exceptions
            Log.d(tag, e.toString())
        }
    }
    public final suspend fun sendFileRequest(file: File) {
        // Note: Since the SSL certificate of our current server is not
        // widely accepte, we have to set
        //    android:usesCleartextTraffic="true"
        // in the AndroidManifest.xml
        //

        //Log.d("sendFile:", "send file to server")
        // Solve SSL certificate issue (we dont need to verify)
        val customTrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // Implement client certificate validation logic if needed
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // Implement custom server certificate validation logic here
                // You can check if the server's certificate is in the chain or implement other checks
                // For example, you can validate the certificate against the TrustAnchor you created
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                //return arrayOf(serverCert)
                return arrayOf() // An empty array signifies that no certificates are accepted
            }
        }

        // Create a custom SSLContext with your custom TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(customTrustManager), null)

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, customTrustManager)
            .certificatePinner(CertificatePinner.Builder().build()) // Disable certificate pinning (optional but not recommended)
            .hostnameVerifier { hostname, session -> true } // Disabling hostname verification (not recommended)
            .build()

        //var url = "https://reqres.in/api/users?page=2"
        var url = xferLink
        //Log.d("xferlink", url)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", // The field name for the file
                file.name, // The desired file name on the server
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(url) // Replace with your server's URL
            .post(requestBody)
            .addHeader("FILENAME", file.name)
            .build()


        var tag = "FILE-HTTP-POST"


        try {
            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(tag, "HTTPS request failed, files: $numOfFailedFiles")
                    //e.printStackTrace()
                    numOfFailedFiles += 1
                    activityReference.get()?.uploadNext(numOfSentFiles+numOfFailedFiles)
                }

                override fun onResponse(call: Call, response: Response) {

                    if (response.isSuccessful) {
                        numOfSentFiles += 1

                        val activity = activityReference.get()
                        activity?.uploadNext(numOfSentFiles+numOfFailedFiles)
                        //Log.d(tag, "SUCCESS")

                        //val responseBody = response.body?.string()
                        //println("https response:")
                        //println(responseBody.toString())
                        // Handle the successful response here
                    } else {
                        Log.d(tag, "UPLOAD FILE ERROR: " +response.message)
                        // Handle errors here
                    }
                }
            })

        } catch (e: Exception) {
            // Handle network or other exceptions
            Log.d(tag, e.toString())
        }

    }
}