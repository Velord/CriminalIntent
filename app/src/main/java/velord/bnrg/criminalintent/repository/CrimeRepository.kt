package velord.bnrg.criminalintent.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.repository.database.CrimeDatabase
import velord.bnrg.criminalintent.repository.database.migration_2_3
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME)
        .addMigrations(migration_2_3)
        .build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrimeById(id: UUID): LiveData<Crime?> = crimeDao.getCrimeById(id)

    fun insertCrime(crime: Crime) =
        GlobalScope.launch {  crimeDao.insertCrime(crime) }

    fun insertCrimes(crime: List<Crime>) =
        GlobalScope.launch { crimeDao.insertCrimes(crime) }

    fun updateCrime(crime: Crime) = executor.execute {
        crimeDao.updateCrime(crime)
    }

    fun addCrimeViaExecutor(crime: Crime) = executor.execute {
        crimeDao.insertCrime(crime)
    }

    fun addCrime(crime: Crime) = GlobalScope.launch {
        crimeDao.insertCrime(crime)
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

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