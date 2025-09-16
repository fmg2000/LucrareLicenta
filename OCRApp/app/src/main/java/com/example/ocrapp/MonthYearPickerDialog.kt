import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.ocrapp.R
import java.util.Calendar

class MonthYearPickerDialog : DialogFragment() {

    private var onDateSetListener: ((year: Int, month: Int) -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null


    fun setOnDateSetListener(listener: (year: Int, month: Int) -> Unit) {
        onDateSetListener = listener
    }
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_month_year_picker)
        dialog.setTitle("Select Month and Year")

        val monthPicker: NumberPicker = dialog.findViewById(R.id.month_picker)
        val yearPicker: NumberPicker = dialog.findViewById(R.id.year_picker)
        val btnSet: Button = dialog.findViewById(R.id.btn_confirm)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)

        val months = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        monthPicker.minValue = 0
        monthPicker.maxValue = months.size - 1
        monthPicker.displayedValues = months

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear

        btnSet.setOnClickListener {
            onDateSetListener?.invoke(yearPicker.value, monthPicker.value + 1)
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
