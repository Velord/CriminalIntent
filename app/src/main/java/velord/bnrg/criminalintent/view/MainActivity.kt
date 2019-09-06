package velord.bnrg.criminalintent.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import velord.bnrg.criminalintent.CrimeFragment
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.viewModel.CrimeListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCrimeListFragment()
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
