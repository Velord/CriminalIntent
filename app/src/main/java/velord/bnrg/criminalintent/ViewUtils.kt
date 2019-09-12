package velord.bnrg.criminalintent

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

inline fun View.doOnGlobalLayout(crossinline action: (view: View) -> Unit) {
    val vto = viewTreeObserver
    vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        @SuppressLint("ObsoleteSdkInt")
        @Suppress("DEPRECATION")
        override fun onGlobalLayout() {
            action(this@doOnGlobalLayout)
            when {
                vto.isAlive -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeOnGlobalLayoutListener(this)
                    } else {
                        vto.removeGlobalOnLayoutListener(this)
                    }
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                }
            }
        }
    })
}

suspend fun Fragment.updatePhotoView(view: ImageView,
                             photoFile: File,
                             width: Int? = null,
                             height: Int? = null) {
    if (photoFile.exists()) {
        if (width != null && height != null) {
            val bitmap = coroutineScope {
                async { getScaledBitmap(photoFile.path, width, height) }
            }
            view.setImageBitmap(bitmap.await())
        } else {
            val bitmap = coroutineScope {
                async { getScaledBitmap(photoFile.path, requireActivity()) }
            }
            view.setImageBitmap(bitmap.await())
        }
    } else
        view.setImageDrawable(null)
}


