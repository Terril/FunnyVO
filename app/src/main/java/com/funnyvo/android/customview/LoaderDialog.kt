package com.funnyvo.android.customview

import android.app.Dialog
import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.funnyvo.android.R
import kotlinx.android.synthetic.main.view_progress_dialog.*

/**
 * A dialog showing a progress indicator. Intended use is to show loader inside dialog in full screen mode.
 *
 * Developer can show dialog through their Java/Kotlin class.
 *
 * #### Kotlin implementation
 *
 * ```kotlin
 *      val dialog =  PgLoaderDialog(context)
 *      dialog.show()
 *
 * ```
 */
class LoaderDialog(val context: Context) {

    private var dialog: Dialog = Dialog(context)

    /**
     * Sets whether this dialog is cancelable with the
     * {@link KeyEvent#KEYCODE_BACK BACK} key.
     * By Default its cancellable
     *
     * @property isCancellable
     */
    var isCancellable: Boolean = true
        set(value) {
            dialog.setCancelable(value)
            field = value
        }

    /**
     * Return true if [LoaderDialog] is currently showing on screen, else false.
     *
     * @property isShowing
     */
    val isShowing: Boolean
        get() = dialog.isShowing

    init {

        dialog.apply {
            setContentView(R.layout.view_progress_dialog)
            val colorDrawable = ColorDrawable(TRANSPARENT)

            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setBackgroundDrawable(colorDrawable)

            setOnDismissListener {
                loader.visibility = INVISIBLE
            }
        }
    }

    /**
     * Start the dialog and display it on screen.
     */
    fun show() {
        dialog.apply {
            if (!isShowing) {
                loader.visibility = VISIBLE
                show()
            }
        }
    }

    /**
     * Dismiss this dialog, removing it from the screen.
     */
    fun dismiss() {
        dialog.apply {
            if (isShowing) {
                loader.visibility = INVISIBLE
                dismiss()
            }
        }
    }
}