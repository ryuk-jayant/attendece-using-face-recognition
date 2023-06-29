package com.example.attendence_nfsu

import android.Manifest
import android.content.Context
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
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
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

        //return if clicked without loading
        val imageCapture = imageCapture ?: return

        val date=Date();
        val name=date.toString();
        val path=name+".jpg";

        val photofile=File(applicationContext.filesDir,path);

        val outputFileOptions= ImageCapture.OutputFileOptions.Builder(photofile).build()

        imageCapture.takePicture(outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException)
                {
                   Log.e("Image","Error");
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // insert your code here.
                    Log.e("Image","image is Saved on ${photofile.toURI()}")
                   // Toast.makeText(this@takingattendencebycamera,"${photofile.toURI()}",Toast.LENGTH_SHORT).show()
                }
            })

        try {
            Thread.sleep(3000) // Wait for 3000 milliseconds (3 seconds)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        if (infoaboutuser != null) {
            sendRequest(infoaboutuser,photofile)
        }
    }

    private fun startCamera() {//detects face and starts camera and shows preview images
        // listening for data from the camera
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            //image capture use case
            imageCapture=ImageCapture.Builder().build()

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
                cameraProvider.bindToLifecycle(this,cameraSelector,preview,analysisUseCase,imageCapture)
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }

        },ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val TAG = "AttendenceApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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


    private fun sendRequest(inputText: Array<String>, image:File) {
        val url = "http://172.18.6.78:8000/Data" // Replace with your API endpoint

        runBlocking {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true;
                    val payload = JSONObject()
//                    val imagebytes = image.readBytes()
//                    val base64image= android.util.Base64.encodeToString(imagebytes,android.util.Base64.DEFAULT)
                    payload.put("school", inputText[0])
                    payload.put("course", inputText[1])
                    payload.put("subject", inputText[2])
                    payload.put("IMAGE",image.toURI())

                    val outputStream = connection.outputStream
                    val writer = OutputStreamWriter(outputStream,"UTF-8")
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
                        //Toast.makeText(this@takingattendencebycamera,responseCode.toString(),Toast.LENGTH_SHORT).show()
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
