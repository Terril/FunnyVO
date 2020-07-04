package com.funnyvo.android.customview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@ActivityScoped
class ActivityIndicator@Inject constructor(appContext: Context) : LifecycleObserver {
    private val dialog = LoaderDialog(appContext).apply { isCancellable = false }
    private var isRealHide = AtomicBoolean(true)
    private val lifecycle: Lifecycle = (appContext as AppCompatActivity).lifecycle

    init {
        lifecycle.addObserver(this)
    }

    fun show() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED) && !dialog.isShowing) {
            dialog.show()
        }
    }

    fun hide() {
        isRealHide.set(true)
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun showForLifecycle() {
        if (!dialog.isShowing && !isRealHide.get()) {
            dialog.show()
        }
    }

    fun hideForLifecycle() {
        if (dialog.isShowing) {
            isRealHide.set(false)
            dialog.dismiss()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        hide() // https://stackoverflow.com/questions/22924825/view-not-attached-to-window-manager-crash

        lifecycle.removeObserver(this)
    }
}
