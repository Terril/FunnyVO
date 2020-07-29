package com.funnyvo.android.main_menu.relatetofragment_onback;

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.funnyvo.android.customview.ActivityIndicator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class RootFragment : Fragment(), OnBackPressListener {

    @Inject
    lateinit var activityIndicator: ActivityIndicator

    override fun onBackPressed(): Boolean {
        return BackPressImplementation(this).onBackPressed();
    }

    fun showProgressDialog() {
        if (activityIndicator != null)
            activityIndicator.show()
    }

    fun dismissProgressDialog() {
        if (activityIndicator != null)
            activityIndicator.hide()
    }

    open fun openBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}