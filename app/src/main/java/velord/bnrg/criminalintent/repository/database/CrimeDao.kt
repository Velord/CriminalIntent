package velord.bnrg.criminalintent.repository.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import velord.bnrg.criminalintent.model.Crime
import java.util.*

@Dao
interface CrimeDao {

    @Query("Select * From crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("Select * From crime Where id=(:id)")
    fun getCrimeById(id: UUID): LiveData<Crime?>
}