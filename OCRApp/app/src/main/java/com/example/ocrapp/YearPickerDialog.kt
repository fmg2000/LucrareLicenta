package com.example.ocrapp


import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class YearPickerDialog  : DialogFragment(){
    private var onDateSetListener: ((year: Int) -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null


    fun setOnDateSetListener(listener: (year: Int) -> Unit) {
        onDateSetListener = listener
    }
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_year_picker)
        dialog.setTitle("Select Year")

        val yearPicker: NumberPicker = dialog.findViewById(R.id.year_picker)
        val btnSet: Button = dialog.findViewById(R.id.btn_confirm)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)


        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear

        btnSet.setOnClickListener {
            onDateSetListener?.invoke(yearPicker.value)
            dismiss()
        }

        btnCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}