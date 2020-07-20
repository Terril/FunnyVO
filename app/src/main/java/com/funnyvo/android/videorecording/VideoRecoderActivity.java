package com.funnyvo.android.videorecording;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.segmentprogress.ProgressBarListener;
import com.funnyvo.android.segmentprogress.SegmentedProgressBar;
import com.funnyvo.android.simpleclasses.FileUtils;
import com.funnyvo.android.simpleclasses.FragmentCallback;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.soundlists.SoundListMainActivity;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Path;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoRecoderActivity extends BaseActivity implements View.OnClickListener, MergeVideoAudioCallBack {

    private CameraView cameraView;
    private int number = 0;

    private ArrayList<String> videopaths = new ArrayList<>();

    private ImageButton record_image, done_btn, time_btn;
    private boolean is_recording = false;
    private boolean is_flash_on = false;

    private ImageButton flash_btn, btnCloseRecordVideo;
    private SegmentedProgressBar video_progress;
    private LinearLayout camera_options;
    private ImageButton rotate_camera;
    private ImageView imvGallery;

    public static int Sounds_list_Request_code = 1;
    private Button btnAddMusic;

    private int sec_passed = 0;
    // this will play the sound with the video when we select the audio
    private MediaPlayer audio;
    private TextView countdown_timer_txt;
    private boolean is_recording_timer_enable;
    private int recording_time = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigation();

        setContentView(R.layout.activity_video_recoder);

        Variables.Selected_sound_id = "null";
        Variables.recording_duration = Variables.max_recording_duration;

        cameraView = findViewById(R.id.camera);
        camera_options = findViewById(R.id.camera_options);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }

            @Override
            public void onError(CameraKitError cameraKitError) {
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        record_image = findViewById(R.id.record_image);
        imvGallery = findViewById(R.id.imvGallery);

        done_btn = findViewById(R.id.btnDone);
        done_btn.setEnabled(false);

        rotate_camera = findViewById(R.id.rotate_camera);
        flash_btn = findViewById(R.id.flash_camera);

        btnCloseRecordVideo = findViewById(R.id.btnCloseRecordVideo);
        btnAddMusic = findViewById(R.id.btnAddMusicRecord);
        time_btn = findViewById(R.id.time_btn);

        Intent intent = getIntent();
        if (intent.hasExtra("sound_name")) {
            btnAddMusic.setText(intent.getStringExtra("sound_name"));
            Variables.Selected_sound_id = intent.getStringExtra("sound_id");
            preparedAudio();
        }

        // this is code hold to record the video
//        final Timer[] timer = {new Timer()};
//        final long[] press_time = {0};
//        record_image.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    timer[0] = new Timer();
//                    press_time[0] = System.currentTimeMillis();
//                    timer[0].schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (!is_recording) {
//                                        press_time[0] = System.currentTimeMillis();
//                                        startOrStopRecording();
//                                    }
//                                }
//                            });
//
//                        }
//                    }, 200);
//
//
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    timer[0].cancel();
//                    if (is_recording && (press_time[0] != 0 && (System.currentTimeMillis() - press_time[0]) < 2000)) {
//                        startOrStopRecording();
//                    }
//                }
//                return false;
//            }
//
//        });

        countdown_timer_txt = findViewById(R.id.countdown_timer_txt);
        initializeVideoProgress();
    }


    private void setListener() {
        record_image.setOnClickListener(this);
        done_btn.setOnClickListener(this);
        btnCloseRecordVideo.setOnClickListener(this);
        rotate_camera.setOnClickListener(this);
        btnAddMusic.setOnClickListener(this);
        time_btn.setOnClickListener(this);
        flash_btn.setOnClickListener(this);
        imvGallery.setOnClickListener(this);
    }

    private void initializeVideoProgress() {
        sec_passed = 0;
        video_progress = findViewById(R.id.video_progress);
        video_progress.enableAutoProgressView(Variables.recording_duration);
        video_progress.setDividerColor(Color.WHITE);
        video_progress.setDividerEnabled(true);
        video_progress.setDividerWidth(4);
        video_progress.setShader(new int[]{Color.CYAN, Color.CYAN, Color.CYAN});

        video_progress.SetListener(new ProgressBarListener() {
            @Override
            public void TimeinMills(long mills) {
                sec_passed = (int) (mills / 1000);

                if (sec_passed > (Variables.recording_duration / 1000) - 1) {
                    startOrStopRecording();
                }

                if (is_recording_timer_enable && sec_passed >= recording_time) {
                    is_recording_timer_enable = false;
                    startOrStopRecording();
                }

            }
        });
    }


    // if the Recording is stop then it we start the recording
    // and if the mobile is recording the video then it will stop the recording
    private void startOrStopRecording() {
        if (!is_recording && sec_passed < (Variables.recording_duration / 1000) - 1) {
            number = number + 1;
            is_recording = true;

            File file = new File(Variables.app_folder + "myvideo" + (number) + ".mp4");
            videopaths.add(Variables.app_folder + "myvideo" + (number) + ".mp4");
            cameraView.captureVideo(file);

            if (audio != null)
                audio.start();

            video_progress.resume();

            done_btn.setEnabled(false);

            record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_video_post));

            camera_options.setVisibility(View.GONE);
            // btnAddMusic.setClickable(false);
            rotate_camera.setVisibility(View.GONE);

        } else if (is_recording) {
            is_recording = false;
            video_progress.pause();
            video_progress.addDivider();

            try {
                if (audio != null)
                    audio.pause();
            } catch (IllegalStateException ignored) {

            }

            if (sec_passed > ((Variables.recording_duration / 1000) / 3)) {
                done_btn.setVisibility(View.VISIBLE);
                done_btn.setEnabled(true);
            }

            cameraView.stopVideo();

            record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_video_pre));
            camera_options.setVisibility(View.VISIBLE);
//            rotate_camera.setVisibility(View.VISIBLE);

        } else if (sec_passed > (Variables.recording_duration / 1000)) {
            Functions.showAlert(this, "Alert", "Video only can be a " + (int) Variables.recording_duration / 1000 + " S");
        }
    }

    // this will append all the videos parts in one  fullvideo
    private boolean append() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showProgressDialog();
                    }
                });

                ArrayList<String> video_list = new ArrayList<>();
                for (int i = 0; i < videopaths.size(); i++) {

                    File file = new File(videopaths.get(i));
                    if (file.exists()) {
                        try {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(VideoRecoderActivity.this, Uri.fromFile(file));
                            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                            boolean isVideo = getString(R.string.yes).equals(hasVideo);

                            if (isVideo && file.length() > 3000) {
                                //        Log.d("resp", videopaths.get(i));
                                video_list.add(videopaths.get(i));
                            }
                        } catch (Exception e) {
                            Log.d(Variables.tag, e.toString());
                        }
                    }
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
                            dismissProgressDialog();
                            if (audio != null)
                                mergeWithAudio();
                            else {
                                goToPreviewActivity();
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
        String audio_file;
        audio_file = Variables.app_folder + Variables.SelectedAudio_AAC;

        MergeVideoAudio mergeVideoAudio = new MergeVideoAudio(VideoRecoderActivity.this, this);
        mergeVideoAudio.doInBackground(audio_file, Variables.outputfile, Variables.outputfile2);
    }

    private void rotateCamera() {
        cameraView.toggleFacing();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_image:
                startOrStopRecording();
                break;
            case R.id.rotate_camera:
                rotateCamera();
                break;
            case R.id.imvGallery:
                pickVideoFromGallery();
                /*
                Intent upload_intent=new Intent(this, GalleryVideos_A.class);
                startActivity(upload_intent);
                overridePendingTransition(R.anim.in_from_bottom,R.anim.out_to_top);
                */
                break;
            case R.id.btnDone:
                append();
                break;
            case R.id.flash_camera:
                if (is_flash_on) {
                    is_flash_on = false;
                    cameraView.setFlash(0);
                    flash_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));

                } else {
                    is_flash_on = true;
                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                    flash_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                }
                break;
            case R.id.btnCloseRecordVideo:
                onBackPressed();
                break;
            case R.id.btnAddMusicRecord:
                Intent intent = new Intent(this, SoundListMainActivity.class);
                startActivityForResult(intent, Sounds_list_Request_code);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;
            case R.id.time_btn:
                if (sec_passed + 1 < Variables.recording_duration / 1000) {
                    RecordingTimeRangeFragment recordingTimeRangeFragment = new RecordingTimeRangeFragment(new FragmentCallback() {
                        @Override
                        public void responseCallBackFromFragment(Bundle bundle) {
                            if (bundle != null) {
                                is_recording_timer_enable = true;
                                recording_time = bundle.getInt("end_time");
                                countdown_timer_txt.setText("3");
                                countdown_timer_txt.setVisibility(View.VISIBLE);
                                record_image.setClickable(false);
                                final Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                new CountDownTimer(4000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        countdown_timer_txt.setText("" + (millisUntilFinished / 1000));
                                        countdown_timer_txt.setAnimation(scaleAnimation);
                                    }

                                    @Override
                                    public void onFinish() {
                                        record_image.setClickable(true);
                                        countdown_timer_txt.setVisibility(View.GONE);
                                        startOrStopRecording();
                                    }
                                }.start();

                            }
                        }
                    });
                    Bundle bundle = new Bundle();
                    if (sec_passed < (Variables.recording_duration / 1000) - 3)
                        bundle.putInt("end_time", (sec_passed + 3));
                    else
                        bundle.putInt("end_time", (sec_passed + 1));

                    bundle.putInt("total_time", (Variables.recording_duration / 1000));
                    recordingTimeRangeFragment.setArguments(bundle);
                    recordingTimeRangeFragment.show(getSupportFragmentManager(), "");
                }
                break;

        }
    }


    private void pickVideoFromGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent, Variables.Pick_video_from_gallery);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Sounds_list_Request_code) {
                if (data != null) {
                    if (data.getStringExtra("isSelected").equals(getString(R.string.yes))) {
                        btnAddMusic.setText(data.getStringExtra("sound_name"));
                        Variables.Selected_sound_id = data.getStringExtra("sound_id");
                        preparedAudio();
                    }

                }

            } else if (requestCode == Variables.Pick_video_from_gallery) {
                Uri uri = data.getData();
                try {
                    File video_file = FileUtils.getFileFromUri(this, uri);
                    if (getFileDuration(uri) < 19500) {
                        changeVideoSize(video_file.getAbsolutePath(), Variables.gallery_resize_video);
                    } else {
                        try {
                            startTrim(video_file, new File(Variables.gallery_trimed_video), 1000, 18000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

    }

    private long getFileDuration(Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Integer.parseInt(durationStr);

            return file_duration;
        } catch (Exception e) {

        }
        return 0;
    }


    private void changeVideoSize(String src_path, String destination_path) {
        showProgressDialog();
        new GPUMp4Composer(src_path, destination_path)
                .size(720, 1280)
                .videoBitrate((int) (0.25 * 16 * 720 * 1280))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                    }

                    @Override
                    public void onCompleted() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                Intent intent = new Intent(VideoRecoderActivity.this, PreviewVideoActivity.class);
                                intent.putExtra("video_path", Variables.gallery_resize_video);
                                intent.putExtra("isFromGallery", true);
                                startActivity(intent);

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
                                    Toast.makeText(VideoRecoderActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {

                                }
                            }
                        });
                    }
                })
                .start();

    }

    private void startTrim(final File src, final File dst, final int startMs, final int endMs) throws IOException {

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {

                    FileDataSourceImpl file = new FileDataSourceImpl(src);
                    Movie movie = MovieCreator.build(file);
                    List<Track> tracks = movie.getTracks();
                    movie.setTracks(new LinkedList<Track>());
                    double startTime = startMs / 1000;
                    double endTime = endMs / 1000;
                    boolean timeCorrected = false;

                    for (Track track : tracks) {
                        if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                            if (timeCorrected) {
                                throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                            }
                            startTime = Functions.correctTimeToSyncSample(track, startTime, false);
                            endTime = Functions.correctTimeToSyncSample(track, endTime, true);
                            timeCorrected = true;
                        }
                    }
                    for (Track track : tracks) {
                        long currentSample = 0;
                        double currentTime = 0;
                        long startSample = -1;
                        long endSample = -1;

                        for (int i = 0; i < track.getSampleDurations().length; i++) {
                            if (currentTime <= startTime) {
                                startSample = currentSample;
                            }
                            if (currentTime <= endTime) {
                                endSample = currentSample;
                            } else {
                                break;
                            }
                            currentTime += (double) track.getSampleDurations()[i] / (double) track.getTrackMetaData().getTimescale();
                            currentSample++;
                        }
                        movie.addTrack(new CroppedTrack(track, startSample, endSample));
                    }

                    Container out = new DefaultMp4Builder().build(movie);
                    MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
                    // mvhd.setMatrix(Matrix.ROTATE_0);
                    if (!dst.exists()) {
                        dst.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(dst);
                    WritableByteChannel fc = fos.getChannel();
                    try {
                        out.writeContainer(fc);
                    } finally {
                        fc.close();
                        fos.close();
                        file.close();
                    }

                    file.close();
                    return "Ok";
                } catch (IOException e) {
                    Log.d(Variables.tag, e.toString());
                    return "error";
                }

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("error")) {
                    Toast.makeText(VideoRecoderActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                } else {
                    changeVideoSize(Variables.gallery_trimed_video, Variables.gallery_resize_video);
                }
            }

        }.execute();

    }

    private void preparedAudio() {
        File file = new File(Variables.app_folder + Variables.SelectedAudio_AAC);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(Variables.app_folder + Variables.SelectedAudio_AAC);
                audio.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, Uri.fromFile(file));
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int fileDuration = Integer.parseInt(durationStr);

            if (fileDuration < Variables.max_recording_duration) {
                Variables.recording_duration = fileDuration;
                initializeVideoProgress();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) {
            cameraView.start();
        }
        setListener();
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
            cameraView.stop();

        } catch (Exception e) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
            cameraView.stop();

        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
            cameraView.stop();

        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Are you Sure?")
                .setMessage("If you go back you can't undo this action")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteFile();
                        finish();
                        overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                    }
                }).show();
    }

    private void goToPreviewActivity() {
        Intent intent = new Intent(this, PreviewVideoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    // this will delete all the video parts that is create during priviously created video
    int delete_count = 0;

    private void deleteFile() {
        delete_count++;
        File output = new File(Variables.outputfile);
        File output2 = new File(Variables.outputfile2);
        File outputFilterFile = new File(Variables.OUTPUT_FILTER_FILE);
        File outputFilterMotionFile = new File(Variables.OUTPUT_FILE_MOTION);
        File outputFilterTrimmedFile = new File(Variables.OUTPUT_FILE_TRIMMED);
        File outputFilterMessageFile = new File(Variables.OUTPUT_FILE_MESSAGE);

        if (output.exists()) {
            output.delete();
        }
        if (output2.exists()) {

            output2.delete();
        }
        if (outputFilterFile.exists()) {
            outputFilterFile.delete();
        }
        if (outputFilterMotionFile.exists()) {
            outputFilterMotionFile.delete();
        }
        if (outputFilterTrimmedFile.exists()) {
            outputFilterTrimmedFile.delete();
        }
        if (outputFilterMessageFile.exists()) {
            outputFilterMessageFile.delete();
        }

        File file = new File(Variables.app_folder + "myvideo" + (delete_count) + ".mp4");
        if (file.exists()) {
            file.delete();
            deleteFile();
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onCompletion(boolean state, String draftFile) {
        Intent intent = new Intent(this, PreviewVideoActivity.class);
        intent.putExtra("video_path", Variables.outputfile2);
        intent.putExtra("draft_file", draftFile);
        startActivity(intent);
    }
}
