package com.funnyvo.android.home;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.danikula.videocache.HttpProxyCacheServer;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.funnyvo.android.R;
import com.funnyvo.android.VideoDownloadedListener;
import com.funnyvo.android.ads.ShowAdvertisement;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.comments.CommentFragment;
import com.funnyvo.android.helper.PermissionUtils;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.MainMenuFragment;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.profile.ProfileFragment;
import com.funnyvo.android.simpleclasses.ApiCallBack;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.FragmentDataSend;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.VideoSoundActivity;
import com.funnyvo.android.taged.TaggedVideosFragment;
import com.funnyvo.android.videoAction.VideoActionFragment;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.SystemClock;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.material.tabs.TabLayout;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.funnyvo.android.FunnyVOApplication.getProxy;
import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;
import static com.funnyvo.android.simpleclasses.Variables.HOME_DATA;

/**
 * A simple {@link Fragment} subclass.
 */

// this is the main view which is show all  the video in list
public class HomeFragment extends RootFragment implements Player.EventListener, FragmentDataSend {

    private View view;
    private Context context;

    private RecyclerView recyclerView;
    private ArrayList<Home> dataList;
    private int currentPage = -1;
    private LinearLayoutManager layoutManager;
    private ProgressBar pBar;
    private SwipeRefreshLayout swiperefresh;

    boolean is_user_stop_video = false;

    boolean is_add_show = false;
    private HomeAdapter adapter;

    // when we swipe for another video this will release the previous player
    private SimpleExoPlayer previousPlayer;
    int swipe_count = 0;

    BaseActivity mActivity;
    private boolean isMuted = false;
    private int pageNumber = 1;
    private VideoDownloadedListener videoDownloadedListener;

    private static HomeFragment fragment;
    private UnifiedNativeAdView adView;
    private PlayerView playerView;

    private HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        if (fragment == null) {
            fragment = new HomeFragment();
        }

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();

        pBar = view.findViewById(R.id.p_bar);

        recyclerView = view.findViewById(R.id.recylerviewHome);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        if (MainMenuActivity.intent != null) {
            dataList = (ArrayList<Home>) MainMenuActivity.intent.getSerializableExtra(HOME_DATA);
        } else {
            dataList = new ArrayList<>();
            handleApiCallRequest();
        }
        setAdapter();
        final ShowAdvertisement advertisement = ShowAdvertisement.Companion.getInstance();
        // this is the scroll listener of recycler view which will tell the current item number
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no = scrollOffset / height;

                if (page_no != currentPage) {
                    if (adView != null) {
                        adView.destroy();
                    }
                    adView = advertisement.showNativeAd(context);
                    currentPage = page_no;
                    releasePreviousPlayer();
                    setPlayer(currentPage);
                }
            }
        });


        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setProgressViewOffset(false, 0, 200);

        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = -1;
                pageNumber = 1;
                if (dataList != null && !dataList.isEmpty()) {
                    dataList.clear();
                }
                handleApiCallRequest();
            }
        });

//        if (!Variables.is_remove_ads)
//            loadAdd();

        return view;
    }

    private void handleApiCallRequest() {
        String url;
        if (Variables.sharedPreferences.getBoolean(Variables.SHOW_ADS, false)) {
            url = Variables.SHOW_ALL_VIDEOS_WITH_ADS;
        } else {
            url = Variables.SHOW_ALL_VIDEOS;
        }
        callApiForGetAllVideos(url);
    }

//    private void loadAdd() {
//        // this is test app id you will get the actual id when you add app in your
//        //add mob account
//        MobileAds.initialize(context,
//                getResources().getString(R.string.ad_app_id));
//
//        final InterstitialAd mInterstitialAd;
//        //code for intertial add
//        mInterstitialAd = new InterstitialAd(context);
//
//        //here we will get the add id keep in mind above id is app id and below Id is add Id
//        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.interstitial_add));
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());
//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
//                mInterstitialAd.loadAd(new AdRequest.Builder().build());
//            }
//        });
//    }

    public void setAdapter() {
        adapter = new HomeAdapter(context, dataList, new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, final Home item, View view) {
                switch (view.getId()) {
                    case R.id.btnMuteUnMuteAudio:
                        isMuted = item.isMute;
                        toggleSound(position, item);
                        break;
                    case R.id.user_pic:
                    case R.id.username:
                        onPause();
                        openProfile(item, false);
                        break;
                    case R.id.like_layout:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            likeVideo(position, item);
                        } else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.comment_layout:
                        openComment(item);
                        break;
                    case R.id.btnShare:
//                        if (!is_add_show && (mInterstitialAd != null && mInterstitialAd.isLoaded())) {
//                          //  mInterstitialAd.show();
//                            is_add_show = true;
//                        } else {
                        is_add_show = false;
                        final VideoActionFragment fragment = new VideoActionFragment(item.video_id, new FragmentCallback() {
                            @Override
                            public void responseCallBackFromFragment(Bundle bundle) {

                                if (bundle.getString("action").equals("save")) {
                                    saveVideo(item);
                                } else if (bundle.getString("action").equals("delete")) {
                                    showProgressDialog();
                                    Functions.callApiForDeleteVideo(getActivity(), item.video_id, new ApiCallBack() {
                                        @Override
                                        public void arrayData(ArrayList arrayList) {

                                        }

                                        @Override
                                        public void onSuccess(String responce) {
                                            dataList.remove(currentPage);
                                            adapter.notifyDataSetChanged();
                                            dismissProgressDialog();

                                        }

                                        @Override
                                        public void onFailure(String responce) {
                                            dismissProgressDialog();
                                        }
                                    });

                                }

                            }
                        });

                        Bundle bundle = new Bundle();
                        bundle.putString("video_id", item.video_id);
                        bundle.putString("user_id", item.fb_id);
                        fragment.setArguments(bundle);
                        fragment.show(getChildFragmentManager(), "");
                        //             }
                        break;
                    case R.id.sound_image_layout:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            if (PermissionUtils.INSTANCE.checkPermissions(getActivity())) {
                                Intent intent = new Intent(getActivity(), VideoSoundActivity.class);
                                intent.putExtra("data", item);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(context, R.string.please_login, Toast.LENGTH_SHORT).show();
                        }

                        break;
                }

            }

        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

    }

    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllVideos(String url) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("token", MainMenuActivity.token);
            parameters.put("page_number", pageNumber);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, url, parameters, new Callback() {
            @Override
            public void response(String resp) {
                swiperefresh.setRefreshing(false);
                parseData(resp);
            }
        });
    }

    public void parseData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals(Variables.API_SUCCESS_CODE)) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                int arrayItems = msgArray.length();
                for (int i = 0; i < arrayItems; i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    Home item = new Home();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject userInfo = itemdata.optJSONObject("user_info");

                    if (userInfo != null) {
                        item.username = userInfo.optString("username");
                        item.first_name = userInfo.optString("first_name", context.getResources().getString(R.string.app_name));
                        item.last_name = userInfo.optString("last_name", "User");
                        item.profile_pic = userInfo.optString("profile_pic", "null");
                        item.verified = userInfo.optString("verified");
                    }

                    JSONObject soundData = itemdata.optJSONObject("sound");
                    if (soundData != null) {
                        item.sound_id = soundData.optString("id");
                        item.sound_name = soundData.optString("sound_name");
                        item.sound_pic = soundData.optString("thum");
                        item.soundUrl = soundData.optString("sound_url");
                    }

                    JSONObject count = itemdata.optJSONObject("count");
                    if (count != null) {
                        item.like_count = count.optString("like_count");
                        item.video_comment_count = count.optString("video_comment_count");
                    }

                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");
                    item.video_url = itemdata.optString("video");

                    item.video_description = itemdata.optString("description");

                    item.thum = itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");
                    if (item.video_url.contains(Variables.base_url)) {
                        item.video_url = item.video_url.replace(Variables.base_url + "/", "");
                    }
                    if (item.sound_pic.contains(Variables.base_url)) {
                        item.sound_pic = item.sound_pic.replace(Variables.base_url + "/", "");
                    }
                    if (item.thum.contains(Variables.base_url)) {
                        item.thum = item.thum.replace(Variables.base_url + "/", "");
                    }

                    item.isMute = isMuted;
                    dataList.add(item);
                }

            } else {
                //   Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // this will call when swipe for another video and
    // this function will set the player to the current video
    private void setPlayer(final int currentPage) {
        if (!dataList.isEmpty() && currentPage >= 0) {
            View layout = layoutManager.findViewByPosition(currentPage);
            final RelativeLayout mainLayout = layout.findViewById(R.id.mainLayoutHome);
            final FrameLayout loadAdsLayout = layout.findViewById(R.id.frameLoadAdsHome);
            playerView = layout.findViewById(R.id.playerViewHome);

            final Home item = dataList.get(currentPage);

            DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024).createDefaultLoadControl();

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
            Looper looper = Looper.myLooper();
            Clock clock = SystemClock.DEFAULT;
            AnalyticsCollector analyticsCollector = new AnalyticsCollector(clock);
            previousPlayer = new SimpleExoPlayer.Builder(context, renderersFactory, trackSelector, loadControl,
                    bandwidthMeter, looper, analyticsCollector, true, clock).build();
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, APP_NAME));
            if (item.video_url.isEmpty()) {
                loadAdsLayout.setVisibility(View.VISIBLE);
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    loadAdsLayout.addView(adView);
                }
            } else {
                loadAdsLayout.setVisibility(View.INVISIBLE);
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                }

                HttpProxyCacheServer proxy = getProxy(context);
                String proxyUrl = proxy.getProxyUrl(item.video_url);
                MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(proxyUrl));
                playerView.setPlayer(previousPlayer);
                previousPlayer.prepare(videoSource);
            }

            setUpVideoCache();

            previousPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            previousPlayer.addListener(this);
            previousPlayer.setPlayWhenReady(is_visible_to_user);

            playerView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        super.onFling(e1, e2, velocityX, velocityY);
                        float deltaX = e1.getX() - e2.getX();
                        float deltaXAbs = Math.abs(deltaX);
                        // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                        if ((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                            if (deltaX > 0) {
                                openProfile(item, true);
                            }
                        }

                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        super.onSingleTapUp(e);
                        if (!previousPlayer.getPlayWhenReady()) {
                            is_user_stop_video = false;
                            previousPlayer.setPlayWhenReady(true);
                        } else {
                            is_user_stop_video = true;
                            previousPlayer.setPlayWhenReady(false);
                        }

                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                        showVideoOption(item);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (!previousPlayer.getPlayWhenReady()) {
                            is_user_stop_video = false;
                            previousPlayer.setPlayWhenReady(true);
                        }
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            showHeartOnDoubleTap(item, mainLayout, e);
                            likeVideo(currentPage, item);
                        } else {
                            Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
                        }
                        return super.onDoubleTap(e);

                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            TextView desc_txt = layout.findViewById(R.id.desc_txt);
            HashTagHelper.Creator.create(context.getResources().getColor(R.color.maincolor), new HashTagHelper.OnHashTagClickListener() {
                @Override
                public void onHashTagClicked(String hashTag) {
                    onPause();
                    openHashtag(hashTag);
                }
            }).handle(desc_txt);

            LinearLayout soundimage = (LinearLayout) layout.findViewById(R.id.sound_image_layout);
            Animation soundAnimation = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation);
            soundimage.startAnimation(soundAnimation);

            if (Variables.sharedPreferences.getBoolean(Variables.islogin, false))
                Functions.callApiForUpdateView(getActivity(), item.video_id);

            swipe_count++;
            if (swipe_count > 4) {
                // showAdd();
                swipe_count = 0;
            }

            if (currentPage == dataList.size() - 1) {
                pageNumber = pageNumber + 1;
                handleApiCallRequest();
            } else {
                recyclerView.post(new Runnable() {
                    public void run() {
                        // There is no need to use notifyDataSetChanged()
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        }
    }

    private void setUpVideoCache() {
        if (currentPage + 1 < dataList.size()) {
            HttpProxyCacheServer proxy = getProxy(context);
            String url = "";
            if (dataList.get(currentPage + 1).video_url.isEmpty()) {

            } else {
                url = dataList.get(currentPage + 1).video_url;
            }
            proxy.getProxyUrl(url);
        }
    }

    public void showHeartOnDoubleTap(Home item, final RelativeLayout mainlayout, MotionEvent e) {

        int x = (int) e.getX() - 100;
        int y = (int) e.getY() - 100;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(getApplicationContext());
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        if (item.liked.equals("1"))
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like));
        else
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like_fill));

        mainlayout.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(context, R.anim.fade_out);

        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainlayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(fadeoutani);

    }

//    public void showAdd() {
//        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        }
//    }

    @Override
    public void onDataSent(String yourData) {
        int comment_count = Integer.parseInt(yourData);
        Home item = dataList.get(currentPage);
        item.video_comment_count = "" + comment_count;
        dataList.remove(currentPage);
        dataList.add(currentPage, item);
        adapter.notifyDataSetChanged();
    }


    public void setDownloadListener(VideoDownloadedListener downloadListener) {
        videoDownloadedListener = downloadListener;
    }

    // this will call when go to the home tab From other tab.
    // this is very importent when for video play and pause when the focus is changes
    boolean is_visible_to_user;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        is_visible_to_user = isVisibleToUser;

        if (previousPlayer != null && (isVisibleToUser && !is_user_stop_video)) {
            previousPlayer.setPlayWhenReady(true);
        } else if (previousPlayer != null && !isVisibleToUser) {
            previousPlayer.setPlayWhenReady(false);
        }
    }


    public void releasePreviousPlayer() {
        if (previousPlayer != null) {
            previousPlayer.removeListener(this);
            previousPlayer.release();
        }
    }


    // this function will call for like the video and Call an Api for like the video
    public void likeVideo(final int position, final Home home_) {
        String action = home_.liked;

        if (action.equals("1")) {
            action = "0";
            home_.like_count = "" + (Integer.parseInt(home_.like_count) - 1);
        } else {
            action = "1";
            home_.like_count = "" + (Integer.parseInt(home_.like_count) + 1);
        }

        dataList.remove(position);
        home_.liked = action;
        dataList.add(position, home_);
        adapter.notifyDataSetChanged();

        Functions.callApiForLikeVideo(getActivity(), home_.video_id, action, new ApiCallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
            }

            @Override
            public void onSuccess(String response) {
            }

            @Override
            public void onFailure(String response) {
            }
        });

    }


    // this will open the comment screen
    private void openComment(Home item) {

        int comment_counnt = Integer.parseInt(item.video_comment_count);
        FragmentDataSend fragment_data_send = this;
        CommentFragment comment_fragment = new CommentFragment(comment_counnt, fragment_data_send);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id", item.video_id);
        args.putString("user_id", item.fb_id);
        comment_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, comment_fragment).commit();

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile(Home item, boolean from_right_to_left) {
        if (Variables.sharedPreferences.getString(Variables.u_id, "0").equals(item.fb_id)) {
            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {
            ProfileFragment profile_fragment = new ProfileFragment(new FragmentCallback() {
                @Override
                public void responseCallBackFromFragment(Bundle bundle) {
                    adapter.notifyDataSetChanged();
                }
            });
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            if (from_right_to_left)
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            else
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            Bundle args = new Bundle();
            args.putString("user_id", item.fb_id);
            args.putString("user_name", item.first_name + " " + item.last_name);
            args.putString("user_pic", item.profile_pic);
            profile_fragment.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, profile_fragment).commit();
        }

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void openHashtag(String tag) {

        TaggedVideosFragment taged_videos_fragment = new TaggedVideosFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, taged_videos_fragment).commit();

    }


    private void showVideoOption(final Home home_) {

        final CharSequence[] options = {"Save Video", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle(null);
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video")) {
                    if (Functions.checkstoragepermision(getActivity()))
                        saveVideo(home_);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    private void saveVideo(final Home item) {
        mActivity.showProgressDialog();
        PRDownloader.initialize(getActivity().getApplicationContext());
        DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.APP_FOLDER, item.video_id + "no_watermark" + ".mp4")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                    }
                });


        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                applyWatermark(item);
            }

            @Override
            public void onError(Error error) {
                deleteFileNoWatermark(item);
                Toast.makeText(context, R.string.error_saving_video, Toast.LENGTH_SHORT).show();
                mActivity.dismissProgressDialog();
            }
        });
    }

    public void applyWatermark(final Home item) {
        mActivity.showProgressDialog();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_watermark, options);
        GlWatermarkFilter filter = new GlWatermarkFilter(logo, GlWatermarkFilter.Position.LEFT_TOP);
        new GPUMp4Composer(Variables.APP_FOLDER + item.video_id + "no_watermark" + ".mp4",
                Variables.APP_FOLDER + item.video_id + ".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                    }

                    @Override
                    public void onCompleted() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.dismissProgressDialog();
                                deleteFileNoWatermark(item);
                                scanFile(item);
                            }
                        });
                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    deleteFileNoWatermark(item);
                                    mActivity.dismissProgressDialog();
                                    Toast.makeText(context, R.string.try_again, Toast.LENGTH_SHORT).show();

                                } catch (Exception e) {

                                }
                            }
                        });

                    }
                })
                .start();
    }


    public void deleteFileNoWatermark(Home item) {
        File file = new File(Variables.APP_FOLDER + item.video_id + "no_watermark" + ".mp4");
        if (file.exists()) {
            file.delete();
        }
    }

    private void scanFile(Home item) {
        MediaScannerConnection.scanFile(getActivity(),
                new String[]{Variables.APP_FOLDER + item.video_id + ".mp4"},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
//                        Log.i("ExternalStorage", "Scanned " + path + ":");
//                        Log.i("ExternalStorage", "-> uri=" + uri);
                        if (videoDownloadedListener != null) {
                            videoDownloadedListener.onDownloadCompleted(uri);
                        }
                    }
                });
    }


    private void toggleSound(int position, Home item) {
        if (previousPlayer != null) {
            float volume = !item.isMute ? 0.0F : 1.0F;
            previousPlayer.setVolume(volume);
            isMuted = !item.isMute;
        }

        if (item != null) {
            dataList.remove(position);
            item.isMute = isMuted;
            dataList.add(position, item);
            adapter.notifyDataSetChanged();
        }
    }

    public boolean doesfragmentExits() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0) {
            return false;
        } else {
            return true;
        }

    }

    // this is lifecyle of the Activity which is important for play, pause video or release the player
    @Override
    public void onResume() {
        super.onResume();
        if ((previousPlayer != null && (is_visible_to_user && !is_user_stop_video)) && !doesfragmentExits()) {
            previousPlayer.setPlayWhenReady(true);
            if (playerView != null) {
                playerView.requestFocus();
                playerView.setPlayer(previousPlayer);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (previousPlayer != null) {
            previousPlayer.setPlayWhenReady(false);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (previousPlayer != null) {
            previousPlayer.setPlayWhenReady(false);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (previousPlayer != null) {
            previousPlayer.release();
        }
        getProxy(context).shutdown();
    }

    // Bottom all the function and the Call back listener of the Expo player
    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }


    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }


    @Override
    public void onLoadingChanged(boolean isLoading) {

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            pBar.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            pBar.setVisibility(View.GONE);
        }


    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    @Override
    public void onSeekProcessed() {

    }

}
