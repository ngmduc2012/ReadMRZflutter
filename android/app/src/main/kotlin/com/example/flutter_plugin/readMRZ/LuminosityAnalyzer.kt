package com.example.flutter_plugin.readMRZ

import android.R.attr.angle
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.util.*


open class LuminosityAnalyzer(private val listener: LuminnosityAnalyzerCallBack) : ImageAnalysis.Analyzer {

    var listTextArray: ArrayList<String> = arrayListOf("")
    var line1Result: String = ""
    var line2Result: String = ""
    var line3Result: String = ""
    var textMRZResult: String = ""
    var i : Int = 0
    val countOfCheck = 5;

    private fun findTextMRZ(text: String): String {
        var line1: String = text.substring(0, 30)
        var line2: String = text.substring(30, 60)
        var line3: String = text.substring(60, 90)
        line1 = line1.substring(0, 5)+line1.substring(5).replace("O", "0")
        line2 = line2.substring(0, 29)+line2.substring(29).replace("O", "0")
        line3 = line3.replace("K<", "<<")
        line3 = line3.replace("S<", "<<")
        line3 = line3.replace("s<", "<<")
        line3 = line3.replace("k<", "<<")
        val pattern1 = "I[A-Z]{4}[0-9]{22}+<{2}[0-9]{1}".toRegex()
        val pattern2 = "[A-Z0-9]{2,30}+<{2,20}[0-9]{1}".toRegex()
        val pattern3 = "\\w+<<(\\w+<)+<{3,15}".toRegex()

        if (pattern1.matches(line1)) {
            line1Result = line1
        }
        if (pattern2.matches(line2)) {
            line2Result = line2
        }
        if (pattern3.matches(line3)) {
            line3Result = line3
        }
        return line1Result + line2Result + line3Result
    }

    private fun compareText(list: ArrayList<String>): String {
        val b = IntArray(list.size)
        for (i in 0 until list.size) {
            for (j in 0 until i) {
                if (list[i].equals(list[j])) b[i]++
            }
        }
        var max = b[0]
        for (i in 0 until list.size) {
            if (b[i] > max) max = b[i]
        }
        for (i in 0 until list.size) {
            if (b[i] == max) return list[i]
        }
        return "Không tìm thấy MRZ"
    }

    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            return
        }

        for (block in resultText.textBlocks) {
            var textIndex = block.text
            if (textIndex.length >= 90 && textIndex.startsWith("I")) {
//                Log.d("ok", textIndex)
                textIndex = textIndex.replace(" ", "").trim()
                textIndex = textIndex.replace("\n", "")
                textIndex = textIndex.replace("<S<", "<<<")
                textIndex = textIndex.replace("<K<", "<<<")
                textIndex = textIndex.replace("<K<", "<<<")
                textIndex = textIndex.replace("<s<", "<<<")
                if (textIndex.length == 90) {
                    Log.d("ok", textIndex)
                    textMRZResult = findTextMRZ(textIndex)
                    if (textMRZResult.length == 90) {
//                        Log.d("ok", findTextMRZ(textIndex))
                        listener.onChangeTextResult(textMRZResult)
                        line1Result = ""
                        line2Result = ""
                        line3Result = ""
//                        if (listTextArray.size < countOfCheck) {
//                            listTextArray.add(textMRZResult)
//                        } else {
//                            if (i == countOfCheck) i = 0
////                            Log.d("ok", i.toString())
//                            listTextArray[i] =  textMRZResult
//                            i++
//                            listener.onChangeTextResult(compareText(listTextArray))
////                            listTextArray.clear()
////                            listTextArray.add("")
//                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
//        val rotation = imageProxy.imageInfo.rotationDegrees
//        Log.d("ok", "rotation: $rotation")
        val source = BitmapUtils.getBitmap(imageProxy)!!
        val rotateBitmap : Bitmap
        val matrix = Matrix()
        matrix.postRotate(270f)
        rotateBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true)
        val image = FirebaseVisionImage.fromBitmap(rotateBitmap)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener {
                    Log.d("ok", "Failed")
                }.addOnCompleteListener {
                    imageProxy.close()
                }
    }



}