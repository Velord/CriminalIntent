package velord.bnrg.criminalintent.viewModel

import androidx.lifecycle.ViewModel
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.repository.CrimeRepository

class MainViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()

    fun addCrime(crime: Crime) = crimeRepository.addCrime(crime)
}