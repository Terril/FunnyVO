package com.funnyvo.android.watchvideos

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.daasuu.gpuv.composer.GPUMp4Composer
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.funnyvo.android.R
import com.funnyvo.android.VideoDownloadedListener
import com.funnyvo.android.comments.CommentFragment
import com.funnyvo.android.customview.ActivityIndicator
import com.funnyvo.android.helper.PermissionUtils.checkPermissions
import com.funnyvo.android.home.datamodel.Home
import com.funnyvo.android.keyboard.KeyboardHeightObserver
import com.funnyvo.android.keyboard.KeyboardHeightProvider
import com.funnyvo.android.main_menu.MainMenuActivity
import com.funnyvo.android.main_menu.MainMenuFragment
import com.funnyvo.android.main_menu.relatetofragment_onback.BackPressImplementation
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment
import com.funnyvo.android.profile.ProfileFragment
import com.funnyvo.android.simpleclasses.*
import com.funnyvo.android.soundlists.VideoSoundActivity
import com.funnyvo.android.taged.TaggedVideosFragment
import com.funnyvo.android.videoAction.VideoActionFragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.SystemClock
import com.google.android.exoplayer2.util.Util
import com.google.firebase.iid.FirebaseInstanceId
import com.volokh.danylo.hashtaghelper.HashTagHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.android.synthetic.main.fragment_watchvideo.*
import kotlinx.android.synthetic.main.item_watch_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import kotlin.math.abs

class WatchVideosFragment : Fragment(), Player.EventListener,
        KeyboardHeightObserver, View.OnClickListener, FragmentDataSend {

    private lateinit var adapter: WatchVideosAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var keyboardHeightProvider: KeyboardHeightProvider
    private var previousPlayer: SimpleExoPlayer? = null
    private var videoDownloadedListener: VideoDownloadedListener? = null
    private var dataList = arrayListOf<Home>()
    private var videoId: String? = null
    private var link: String? = null

    private var position = 0
    private var currentPage = -1

    private object HOLDER {
        val INSTANCE = WatchVideosFragment()
    }

    companion object {
        val instance: WatchVideosFragment by lazy { HOLDER.INSTANCE }
    }

    lateinit var activityIndicator: ActivityIndicator

    fun showProgressDialog() {
        if (activityIndicator != null)
            activityIndicator.show()
    }

    fun dismissProgressDialog() {
        if (activityIndicator != null)
            activityIndicator.hide()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_watchvideo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("video_id")?.let {
            videoId = it
        }
        arguments?.getString("link")?.let {
            link = it
        }
        arguments?.getInt("position")?.let {
            position = it
        }
        arguments?.getSerializable("arraylist")?.let {
            dataList = it as ArrayList<Home>
        }

        if (videoId != null) {
            callApiForGetAllVideos(videoId!!)
        } else if (link?.isNotEmpty()!!) {
            val parts = link!!.split("=".toRegex()).toTypedArray()
            videoId = parts[1]
            callApiForGetAllVideos(parts[1])
        }
        activityIndicator = context?.let { ActivityIndicator(it) }!!
        setAdapter()
        btnReturnWatchVideo.setOnClickListener { activity?.onBackPressed() }
        keyboardHeightProvider = KeyboardHeightProvider(activity)
        WatchVideo_F.post(Runnable { keyboardHeightProvider.start() })
        send_btn.setOnClickListener(this)
    }

    private fun setPlayer(currentPage: Int) {
        val item: Home = dataList[currentPage]
        val loadControl = DefaultLoadControl.Builder().setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024).createDefaultLoadControl()

        val trackSelector = DefaultTrackSelector(context!!)
        val renderersFactory: RenderersFactory = DefaultRenderersFactory(context!!)
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val looper = Looper.myLooper()
        val clock = SystemClock.DEFAULT
        val analyticsCollector = AnalyticsCollector(clock)
        val player = SimpleExoPlayer.Builder(context!!, renderersFactory, trackSelector, loadControl,
                bandwidthMeter, looper!!, analyticsCollector, true, clock).build()
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context!!, Variables.APP_NAME))
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(item.video_url))
        if (Variables.is_secure_info) Log.d(Variables.tag, item.video_url)
        player.prepare(videoSource)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(this)
        val layout = layoutManager.findViewByPosition(currentPage)
        val playerView: PlayerView = layout!!.findViewById(R.id.playerview)
        playerView.player = player
        player.playWhenReady = true
        previousPlayer = player
        playerView.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                    super.onFling(e1, e2, velocityX, velocityY)
                    val deltaX = e1.x - e2.x
                    val deltaXAbs = abs(deltaX)
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
                    previousPlayer?.playWhenReady = !player.playWhenReady
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    super.onLongPress(e)
                    showVideoOption(item)
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!player.playWhenReady) {
                        previousPlayer?.playWhenReady = true
                    }
                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                        showHeartOnDoubleTap(item, mainlayout, e)
                        likeVideo(currentPage, item)
                    } else {
                        Toast.makeText(context, "Please Login into ", Toast.LENGTH_SHORT).show()
                    }
                    return super.onDoubleTap(e)
                }
            })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                return true
            }
        })
        HashTagHelper.Creator.create(context!!.resources.getColor(R.color.maincolor)) { hashTag -> openHashtag(hashTag) }.handle(desc_txt)
        val aniRotate = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation)
        sound_image_layout.startAnimation(aniRotate)
        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) Functions.callApiForUpdateView(context, item.video_id)
        callApiForSingleVideos(currentPage)
    }

    fun setAdapter() {
        layoutManager = LinearLayoutManager(context)
        recylerViewWatchVideo.layoutManager = layoutManager
        recylerViewWatchVideo.setHasFixedSize(false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        recylerViewWatchVideo.onFlingListener = null
        snapHelper.attachToRecyclerView(recylerViewWatchVideo)
        adapter = WatchVideosAdapter(context, dataList, WatchVideosAdapter.OnItemClickListener { postion, item, view ->
            when (view.id) {
                R.id.user_pic -> {
                    onPause()
                    openProfile(item, false)
                }
                R.id.like_layout -> if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                    likeVideo(postion, item)
                } else {
                    Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show()
                }
                R.id.comment_layout -> openComment(item)
                R.id.btnShare -> {
                    val fragment = VideoActionFragment(item.video_id, FragmentCallback { bundle ->
                        if (bundle.getString("action") == "save") {
                            saveVideo(item)
                        }
                        if (bundle.getString("action") == "delete") {
                            showProgressDialog()
                            Functions.callApiForDeleteVideo(activity, item.video_id, object : ApiCallBack {
                                override fun arrayData(arrayList: ArrayList<*>?) {}
                                override fun onSuccess(responce: String) {
                                    dismissProgressDialog()
                                    activity?.finish()
                                }

                                override fun onFailure(responce: String) {}
                            })
                        }
                    })
                    val bundle = Bundle()
                    bundle.putString("video_id", item.video_id)
                    bundle.putString("user_id", item.fb_id)
                    fragment.arguments = bundle
                    fragment.show(childFragmentManager, "")
                }
                R.id.sound_image_layout -> if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                    if (activity?.let { checkPermissions(it) }!!) {
                        val intent = Intent(activity, VideoSoundActivity::class.java)
                        intent.putExtra("data", item)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show()
                }
            }
        })
        adapter.setHasStableIds(true)
        recylerViewWatchVideo.adapter = adapter


        // this is the scroll listener of recycler view which will tell the current item number
        recylerViewWatchVideo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //here we find the current item number
                val scrollOffset = recyclerView.computeVerticalScrollOffset()
                val height = recyclerView.height
                val pageNo = scrollOffset / height
                if (pageNo != currentPage) {
                    currentPage = pageNo
                    previousPlayer()
                    setPlayer(currentPage)
                }
            }
        })
        recylerViewWatchVideo.scrollToPosition(position)
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
        adapter.notifyDataSetChanged()
        Functions.callApiForLikeVideo(activity, home_.video_id, action, object : ApiCallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {}
            override fun onSuccess(responce: String) {}
            override fun onFailure(responce: String) {}
        })
    }

    // this will open the comment screen
    private fun openComment(item: Home) {
        val commentCount = item.video_comment_count.toInt()
        val fragmentDataSend: FragmentDataSend = this
        val commentFragment = CommentFragment(commentCount, fragmentDataSend)
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
        val args = Bundle()
        args.putString("video_id", item.video_id)
        args.putString("user_id", item.fb_id)
        commentFragment.arguments = args
        transaction.addToBackStack(null)
        transaction.replace(R.id.WatchVideo_F, commentFragment).commit()
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private fun openProfile(item: Home, from_right_to_left: Boolean) {
        if (Variables.sharedPreferences.getString(Variables.u_id, "0") == item.fb_id) {
            if (MainMenuFragment.tabLayout != null) {
                val profile = MainMenuFragment.tabLayout.getTabAt(4)
                profile!!.select()
            }
        } else {
            val profileFragment = ProfileFragment(FragmentCallback { callApiForSingleVideos(currentPage) })
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            if (from_right_to_left) transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right) else transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
            val args = Bundle()
            args.putString("user_id", item.fb_id)
            args.putString("user_name", item.first_name + " " + item.last_name)
            args.putString("user_pic", item.profile_pic)
            profileFragment.arguments = args
            transaction.addToBackStack(null)
            transaction.replace(R.id.WatchVideo_F, profileFragment).commit()
        }
    }

    private fun showHeartOnDoubleTap(item: Home, mainlayout: RelativeLayout, e: MotionEvent) {
        val x = e.x.toInt() - 100
        val y = e.y.toInt() - 100
        val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        val iv = ImageView(context)
        lp.setMargins(x, y, 0, 0)
        iv.layoutParams = lp
        if (item.liked == "1") iv.setImageDrawable(resources.getDrawable(
                R.drawable.ic_like)) else iv.setImageDrawable(resources.getDrawable(
                R.drawable.ic_like_fill))
        mainlayout.addView(iv)
        val fadeoutani = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        fadeoutani.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mainlayout.removeView(iv)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        iv.startAnimation(fadeoutani)
    }

    private fun sendComments(user_id: String, video_id: String, comment: String) {
        send_progress.visibility = View.VISIBLE
        send_btn.visibility = View.GONE
        Functions.callApiToSendComment(activity, video_id, comment, object : ApiCallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
                message_edit.text = null
                send_progress.visibility = View.GONE
                send_btn.visibility = View.VISIBLE
                var commentCount: Int = dataList[currentPage].video_comment_count.toInt()
                commentCount++
                onDataSent("" + commentCount)
            }

            override fun onSuccess(responce: String) {}
            override fun onFailure(responce: String) {}
        })
        sendPushNotification(user_id, comment)
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun openHashtag(tag: String) {
        val tagedVideosFragment = TaggedVideosFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom)
        val args = Bundle()
        args.putString("tag", tag)
        tagedVideosFragment.arguments = args
        transaction.addToBackStack(null)
        transaction.replace(R.id.WatchVideo_F, tagedVideosFragment).commit()
    }

    private fun saveVideo(item: Home) {
        showProgressDialog()
        PRDownloader.initialize(context)
        val prDownloader = PRDownloader.download(item.video_url, Variables.APP_FOLDER, item.video_id + "no_watermark" + ".mp4")
                .build()
                .setOnStartOrResumeListener { }
                .setOnPauseListener { }
                .setOnCancelListener { }
                .setOnProgressListener { }
        prDownloader.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                applyWatermark(item)
            }

            override fun onError(error: Error) {
                deleteFileNoWatermark(item)
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                dismissProgressDialog()
            }
        })
    }

    private fun applyWatermark(item: Home) {
        val logo = (resources.getDrawable(R.mipmap.ic_launcher_watermark) as BitmapDrawable).bitmap
        val filter = GlWatermarkFilter(logo, GlWatermarkFilter.Position.LEFT_TOP)
        GPUMp4Composer(Variables.APP_FOLDER + item.video_id + "no_watermark" + ".mp4",
                Variables.APP_FOLDER + item.video_id + ".mp4")
                .filter(filter)
                .listener(object : GPUMp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        showProgressDialog()
                    }

                    override fun onCompleted() {
                        activity?.runOnUiThread(Runnable {
                            dismissProgressDialog()
                            deleteFileNoWatermark(item)
                            scanFile(item)
                        })
                    }

                    override fun onCanceled() {
                        Log.d("resp", "onCanceled")
                    }

                    override fun onFailed(exception: Exception) {
                        Log.d("resp", exception.toString())
                        activity?.runOnUiThread(Runnable {
                            try {
                                deleteFileNoWatermark(item)
                                dismissProgressDialog()
                                Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                            }
                        })
                    }
                })
                .start()
    }

    private fun deleteFileNoWatermark(item: Home) {
        val file = File(Variables.APP_FOLDER + item.video_id + "no_watermark" + ".mp4")
        if (file.exists()) {
            file.delete()
        }
    }

    private fun scanFile(item: Home) {
        MediaScannerConnection.scanFile(context, arrayOf(Variables.APP_FOLDER + item.video_id + ".mp4"),
                null
        ) { path, uri ->
           // Log.i("ExternalStorage", "Scanned " + path + ":");
          //  Log.i("ExternalStorage", "-> uri=" + uri);
            videoDownloadedListener?.onDownloadCompleted(uri)
        }
    }


    private fun sendPushNotification(userId: String, comment: String) {
        val notimap = JSONObject()
        try {
            notimap.put("title", Variables.sharedPreferences.getString(Variables.u_name, "") + " Comment on your video")
            notimap.put("message", comment)
            notimap.put("icon", Variables.sharedPreferences.getString(Variables.u_pic, ""))
            notimap.put("senderid", Variables.sharedPreferences.getString(Variables.u_id, ""))
            notimap.put("receiverid", userId)
            notimap.put("action_type", "comment")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        ApiRequest.callApi(context, Variables.SEND_PUSH_NOTIFICATION, notimap, null)
    }

    private fun showVideoOption(home: Home) {
        var options = arrayOf<CharSequence>("Save Video", "Cancel")
        if (home.fb_id == Variables.sharedPreferences.getString(Variables.u_id, "")) options = arrayOf<CharSequence>("Save Video", "Delete Video", "Cancel")
        val builder = AlertDialog.Builder(context!!, R.style.AlertDialogCustom)
        builder.setTitle(null)
        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options.get(item) == "Save Video") {
                if (Functions.checkstoragepermision(activity)) saveVideo(home)
            } else if (options.get(item) == "Delete Video") {
                if (Variables.is_secure_info) {
                    Toast.makeText(context, getString(R.string.delete_function_not_available_in_demo), Toast.LENGTH_SHORT).show()
                } else {
                    Functions.callApiForDeleteVideo(activity, home.video_id, object : ApiCallBack {
                        override fun arrayData(arrayList: ArrayList<*>?) {}
                        override fun onSuccess(responce: String) {
                            activity?.finish()
                        }

                        override fun onFailure(responce: String) {}
                    })
                }
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    private fun previousPlayer() {
        if (previousPlayer != null) {
            previousPlayer?.removeListener(this)
            previousPlayer?.release()
        }
    }

    private fun callApiForSingleVideos(position: Int) {
        try {
            val parameters = JSONObject()
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"))
            parameters.put("token", Variables.sharedPreferences.getString(Variables.device_token, "Null"))
            parameters.put("video_id", dataList.get(position).video_id)
            ApiRequest.callApi(context, Variables.SHOW_ALL_VIDEOS, parameters) { resp -> singleVideoParseData(position, resp) }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
        }
    }

    private fun singleVideoParseData(pos: Int, response: String?) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val item = Home()
                    item.fb_id = itemdata.optString("fb_id")
                    val userInfo = itemdata.optJSONObject("user_info")
                    item.username = userInfo.optString("username")
                    item.first_name = userInfo.optString("first_name", context!!.resources.getString(R.string.app_name))
                    item.last_name = userInfo.optString("last_name", "User")
                    item.profile_pic = userInfo.optString("profile_pic", "null")
                    item.verified = userInfo.optString("verified")
                    val soundData = itemdata.optJSONObject("sound")
                    item.sound_id = soundData.optString("id")
                    item.sound_name = soundData.optString("sound_name")
                    item.sound_pic = soundData.optString("thum")
                    val count = itemdata.optJSONObject("count")
                    item.like_count = count.optString("like_count")
                    item.video_comment_count = count.optString("video_comment_count")
                    item.video_id = itemdata.optString("id")
                    item.liked = itemdata.optString("liked")
                    item.video_url = itemdata.optString("video")
                    item.video_description = itemdata.optString("description")
                    item.thum = itemdata.optString("thum")
                    item.created_date = itemdata.optString("created")
                    if (item.video_url.contains(Variables.base_url)) {
                        item.video_url = item.video_url.replace(Variables.base_url + "/", "")
                    }
                    if (item.thum.contains(Variables.base_url)) {
                        item.thum = item.thum.replace(Variables.base_url + "/", "")
                    }
                    dataList.removeAt(pos)
                    dataList.add(pos, item)
                    adapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun callApiForGetAllVideos(id: String) {
        if (MainMenuActivity.token == null) MainMenuActivity.token = FirebaseInstanceId.getInstance().token
        val parameters = JSONObject()
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"))
            parameters.put("token", MainMenuActivity.token)
            parameters.put("video_id", id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        ApiRequest.callApi(context, Variables.SHOW_ALL_VIDEOS, parameters) { resp -> parseData(resp) }
    }

    private fun parseData(response: String?) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val item = Home()
                    item.fb_id = itemdata.optString("fb_id")
                    val userInfo = itemdata.optJSONObject("user_info")
                    item.username = userInfo.optString("username")
                    item.first_name = userInfo.optString("first_name", context!!.resources.getString(R.string.app_name))
                    item.last_name = userInfo.optString("last_name", "User")
                    item.profile_pic = userInfo.optString("profile_pic", "null")
                    item.verified = userInfo.optString("verified")
                    val soundData = itemdata.optJSONObject("sound")
                    item.sound_id = soundData.optString("id")
                    item.sound_name = soundData.optString("sound_name")
                    item.sound_pic = soundData.optString("thum")
                    val count = itemdata.optJSONObject("count")
                    item.like_count = count.optString("like_count")
                    item.video_comment_count = count.optString("video_comment_count")
                    item.video_id = itemdata.optString("id")
                    item.liked = itemdata.optString("liked")
                    item.video_url = itemdata.optString("video")
                    item.video_description = itemdata.optString("description")
                    item.thum = itemdata.optString("thum")
                    item.created_date = itemdata.optString("created")
                    if (item.video_url.contains(Variables.base_url)) {
                        item.video_url = item.video_url.replace(Variables.base_url + "/", "")
                    }
                    if (item.thum.contains(Variables.base_url)) {
                        item.thum = item.thum.replace(Variables.base_url + "/", "")
                    }
                    dataList.add(item)
                }
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setVideoDownloadListener(downloadListener: VideoDownloadedListener) {
        videoDownloadedListener = downloadListener
    }

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        if (previousPlayer != null) {
            previousPlayer?.playWhenReady = false
        }
        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onStop() {
        super.onStop()
        if (previousPlayer != null) {
            previousPlayer?.playWhenReady = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (previousPlayer != null) {
            previousPlayer?.release()
        }
        keyboardHeightProvider.close()
    }


    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        val params = RelativeLayout.LayoutParams(writeLayout.width, writeLayout.height)
        params.bottomMargin = height
        writeLayout.layoutParams = params
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.send_btn -> if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                val commentTxt = message_edit.text.toString()
                if (!TextUtils.isEmpty(commentTxt)) {
                    sendComments(dataList[currentPage].fb_id, dataList[currentPage].video_id, commentTxt)
                }
            } else {
                Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDataSent(yourData: String?) {
        val commentCount = yourData!!.toInt()
        val item: Home = dataList[currentPage]
        item.video_comment_count = "" + commentCount
        dataList.add(currentPage, item)
        adapter.notifyDataSetChanged()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            p_bar.visibility = View.VISIBLE
        } else if (playbackState == Player.STATE_READY) {
            p_bar.visibility = View.GONE
        }
    }
}