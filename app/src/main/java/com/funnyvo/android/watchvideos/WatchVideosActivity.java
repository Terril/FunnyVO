package com.funnyvo.android.watchvideos;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
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
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.comments.CommentFragment;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.keyboard.KeyboardHeightObserver;
import com.funnyvo.android.keyboard.KeyboardHeightProvider;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.MainMenuFragment;
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
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class WatchVideosActivity extends BaseActivity implements Player.EventListener,
        KeyboardHeightObserver, View.OnClickListener, FragmentDataSend {

    private Context context;

    private RecyclerView recyclerView;
    private ArrayList<Home> data_list = new ArrayList<>();

    private int position = 0;
    private int currentPage = -1;
    private LinearLayoutManager layoutManager;

    private WatchVideosAdapter adapter;
    private ProgressBar p_bar;
    private KeyboardHeightProvider keyboardHeightProvider;
    private RelativeLayout write_layout;

    private EditText message_edit;
    private ImageButton send_btn;
    private ProgressBar send_progress;

    private String video_id;
    private String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_watchvideo);
        context = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Variables.sharedPreferences == null) {
            Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE);
        }

        p_bar = findViewById(R.id.p_bar);

        Intent bundle = getIntent();
        if (bundle != null) {

            Uri appLinkData = bundle.getData();
            video_id = bundle.getStringExtra("video_id");

            if (video_id != null) {
                callApiForGetAllvideos(video_id);
            } else if (appLinkData == null) {
                data_list = (ArrayList<Home>) bundle.getSerializableExtra("arraylist");
                position = bundle.getIntExtra("position", 0);
            } else {
                link = appLinkData.toString();
                String[] parts = link.split("=");
                video_id = parts[1];
                callApiForGetAllvideos(parts[1]);
            }
        }

        setAdapter();
        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        write_layout = findViewById(R.id.write_layout);
        message_edit = findViewById(R.id.message_edit);
        send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(this);

        send_progress = findViewById(R.id.send_progress);

        keyboardHeightProvider = new KeyboardHeightProvider(this);
        findViewById(R.id.WatchVideo_F).post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();

            }
        });

    }


    @Override
    public void onBackPressed() {

        if (video_id != null && link != null) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }

    }

    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllvideos(String id) {
        if (MainMenuActivity.token == null)
            MainMenuActivity.token = FirebaseInstanceId.getInstance().getToken();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("token", MainMenuActivity.token);
            parameters.put("video_id", id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.callApi(context, Variables.showAllVideos, parameters, new Callback() {
            @Override
            public void response(String resp) {
                parseData(resp);
            }
        });
    }

    public void parseData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    Home item = new Home();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject user_info = itemdata.optJSONObject("user_info");
                    item.username = user_info.optString("username");
                    item.first_name = user_info.optString("first_name", context.getResources().getString(R.string.app_name));
                    item.last_name = user_info.optString("last_name", "User");
                    item.profile_pic = user_info.optString("profile_pic", "null");
                    item.verified = user_info.optString("verified");

                    JSONObject sound_data = itemdata.optJSONObject("sound");
                    item.sound_id = sound_data.optString("id");
                    item.sound_name = sound_data.optString("sound_name");
                    item.sound_pic = sound_data.optString("thum");


                    JSONObject count = itemdata.optJSONObject("count");
                    item.like_count = count.optString("like_count");
                    item.video_comment_count = count.optString("video_comment_count");

                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");

                    item.video_url = itemdata.optString("video");
                    item.video_description = itemdata.optString("description");

                    item.thum = itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");
                    if (item.video_url.contains(Variables.base_url)) {
                        item.video_url = item.video_url.replace(Variables.base_url + "/", "");
                    }
                    if (item.thum.contains(Variables.base_url)) {
                        item.thum = item.thum.replace(Variables.base_url + "/", "");
                    }
                    data_list.add(item);
                }

                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    private void callApiForSinglevideos(final int postion) {

        try {
            JSONObject parameters = new JSONObject();

            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("token", Variables.sharedPreferences.getString(Variables.device_token, "Null"));
            parameters.put("video_id", data_list.get(postion).video_id);


            ApiRequest.callApi(context, Variables.showAllVideos, parameters, new Callback() {
                @Override
                public void response(String resp) {
                    singalVideoParseData(postion, resp);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    public void singalVideoParseData(int pos, String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    Home item = new Home();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject user_info = itemdata.optJSONObject("user_info");

                    item.username = user_info.optString("username");
                    item.first_name = user_info.optString("first_name", context.getResources().getString(R.string.app_name));
                    item.last_name = user_info.optString("last_name", "User");
                    item.profile_pic = user_info.optString("profile_pic", "null");
                    item.verified = user_info.optString("verified");

                    JSONObject sound_data = itemdata.optJSONObject("sound");
                    item.sound_id = sound_data.optString("id");
                    item.sound_name = sound_data.optString("sound_name");
                    item.sound_pic = sound_data.optString("thum");


                    JSONObject count = itemdata.optJSONObject("count");
                    item.like_count = count.optString("like_count");
                    item.video_comment_count = count.optString("video_comment_count");


                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");

                    item.video_url = itemdata.optString("video");

                    item.video_description = itemdata.optString("description");

                    item.thum = itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");
                    if (item.video_url.contains(Variables.base_url)) {
                        item.video_url = item.video_url.replace(Variables.base_url + "/", "");
                    }
                    if (item.thum.contains(Variables.base_url)) {
                        item.thum = item.thum.replace(Variables.base_url + "/", "");
                    }

                    data_list.remove(pos);
                    data_list.add(pos, item);
                    adapter.notifyDataSetChanged();
                }


            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    public void setAdapter() {
        recyclerView = findViewById(R.id.recylerview);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper = new PagerSnapHelper();
        recyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(recyclerView);


        adapter = new WatchVideosAdapter(context, data_list, new WatchVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, final Home item, View view) {
                switch (view.getId()) {
                    case R.id.user_pic:
                        onPause();
                        openProfile(item, false);
                        break;

                    case R.id.like_layout:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            Like_Video(postion, item);
                        } else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.comment_layout:
                        openComment(item);
                        break;

                    case R.id.btnShare:
                        final VideoActionFragment fragment = new VideoActionFragment(item.video_id, new FragmentCallback() {
                            @Override
                            public void responseCallBackFromFragment(Bundle bundle) {

                                if (bundle.getString("action").equals("save")) {
                                    saveVideo(item);
                                }
                                if (bundle.getString("action").equals("delete")) {
                                    showProgressDialog();
                                    Functions.callApiForDeleteVideo(WatchVideosActivity.this, item.video_id, new ApiCallBack() {
                                        @Override
                                        public void arrayData(ArrayList arrayList) {

                                        }

                                        @Override
                                        public void onSuccess(String responce) {

                                            dismissProgressDialog();
                                            finish();

                                        }

                                        @Override
                                        public void onFailure(String responce) {

                                        }
                                    });

                                }
                            }
                        });

                        Bundle bundle = new Bundle();
                        bundle.putString("video_id", item.video_id);
                        bundle.putString("user_id", item.fb_id);
                        fragment.setArguments(bundle);

                        fragment.show(getSupportFragmentManager(), "");

                        break;


                    case R.id.sound_image_layout:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            if (check_permissions()) {
                                Intent intent = new Intent(WatchVideosActivity.this, VideoSoundActivity.class);
                                intent.putExtra("data", item);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                }

            }
        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);


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
                    currentPage = page_no;

                    Privious_Player();
                    setPlayer(currentPage);
                }

            }
        });

        recyclerView.scrollToPosition(position);

    }


    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }


    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        Log.d("resp", "" + height);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(write_layout.getWidth(), write_layout.getHeight());
        params.bottomMargin = height;
        write_layout.setLayoutParams(params);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {

                    String comment_txt = message_edit.getText().toString();
                    if (!TextUtils.isEmpty(comment_txt)) {
                        sendComments(data_list.get(currentPage).fb_id, data_list.get(currentPage).video_id, comment_txt);
                    }


                } else {
                    Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onDataSent(String yourData) {
        int comment_count = Integer.parseInt(yourData);
        Home item = data_list.get(currentPage);
        item.video_comment_count = "" + comment_count;
        data_list.add(currentPage, item);
        adapter.notifyDataSetChanged();
    }


    public void setPlayer(final int currentPage) {

        final Home item = data_list.get(currentPage);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(item.video_url));

        if (!Variables.is_secure_info)
            Log.d(Variables.tag, item.video_url);


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);


        View layout = layoutManager.findViewByPosition(currentPage);
        PlayerView playerView = layout.findViewById(R.id.playerview);
        playerView.setPlayer(player);


        player.setPlayWhenReady(true);
        privious_player = player;


        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
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
                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    } else {
                        privious_player.setPlayWhenReady(false);
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    }

                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {

                        Show_heart_on_DoubleTap(item, mainlayout, e);
                        Like_Video(currentPage, item);

                    } else {
                        Toast.makeText(context, "Please Login into ", Toast.LENGTH_SHORT).show();
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

                OpenHashtag(hashTag);

            }
        }).handle(desc_txt);


        LinearLayout soundimage = (LinearLayout) layout.findViewById(R.id.sound_image_layout);
        Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation);
        soundimage.startAnimation(aniRotate);

        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false))
            Functions.callApiForUpdateView(WatchVideosActivity.this, item.video_id);


        callApiForSinglevideos(currentPage);
    }


    // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;

    public void Privious_Player() {
        if (privious_player != null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }


    public void Show_heart_on_DoubleTap(Home item, final RelativeLayout mainlayout, MotionEvent e) {

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


    // this function will call for like the video and Call an Api for like the video
    public void Like_Video(final int position, final Home home_) {

        String action = home_.liked;

        if (action.equals("1")) {
            action = "0";
            home_.like_count = "" + (Integer.parseInt(home_.like_count) - 1);
        } else {
            action = "1";
            home_.like_count = "" + (Integer.parseInt(home_.like_count) + 1);
        }


        data_list.remove(position);
        home_.liked = action;
        data_list.add(position, home_);
        adapter.notifyDataSetChanged();


        Functions.callApiForLikeVideo(this, home_.video_id, action, new ApiCallBack() {

            @Override
            public void arrayData(ArrayList arrayList) {

            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }
        });
    }


    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 2);
        } else {

            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    // this will open the comment screen
    public void openComment(Home item) {
        int comment_count = Integer.parseInt(item.video_comment_count);
        FragmentDataSend fragment_data_send = this;

        CommentFragment comment_fragment = new CommentFragment(comment_count, fragment_data_send);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id", item.video_id);
        args.putString("user_id", item.fb_id);
        comment_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, comment_fragment).commit();

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

                    callApiForSinglevideos(currentPage);

                }
            });
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

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
            transaction.replace(R.id.WatchVideo_F, profile_fragment).commit();

        }


    }


    public void sendComments(final String user_id, String video_id, final String comment) {

        send_progress.setVisibility(View.VISIBLE);
        send_btn.setVisibility(View.GONE);

        Functions.callApiToSendComment(this, video_id, comment, new ApiCallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {

                message_edit.setText(null);
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);

                int comment_count = Integer.parseInt(data_list.get(currentPage).video_comment_count);
                comment_count++;
                onDataSent("" + comment_count);


            }

            @Override
            public void onSuccess(String responce) {

            }

            @Override
            public void onFailure(String responce) {

            }
        });

        SendPushNotification(user_id, comment);
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

        TaggedVideosFragment taged_videos_fragment = new TaggedVideosFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_fragment.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, taged_videos_fragment).commit();

    }


    CharSequence[] options;

    private void Show_video_option(final Home home_) {

        options = new CharSequence[]{"Save Video", "Cancel"};

        if (home_.fb_id.equals(Variables.sharedPreferences.getString(Variables.u_id, "")))
            options = new CharSequence[]{"Save Video", "Delete Video", "Cancel"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video")) {
                    if (Functions.checkstoragepermision(WatchVideosActivity.this))
                        saveVideo(home_);

                } else if (options[item].equals("Delete Video")) {
                    if (Variables.is_secure_info) {
                        Toast.makeText(context, getString(R.string.delete_function_not_available_in_demo), Toast.LENGTH_SHORT).show();
                    } else {
                        Functions.showLoader(WatchVideosActivity.this, false, false);
                        Functions.callApiForDeleteVideo(WatchVideosActivity.this, home_.video_id, new ApiCallBack() {
                            @Override
                            public void arrayData(ArrayList arrayList) {

                            }

                            @Override
                            public void onSuccess(String responce) {

                                Functions.cancelLoader();
                                finish();

                            }

                            @Override
                            public void onFailure(String responce) {

                            }
                        });
                    }
                } else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    public void saveVideo(final Home item) {
        showProgressDialog();
        PRDownloader.initialize(getApplicationContext());
        DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.app_folder, item.video_id + "no_watermark" + ".mp4")
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
                Delete_file_no_watermark(item);
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
            }


        });


    }

    public void applyWatermark(final Home item) {
        Bitmap logo = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_watermark)).getBitmap();
        GlWatermarkFilter filter = new GlWatermarkFilter(logo, GlWatermarkFilter.Position.LEFT_TOP);
        new GPUMp4Composer(Variables.app_folder + item.video_id + "no_watermark" + ".mp4",
                Variables.app_folder + item.video_id + ".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                        showProgressDialog();

                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                Delete_file_no_watermark(item);
                                Scan_file(item);

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp", exception.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Delete_file_no_watermark(item);
                                    dismissProgressDialog();
                                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();

                                } catch (Exception e) {

                                }
                            }
                        });

                    }
                })
                .start();
    }


    public void Delete_file_no_watermark(Home item) {
        File file = new File(Variables.app_folder + item.video_id + "no_watermark" + ".mp4");
        if (file.exists()) {
            file.delete();
        }
    }

    public void Scan_file(Home item) {
        MediaScannerConnection.scanFile(WatchVideosActivity.this,
                new String[]{Variables.app_folder + item.video_id + ".mp4"},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }


    public void SendPushNotification(String user_id, String comment) {

        JSONObject notimap = new JSONObject();
        try {
            notimap.put("title", Variables.sharedPreferences.getString(Variables.u_name, "") + " Comment on your video");
            notimap.put("message", comment);
            notimap.put("icon", Variables.sharedPreferences.getString(Variables.u_pic, ""));
            notimap.put("senderid", Variables.sharedPreferences.getString(Variables.u_id, ""));
            notimap.put("receiverid", user_id);
            notimap.put("action_type", "comment");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.sendPushNotification, notimap, null);

    }


    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onPause() {
        super.onPause();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (privious_player != null) {
            privious_player.release();
        }

        keyboardHeightProvider.close();
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
            p_bar.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            p_bar.setVisibility(View.GONE);
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
