package com.funnyvo.android.helper

import com.google.android.exoplayer2.Player
import javax.inject.Inject

class PlayerEventListener@Inject constructor() : Player.EventListener{
    // Bottom all the function and the Call back listener of the Expo player
    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

    override fun onPositionDiscontinuity(reason: Int) {}

    override fun onSeekProcessed() {}
}