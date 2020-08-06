package com.funnyvo.android.ads

import android.content.Context
import android.view.LayoutInflater
import com.funnyvo.android.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAdView

class ShowAdvertisement private constructor() {
    private var adView: UnifiedNativeAdView? = null
    private object HOLDER {
        val INSTANCE = ShowAdvertisement()
    }

    companion object {
        val instance: ShowAdvertisement by lazy { HOLDER.INSTANCE }
    }

    fun showNativeAd(context: Context): UnifiedNativeAdView? {

        val adLoader = AdLoader.Builder(context, context.getString(R.string.native_ad)) //
                .forUnifiedNativeAd { unifiedNativeAd ->
                    // Show the ad.
                    adView = LayoutInflater.from(context).inflate(R.layout.view_ads, null) as UnifiedNativeAdView
                    val mediaView = adView?.findViewById(R.id.ad_media) as MediaView
                    adView?.mediaView = mediaView
                    mediaView.setMediaContent(unifiedNativeAd.mediaContent)
                    adView?.setNativeAd(unifiedNativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder() // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build()
        adLoader.loadAd(AdRequest.Builder().build())

        return adView
    }
}