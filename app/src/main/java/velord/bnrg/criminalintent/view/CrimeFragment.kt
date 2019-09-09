package velord.bnrg.criminalintent.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.viewModel.CrimeDetailViewModel
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_CODE = 0

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    //The values in  this crime property represent
    // the edits the user is  currently making
    private lateinit var crime: Crime

    private lateinit var titleField: EditText
    private val titleWatcher = object : TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            crime.title = p0.toString()
        }

        override fun afterTextChanged(p0: Editable?) {

        }
    }

    private lateinit var dateButoon: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        loadCrimeFromDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        findAllViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        applyAllEventsToViews()
        titleField.addTextChangedListener(titleWatcher)
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButoon.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
    }

    private val loadCrimeFromDatabase: () -> Unit = {
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    private val findAllViews: (View) -> Unit = { view ->
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButoon = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
    }

    private val applyAllEventsToViews = {
        titleField.addTextChangedListener(titleWatcher)

        dateButoon.setOnClickListener {
            openDialogFragment(DatePickerFragment.newInstance(crime.date), DIALOG_DATE)
        }

        timeButton.setOnClickListener {
            openDialogFragment(TimePickerFragment.newInstance(crime.date), DIALOG_TIME)
        }

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
    }

    private val openDialogFragment: (DialogFragment, String) -> Unit = { dialogFragment, dialogArg ->
        dialogFragment.apply {
            setTargetFragment(this@CrimeFragment, REQUEST_CODE)
            show(this@CrimeFragment.requireFragmentManager(), dialogArg)
        }
    }

    companion object {
        val newInstance: (UUID) -> CrimeFragment = { crimeId ->
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            CrimeFragment().apply {
                arguments = args
            }
        }
    }
}