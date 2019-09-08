package velord.bnrg.criminalintent.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.repository.database.CrimeDatabase
import java.util.*

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).fallbackToDestructiveMigration()
        .build()


    private val crimeDao = database.crimeDao()

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrimeById(id: UUID): LiveData<Crime?> = crimeDao.getCrimeById(id)

    companion object {

        private var INSTANCE: CrimeRepository? =  null

        fun initialize(context: Context) {
            if (INSTANCE == null) INSTANCE = CrimeRepository(context)
        }

        fun get(): CrimeRepository {
            return INSTANCE ?:
                    throw  IllegalStateException("CrimeRepository must be initialized")
        }
    }
}