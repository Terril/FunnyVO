package com.funnyvo.android.main_menu.relatetofragment_onback;

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.funnyvo.android.customview.ActivityIndicator

open class RootFragment : Fragment(), OnBackPressListener {

    lateinit var activityIndicator: ActivityIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityIndicator = context?.let { ActivityIndicator(it) }!!
    }

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