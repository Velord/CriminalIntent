package velord.bnrg.criminalintent.viewModel

import androidx.lifecycle.ViewModel
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.repository.CrimeRepository

class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) = crimeRepository.addCrime(crime)

    tailrec fun initCrimesFake(n: Int, acc: MutableList<Crime>
    ): MutableList<Crime> = when {
        n == 0 -> acc
        else -> {
            val crime = Crime().apply {
                title = "Crime #$n"
                isSolved = n % 2 == 0
                requiresPolice = n % 3 == 0
            }
            acc += crime
            initCrimesFake(n - 1, acc)
        }
    }
}