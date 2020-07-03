package com.funnyvo.android.base;

import android.net.Uri
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.daasuu.gpuv.player.GPUPlayerView
import com.daasuu.gpuv.player.PlayerScaleType
import com.funnyvo.android.customview.ActivityIndicator
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

    open fun setPlayer(path: String?, listener: Player.EventListener): GPUPlayerView {
        val trackSelector = DefaultTrackSelector()
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "FunnyVO"))
        val videoSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path))
        player.prepare(videoSource)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(listener)
        player.playWhenReady = true
        val gpuPlayerView = GPUPlayerView(this)
        gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_NONE)
        gpuPlayerView.setSimpleExoPlayer(player)
        gpuPlayerView.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return gpuPlayerView
    }
}
