package com.funnyvo.android.main_menu.relatetofragment_onback;

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
}