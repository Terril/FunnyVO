package com.funnyvo.android.videocache

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.funnyvo.android.FunnyVOApplication
import com.funnyvo.android.R
import com.funnyvo.android.home.datamodel.Home
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.videocache.utility.Constants
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.upstream.cache.CacheUtil.DEFAULT_BUFFER_SIZE_BYTES
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VideoPreLoadingService :
        IntentService(VideoPreLoadingService::class.java.simpleName) {
    private lateinit var mContext: Context
    private var simpleCache: SimpleCache? = null
    private var cachingJob: Job? = null
    private var videosList: ArrayList<Home>? = null

    override fun onHandleIntent(intent: Intent?) {
        mContext = applicationContext
        simpleCache = FunnyVOApplication.simpleCache

        if (intent != null) {
            val extras = intent.extras
            videosList = extras?.getSerializable(Constants.VIDEO_LIST) as ArrayList<Home>?

            if (!videosList.isNullOrEmpty()) {
                preCacheVideo(videosList)
            }
        }
    }

    private fun preCacheVideo(videosList: ArrayList<Home>?) {
        var videoUrl: String? = null
        if (!videosList.isNullOrEmpty()) {
            videoUrl = videosList[0].video_url
            videosList.removeAt(0)
        } else {
            stopSelf()
        }
        if (!videoUrl.isNullOrBlank()) {
            val videoUri = Uri.parse(videoUrl)
            val dataSpec = DataSpec(videoUri)
            val defaultCacheKeyFactory = CacheUtil.DEFAULT_CACHE_KEY_FACTORY
            val progressListener =
                    CacheUtil.ProgressListener { requestLength, bytesCached, newBytesCached ->
                        val downloadPercentage: Double = (bytesCached * 100.0
                                / requestLength)
                    }
            val cacheDataSourceFactory = CacheDataSourceFactory(
                    simpleCache,
                    DefaultDataSourceFactory(mContext,
                            Util.getUserAgent(this, Variables.APP_NAME)),
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
            ).createDataSource()

            cachingJob = GlobalScope.launch(Dispatchers.IO) {
                cacheVideo(dataSpec, defaultCacheKeyFactory, cacheDataSourceFactory, progressListener)
                preCacheVideo(videosList)
            }
        }
    }

    private fun cacheVideo(
            dataSpec: DataSpec,
            defaultCacheKeyFactory: CacheKeyFactory?,
            dataSource: CacheDataSource,
            progressListener: CacheUtil.ProgressListener
    ) {
        CacheUtil.cache(dataSpec, simpleCache, defaultCacheKeyFactory, dataSource, ByteArray(DEFAULT_BUFFER_SIZE_BYTES), null, 0, progressListener, null, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        cachingJob?.cancel()
    }
}