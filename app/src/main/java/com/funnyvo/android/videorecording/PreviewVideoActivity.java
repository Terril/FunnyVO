package com.funnyvo.android.videorecording;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.coremedia.iso.boxes.Container;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;
import com.daasuu.gpuv.player.GPUPlayerView;
import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.filter.FilterAdapter;
import com.funnyvo.android.filter.FilterType;
import com.funnyvo.android.helper.DimensionData;
import com.funnyvo.android.helper.PermissionUtils;
import com.funnyvo.android.helper.PlayerEventListener;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.SoundListMainActivity;
import com.funnyvo.android.videorecording.merge.MergeVideoAudio;
import com.funnyvo.android.videorecording.merge.MergeVideoAudioCallBack;
import com.funnyvo.android.videorecording.photoeditor.OnPhotoEditorListener;
import com.funnyvo.android.videorecording.photoeditor.PhotoEditor;
import com.funnyvo.android.videorecording.photoeditor.PhotoEditorView;
import com.funnyvo.android.videorecording.photoeditor.SaveSettings;
import com.funnyvo.android.videorecording.photoeditor.TextStyleBuilder;
import com.funnyvo.android.videorecording.photoeditor.ViewType;
import com.google.android.material.snackbar.Snackbar;
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

import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static com.funnyvo.android.helper.MediaUtils.getScaledDimension;
import static com.funnyvo.android.simpleclasses.Variables.APP_FOLDER;
import static com.funnyvo.android.simpleclasses.Variables.APP_NAME;
import static com.funnyvo.android.simpleclasses.Variables.SOUNDS_LIST_REQUEST_CODE;

public class PreviewVideoActivity extends BaseActivity implements View.OnClickListener, MergeVideoAudioCallBack, OnPhotoEditorListener, PropertiesBSFragment.Properties {

    public static final int CROP_RESULT = 101;
    private GPUPlayerView gpuPlayerView;
    private MediaPlayer audio;
    public static int selectPostion = 0;
    private final List<FilterType> filterTypes = FilterType.createFilterList();
    private FilterAdapter adapter;
    private RecyclerView recylerview;
    private PlayerEventListener eventListener;
    private Button btnAddMusic;
    private String draftFile;
    private String path;
    private boolean isMotionFilterSelected = false, isTextFilterSelected = false;
    private boolean isUp = false;
    private boolean isSlowMoEnabled = true;
    private boolean isFastMoEnabled = true;
    private boolean isFromGallery = false;
    private String tempOutputSource = Variables.outputfile2;
    private boolean isScaleModeSet = true;
    private PhotoEditor photoEditor;
    private PropertiesBSFragment propertiesBSFragment;
    // this function will set the player to the current video

    private int originalDisplayWidth;
    private int originalDisplayHeight;
    private int newCanvasWidth, newCanvasHeight;
    private int DRAW_CANVASW = 0;
    private int DRAW_CANVASH = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigation();
        setContentView(R.layout.activity_preview_video);

        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("video_path");
            draftFile = intent.getStringExtra("draft_file");
            isFromGallery = intent.getBooleanExtra("isFromGallery", false);
            isScaleModeSet = intent.getBooleanExtra("isScaleMode", true);
        }

        eventListener = new PlayerEventListener();
        selectPostion = 0;
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
        findViewById(R.id.btnFreeDraw).setOnClickListener(this);
        findViewById(R.id.btnUndo).setOnClickListener(this);

        PhotoEditorView ivImage = findViewById(R.id.ivImage);
        Button btnDelete = findViewById(R.id.btnDelete);

        Glide.with(this).load(R.drawable.trans).centerCrop().into(ivImage.getSource());
        propertiesBSFragment = new PropertiesBSFragment();
        propertiesBSFragment.setPropertiesChangeListener(this);
        photoEditor = new PhotoEditor.Builder(this, ivImage)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                .setDeleteView(btnDelete)
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk

        photoEditor.setOnPhotoEditorListener(this);

        if (!isFromGallery) {
            btnAddMusic.setText(getString(R.string.preview));
            btnAddMusic.setEnabled(false);
        } else {
            videoUrl = path;
            Variables.Selected_sound_id = "null";
            btnAddMusic.setEnabled(true);
            append(false, path); //galleryAppend();
            if (getFileDuration(Uri.parse(path)) > 30000) {
                Snackbar.make(btnAddMusic, R.string.crop_the_video_message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.palette_cheddar))
                        .show();
            }
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoUrl);
        String metaRotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION);
        int rotation = metaRotation == null ? 0 : Integer.parseInt(metaRotation);
        if (rotation == 90 || rotation == 270) {
            DRAW_CANVASH = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            DRAW_CANVASW = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        } else {
            DRAW_CANVASW = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            DRAW_CANVASH = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        }

        setCanvasAspectRatio();
//        videoSurface.getLayoutParams().width = newCanvasWidth;
//        videoSurface.getLayoutParams().height = newCanvasHeight;

        ivImage.getLayoutParams().width = newCanvasWidth;
        ivImage.getLayoutParams().height = newCanvasHeight;

        gpuPlayerView = setPlayer(this, Uri.parse(videoUrl), eventListener, isScaleModeSet);
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(gpuPlayerView);
        gpuPlayerView.onResume();
        recylerview = findViewById(R.id.recylerviewPreview);

        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoUrl,
                MediaStore.Video.Thumbnails.MINI_KIND);
        Bitmap bmThumbnailResized = Bitmap.createScaledBitmap(bmThumbnail, (int) (bmThumbnail.getWidth() * 0.4), (int) (bmThumbnail.getHeight() * 0.4), true);

        adapter = new FilterAdapter(this, filterTypes, bmThumbnailResized, (view, position, item) -> {
            selectPostion = position;
            gpuPlayerView.setElevation(3.0F);

            gpuPlayerView.setGlFilter(new GlFilterGroup(FilterType.createGlFilter(filterTypes.get(position), getApplicationContext(), null)));
            adapter.notifyDataSetChanged();
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
                .filter(new GlFilterGroup(FilterType.createGlFilterWithOverlay(filterTypes.get(selectPostion), getApplicationContext(), getBitmapForLogo())))
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
                                    Variables.outputfile2 =  Variables.OUTPUT_FILTER_FILE;
                                    dismissProgressDialog();
                                    applyFreeOverLayFeature();
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
        intent.putExtra("draft_file", draftFile);
        intent.putExtra("isScaleMode", isScaleModeSet);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void prepareAudio() {
        player.setVolume(0);
        File file = new File(APP_FOLDER + Variables.SELECTED_AUDIO_AAC);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(APP_FOLDER + Variables.SELECTED_AUDIO_AAC);
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
        String message = ""; //edtVideoMessage.getText().toString().trim();
        String msg = message.substring(0, message.length() / 2);
        String msg1 = message.substring((message.length() / 2) + 1);

        message = msg + "\n" + msg1;
        final String[] complexCommand = new String[]{
                "-y", "-i", Variables.outputfile2, "-vf", "drawtext=text=" + message + ":fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=21: x=(w-tw)/2: y=(h/PHI)+th", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", Variables.OUTPUT_FILE_MESSAGE
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
        TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this, 0);
        textEditorDialogFragment.setOnTextEditorListener((inputText, colorCode, position) -> {
            final TextStyleBuilder styleBuilder = new TextStyleBuilder();
            styleBuilder.withTextColor(colorCode);
            Typeface typeface = ResourcesCompat.getFont(PreviewVideoActivity.this, TextEditorDialogFragment.getDefaultFontIds(PreviewVideoActivity.this).get(position));
            styleBuilder.withTextFont(typeface);
            photoEditor.addText(inputText, styleBuilder, position);
        });
    }

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
                        Log.e(APP_NAME, "Internal audio system crashed");
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
            case R.id.btnAddMusic:
                Intent intent = new Intent(this, SoundListMainActivity.class);
                startActivityForResult(intent, SOUNDS_LIST_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;
            case R.id.btnFreeDraw:
                setDrawingMode();
                break;
            case R.id.btnUndo:
                photoEditor.clearBrushAllViews();
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

        if (selectPostion > 0) {
            saveVideo(Variables.outputfile2, Variables.OUTPUT_FILTER_FILE);
        } else {
            applyFreeOverLayFeature();
        }
    }

    @SuppressLint("MissingPermission")
    private void applyFreeOverLayFeature() {
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + ""
                + System.currentTimeMillis() + ".png");
        try {
            file.createNewFile();

            SaveSettings saveSettings = new SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(false)
                    .build();

            if(PermissionUtils.INSTANCE.checkPermissions(this)) {
                photoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                        copyVideoUsingFfmpeg(imagePath);
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private void copyVideoUsingFfmpeg(String imagePath) {
        Variables.OUTPUT_FILTER_FILE = APP_FOLDER + Functions.getRandomString() + ".mp4";
        final String[] complexCommand = new String[]{
                "-y", "-i", Variables.outputfile2, "-i", imagePath, "-filter_complex", "[1:v]scale=" + DRAW_CANVASW + ":" + DRAW_CANVASH + "[ovrl];[0:v][ovrl]overlay=x=0:y=0", "-c:v", "libx264", "-preset", "ultrafast", Variables.OUTPUT_FILTER_FILE_OTHER
        };
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

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
                    gotoPostScreen();
                } else if (rc == RETURN_CODE_CANCEL) {
                    dismissProgressDialog();
                } else {
                    Config.printLastCommandOutput(Log.INFO);
                    dismissProgressDialog();
                }

            }
        }.execute();
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

                ArrayList<String> videoList = new ArrayList<>();
                File file = new File(videoPath);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(PreviewVideoActivity.this, Uri.parse(videoPath));
                String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                boolean isVideo = getString(R.string.yes).equals(hasVideo);

                if (isVideo && file.length() > 3000) {
                    videoList.add(videoPath);
                }

                try {
                    Movie[] inMovies = new Movie[videoList.size()];
                    for (int i = 0; i < videoList.size(); i++) {
                        inMovies[i] = MovieCreator.build(videoList.get(i));
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

    private void setCanvasAspectRatio() {
        originalDisplayHeight = getDisplayHeight();
        originalDisplayWidth = getDisplayWidth();

        DimensionData displayDimension =
                getScaledDimension(new DimensionData((int) DRAW_CANVASW, (int) DRAW_CANVASH),
                        new DimensionData(originalDisplayWidth, originalDisplayHeight));
        newCanvasWidth = displayDimension.width;
        newCanvasHeight = displayDimension.height;

    }

    private void setDrawingMode() {
        if (photoEditor.getBrushDrawableMode()) {
            photoEditor.setBrushDrawingMode(false);
        } else {
            photoEditor.setBrushDrawingMode(true);
            propertiesBSFragment.show(getSupportFragmentManager(), propertiesBSFragment.getTag());
        }
    }

    private int getDisplayWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private int getDisplayHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    // this will add the select audio with the video
    private void mergeWithAudio() {
        final String audioFile = APP_FOLDER + Variables.SELECTED_AUDIO_AAC;

        MergeVideoAudio mergeVideoAudio = new MergeVideoAudio(this);
        mergeVideoAudio.doInBackground(audioFile, Variables.outputfile, Variables.outputfile2, draftFile);
    }

    @Override
    public void onCompletion(boolean state, String draftFile) {
        this.draftFile = draftFile;
        finalTouchesToVideo();
    }

    private Long getFileDuration(Uri uri) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long fileDuration = Long.parseLong(durationStr);
            return fileDuration;
        } catch (Exception e) {
        }
        return 0l;
    }

    /**
     * When user long press the existing text this event will trigger implying that user want to
     * edit the current {@link TextView}
     *
     * @param rootView  view on which the long press occurs
     * @param text      current text set on the view
     * @param colorCode current color value set on view
     * @param pos
     */
    @Override
    public void onEditTextChangeListener(View rootView, String text, int colorCode, int pos) {
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode, pos);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
            @Override
            public void onDone(String inputText, int colorCode, int position) {
                final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                styleBuilder.withTextColor(colorCode);
                Typeface typeface = ResourcesCompat.getFont(PreviewVideoActivity.this, TextEditorDialogFragment.getDefaultFontIds(PreviewVideoActivity.this).get(position));
                styleBuilder.withTextFont(typeface);
                photoEditor.editText(rootView, inputText, styleBuilder, position);
            }
        });
    }

    /**
     * This is a callback when user adds any view on the {@link PhotoEditorView} it can be
     * brush,text or sticker i.e bitmap on parent view
     *
     * @param viewType           enum which define type of view is added
     * @param numberOfAddedViews number of views currently added
     * @see ViewType
     */
    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

    }

    /**
     * This is a callback when user remove any view on the {@link PhotoEditorView} it happens when usually
     * undo and redo happens or text is removed
     *
     * @param viewType           enum which define type of view is added
     * @param numberOfAddedViews number of views currently added
     */
    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {

    }

    /**
     * A callback when user start dragging a view which can be
     * any of {@link ViewType}
     *
     * @param viewType enum which define type of view is added
     */
    @Override
    public void onStartViewChangeListener(ViewType viewType) {

    }

    /**
     * A callback when user stop/up touching a view which can be
     * any of {@link ViewType}
     *
     * @param viewType enum which define type of view is added
     */
    @Override
    public void onStopViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onColorChanged(int colorCode) {
        photoEditor.setBrushColor(colorCode);
    }

    @Override
    public void onOpacityChanged(int opacity) {
        photoEditor.setOpacity(opacity);
    }

    @Override
    public void onBrushSizeChanged(int brushSize) {
        photoEditor.setBrushSize(brushSize);
    }
}
