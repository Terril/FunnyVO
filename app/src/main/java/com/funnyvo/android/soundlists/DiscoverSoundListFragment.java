package com.funnyvo.android.soundlists;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Variables;
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

import static android.app.Activity.RESULT_OK;
import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;

public class DiscoverSoundListFragment extends RootFragment implements Player.EventListener {

    private RecyclerView recylerView;
    private SoundAdapter adapter;
    private ArrayList<SoundCategory> datalist;

    private DownloadRequest prDownloader;
    static boolean active = false;

    private View view;
    private Context context;

    private SwipeRefreshLayout swiperefresh;

    public static String running_sound_id;

    private View previous_view;
    private Thread thread;
    private SimpleExoPlayer player;
    private String previous_url = "none";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_sound_list, container, false);
        context = getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        running_sound_id = "none";

        PRDownloader.initialize(context);
        datalist = new ArrayList<>();

        recylerView = view.findViewById(R.id.recylerViewSoundList);
        recylerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recylerView.setNestedScrollingEnabled(true);
        recylerView.setHasFixedSize(true);
        recylerView.getLayoutManager().setMeasurementCacheEnabled(false);


        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                previous_url = "none";
                stopPlaying();
                callApiForGetAllSound();
            }
        });

        callApiForGetAllSound();
    }

    public void setAdapter() {

        adapter = new SoundAdapter(context, datalist, new SoundAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Sounds item) {
                if (view.getId() == R.id.done) {
                    stopPlaying();
                    downLoadMp3(item.id, item.sound_name, item.soundUrl);
                } else if (view.getId() == R.id.fav_btn) {
                    callApiForFavSound(postion, item);
                } else {
                    if (thread != null && !thread.isAlive()) {
                        stopPlaying();
                        playAudio(view, item);
                    } else if (thread == null) {
                        stopPlaying();
                        playAudio(view, item);
                    }
                }

            }
        });

        recylerView.setAdapter(adapter);

    }


    private void callApiForGetAllSound() {
        showProgressDialog();
        JSONObject parameters = new JSONObject();
        try {
            if (Variables.sharedPreferences != null)
                parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(context, Variables.ALL_SOUNDS, parameters, new Callback() {
            @Override
            public void response(String resp) {
                swiperefresh.setRefreshing(false);
                dismissProgressDialog();
                parseData(resp);
            }
        });

    }


    public void parseData(String response) {
        datalist = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = msgArray.length() - 1; i >= 0; i--) {
                    JSONObject object = msgArray.getJSONObject(i);
                    JSONArray section_array = object.optJSONArray("sections_sounds");

                    ArrayList<Sounds> sound_list = new ArrayList<>();

                    for (int j = 0; j < section_array.length(); j++) {
                        JSONObject itemdata = section_array.optJSONObject(j);

                        Sounds item = new Sounds();
                        item.id = itemdata.optString("id");

                        JSONObject audio_path = itemdata.optJSONObject("audio_path");
                        item.acc_path = audio_path.optString("acc");

                        item.sound_name = itemdata.optString("sound_name");
                        item.description = itemdata.optString("description");
                        item.section = itemdata.optString("section");
                        item.thum = itemdata.optString("thum");
                        item.date_created = itemdata.optString("created");
                        item.fav = itemdata.optString("fav");
                        item.soundUrl = itemdata.optString("sound_url");

                        sound_list.add(item);
                    }

                    SoundCategory sound_category = new SoundCategory();
                    sound_category.catagory = object.optString("section_name");
                    sound_category.sound_list = sound_list;
                    datalist.add(sound_category);
                }

                setAdapter();
            } else {
                //   Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }

    public void playAudio(View view, final Sounds item) {
        previous_view = view;

        if (previous_url.equals(item.soundUrl)) {
            previous_url = "none";
            running_sound_id = "none";
        } else {
            previous_url = item.soundUrl;
            running_sound_id = item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, APP_NAME));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.soundUrl));

            player.prepare(videoSource);
            player.addListener(this);

            player.setPlayWhenReady(true);
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        showStopState();
    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPlaying();
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

        showStopState();
        stopPlaying();
    }


    public void showRunState() {
        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }
    }


    public void showLoadingState() {
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void showStopState() {

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
        prDownloader = PRDownloader.download(url, Variables.APP_FOLDER, Variables.SelectedAudio_AAC)
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
                Intent output = new Intent();
                output.putExtra("isSelected", getString(R.string.yes));
                output.putExtra("sound_name", sound_name);
                output.putExtra("sound_id", id);
                getActivity().setResult(RESULT_OK, output);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }

            @Override
            public void onError(Error error) {
                dismissProgressDialog();
            }
        });

    }


    private void callApiForFavSound(int pos, final Sounds item) {
        showProgressDialog();
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

        ApiRequest.callApi(context, Variables.FAV_SOUND, parameters, new Callback() {
            @Override
            public void response(String resp) {
                dismissProgressDialog();
                if (item.fav.equals("1"))
                    item.fav = "0";
                else
                    item.fav = "1";

                for (int i = 0; i < datalist.size(); i++) {
                    SoundCategory catagory_get_set = datalist.get(i);
                    if (catagory_get_set.sound_list.contains(item)) {
                        int index = catagory_get_set.sound_list.indexOf(item);
                        catagory_get_set.sound_list.remove(item);
                        catagory_get_set.sound_list.add(index, item);
                        break;
                    }
                }

                adapter.notifyDataSetChanged();

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
            showLoadingState();
        } else if (playbackState == Player.STATE_READY) {
            showRunState();
        } else if (playbackState == Player.STATE_ENDED) {
            showStopState();
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
