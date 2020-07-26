package com.funnyvo.android.videorecording;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.coremedia.iso.boxes.Container;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;
import com.daasuu.gpuv.player.GPUPlayerView;
import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.customview.FunnyVOEditTextView;
import com.funnyvo.android.filter.FilterAdapter;
import com.funnyvo.android.filter.FilterType;
import com.funnyvo.android.helper.PlayerEventListener;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.SoundListMainActivity;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static com.funnyvo.android.videorecording.VideoRecoderActivity.SOUNDS_LIST_REQUEST_CODE;

public class PreviewVideoActivity extends BaseActivity implements View.OnClickListener, MergeVideoAudioCallBack {

    public static final int CROP_RESULT = 101;
    private GPUPlayerView gpuPlayerView;
    private MediaPlayer audio;
    public static int select_postion = 0;
    private final List<FilterType> filterTypes = FilterType.createFilterList();
    private FilterAdapter adapter;
    private RecyclerView recylerview;
    private LinearLayout layoutViewInputText;
    private PlayerEventListener eventListener;
    private FunnyVOEditTextView edtVideoMessage;
    private Button btnAddMusic;
    private String draft_file;
    private String path;
    private boolean isMotionFilterSelected = false, isTextFilterSelected = false;
    private boolean isUp = false;
    private boolean isSlowMoEnabled = true;
    private boolean isFastMoEnabled = true;
    private boolean isFromGallery = false;
    private String tempOutputSource = Variables.outputfile2;
    // this function will set the player to the current video

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigation();
        setContentView(R.layout.activity_preview_video);

        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("video_path");
            draft_file = intent.getStringExtra("draft_file");
            isFromGallery = intent.getBooleanExtra("isFromGallery", false);
        }

        eventListener = new PlayerEventListener();
        select_postion = 0;
        String videoUrl = Variables.outputfile2;

        btnAddMusic = findViewById(R.id.btnAddMusic);
        btnAddMusic.setOnClickListener(this);
        findViewById(R.id.btnGoBackPreview).setOnClickListener(this);
        findViewById(R.id.btnSlowMotion).setOnClickListener(this);
        findViewById(R.id.btnNext).setOnClickListener(this);
        findViewById(R.id.btnFastMotion).setOnClickListener(this);
        findViewById(R.id.btnCrop).setOnClickListener(this);
        findViewById(R.id.btnFilter).setOnClickListener(this);
        findViewById(R.id.btnTextEditor).setOnClickListener(this);
        findViewById(R.id.btnTextAdded).setOnClickListener(this);

        if (!isFromGallery) {
            btnAddMusic.setText(getString(R.string.preview));
            btnAddMusic.setEnabled(false);
        } else {
            videoUrl = path;
            Variables.Selected_sound_id = "null";
            btnAddMusic.setEnabled(true);
            append(false, path); //galleryAppend();
        }


        gpuPlayerView = setPlayer(Uri.parse(videoUrl), eventListener);
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(gpuPlayerView);
        gpuPlayerView.onResume();
        recylerview = findViewById(R.id.recylerviewPreview);
        layoutViewInputText = findViewById(R.id.layoutViewInputText);
        edtVideoMessage = findViewById(R.id.edtVideoMessage);
        edtVideoMessage.setFilters(new InputFilter[]{EMOJI_FILTER});

        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoUrl,
                MediaStore.Video.Thumbnails.MINI_KIND);
        Bitmap bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (int) (bmThumbnail.getWidth() * 0.4), (int) (bmThumbnail.getHeight() * 0.4), true);

        adapter = new FilterAdapter(this, filterTypes, bmThumbnailResized, new FilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, FilterType item) {
                select_postion = postion;
                gpuPlayerView.setElevation(3.0F);

                gpuPlayerView.setGlFilter(new GlFilterGroup(FilterType.createGlFilter(filterTypes.get(postion), getApplicationContext(), null)));
                adapter.notifyDataSetChanged();
            }
        });
        recylerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recylerview.setAdapter(adapter);

    }

    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        try {
            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
        } catch (Exception e) {

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.removeListener(eventListener);
            player.release();
            player = null;
        }
        try {
            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
        } catch (Exception e) {

        }
    }

    // this function will add the filter to video and save that same video for post the video in post video screen
    private void saveVideo(String srcMp4Path, final String destMp4Path) {
        showProgressDialog();
        new GPUMp4Composer(srcMp4Path, destMp4Path)
                //.size(540, 960)
                //.videoBitrate((int) (0.25 * 16 * 540 * 960))
                .filter(new GlFilterGroup(FilterType.createGlFilterWithOverlay(filterTypes.get(select_postion), getApplicationContext(), getBitmapForLogo())))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                    }

                    @Override
                    public void onCompleted() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    dismissProgressDialog();
                                    gotoPostScreen();
                                } catch (Exception e) {

                                }
                            }
                        });
                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    dismissProgressDialog();
                                    Toast.makeText(PreviewVideoActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {

                                }
                            }
                        });

                    }
                })
                .start();
    }

    private Bitmap getBitmapForLogo() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_watermark, options);
    }

    private void gotoPostScreen() {
        Intent intent = new Intent(PreviewVideoActivity.this, PostVideoActivity.class);
        intent.putExtra("path", Variables.outputfile2);
        intent.putExtra("draft_file", draft_file);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void prepareAudio() {
        player.setVolume(0);
        File file = new File(Variables.app_folder + Variables.SelectedAudio_AAC);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(Variables.app_folder + Variables.SelectedAudio_AAC);
                audio.prepare();
                audio.setLooping(true);

                player.seekTo(0);
                player.setPlayWhenReady(true);
                audio.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        super.onBackPressed();
    }

    private void applySlowMoVideo() {
        showProgressDialog();
        final String[] complexCommand = {"-y", "-i", Variables.outputfile2, "-filter_complex", "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MOTION};

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
                    updateMediaSource(Variables.OUTPUT_FILE_MOTION);
                    isMotionFilterSelected = true;
                    tempOutputSource = Variables.outputfile2;
                    Variables.outputfile2 = Variables.OUTPUT_FILE_MOTION;
                } else if (rc == RETURN_CODE_CANCEL) {
                    dismissProgressDialog();
                } else {
                    Config.printLastCommandOutput(Log.INFO);
                    dismissProgressDialog();
                }

            }
        }.execute();
    }

    private void applyFastMoVideo() {
        showProgressDialog();
        final String[] complexCommand = {"-y", "-i", Variables.outputfile2, "-filter_complex", "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MOTION};

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
                    dismissProgressDialog();
                    updateMediaSource(Variables.OUTPUT_FILE_MOTION);
                    isMotionFilterSelected = true;
                    tempOutputSource = Variables.outputfile2;
                    Variables.outputfile2 = Variables.OUTPUT_FILE_MOTION;
                } else if (rc == RETURN_CODE_CANCEL) {
                    dismissProgressDialog();
                } else {
                    Config.printLastCommandOutput(Log.INFO);
                    dismissProgressDialog();
                }

            }
        }.execute();
    }

    private void applyMessageOnVideo() {
        showProgressDialog();
        String mesaage = edtVideoMessage.getText().toString().trim();
        String msg = mesaage.substring(0, mesaage.length() / 2);
        String msg1 = mesaage.substring((mesaage.length() / 2) + 1);

        mesaage = msg + "\n" + msg1;
        final String[] complexCommand = new String[]{
                "-y", "-i", Variables.outputfile2, "-vf", "drawtext=text=" + mesaage + ":fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=21: x=(w-tw)/2: y=(h/PHI)+th", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MESSAGE
        };
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
                    dismissProgressDialog();
                    updateMediaSource(Variables.OUTPUT_FILE_MESSAGE);
                    isTextFilterSelected = true;
                } else if (rc == RETURN_CODE_CANCEL) {
                    dismissProgressDialog();
                } else {
                    Config.printLastCommandOutput(Log.INFO);
                    dismissProgressDialog();
                }

            }
        }.execute();
    }

    private void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    private void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void slideFilterView() {
        if (isUp) {
            slideDown(recylerview);
        } else {
            slideUp(recylerview);
        }
        isUp = !isUp;
    }

    private void toggleSlowMo() {
        if (isSlowMoEnabled) {
            applySlowMoVideo();
        } else {
            isMotionFilterSelected = false;
            Variables.outputfile2 = tempOutputSource;
            updateMediaSource(Variables.outputfile2);
        }
        isSlowMoEnabled = !isSlowMoEnabled;
    }

    private void toggleFastMo() {
        if (isFastMoEnabled) {
            applyFastMoVideo();
        } else {
            isMotionFilterSelected = false;
            Variables.outputfile2 = tempOutputSource;
            updateMediaSource(Variables.outputfile2);
        }
        isFastMoEnabled = !isFastMoEnabled;
    }

    private void slideFilterTextView() {
        if (isUp) {
            isTextFilterSelected = false;
            slideDown(layoutViewInputText);
        } else {
            slideUp(layoutViewInputText);
        }
        isUp = !isUp;
    }

    private static InputFilter EMOJI_FILTER = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {
                int type = Character.getType(source.charAt(index));
                if (type == Character.SURROGATE) {
                    return "";
                }
            }
            return null;
        }
    };

    private void openVideoCropActivity() {
        Intent intent = new Intent(this, VideoCropActivity.class);
        startActivityForResult(intent, CROP_RESULT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSlowMotion:
                toggleSlowMo();
                break;
            case R.id.btnFastMotion:
                toggleFastMo();
                break;
            case R.id.btnGoBackPreview:
                onBackPressed();
                break;
            case R.id.btnNext:
                if (isFromGallery) {
                    try {
                        if (player != null) {
                            player.setPlayWhenReady(false);
                        }
                        if (audio != null) {
                            audio.release();
                        }
                    } catch (IllegalStateException ile) {
                        Log.e(Variables.APP_NAME, "Internal audio system crashed");
                    }
                    // append(true, path);
                }
                if (isFromGallery && audio != null) {
                    append(true, Variables.outputfile2);
                } else {
                    finalTouchesToVideo();
                }
                break;
            case R.id.btnFilter:
                slideFilterView();
                break;
            case R.id.btnCrop:
                openVideoCropActivity();
                break;
            case R.id.btnTextEditor:
                slideFilterTextView();
                break;
            case R.id.btnTextAdded:
                applyMessageOnVideo();
                break;
            case R.id.btnAddMusic:
                Intent intent = new Intent(this, SoundListMainActivity.class);
                startActivityForResult(intent, SOUNDS_LIST_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CROP_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                updateMediaSource(result);
                Variables.outputfile2 = result;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        if (requestCode == SOUNDS_LIST_REQUEST_CODE) {
            if (data != null) {
                if (data.getStringExtra("isSelected").equals("yes")) {
                    btnAddMusic.setText(data.getStringExtra("sound_name"));
                    Variables.Selected_sound_id = data.getStringExtra("sound_id");
                    prepareAudio();
                }

            }
        }
    }

    private void finalTouchesToVideo() {
        if (isMotionFilterSelected) {
            Variables.outputfile2 = Variables.OUTPUT_FILE_MOTION;
        } else if (isTextFilterSelected) {
            Variables.outputfile2 = Variables.OUTPUT_FILE_MESSAGE;
        }
        saveVideo(Variables.outputfile2, Variables.OUTPUT_FILTER_FILE);
    }

    // this will append all the videos parts in one  fullvideo
    private boolean append(final boolean isPostNeeded, final String videoPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showProgressDialog();
                    }
                });

                ArrayList<String> video_list = new ArrayList<>();
                File file = new File(videoPath);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(PreviewVideoActivity.this, Uri.parse(videoPath));
                String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                boolean isVideo = getString(R.string.yes).equals(hasVideo);

                if (isVideo && file.length() > 3000) {
                    video_list.add(videoPath);
                }

                try {
                    Movie[] inMovies = new Movie[video_list.size()];
                    for (int i = 0; i < video_list.size(); i++) {
                        inMovies[i] = MovieCreator.build(video_list.get(i));
                    }

                    List<Track> videoTracks = new LinkedList<Track>();
                    List<Track> audioTracks = new LinkedList<Track>();
                    for (Movie m : inMovies) {
                        for (Track t : m.getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    }
                    Movie result = new Movie();
                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }

                    Container out = new DefaultMp4Builder().build(result);

                    String outputFilePath = null;
                    if (audio != null) {
                        outputFilePath = Variables.outputfile;
                    } else {
                        outputFilePath = Variables.outputfile2;
                    }

                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                    out.writeContainer(fos.getChannel());
                    fos.close();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (audio != null)
                                mergeWithAudio();
                            else {
                                if (isPostNeeded) {
                                    saveVideo(Variables.outputfile2, Variables.OUTPUT_FILTER_FILE);
                                } else {
                                    dismissProgressDialog();
                                }
                            }
                        }
                    });

                } catch (Exception e) {

                }
            }
        }).start();

        return true;
    }

    // this will add the select audio with the video
    private void mergeWithAudio() {
        String audioFile = Variables.app_folder + Variables.SelectedAudio_AAC;

        MergeVideoAudio mergeVideoAudio = new MergeVideoAudio(PreviewVideoActivity.this, this);
        mergeVideoAudio.doInBackground(audioFile, Variables.outputfile, Variables.outputfile2, draft_file);
    }

    @Override
    public void onCompletion(boolean state, String draftFile) {
        draft_file = draftFile;
        finalTouchesToVideo();
    }

}
