package velord.bnrg.criminalintent.viewModel

import androidx.lifecycle.ViewModel
import velord.bnrg.criminalintent.model.Crime

class CrimeListViewModel:ViewModel() {

    val crimes = mutableListOf<Crime>()

    init {
        initCrimes(100, crimes).reverse()
    }


    private tailrec fun initCrimes(n: Int, acc: MutableList<Crime>
    ): MutableList<Crime> = when {
        n == 0 -> acc
        else -> {
            val crime = Crime().apply {
                title = "Crime #$n"
                isSolved = n % 2 == 0
                requiresPolice = n % 3 == 0
            }
            acc += crime
            initCrimes(n - 1, acc)
        }
    }
}