package com.uob.digitalbank.playground.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.uob.digitalbank.playground.R
import kotlinx.android.synthetic.main.pg_loader_dialog.*

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
class PgLoaderDialog(val context: Context) {

    private var dialog: Dialog = Dialog(context, R.style.Widget_Pg_Loader)

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
     * Return true if [PgLoaderDialog] is currently showing on screen, else false.
     *
     * @property isShowing
     */
    val isShowing: Boolean
        get() = dialog.isShowing

    init {

        dialog.apply {
            setContentView(R.layout.pg_loader_dialog)

            pgLoader.setAnimation("pg_tmrw_loader.json")

            val colorDrawable =
                ColorDrawable(ContextCompat.getColor(context, R.color.pg_palette_charcoal))
            colorDrawable.alpha = 128

            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setBackgroundDrawable(colorDrawable)

            setOnDismissListener {
                pgLoader.cancelAnimation()
            }
        }
    }

    /**
     * Start the dialog and display it on screen.
     */
    fun show() {
        dialog.apply {
            if (!isShowing) {
                pgLoader.playAnimation()
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
                pgLoader.cancelAnimation()
                dismiss()
            }
        }
    }
}