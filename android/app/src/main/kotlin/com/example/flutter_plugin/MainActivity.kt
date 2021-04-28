@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.example.flutter_plugin

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flutter_plugin.camera.NativeViewFactory

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.security.Permission

class MainActivity : FlutterActivity() {
    private val CHANNEL = "permissionChannel"
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        flutterEngine
                .platformViewsController
                .registry
                .registerViewFactory("camera", NativeViewFactory(flutterEngine.dartExecutor.binaryMessenger))
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
            call, result ->
            if (call.method == "permission") {
                permission()
            } else {
                result.notImplemented()
            }
        }
    }

    fun permission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !==
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.CAMERA) !==
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA), 432)
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            432 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.CAMERA) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }

        }
    }

}


