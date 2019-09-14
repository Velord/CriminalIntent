package velord.bnrg.criminalintent.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import velord.bnrg.criminalintent.R
import velord.bnrg.criminalintent.utils.doOnGlobalLayout
import velord.bnrg.criminalintent.utils.updatePhotoView
import java.io.File

private const val PHOTO_FILE = "photo"

class ZoomedPhotoFragment : Fragment()  {

    private lateinit var zoomImageView: ImageView
    private lateinit var photoFile: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_zoomed_photo, container, false)
        zoomImageView = view.findViewById(R.id.crime_photo_zoomed)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoFile = arguments?.getSerializable(PHOTO_FILE) as File
    }

    override fun onStart() {
        super.onStart()
        applyAllEventsToViews()
    }

    private fun applyAllEventsToViews() {
        zoomImageView.doOnGlobalLayout {
            val photoViewWidth = view?.measuredWidth
            val photoViewHeight = view?.measuredHeight

            updatePhotoView(photoViewWidth, photoViewHeight)
        }
    }

    private fun updatePhotoView(width: Int? = null,
                                height: Int? = null) {
        GlobalScope.launch {
            if (::photoFile.isInitialized)
                updatePhotoView(zoomImageView, photoFile, width, height)
        }
    }

    companion object {
        val newInstance: (File) -> ZoomedPhotoFragment = {
            val args = Bundle().apply {
                putSerializable(PHOTO_FILE, it)
            }
            ZoomedPhotoFragment().apply {
                arguments = args
            }
        }
    }
}