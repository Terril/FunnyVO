package com.funnyvo.android.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.funnyvo.android.FunnyVOApplication.Companion.simpleCache
import com.funnyvo.android.R
import com.funnyvo.android.ads.ShowAdvertisement
import com.funnyvo.android.comments.CommentFragment
import com.funnyvo.android.helper.PermissionUtils.checkPermissions
import com.funnyvo.android.home.datamodel.Home
import com.funnyvo.android.home.viewmodel.HomeViewModel
import com.funnyvo.android.main_menu.MainMenuActivity
import com.funnyvo.android.main_menu.MainMenuFragment
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment
import com.funnyvo.android.profile.ProfileFragment
import com.funnyvo.android.simpleclasses.ApiCallBack
import com.funnyvo.android.simpleclasses.FragmentDataSend
import com.funnyvo.android.simpleclasses.Functions
import com.funnyvo.android.simpleclasses.Variables
import com.funnyvo.android.soundlists.VideoSoundActivity
import com.funnyvo.android.videoAction.VideoActionFragment
import com.funnyvo.android.videocache.VideoPreLoadingService
import com.funnyvo.android.videocache.utility.Constants
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.SystemClock
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.volokh.danylo.hashtaghelper.HashTagHelper
import kotlinx.android.synthetic.main.fragment_home_new.*
import kotlinx.android.synthetic.main.item_home_layout.*
import java.util.*

class HomeFragmentNew : RootFragment(), FragmentDataSend, Player.EventListener {

    private val homeViewModel: HomeViewModel by viewModels()
    private var dataList: ArrayList<Home> = arrayListOf()
    private var pageNumber = 1
    private lateinit var homeAdapter: HomeAdapter
    private var adView: UnifiedNativeAdView? = null
    private lateinit var  player : SimpleExoPlayer

    private lateinit var advertisement : ShowAdvertisement
    private object HOLDER {
        val INSTANCE = HomeFragmentNew()
    }

    companion object {
        val instance: HomeFragmentNew by lazy { HOLDER.INSTANCE }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (MainMenuActivity.intent != null) {
            dataList = MainMenuActivity.intent.getSerializableExtra(Variables.HOME_DATA) as ArrayList<Home>
        }
        if (dataList == null || dataList.isEmpty()) {
            dataList = ArrayList()
            handleApiCallRequest()
        }

        setAdapter()
        startPreLoadingService()
        advertisement = ShowAdvertisement.instance

        val loadControl = DefaultLoadControl.Builder().setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024).createDefaultLoadControl()
        val trackSelector = DefaultTrackSelector(requireContext())
        val renderersFactory: RenderersFactory = DefaultRenderersFactory(requireContext())
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val looper = Looper.myLooper()
        val clock = SystemClock.DEFAULT
        val analyticsCollector = AnalyticsCollector(clock)
        player = SimpleExoPlayer.Builder(requireContext(), renderersFactory, trackSelector, loadControl,
                bandwidthMeter, looper!!, analyticsCollector, true, clock).build()

        pagerHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (adView != null) {
                    adView?.destroy()
                }
                adView = context?.let { advertisement.showNativeAd(it) }
                setPlayer(position)
            }
        })
        setListener()
    }

    private fun setListener() {
        homeViewModel.videoResponseEvent.observe(viewLifecycleOwner) {

        }
    }

    private fun startPreLoadingService() {
        val preloadingServiceIntent = Intent(context, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putExtra(Constants.VIDEO_LIST, dataList)
        activity?.startService(preloadingServiceIntent)
    }

    private fun handleApiCallRequest() {
        val url: String = if (Variables.sharedPreferences.getBoolean(Variables.SHOW_ADS, false)) {
            Variables.SHOW_ALL_VIDEOS_WITH_ADS
        } else {
            Variables.SHOW_ALL_VIDEOS
        }
        context?.let { homeViewModel.requestForVideos(it, url, pageNumber = pageNumber.toString()) }
    }

    private fun setAdapter() {
        homeAdapter = HomeAdapter(context, dataList) { position, item, view ->
            when (view) {
                btnMuteUnMuteAudio -> {
                    //   isMuted = item.isMute
                    //   toggleSound(position, item)
                }
                user_pic, chipUernameHome -> {
                    onPause()
                    openProfile(item, false)
                }
                like_layout -> if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                    likeVideo(position, item)
                } else {
                    Toast.makeText(context, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
                }
                comment_layout -> openComment(item)
                btnShare -> {
                    val fragment = VideoActionFragment(item.video_id) { bundle ->
                        if (bundle.getString("action") == resources.getString(R.string.save).toLowerCase()) {
                            // saveVideo(item)
                        } else if (bundle.getString("action") == getString(R.string.delete)) {
                            showProgressDialog()
                            Functions.callApiForDeleteVideo(activity, item.video_id, object : ApiCallBack {
                                override fun arrayData(arrayList: ArrayList<*>?) {}
                                override fun onSuccess(responce: String) {
                                    //   dataList.removeAt(currentPage)
                                    homeAdapter.notifyDataSetChanged()
                                    dismissProgressDialog()
                                }

                                override fun onFailure(responce: String) {
                                    dismissProgressDialog()
                                }
                            })
                        }
                    }
                    val bundle = Bundle()
                    bundle.putString("video_id", item.video_id)
                    bundle.putString("user_id", item.fb_id)
                    fragment.arguments = bundle
                    fragment.show(childFragmentManager, "")
                }
                sound_image_layout -> if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                    if (checkPermissions(requireActivity())) {
                        val intent = Intent(activity, VideoSoundActivity::class.java)
                        intent.putExtra("data", item)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, R.string.please_login, Toast.LENGTH_SHORT).show()
                }
            }
        }

        homeAdapter.setHasStableIds(true)
        pagerHome.adapter = homeAdapter
    }

    // this function will call for like the video and Call an Api for like the video
    private fun likeVideo(position: Int, home_: Home) {
        var action = home_.liked
        if (action == "1") {
            action = "0"
            home_.like_count = "" + (home_.like_count.toInt() - 1)
        } else {
            action = "1"
            home_.like_count = "" + (home_.like_count.toInt() + 1)
        }
        dataList.removeAt(position)
        home_.liked = action
        dataList.add(position, home_)
        homeAdapter.notifyDataSetChanged()
        activity?.let { homeViewModel.requestToLikeVideo(it, home_.video_id, action) }
    }

    // this will open the comment screen
    private fun openComment(item: Home) {
        val commentCount = item.video_comment_count.toInt()
        val fragmentDataSend: FragmentDataSend = this
        val commentFragment = CommentFragment(commentCount, fragmentDataSend)
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
        val args = Bundle()
        args.putString("video_id", item.video_id)
        args.putString("user_id", item.fb_id)
        commentFragment.arguments = args
        transaction?.addToBackStack(null)
        transaction?.replace(R.id.main_menu_container, commentFragment)?.commit()
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun openProfile(item: Home, fromRightToLeft: Boolean) {
        if (Variables.sharedPreferences.getString(Variables.u_id, "0") == item.fb_id) {
            val profile = MainMenuFragment.tabLayout.getTabAt(4)
            profile!!.select()
        } else {
            val profileFragment = ProfileFragment { homeAdapter.notifyDataSetChanged() }
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            if (fromRightToLeft) transaction?.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right)
            else transaction?.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
            val args = Bundle()
            args.putString("user_id", item.fb_id)
            args.putString("user_name", item.first_name + " " + item.last_name)
            args.putString("user_pic", item.profile_pic)
            profileFragment.arguments = args
            transaction?.addToBackStack(null)
            transaction?.replace(R.id.main_menu_container, profileFragment)?.commit()
        }
    }

    private fun setPlayer(currentPage: Int) {
        if (dataList.isNotEmpty() && currentPage >= 0) {
//            val layout: View = layoutManager.findViewByPosition(currentPage)
//            val mainLayout = layout.findViewById<RelativeLayout>(R.id.mainLayoutHome)
//            val loadAdsLayout = layout.findViewById<FrameLayout>(R.id.frameLoadAdsHome)
 //           imvHomeVideoSnap = layout.findViewById(R.id.imvHomeVideoSnap)
 //           playerView = layout.findViewById(R.id.playerViewHome)
            val item = dataList[currentPage]

            val cacheDataSourceFactory = CacheDataSourceFactory(
                    simpleCache,
                    DefaultDataSourceFactory(context,
                            Util.getUserAgent(requireContext(), Variables.APP_NAME)),
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
            )
            if (item.video_url.isEmpty()) {
                frameLoadAdsHome.visibility = View.VISIBLE
                if (adView != null) {
                    if (adView?.parent != null) {
                        (adView?.parent as ViewGroup).removeView(adView)
                    }
                    frameLoadAdsHome.removeAllViews()
                    frameLoadAdsHome.addView(adView)
                }
            } else {
//                Glide.with(requireContext())
//                        .load(item.thum)
//                        .into(imvHomeVideoSnap)
                frameLoadAdsHome.visibility = View.INVISIBLE
                if (adView != null) {
                    if (adView?.parent != null) {
                        (adView?.parent as ViewGroup).removeView(adView)
                    }
                }
                val videoSource: MediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(Uri.parse(item.video_url))
                playerViewHome.player = player
                player.prepare(videoSource)
            }
            player.repeatMode = Player.REPEAT_MODE_ALL
            player.addListener(this)
            player.playWhenReady = true

            playerViewHome.setOnTouchListener(object : OnTouchListener {
                private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                        super.onFling(e1, e2, velocityX, velocityY)
                        val deltaX = e1.x - e2.x
                        val deltaXAbs = Math.abs(deltaX)
                        // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                        if (deltaXAbs > 100 && deltaXAbs < 1000) {
                            if (deltaX > 0) {
                                openProfile(item, true)
                            }
                        }
                        return true
                    }

                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        super.onSingleTapUp(e)
                        if (!player.playWhenReady) {
                          //  is_user_stop_video = false
                            player.playWhenReady = true
                        } else {
                           // is_user_stop_video = true
                            player.playWhenReady = false
                        }
                        return true
                    }

                    override fun onLongPress(e: MotionEvent) {
                        super.onLongPress(e)
                      //  showVideoOption(item)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!player.playWhenReady) {
                           // is_user_stop_video = false
                            player.playWhenReady = true
                        }
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                           // showHeartOnDoubleTap(item, mainLayout, e)
                            likeVideo(currentPage, item)
                        } else {
                            Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show()
                        }
                        return super.onDoubleTap(e)
                    }
                })

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    gestureDetector.onTouchEvent(event)
                    return true
                }
            })
            HashTagHelper.Creator.create(requireContext().resources.getColor(R.color.maincolor)) { hashTag ->
                onPause()
              //  openHashtag(hashTag)
            }.handle(descTxt)

            val soundAnimation = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation)
            sound_image_layout.startAnimation(soundAnimation)
            if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) Functions.callApiForUpdateView(activity, item.video_id)

            if (currentPage == dataList.size - 1) {
                pageNumber += 1
                handleApiCallRequest()
            } else {
                pagerHome.post(Runnable { // There is no need to use notifyDataSetChanged()
                    homeAdapter.notifyDataSetChanged()
                })
            }
        }
    }

    override fun onDataSent(yourData: String?) {
        TODO("Not yet implemented")
    }
}