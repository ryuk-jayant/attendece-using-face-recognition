package com.example.attendence_nfsu

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.core.content.PermissionChecker
import com.example.attendence_nfsu.databinding.ActivityTakingattendencebycameraBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

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
        this.addContentView(overlay,layoutOverlay)

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
            // ImageAnalysis UseCase
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


}
