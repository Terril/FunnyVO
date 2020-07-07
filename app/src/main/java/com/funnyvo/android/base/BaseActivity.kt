package com.funnyvo.android.base;

import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.daasuu.gpuv.player.GPUPlayerView
import com.daasuu.gpuv.player.PlayerScaleType
import com.funnyvo.android.customview.ActivityIndicator
import com.funnyvo.android.simpleclasses.Variables.APP_NAME
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    lateinit var player: SimpleExoPlayer

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

//     This will hide the bottom mobile navigation control
    open fun hideNavigation() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            val decorView = window.decorView
            decorView
                    .setOnSystemUiVisibilityChangeListener { visibility ->
                        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                            decorView.systemUiVisibility = flags
                        }
                    }
        }
    }

    open fun setPlayer(path: String?, listener: Player.EventListener): GPUPlayerView {
        val trackSelector = DefaultTrackSelector()
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, APP_NAME))
        val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path))
        player.prepare(videoSource)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(listener)
        player.playWhenReady = true
        val gpuPlayerView = GPUPlayerView(this)
        gpuPlayerView.setSimpleExoPlayer(player)
        gpuPlayerView.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return gpuPlayerView
    }

    open fun updateMediaSource(path: String?) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, APP_NAME))
        val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path))
        player.prepare(videoSource)
    }

}
