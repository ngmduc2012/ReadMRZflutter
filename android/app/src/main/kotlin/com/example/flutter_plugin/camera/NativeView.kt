package com.example.flutter_plugin.camera


import android.content.ContentValues.TAG
import android.content.Context

import android.util.Log
import android.view.View

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

import com.example.flutter_plugin.R
import com.example.flutter_plugin.readMRZ.LuminnosityAnalyzerCallBack
import com.example.flutter_plugin.readMRZ.LuminosityAnalyzer

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class NativeView(private val context: Context, messenger: BinaryMessenger,
                 id: Int, private val creationParams: Map<String?, Any?>?) : PlatformView, MethodChannel.MethodCallHandler,
        LifecycleOwner, LuminnosityAnalyzerCallBack {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val cameraKitView: View = View.inflate(context, R.layout.mainactivity, null)
    private val cameraPreview: PreviewView
    private var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val methodChannel: MethodChannel = MethodChannel(messenger, "camera/camera_$id")
    private val methodChannel1: MethodChannel = MethodChannel(messenger, "FlashLight")

    private var hasFaceDetection: Boolean = false
    private var cameraPosition: String = "F"
    private var changeFlashlight: Boolean = true


    override fun getView(): View {
        return cameraKitView
    }

    override fun dispose() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        cameraPreview = cameraKitView.findViewById(R.id.preview_view)
        methodChannel.setMethodCallHandler(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            if (cameraProvider == null) {
                cameraProvider = cameraProviderFuture.get()
            }
            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(cameraPreview.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer(this))
                    }


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                val cameraFlashLight = cameraProvider?.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                methodChannel1.setMethodCallHandler() { call, result ->
                    if (call.method == "btnFlashLight") {
                        cameraFlashLight?.cameraInfo?.hasFlashUnit()
                        cameraFlashLight?.cameraControl?.enableTorch(changeFlashlight)
                        changeFlashlight = !changeFlashlight
                        result.success(changeFlashlight)
                    } else {
                        result.notImplemented()
                    }
                }


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "initCamera" -> {
//                Log.d("ok", "initCamera")
                hasFaceDetection = call.argument<Boolean>("hasFaceDetection") ?: false
                cameraPosition = call.argument<String>("cameraPosition") ?: "F"
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                startCamera()
            }
            "flipCamera" -> {
                cameraPosition = if (cameraPosition == "F") {
                    "B"
                } else {
                    "F"
                }
                startCamera()
            }
            "resumeCamera" -> {
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                startCamera()
            }
            "pauseCamera" -> {
                cameraProvider?.unbindAll();
            }
            else -> result.notImplemented()
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onChangeTextResult(mrz: String) {
//        Log.d("ok", mrz)
        io.reactivex.rxjava3.core.Observable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe {
//                    Log.d("ok","callBack" )
                    methodChannel.invokeMethod("callBack", mrz)
                }
    }


}
