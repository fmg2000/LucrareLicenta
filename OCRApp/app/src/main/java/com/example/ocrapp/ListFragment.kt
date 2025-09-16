package com.example.ocrapp

import MonthYearPickerDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListFragment : Fragment(), AdabterReceipt.OnItemClickListener{

    private val calendar = Calendar.getInstance()
    private lateinit var searchView : SearchView
    private lateinit var receiptViewModel: ListReceiptViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdabterReceipt
    private var mAuth: FirebaseAuth? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var myref: DatabaseReference
    private var  storage = Firebase.storage
    private var storageRef = storage.reference
    private lateinit var toggleButtonGroup : MaterialButtonToggleGroup
    private lateinit var textFilter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiptViewModel = ViewModelProvider(requireActivity()).get(ListReceiptViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        toggleButtonGroup = view.findViewById(R.id.toggleButtonGroup)
        textFilter = view.findViewById(R.id.textFilter)
        // search View init
        searchView = view.findViewById(R.id.searchView)

        //Firebase
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myref = database.getReference()

        //RecyclerView
         recyclerView = view.findViewById(R.id.listrecyclerView)
        //este responsabil pentru aranjarea item-urilor într-o listă.Fără un LayoutManager, RecyclerView nu știe cum să aranjeze item-urile
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //Fără această setare, RecyclerView va trebui să recalculeze dimensiunile și layout-ul de fiecare dată când un item este adăugat sau eliminat, ceea ce poate reduce performanța.
        recyclerView.setHasFixedSize(true)

        adapter = AdabterReceipt(emptyList(), this)
        recyclerView.adapter = adapter

        receiptViewModel.receiptList.observe(viewLifecycleOwner) { receiptList ->
            adapter.updateList(receiptList)
        }

        //tooglebutton click
        toggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

            if (checkedId == R.id.selectMonth) {
                if(isChecked)
                    showMonthYearPicker()
                else
                    clearFilster()

            }
            if(checkedId == R.id.selectDate){
                if(isChecked)
                    showDatePicker()
                else
                    clearFilster()

            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterReceipts(newText)
                return true
            }
        })
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
                val dateFormat = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                textFilter.text = "Date: $formattedDate"
                filterReceiptsByDayMonthYear(year, monthOfYear, dayOfMonth)
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        datePickerDialog?.setOnCancelListener {
            // Deselecteaza toate butoanele din ToggleGroup
            toggleButtonGroup.clearChecked()
        }
        // Show the DatePicker dialog
        datePickerDialog?.show()
    }
    private fun showMonthYearPicker() {
        val dialog = MonthYearPickerDialog()
        dialog.setOnDateSetListener { year, month ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month - 1, 1)
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val formattedDate = dateFormat.format(selectedDate.time)
            textFilter.text = "Date : $formattedDate"
            filterReceiptsByMonthYear(year, month)
        }
        dialog.setOnCancelListener {
            // Deselecteaza toate butoanele din ToggleGroup
            toggleButtonGroup.clearChecked()
        }
        dialog.show(childFragmentManager, "MonthYearPickerDialog")
    }

    private fun filterReceiptsByDayMonthYear(year: Int, month: Int , day: Int) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Ajustează formatul după cum e necesar
        val filteredList = receiptViewModel.receiptList.value?.filter {
            val date = dateFormat.parse(it.date)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.DAY_OF_MONTH) == day
        }
        adapter.updateList(filteredList ?: emptyList())
    }

    private fun filterReceiptsByMonthYear(year: Int, month: Int) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Ajustează formatul după cum e necesar
        val filteredList = receiptViewModel.receiptList.value?.filter {
            val date = dateFormat.parse(it.date)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) + 1 == month
        }
        adapter.updateList(filteredList ?: emptyList())
    }


    private fun filterReceipts(query: String?) {
        val filteredList = receiptViewModel.receiptList.value?.filter {
            it.sum.toString().contains(query ?: "", ignoreCase = true) || it.date!!.contains(query ?: "", ignoreCase = true) || it.location!!.contains(query ?: "", ignoreCase = true)
        }
        adapter.updateList(filteredList ?: emptyList())
    }

    private fun clearFilster()
    {
        toggleButtonGroup.clearChecked()
        textFilter.setText("Filtered by Date")
        receiptViewModel.receiptList.value?.let { adapter.updateList(it) }
    }

    override fun onItemClick(receipt: Receipt) {
        val detailFragment = ReadFragment.newInstance(receipt)
        requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, detailFragment)
                    .addToBackStack(null)
                    .commit()

    }

    override fun onDeleteClick(receipt: Receipt) {

        val currentUser = mAuth?.currentUser
        val receiptId = receipt.id
        var receiptimgName = receipt.imgName


        if (currentUser != null && receiptId != null) {
            myref.child(currentUser.uid).child(receiptId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    receiptViewModel.deleteStore(receipt)
                } else {
                    Log.e("FirebaseDelete", "Failed to delete receipt from Firebase", task.exception)
                }
            }
            if(receiptimgName != null)
            {
                val desertRef = mAuth!!.currentUser?.uid?.let { storageRef.child("images/$it/${receipt.imgName}") }!!
                desertRef.delete()
            }
        } else {
            Log.e("FirebaseDelete", "Current user or receipt ID is null")
        }

    }
}