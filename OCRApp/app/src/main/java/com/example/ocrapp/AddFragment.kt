package com.example.ocrapp

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class AddFragment : Fragment(),View.OnClickListener{

    enum class Payment(val pay: String) {
        TOTAL("TOTAL"),
        TOTAL_2("TOTAL:"),
        TOTAL_LEI("TOTAL LEI"),
        TOTAL_PLATA("TOTAL PLATA"),
        TOTAL_DE_PLATA("TOTAL DE PLATA"),
        SUMA("SUMA")
    }

    private val calendar = Calendar.getInstance()
    private var regexSuma = """\d+[.,]\d{1,2}"""
    private var regexData = """\b(?:0?[1-9]|[12][0-9]|3[01])\s*[,./-]\s*(?:0?[1-9]|1[0-2])\s*[,./-]?\s*\d{4}\b"""
    private var regexLocation =  """(?:S[.,]?\s*C[.,]?\s*)?.*?\s*S[.,]?\s*R[.,]?\s*L[.,]?"""
    private var enumpayments = Payment.entries.map { it.pay }
    private var  storage = Firebase.storage
    private var storageRef = storage.reference
    private lateinit var receiptViewModel: ListReceiptViewModel
    private var mAuth: FirebaseAuth? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var myref: DatabaseReference
    private lateinit var pushedKey: DatabaseReference
    private lateinit var sum:EditText
    private lateinit var date:EditText
    private lateinit var loc:EditText
    private lateinit var imageView:ImageView
    private var uri: Uri? = null
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(2)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

    private var scanner = GmsDocumentScanning.getClient(options)
    private var scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data!!)
            if (scanningResult != null) {
                uri = scanningResult.pages?.get(0)?.imageUri!!
                if (uri != null) {
                    imageView.setImageURI(uri)
                    recognizeText(uri!!)
//                    var mAuth = FirebaseAuth.getInstance()
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiptViewModel =ViewModelProvider(requireActivity()).get(ListReceiptViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myref = database.getReference()


        ////
        sum = view.findViewById(R.id.sumEditText)
        date = view.findViewById(R.id.dateEditText)
        loc = view.findViewById(R.id.locationEditText)
        imageView = view.findViewById(R.id.imageView)


        view.findViewById<View>(R.id.btnAdd).setOnClickListener(this)
        view.findViewById<View>(R.id.buttonCapture).setOnClickListener(this)
        view.findViewById<View>(R.id.selectDate).setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if(v!=null) {
            if (v.id == R.id.btnAdd) {
                if (sum.text.toString().isNotEmpty() && date.text.toString().isNotEmpty() && loc.text.toString().isNotEmpty()) {
                    val isNumber = sum.text.toString().matches("""\d+[.]\d{1,2}""".toRegex())
                    if(isNumber)
                    {
                        val isDate = date.text.toString().matches("""\b(?:0?[1-9]|[12][0-9]|3[01])[.](?:0?[1-9]|1[0-2])[.]\d{4}\b""".toRegex())
                        if(isDate)
                        {
                            addFireStorepreaperDatabse(uri)
                        }
                        else
                            Toast.makeText(requireContext(), "Date format invalid: DD.MM.YYYY", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "TOTAL invalid or missing .00", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                    Toast.makeText(requireContext(), "Content empty", Toast.LENGTH_SHORT).show()
            }
            if (v.id == R.id.buttonCapture)
            {
                imageView.setImageURI(null)
                startDocumentScanner(null)
            }

            if (v.id == R.id.selectDate)
            {
                showDatePicker()
            }

        }
    }

    private fun showDatePicker() {
        // Create a DatePickerDialog
        val datePickerDialog = context?.let {
            DatePickerDialog(
                    it, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the data
                date.setText("$formattedDate")
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        // Show the DatePicker dialog
        datePickerDialog?.show()
    }


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
        val patternSum = Pattern.compile(regexSuma)
        val patternData = Pattern.compile(regexData)
        val patternLoc = Pattern.compile(regexLocation)
        try {
            image = InputImage.fromFilePath(requireContext(), uri)
            recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        var sum_case = 0
                        var bottom_aprox = 0
                        for (block in visionText.textBlocks) {

                            for(line in block.lines) {

                                Log.d("Line", line.text)
                                Log.d("line bottoom" , line.boundingBox.let { box -> box!!.bottom }.toString())

                                val matcherSum = patternSum.matcher(line.text)
                                val matcherData = patternData.matcher(line.text)
                                val matcherLoc = patternLoc.matcher(line.text)
                                if(line.text in enumpayments && sum_case == 0)
                                {
                                    line.boundingBox.let { box ->
                                        bottom_aprox = box!!.bottom
                                        Log.d("Aici" ,bottom_aprox.toString())
                                    }
                                    if (matcherSum.find()) {
                                        sum.setText(matcherSum.group().replace(",","."))
                                        sum_case = 2
                                    }
                                    else
                                        sum_case =1
                                }
                                else if(line.boundingBox!!.bottom <= bottom_aprox +100 && line.boundingBox!!.bottom >= bottom_aprox -100 && sum_case==1)
                                {
                                    if (matcherSum.find()) {
                                        sum.setText(matcherSum.group().replace(",","."))
                                        sum_case =2
                                    }
                                }

                                if (matcherData.find()) {
                                    date.setText(matcherData.group().replace(" ","").replace("-",".").replace("/",".").replace(",","."))
                                }

                                if (matcherLoc.find()) {
                                    loc.setText(matcherLoc.group().replace(",","."))
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TextRecognition", "Text recognition failed", e)
                    }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun addFireStorepreaperDatabse(uri: Uri?)
    {
        if (uri !=null) {
            var imageRef: StorageReference? = mAuth!!.currentUser?.uid?.let { storageRef.child("images/$it/${System.currentTimeMillis()}.jpg") }!!
            var imgName = imageRef?.name
            imageRef?.putFile(uri)?.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("ULR", downloadUri.toString())
                    Glide.with(this)
                            .load(downloadUri)
                            .preload()  // incarca imageaniea in memoria cache
                    val addReceipt = AddReceipt(loc.text.toString(), sum.text.toString().toFloat(), date.text.toString(), downloadUri.toString(),imgName)
                    addDataBase(addReceipt)
                }
            }?.addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Failed to upload image", e)
            }
        }
        else
        {
            val addReceipt = AddReceipt(loc.text.toString(), sum.text.toString().toFloat(), date.text.toString(), null, null)
            addDataBase(addReceipt)
        }

    }

    fun addDataBase(addstore: AddReceipt)
    {
        pushedKey = mAuth!!.currentUser?.uid?.let { myref.child(it).push() }!!
        receiptViewModel.addStore(Receipt(pushedKey.key.toString(), addstore.location.toString(), addstore.sum.toString().toFloat(),addstore.date.toString(),addstore.img.toString(), addstore.imgName))
        pushedKey.setValue(addstore)
        Toast.makeText(requireContext(), "Done!", Toast.LENGTH_SHORT).show()
        sum.setText("")
        date.setText("")
        loc.setText("")
        uri = null
        imageView.setImageURI(uri)


    }

}
