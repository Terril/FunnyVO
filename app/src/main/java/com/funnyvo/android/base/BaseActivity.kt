package com.funnyvo.android.base;

import androidx.appcompat.app.AppCompatActivity;

import com.funnyvo.android.customview.ActivityIndicator;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var activityIndicator: ActivityIndicator
    fun showProgressDialog() {
        if (activityIndicator != null)
            activityIndicator.show()
    }

    fun dismissProgressDialog() {
        if (activityIndicator != null)
            activityIndicator.hide()
    }
}
