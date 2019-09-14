package velord.bnrg.criminalintent.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.model.Crime
import velord.bnrg.criminalintent.utils.doOnGlobalLayout
import velord.bnrg.criminalintent.utils.parseLocalDateTime
import velord.bnrg.criminalintent.utils.updatePhotoView
import velord.bnrg.criminalintent.viewModel.CrimeDetailViewModel
import java.io.File
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_CODE = 0
private const val REQUEST_DATE = 1
private const val REQUEST_CONTACT = 2
private const val PERMISSIONS_REQUEST_READ_CONTACTS = 3
private const val REQUEST_PHOTO = 4
private const val DATE_FORMAT = "EEE, MMM, dd, yyyy"
private const val TIME_FORMAT = "HH : mm"
private const val PHOTO_SUCCESS_ADDED_DURATION_IN_MILLIS = 1000L

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    interface Callbacks {
        fun onCrimePhotoPressed(file: File)
    }
    private var callbacks: Callbacks? = null
    //The values in  this crime property represent
    // the edits the user is  currently making
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    private lateinit var titleField: EditText
    private val titleWatcher = object : TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            crime.title = p0.toString()
        }

        override fun afterTextChanged(p0: Editable?) {

        }
    }

    private lateinit var dateButoon: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var photoView: ImageView
    private lateinit var cameraButton: ImageButton

    private var photoViewWidth: Int? = null
    private var photoViewHeight: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        loadCrimeFromDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        findAllViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCrimeLiveData()
    }

    override fun onStart() {
        super.onStart()
        applyAllEventsToViews()
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> {
                when {
                    requestCode == REQUEST_PHOTO && data == null ->
                        talkBackOfMakePhotoResult(
                            getString(R.string.photo_unsuccessfully_added))
                    else -> return
                }
            }
            resultCode == Activity.RESULT_OK -> {
                when {
                    requestCode == REQUEST_CONTACT && data != null ->
                        requestContactSuccess(data)
                    requestCode == REQUEST_PHOTO ->
                        requestPhotoSuccess()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getContactPhone(crime.suspect)
            else
                Toast.makeText(context,
                    getString(R.string.permission_request_read_contacts_disabled),
                    Toast.LENGTH_LONG).show()

        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        callbacks = null
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun talkBackOfMakePhotoResult(pronounce: String) {
        //add similar experience via TalkBack by announcing
        // what happened as a result of the camera app closing.
        view?.apply {
            postDelayed(Runnable {
                announceForAccessibility(pronounce)
            }, PHOTO_SUCCESS_ADDED_DURATION_IN_MILLIS)
        }
    }

    private fun requestPhotoSuccess() {
        //remove permission for another apps
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        talkBackOfMakePhotoResult(getString(R.string.photo_success_added))
        updatePhotoView()
    }

    private fun requestContactSuccess(data: Intent?) {
        val contactUri: Uri? =  data!!.data
        // Specify which fields you want your query to return values for
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        // Perform your query - the contactUri is like a "where" clause here
        val cursor = requireActivity().contentResolver
            .query(contactUri!!, queryFields, null, null, null)
        cursor?.use {
            // Verify cursor contains at least one result
            if (it.count == 0)
                return
            // Pull out the first column of the first row of data -
            // that is your suspect's name
            it.moveToFirst()
            val suspect = it.getString(0)
            crime.suspect = suspect
            crimeDetailViewModel.saveCrime(crime)
            suspectButton.text = suspect
        }
    }

    private fun updatePhotoView(width: Int? = photoViewWidth,
                                height: Int? = photoViewHeight) {
        GlobalScope.launch {
            if (::photoFile.isInitialized)
                updatePhotoView(photoView, photoFile, width, height)
        }
    }

    private fun observeCrimeLiveData() {
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "velord.bnrg.criminalintent.fileProvider",
                        photoFile)
                    updateUI()
                }
            }
        )
    }

    private fun getContactPhone(suspectName: String) {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

        val  cursor = requireActivity().contentResolver
            .query(uri,
                projection,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                null,null)

        cursor.use {
            cursor!!.moveToFirst();
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == suspectName)
                    makeCall(cursor.getString(0))
            }
        }
    }

    private fun checkRuntimeReadContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission( context!!,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS)
            //callback onRequestPermissionsResult
        } else // if permission is granted
           getContactPhone(crime.suspect)
    }

    private fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.setData(Uri.parse("tel:$phone"))
        startActivity(intent)
    }

    private fun updateUI() {
        titleField.setText(crime.title)

        val dateForDateButton = parseLocalDateTime(
            crime.date,
            DATE_FORMAT
        )
        val dateForTimeButton = parseLocalDateTime(
            crime.date,
            TIME_FORMAT
        )
        dateButoon.text = dateForDateButton
        timeButton.text = dateForTimeButton

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
            callButton.isEnabled = true
        } else
            callButton.isEnabled = false

        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved)
            getString(R.string.crime_report_solved)
        else getString(R.string.crime_report_unsolved)

        val dateString = parseLocalDateTime(
            crime.date,
            DATE_FORMAT
        )
        val suspect = if (crime.suspect.isBlank())
            getString(R.string.crime_report_no_suspect)
        else
            getString(R.string.crime_report_suspect, crime.suspect)

        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    private val loadCrimeFromDatabase: () -> Unit = {
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    private fun findAllViews(view: View) {
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButoon = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callButton = view.findViewById(R.id.crime_call) as Button
        photoView =  view.findViewById(R.id.crime_photo) as ImageView
        cameraButton = view.findViewById(R.id.crime_camera) as ImageButton
    }

    private fun applyAllEventsToViews() {
        titleField.addTextChangedListener(titleWatcher)
        dateButoon.setOnClickListener {
            openDialogFragment(DatePickerFragment.newInstance(crime.date), DIALOG_DATE)
        }
        timeButton.setOnClickListener {
            openDialogFragment(TimePickerFragment.newInstance(crime.date), DIALOG_TIME)
        }
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK,
                    ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null)
                isEnabled =  false
        }
        callButton.apply {
            setOnClickListener {
                checkRuntimeReadContactPermission()
            }
        }
        cameraButton.apply {
            val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val packageManager = requireActivity().packageManager
            val resolvedActivity = packageManager.resolveActivity(captureImageIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity ==  null)
                isEnabled = false

            setOnClickListener {
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImageIntent,
                        PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities)
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                startActivityForResult(captureImageIntent, REQUEST_PHOTO)
            }
        }
        photoView.apply {
            setOnClickListener {
                if (::photoFile.isInitialized && photoFile.exists())
                    callbacks?.onCrimePhotoPressed(photoFile)
                else
                    Toast.makeText( context , R.string.photo_not_exist, Toast.LENGTH_LONG)
                        .show()
            }

            doOnGlobalLayout {
                photoViewWidth = view?.measuredWidth
                photoViewHeight = view?.measuredHeight

                updatePhotoView()
            }
        }
    }

    private val openDialogFragment: (DialogFragment, String) -> Unit =
        { dialogFragment, dialogArg ->
            dialogFragment.apply {
                setTargetFragment(this@CrimeFragment, REQUEST_CODE)
                show(this@CrimeFragment.requireFragmentManager(), dialogArg)
            }
        }

    companion object {
        val newInstance: (UUID) -> CrimeFragment = { crimeId ->
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            CrimeFragment().apply {
                arguments = args
            }
        }
    }
}