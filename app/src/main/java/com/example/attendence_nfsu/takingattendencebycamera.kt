package com.example.attendence_nfsu

import android.Manifest
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.attendence_nfsu.databinding.ActivityTakingattendencebycameraBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//typealias LumaListener = (luma: Double) -> Unit


class takingattendencebycamera: AppCompatActivity() {

    private lateinit var binding: ActivityTakingattendencebycameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var overlay: Overlay
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector:CameraSelector
    private lateinit var imgCaptureExecutor: ExecutorService

    private val cameraProviderResult = registerForActivityResult(ActivityResultContracts.RequestPermission()){ permissionGranted->
        if(permissionGranted){
            // cut and paste the previous startCamera() call here.
            startCamera()
        }else {
            Snackbar.make(binding.root,"The camera permission is required", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakingattendencebycameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overlay = Overlay(this)

        val layoutOverlay = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        //this.addContentView(overlay,layoutOverlay)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProviderResult.launch(android.Manifest.permission.CAMERA)//permission execution
        imgCaptureExecutor=Executors.newSingleThreadExecutor()

        binding.cameraCaptureButton.setOnClickListener{
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animateFlash()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)//flash animation for picture clicking
    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 500)
        }, 100)
    }

    fun takePhoto() {//takes photo and sends it to post request
        var infoaboutuser=intent.getStringArrayExtra("infoaboutuser");
//        Toast.makeText(this, (infoaboutuser?.get(0) ?: "data1") + (infoaboutuser?.get(1) ?: "data2")+(infoaboutuser?.get(2) ?: "data3"), Toast.LENGTH_SHORT).show()
//        var str="";
//        if (infoaboutuser != null) {
//            for(s:String in infoaboutuser){
//                str+=s;
//            }
//        };
//        Toast.makeText(this,str, Toast.LENGTH_SHORT).show()
        if (infoaboutuser != null) {
            sendRequest(infoaboutuser)
        }
    }

    private fun startCamera() {//detects face and starts camera and shows preview images
        // listening for data from the camera
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val imageCapture=ImageCapture.Builder().build()


            // connecting a preview use case to the preview in the xml file.
            val preview = Preview.Builder().build().also{
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

             //ImageAnalysis UseCase
            val analysisUseCase = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer( imgCaptureExecutor,FaceAnalyzer(lifecycle,overlay,this))
                }
            try{
                // clear all the previous use cases first.
                cameraProvider.unbindAll()
                // binding the lifecycle of the camera to the lifecycle of the application.
                cameraProvider.bindToLifecycle(this,cameraSelector,preview,analysisUseCase)
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }

        },ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

//    private fun sendRequest(Text:Array<String>){
//        val url="http://127.0.0.1:3000";
//
//        // creating a new variable for our request queu
//        val queue: RequestQueue = Volley.newRequestQueue(this);
//        StringRequest request = new StringRequest(Request.Method.POST, url,
//
//    }
    private fun sendRequest(inputText: Array<String>) {
        val url = "http://172.18.6.78:8000/Data" // Replace with your API endpoint

        runBlocking {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true;
                    val payload = JSONObject()
                    payload.put("school", inputText[0])
                    payload.put("course", inputText[1])
                    payload.put("subject", inputText[2])

                    val outputStream = connection.outputStream
                    val writer = OutputStreamWriter(outputStream)
                    writer.write(payload.toString())
                    writer.flush()
                    writer.close()

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()

                        var line: String? = reader.readLine()
                        while (line != null) {
                            response.append(line)
                            line = reader.readLine()
                        }

                        reader.close()
                        inputStream.close()

                        // Process the response here
                        Log.e("API",response.toString())
                        Log.e("API DATA",payload.toString())
                    } else {
                        // Handle the error case
                        Log.e("API","Request failed with response code: $responseCode")
                    }

                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


}
