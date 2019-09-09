package velord.bnrg.criminalintent.view

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "date"

class TimePickerFragment : DialogFragment() {

    interface Callbacks {
        fun onTimeSelected(date: Date)
    }

    private lateinit var dateCrime: Date

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return initDatePickerDialog(initTimeListener)
    }

    private val initTimeListener = {
        TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
            dateCrime.apply {
                hours = i
                minutes = i2
            }
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(date = dateCrime)
            }
        }
    }

    private val initDatePickerDialog:
                (() -> TimePickerDialog.OnTimeSetListener) -> TimePickerDialog = { timeListener ->
        dateCrime = arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = dateCrime
        val initialHour = calendar.get(Calendar.HOUR)
        val initialMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            timeListener(),
            initialHour,
            initialMinute,
            true
        )
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}