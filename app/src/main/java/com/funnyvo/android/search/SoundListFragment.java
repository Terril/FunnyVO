package com.funnyvo.android.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.funnyvo.android.R;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.simpleclasses.AdapterClickListener;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.Sounds;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SoundListFragment extends RootFragment implements Player.EventListener {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    ArrayList<Object> data_list;
    RecyclerView recyclerView;
    SoundListAdapter adapter;
    static boolean active = false;

    DownloadRequest prDownloader;

    public static String running_sound_id;

    public SoundListFragment(String type) {
        this.type = type;
    }

    public SoundListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sound_list, container, false);
        context = getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView = view.findViewById(R.id.recylerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        data_list = new ArrayList<>();
        adapter = new SoundListAdapter(context, data_list, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                Sounds item = (Sounds) object;
                if (view.getId() == R.id.done) {
                    stopPlaying();
                    downLoadMp3(item.id, item.sound_name, item.acc_path);
                } else if (view.getId() == R.id.fav_btn) {
                    callApiForFavSound(pos, item);
                } else {
                    if (thread != null && !thread.isAlive()) {
                        stopPlaying();
                        playaudio(view, item);
                    } else if (thread == null) {
                        stopPlaying();
                        playaudio(view, item);
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (view != null && data_list.isEmpty()) {
            callApi();
        }
    }

    public void callApi() {

        JSONObject params = new JSONObject();
        try {
            params.put("type", type);
            params.put("keyword", SearchMainFragment.search_edit.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.search, params, new Callback() {
            @Override
            public void response(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equals("sound"))
                    parseSounds(resp);
            }
        });

    }

    public void parseSounds(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    Sounds item = new Sounds();

                    item.id = itemdata.optString("id");

                    JSONObject audio_path = itemdata.optJSONObject("audio_path");
                    item.acc_path = audio_path.optString("acc");
                    item.sound_name = itemdata.optString("sound_name");
                    item.description = itemdata.optString("description");
                    item.section = itemdata.optString("section");
                    item.thum = Variables.base_url + itemdata.optString("thum");
                    item.date_created = itemdata.optString("created");
                    item.fav = itemdata.optString("fav");

                    data_list.add(item);
                }

                if (data_list.isEmpty()) {
                    view.findViewById(R.id.no_data_image).setVisibility(View.VISIBLE);
                } else
                    view.findViewById(R.id.no_data_image).setVisibility(View.GONE);

                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
    }


    private void callApiForFavSound(final int pos, final Sounds item) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("sound_id", item.id);
            if (item.fav.equals("1"))
                parameters.put("fav", "0");
            else
                parameters.put("fav", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        ApiRequest.callApi(context, Variables.fav_sound, parameters, new Callback() {
            @Override
            public void response(String resp) {
                Functions.cancelLoader();

                if (item.fav.equals("1"))
                    item.fav = "0";
                else
                    item.fav = "1";

                data_list.remove(item);
                data_list.add(pos, item);
                adapter.notifyDataSetChanged();

                adapter.notifyDataSetChanged();

            }
        });

    }

    View previous_view;
    Thread thread;
    SimpleExoPlayer player;
    String previous_url = "none";

    public void playaudio(View view, final Sounds item) {
        previous_view = view;

        if (previous_url.equals(item.acc_path)) {

            previous_url = "none";
            running_sound_id = "none";
        } else {

            previous_url = item.acc_path;
            running_sound_id = item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.acc_path));


            player.prepare(videoSource);
            player.addListener(this);


            player.setPlayWhenReady(true);


        }

    }


    public void stopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }
        show_Stop_state();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;

        running_sound_id = "null";

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    public void Show_Run_State() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }

    }


    public void Show_loading_state() {
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void show_Stop_state() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            previous_view.findViewById(R.id.done).setVisibility(View.GONE);
        }

        running_sound_id = "none";

    }


    public void downLoadMp3(final String id, final String sound_name, String url) {
        showProgressDialog();
        prDownloader = PRDownloader.download(url, Variables.app_folder, sound_name + id)
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
                dismissProgressDialog();
                Toast.makeText(context, "audio saved into your phone", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Error error) {
                dismissProgressDialog();
            }
        });

    }


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
            Show_loading_state();
        } else if (playbackState == Player.STATE_READY) {
            Show_Run_State();
        } else if (playbackState == Player.STATE_ENDED) {
            show_Stop_state();
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
