package com.funnyvo.android.base;

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.daasuu.gpuv.player.GPUPlayerView
import com.daasuu.gpuv.player.PlayerScaleType
import com.funnyvo.android.customview.ActivityIndicator
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.simpleclasses.Variables.APP_NAME
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.SystemClock
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.ReadOnlyProperty

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE)
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

    open fun setPlayer(context: Context, path: Uri?, listener: Player.EventListener): GPUPlayerView {
        val loadControl = DefaultLoadControl.Builder().setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024).createDefaultLoadControl()

        val trackSelector = DefaultTrackSelector(context)
        val renderersFactory: RenderersFactory = DefaultRenderersFactory(context)
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val looper = Looper.getMainLooper()
        val clock = SystemClock.DEFAULT
        val analyticsCollector = AnalyticsCollector(clock)
        player = SimpleExoPlayer.Builder(context, renderersFactory, trackSelector, loadControl,
                bandwidthMeter, looper, analyticsCollector, true, clock).build()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, APP_NAME))
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(path)
        player.prepare(videoSource)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(listener)
        player.playWhenReady = true
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

        val gpuPlayerView = GPUPlayerView(this)
        gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_FIT_HEIGHT)
        gpuPlayerView.setSimpleExoPlayer(player)
        gpuPlayerView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        return gpuPlayerView
    }

    open fun updateMediaSource(path: String?) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, APP_NAME))
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path))
        if (player != null)
            player.prepare(videoSource)
    }

}
