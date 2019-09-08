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
import androidx.fragment.app.Fragment
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.model.Crime

class CrimeFragment: Fragment() {

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
    private lateinit var solvedCheckBox: CheckBox


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
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

    override fun onStart() {
        super.onStart()
        applyAllEventsToViews()
        titleField.addTextChangedListener(titleWatcher)
    }

    private val findAllViews: (View) -> Unit = { view ->
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButoon = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
    }

    private val applyAllEventsToViews = {
        titleField.addTextChangedListener(titleWatcher)
        
        dateButoon.apply {
            text = crime.date.toString()
            isEnabled = false
        }
        
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
    }
}