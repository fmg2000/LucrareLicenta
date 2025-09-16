package com.example.ocrapp

import MonthYearPickerDialog
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ShowFragment : Fragment(){

    private lateinit var receiptViewModel: ListReceiptViewModel
    private lateinit var receptListwork : List<Receipt>
    private lateinit var textTotalSum : TextView
    private lateinit var textDate: TextView
    private lateinit var toggleButtonGroup : MaterialButtonToggleGroup
    private val calendar = Calendar.getInstance()
    private lateinit var lineChart: LineChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterTopList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiptViewModel = ViewModelProvider(requireActivity()).get(ListReceiptViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textDate = view.findViewById(R.id.textDate)
        textTotalSum = view.findViewById(R.id.TotalSum)
        toggleButtonGroup = view.findViewById(R.id.toggleButtonGroup)
        lineChart = view.findViewById(R.id.lineChart)


        //RecyclerView
        recyclerView = view.findViewById(R.id.listTop5Expenses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        adapter = AdapterTopList(emptyList())
        recyclerView.adapter = adapter


        // format date and get / set date
        val selectdate = Calendar.getInstance().getTime();
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        val date = dateFormat.format(selectdate)
        textDate.text = date

        // Observe receipt list changes
        Log.d("aici", "salut")
        receiptViewModel.receiptList.observe(viewLifecycleOwner, Observer { storesList ->
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            filterReceiptsByGetDateAndMonth(year, month)
        })

        toggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

            if (checkedId == R.id.selectYear) {
                if(isChecked)
                    showYearPicker()
                else
                    clearFilster()

            }
            if(checkedId == R.id.selectMonth){
                if(isChecked)
                    showMonthYearPicker()
                else
                    clearFilster()

            }
            if (checkedId == R.id.selectDate) {
                if(isChecked)
                    showDatePicker()
                else
                    clearFilster()

            }
            if (checkedId == R.id.selectOther) {
                if(isChecked)
                    showDateRangePicker()
                else
                    clearFilster()

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
                val dateFormat = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                textDate.text = "$formattedDate"
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

    private fun filterReceiptsByDayMonthYear(year: Int, month: Int , day: Int) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Ajustează formatul după cum e necesar
        val filteredList = receiptViewModel.receiptList.value?.filter {
            val date = dateFormat.parse(it.date)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.DAY_OF_MONTH) == day
        }

        if (filteredList != null) {
            for (i in filteredList)
            {
                Log.d("Salut", i.toString())
            }
        }
        receptListwork = filteredList ?: emptyList()

        // Calculează suma totală a prețurilor
        val totalSum = receptListwork.sumOf { it.sum!!.toDouble() }

        // Afișează suma totală
        textTotalSum.text = "TOTAL: ${String.format(Locale.ENGLISH,"%.2f", totalSum)}"
        updateLineChart(receptListwork)

        val top5Receipts = receptListwork.sortedByDescending { it.sum }.take(5)
        adapter.updateList(top5Receipts)
    }

    private fun showMonthYearPicker() {
        val dialog = MonthYearPickerDialog()
        dialog.setOnDateSetListener { year, month ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month - 1, 1)
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
            val formattedDate = dateFormat.format(selectedDate.time)
            textDate.text = "$formattedDate"
            filterReceiptsByGetDateAndMonth(year, month)
        }
        dialog.setOnCancelListener {
            // Deselecteaza toate butoanele din ToggleGroup
            toggleButtonGroup.clearChecked()
        }
        dialog.show(childFragmentManager, "MonthYearPickerDialog")
    }

    private fun filterReceiptsByGetDateAndMonth(year: Int, month: Int) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val filteredList = receiptViewModel.receiptList.value?.filter {
            val date = dateFormat.parse(it.date)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) + 1 == month
        }
        receptListwork = filteredList ?: emptyList()

        // Calculează suma totală a prețurilor
        val totalSum = receptListwork.sumOf { it.sum!!.toDouble() }

        // Afișează suma totală
        textTotalSum.text = "TOTAL: ${String.format(Locale.ENGLISH,"%.2f", totalSum)}"
        updateLineChart(receptListwork)
        val top5Receipts = receptListwork.sortedByDescending { it.sum }.take(5)
        adapter.updateList(top5Receipts)

    }

    private fun showYearPicker() {
        val dialog = YearPickerDialog()
        dialog.setOnDateSetListener { year ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year,Calendar.JANUARY, 1 )
            val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            textDate.text = "Year: $formattedDate"
            filterReceiptsByYear(year)
        }
        dialog.setOnCancelListener {
            // Deselecteaza toate butoanele din ToggleGroup
            toggleButtonGroup.clearChecked()
        }
        dialog.show(childFragmentManager, "YearPickerDialog")
    }

    private fun filterReceiptsByYear(year: Int) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val filteredList = receiptViewModel.receiptList.value?.filter {
            val date = dateFormat.parse(it.date)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.YEAR) == year
        }
        receptListwork = filteredList ?: emptyList()

        // Calculează suma totală a prețurilor
        val totalSum = receptListwork.sumOf { it.sum!!.toDouble() }

        // Afișează suma totală
        textTotalSum.text = "TOTAL: ${String.format(Locale.ENGLISH,"%.2f", totalSum)}"
        updateLineChart(receptListwork)

        val top5Receipts = receptListwork.sortedByDescending { it.sum }.take(5)
        adapter.updateList(top5Receipts)
    }

    private fun showDateRangePicker() {
        val calendarStart = Calendar.getInstance()
        val calendarEnd = Calendar.getInstance()

        // DatePickerDialog pentru data de început
        val startDatePickerDialog = context?.let {
            DatePickerDialog(
                    it, { _, startYear, startMonth, startDay ->
                calendarStart.set(startYear, startMonth, startDay)
                // DatePickerDialog pentru data de sfârșit
                val endDatePickerDialog = DatePickerDialog(
                        it, { _, endYear, endMonth, endDay ->
                    calendarEnd.set(endYear, endMonth, endDay)
                    // Formatează și afișează intervalul de date selectat
                    val dateFormat = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                    val startDate = dateFormat.format(calendarStart.time)
                    val endDate = dateFormat.format(calendarEnd.time)
                    textDate.text = "From: $startDate To: $endDate"
                    // Filtrează chitanțele pe baza intervalului de date selectat
                    filterReceiptsByDateRange(calendarStart, calendarEnd)
                },
                        calendarEnd.get(Calendar.YEAR),
                        calendarEnd.get(Calendar.MONTH),
                        calendarEnd.get(Calendar.DAY_OF_MONTH)
                )
                endDatePickerDialog.setOnCancelListener {
                    toggleButtonGroup.clearChecked()
                }
                endDatePickerDialog.show()
            },
                    calendarStart.get(Calendar.YEAR),
                    calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH)
            )
        }
        startDatePickerDialog?.setOnCancelListener {
            toggleButtonGroup.clearChecked()
        }
        startDatePickerDialog?.show()
    }

    private fun filterReceiptsByDateRange(startCalendar: Calendar, endCalendar: Calendar) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val filteredList = receiptViewModel.receiptList.value?.filter {
            val date = dateFormat.parse(it.date)
            date != null && !date.before(startCalendar.time) && !date.after(endCalendar.time)
        }
        receptListwork = filteredList ?: emptyList()

        // Calculează suma totală a prețurilor
        val totalSum = receptListwork.sumOf { it.sum!!.toDouble() }

        // Afișează suma totală
        textTotalSum.text = "TOTAL: ${String.format(Locale.ENGLISH,"%.2f", totalSum)}"
        updateLineChart(receptListwork)
        val top5Receipts = receptListwork.sortedByDescending { it.sum }.take(5)
        adapter.updateList(top5Receipts)
    }


    private fun clearFilster()
    {
        toggleButtonGroup.clearChecked()
        val selectdate = Calendar.getInstance().getTime();
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        val date = dateFormat.format(selectdate)
        textDate.text = date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        filterReceiptsByGetDateAndMonth(year, month)
    }


    private fun updateLineChart(receipts: List<Receipt>) {
        val sortedReceipts = receipts.sortedBy { receipt ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateFormat.parse(receipt.date)
        }
        val linevalues = ArrayList<Entry>()
        val dates = ArrayList<String>()

        sortedReceipts.forEach { receipt ->
            Log.d("salut", receipt.date.toString())
            val price = receipt.sum?.toFloat() ?: 0f
            linevalues.add(Entry(linevalues.size.toFloat(), price))
            dates.add(receipt.date ?: "Nedeterminat") // Adaugăm data la lista de date
        }

        val linedataset = LineDataSet(linevalues, "Receipts")

        // Dezactivăm afișarea axei X și etichetele acesteia
        lineChart.xAxis.isEnabled = false

        // Adăugăm restul funcționalităților graficului
        linedataset.color = resources.getColor(R.color.black_menu)
        linedataset.setCircleColor(resources.getColor(R.color.black_menu))
        linedataset.circleRadius = 7f
        linedataset.setDrawFilled(true)
        linedataset.valueTextSize = 0F
        linedataset.fillColor = resources.getColor(R.color.blue_backound)
        linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER)

        // Conectăm datele la ecranul UI
        val data = LineData(linedataset)
        lineChart.data = data
        lineChart.animateXY(1000, 2000, Easing.EaseInCubic)
        lineChart.invalidate()

        // Adăugăm un listener pentru clicuri pe puncte
        //Această metodă setează un obiect de tip OnChartValueSelectedListener care ascultă evenimentele de selectare a unei valori în grafic.
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            //Parametrul e reprezintă intrarea (punctul) selectat de către utilizator. Un obiect Entry conține informații despre coordonatele (x, y) ale punctului.
            //h: Highlight?: Parametrul h conține informații suplimentare despre punctul selectat
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    val index = e.x.toInt()
                    val date = dates.getOrNull(index) ?: "Nedeterminat"
                    val price = e.y
                    showPointDialog(date, price) // Afișăm dialogul cu data și prețul atunci când un punct este apăsat
                }
            }

            override fun onNothingSelected() {
                // Implementează acțiuni suplimentare pentru cazul în care nu este selectat niciun punct, dacă este necesar
            }
        })
    }

    private fun showPointDialog(date: String, price: Float) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Information Receipt")
        alertDialogBuilder.setMessage("Data: $date\nPreț: $price")
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


//    override fun onClick(v: View?) {
//
//        if (v != null) {
//            when (v.id) {
//                R.id.Sum -> goBunttonShow()
//            }
//        }
//    }
//    fun goBunttonShow()
//    {
//        textDate.text =text
//    }
}
