package velord.bnrg.criminalintent.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.viewModel.MainViewModel
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {

    private lateinit var noOneCrimeTextView: TextView
    private lateinit var newCrimeButton: Button

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCrimeListFragment()
    }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG, "Main Activity.onCrimeSelected $crimeId")
        val fragment = CrimeFragment.newInstance(crimeId)
        replaceFragment(fragment)
    }

    override fun onNoOneCrime() {
        Log.d(TAG, "Main Activity.onNoOneCrime")
        findViews()
        initViewsVisibility()
        initViewsEvent()
    }

    override fun hide() {
        findViews()
        disableViewsVisibility()
    }


    private val replaceFragment: (Fragment) -> Unit = {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, it)
            .addToBackStack(null)
            .commit()
    }

    private val initViewsEvent = {
        newCrimeButton.setOnClickListener {
            disableViewsVisibility()
            val crime = Crime()
            mainViewModel.addCrime(crime)
            val fragment = CrimeFragment.newInstance(crime.id)
            replaceFragment(fragment)
        }
    }

    private val initViewsVisibility = {
        noOneCrimeTextView.visibility = View.VISIBLE
        newCrimeButton.visibility = View.VISIBLE
    }

    private val disableViewsVisibility = {
        noOneCrimeTextView.visibility = View.GONE
        newCrimeButton.visibility = View.GONE
    }

    private val findViews = {
        noOneCrimeTextView = findViewById(R.id.no_crimes) as TextView
        newCrimeButton = findViewById(R.id.new_crime) as Button
    }

    private val initCrimeListFragment = {
        initFragment(CrimeListFragment(), R.id.fragment_container)
    }

    private val initCrimeFragment = {
        initFragment(CrimeFragment(), R.id.fragment_container)
    }

    private val initFragment: (Fragment, Int) -> Unit = { fragment, containerId ->
        val currentFragment =
            supportFragmentManager.findFragmentById(containerId)

        if (currentFragment == null)
            addFragment(fragment, containerId)
    }

    private val addFragment: (Fragment, Int) -> Unit = { fragment, containerId ->
        supportFragmentManager
            .beginTransaction()
            .add(containerId, fragment)
            .commit()
    }
}
