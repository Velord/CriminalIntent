package velord.bnrg.criminalintent.repository.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import velord.bnrg.criminalintent.model.Crime
import java.util.*

@Dao
interface CrimeDao {

    @Query("Select * From crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("Select * From crime Where id=(:id)")
    fun getCrimeById(id: UUID): LiveData<Crime?>

    @Insert
    fun insertCrime(crime: Crime)

    @Insert
    fun insertCrimes(crimes: List<Crime>)

    @Update
    fun updateCrime(crime: Crime)
}