package com.funnyvo.android.soundlists.favouritesounds;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.funnyvo.android.R;
import com.funnyvo.android.helper.PermissionUtils;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.Sounds;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceSoundFragment extends RootFragment implements Player.EventListener, SearchView.OnQueryTextListener {

    private Context context;
    private View view;
    private ArrayList<Sounds> audioList;
    private DeviceSoundAdapter adapter;
    static boolean active = false;
    private RecyclerView recyclerView;

    private DownloadRequest prDownloader;

    static String running_sound_id;
    private SwipeRefreshLayout swiperefresh;

    private View previous_view;
    private Thread thread;
    private SimpleExoPlayer player;
    private String previous_url = "none";

    public DeviceSoundFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_sound_list, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();

        running_sound_id = "none";

        PRDownloader.initialize(context);

        recyclerView = view.findViewById(R.id.recylerViewSoundList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllAudioFromDevice(context);
            }
        });

        if (PermissionUtils.INSTANCE.checkPermissions(getActivity())) {
            getAllAudioFromDevice(context);
        }
        setHasOptionsMenu(true);
    }

    private void searchSound(Menu menu) {
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.searchSound).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sound_search_option_menu, menu);
        searchSound(menu);
    }

    private void setAdapter() {
        adapter = new DeviceSoundAdapter(context, audioList, new DeviceSoundAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Sounds item) {

                if (view.getId() == R.id.btnSoundSelected) {
                    stopPlaying();
                    downLoadMp3(item.id, item.sound_name, item.acc_path);
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

        recyclerView.setAdapter(adapter);
    }

    private void getAllAudioFromDevice(final Context context) {
        audioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST,};
        Cursor c = context.getContentResolver().query(uri,
                projection,
                null,
                null,
                null);

        if (c != null) {
            while (c.moveToNext()) {

                Sounds audioModel = new Sounds();
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.sound_name = name;
                audioModel.description = album;
                // audioModel.setaArtist(artist);
                audioModel.acc_path = path;
//
//                Log.e("Name :" + name, " Album :" + album);
//                Log.e("Path :" + path, " Artist :" + artist);

                audioList.add(audioModel);
            }
            c.close();
        }

        setAdapter();
    }

    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }

    private void playAudio(View view, final Sounds item) {
        previous_view = view;

        if (previous_url.equals(item.acc_path)) {
            previous_url = "none";
            running_sound_id = "none";
        } else {

            previous_url = item.acc_path;
            running_sound_id = item.id;

            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            player = new SimpleExoPlayer.Builder(context, renderersFactory).build();

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, Variables.APP_NAME));

            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
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
            previous_view.findViewById(R.id.btnSoundSelected).setVisibility(View.VISIBLE);
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
            previous_view.findViewById(R.id.btnSoundSelected).setVisibility(View.GONE);
        }

        running_sound_id = "none";

    }


    private void downLoadMp3(final String id, final String soundName, String url) {
        copyAudio(soundName, url);
    }

    private void copyAudio(final String soundName, String sourcePath) {
        showProgressDialog();
        final String[] complexCommand = {"-y", "-i", sourcePath, Variables.APP_FOLDER + Variables.SelectedAudio_AAC};

        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object[] objects) {
                int rc = FFmpeg.execute(complexCommand);
                return rc;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                int rc = (int) o;

                if (rc == RETURN_CODE_SUCCESS) {
                    Log.d(Variables.tag, "Command execution completed successfully.");
                    dismissProgressDialog();
                    Intent output = new Intent();
                    output.putExtra("isSelected", getString(R.string.yes));
                    output.putExtra("sound_name", soundName);
                    output.putExtra("sound_id", "null");
                    getActivity().setResult(RESULT_OK, output);
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
                } else if (rc == RETURN_CODE_CANCEL) {
                    dismissProgressDialog();
                } else {
                    Config.printLastCommandOutput(Log.INFO);
                    dismissProgressDialog();
                }
            }
        }.execute();
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


    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        filter(query.trim());
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText.trim());
        return false;
    }

    private void filter(String text) {
        if (!text.isEmpty()) {
            ArrayList tempSounds = new ArrayList<Sounds>();
            for (Sounds sounds : audioList) {
                if (sounds.sound_name.toLowerCase().contains(text.toLowerCase())) {
                    tempSounds.add(sounds);
                }
            }
            adapter.updateList(tempSounds);
        } else {
            setAdapter();
            stopPlaying();
        }
    }
}
