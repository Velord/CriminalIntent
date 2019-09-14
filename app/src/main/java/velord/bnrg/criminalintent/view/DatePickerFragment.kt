package velord.bnrg.criminalintent.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment: DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date: Date)
    }

    private lateinit var dateCrime: Date

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return initDatePickerDialog(initDateListener)
    }

    private val initDateListener = {
        DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date =
                GregorianCalendar(year, month, day).time
                    .apply {
                hours = dateCrime.hours
                minutes = dateCrime.minutes
            }

            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }
    }

    private val initDatePickerDialog:
                (() -> DatePickerDialog.OnDateSetListener) -> DatePickerDialog = { dateListener ->
        dateCrime = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = dateCrime
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val inititalDay = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            dateListener(),
            initialYear,
            initialMonth,
            inititalDay
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args =  Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}