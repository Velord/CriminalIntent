package velord.bnrg.criminalintent.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import velord.bnrg.criminalintent.R
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCrimeListFragment()
    }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG, "Main Activity.onCrimeSelected $crimeId")
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
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
