package com.example.ocrapp

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.pm.PackageManager
import java.util.regex.Pattern
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException


class PictureFragment : Fragment() {

    enum class Payment(val pay: String) {
        TOTAL("TOTAL"),
        TOTAL_LEI("TOTAL LEI"),
        TOTAL_PLATA("TOTAL PLATA"),
        TOTAL_DE_PLATA("TOTAL DE PLATA"),
        SUMA("SUMA")
    }

    private var regexSuma = """\d+[.,]\d{2}"""
    private var regexData = """\b(?:0?[1-9]|[12][0-9]|3[01])[./-](?:0?[1-9]|1[0-2])[./-]\d{4}\b"""
    private var regexLocation =  """(?:S\.?C\.?\s*)?.*?\s*S\.?R\.?L"""
    private var enumpayments = Payment.entries.map { it.pay }
    private var storage = Firebase.storage
    private var storageRef = storage.reference
    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var textPicture: TextView
    private lateinit var imageCapture: ImageCapture
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(2)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_JPEG)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()

    private var scanner = GmsDocumentScanning.getClient(options)
    private var scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data!!)
            if (scanningResult != null) {
                val uri = scanningResult.pages?.get(0)?.imageUri
                if (uri != null) {
                    recognizeText(uri)
                    var mAuth = FirebaseAuth.getInstance()
                    var imageRef: StorageReference? = mAuth!!.currentUser?.uid?.let { storageRef.child("images/$it/${System.currentTimeMillis()}.jpg")}!!
                    if (imageRef != null) {
                        imageRef.putFile(uri)
                                .addOnSuccessListener { taskSnapshot ->
                                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                        Log.d("ULR", downloadUri.toString())
                                        Glide.with(this)
                                                .load(downloadUri)
                                                .placeholder(R.drawable.background) // Imagine temporară
                                                .error(R.drawable.ic_launcher_background) // Imagine în caz de eroare
                                                .into(imageView)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirebaseStorage", "Failed to upload image", e)
                                }
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_picture, container, false)
        //previewView = view.findViewById(R.id.previewView)
        imageView = view.findViewById(R.id.imageView)
        textPicture = view.findViewById(R.id.textPicture)
        val captureButton: Button = view.findViewById(R.id.captureButton)

        Glide.with(this)
                .load("https://firebasestorage.googleapis.com/v0/b/applicenta-84bad.appspot.com/o/images%2F6V2dWuQKDqVvhdHm8RjfUpFAAfH2%2F1716593199422.jpg?alt=media&token=9e34ddc2-e532-4ffa-8322-d7206b3acbc7")
                .placeholder(R.drawable.background) // Imagine temporară
                .error(R.drawable.ic_launcher_background) // Imagine în caz de eroare
                .into(imageView)

        Log.d("Start", "salut")


        captureButton.setOnClickListener {
            imageView.setImageURI(null)
            startDocumentScanner(null)
        }

//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }

        return view
    }

//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//
//        cameraProviderFuture.addListener(Runnable {
//            val cameraProvider = cameraProviderFuture.get()
//            val preview = androidx.camera.core.Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }
//
//            imageCapture = ImageCapture.Builder().build()
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
//            } catch(exc: Exception) {
//                // Handle exception
//            }
//
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }

//    private fun takePhoto() {
//        val photoFile = File(requireContext().externalMediaDirs.firstOrNull(), "photo.jpg")
//        Log.d("PhotoCapture", "Photo file path: ${photoFile.absolutePath}")
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
//            override fun onError(exception: ImageCaptureException) {
//                // Handle error
//            }
//
//            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                val savedUri = outputFileResults.savedUri ?: return
//                recognizeText(savedUri)
//            }
//        })
//    }

    private fun startDocumentScanner(uri: Uri?) {
        activity?.let {
            scanner.getStartScanIntent(it)
                    .addOnSuccessListener { intentSender ->
                        scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                    .addOnFailureListener {
                        Log.e("DocumentScanner", "Failed to start document scanner", it)
                    }
        }
    }

    private fun recognizeText(uri: Uri) {
        val image: InputImage
        val results = mutableListOf<Float>()
        val patternSum = Pattern.compile(regexSuma)
        val patternData = Pattern.compile(regexData)
        val patternLoc = Pattern.compile(regexLocation)
        try {
            image = InputImage.fromFilePath(requireContext(), uri)
            recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        var recognizedText = ""
                        var sum_case = 0
                        var bottom_aprox = 0
                        for (block in visionText.textBlocks) {
                            //for (line in block.lines)
                                for(line in block.lines) {

                                    val matcherSum = patternSum.matcher(line.text)
                                    val matcherData = patternData.matcher(line.text)
                                    val matcherLoc = patternLoc.matcher(line.text)
                                    if(line.text in enumpayments && sum_case == 0)
                                    {
                                        recognizedText += line.text + "\n"
                                        line.boundingBox.let { box ->
                                            bottom_aprox = box!!.bottom
                                        }
                                        if (matcherSum.find()) {
                                            recognizedText += matcherSum.group() + "\n"
                                            sum_case = 2
                                        }
                                        else
                                            sum_case =1
                                    }
                                    else if(line.boundingBox!!.bottom <= bottom_aprox +100 && line.boundingBox!!.bottom >= bottom_aprox -100 && sum_case==1)
                                        {
                                            if (matcherSum.find()) {
                                                recognizedText += matcherSum.group() + "\n"
                                                sum_case =2
                                            }
                                        }
//                                    while (matcherSum.find()) {
//                                        results.add(matcherSum.group().replace(",",".").toFloat())
//                                    }

                                    if (matcherData.find()) {
                                        recognizedText += matcherData.group() + "\n"
                                    }

                                    if (matcherLoc.find()) {
                                        recognizedText += matcherLoc.group() + "\n"
                                    }
//
//                                    if (elemet.text == "TOTAL") {
//                                        elemet.boundingBox?.let { box ->
//                                            recognizedText += ("[left: ${box.left}, top: ${box.top}, right: ${box.right}, bottom: ${box.bottom}]")
//                                        }
//                                        recognizedText += elemet.text + "\n"
//                                        recognizedText += elemet.boundingBox.toString() + "\n"
//                                        recognizedText += elemet.cornerPoints.toString() + "\n"
//                                        recognizedText += elemet.angle.toString() + "\n"
//                                    }
//                                    if(elemet.text == "7.79" || elemet.text == "150.00 RON")
//                                    {
//                                        elemet.boundingBox?.let { box ->
//                                            recognizedText += ("[left: ${box.left}, top: ${box.top}, right: ${box.right}, bottom: ${box.bottom}]")
//                                        }
//                                        recognizedText += elemet.text + "\n"
//                                        recognizedText += elemet.boundingBox.toString() + "\n"
//                                        recognizedText += elemet.cornerPoints.toString() + "\n"
//                                        recognizedText += elemet.angle.toString() + "\n"
//                                    }
                                }
                        }
//                        if(sum_case!=2)
//                            recognizedText += results.maxOrNull().toString()+ "\n"

                        textPicture.text = recognizedText

                    }
                    .addOnFailureListener { e ->
                        Log.e("TextRecognition", "Text recognition failed", e)
                    }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
//    }
}

