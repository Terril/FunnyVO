package com.funnyvo.android.soundlists;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.videorecording.VideoRecoderActivity;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;


public class VideoSoundActivity extends BaseActivity implements View.OnClickListener {

    Home item;
    TextView sound_name, description_txt;
    ImageView sound_image;
    File video_file, audio_file;
    SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_sound);

        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            item = (Home) intent.getSerializableExtra("data");
        }

        video_file = new File(Variables.app_folder + item.video_id + ".mp4");

        sound_name = findViewById(R.id.sound_name);
        description_txt = findViewById(R.id.description_txt);
        sound_image = findViewById(R.id.sound_image);

        if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
            sound_name.setText("original sound - " + item.first_name + " " + item.last_name);
        } else {
            sound_name.setText(item.sound_name);
        }
        description_txt.setText(item.video_description);


        findViewById(R.id.back_btn).setOnClickListener(this);

        findViewById(R.id.save_btn).setOnClickListener(this);
        findViewById(R.id.create_btn).setOnClickListener(this);

        findViewById(R.id.play_btn).setOnClickListener(this);
        findViewById(R.id.pause_btn).setOnClickListener(this);

        if (video_file.exists()) {
            Glide.with(this)
                    .load(Uri.fromFile(video_file))
                    .into(sound_image);

            loadFFmpeg();
        } else {
            saveVideo();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_btn:
                finish();
                break;
            case R.id.save_btn:
                try {
                    copyFile(new File(Variables.app_folder + Variables.SelectedAudio_MP3),
                            new File(Variables.app_folder + item.video_id + ".mp3"));
                    Toast.makeText(this, "Audio Saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.create_btn:
                if (player.getPlayWhenReady())
                    player.setPlayWhenReady(false);

                convertMp3ToAcc();
                break;

            case R.id.play_btn:
                if (audio_file.exists())
                    playaudio();
                else if (video_file.exists())
                    loadFFmpeg();
                else
                    saveVideo();

                break;

            case R.id.pause_btn:
                stopPlaying();
                break;
        }
    }

    public void playaudio() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(audio_file));


        player.prepare(videoSource);
        player.setPlayWhenReady(true);

        showPlayingState();
    }


    public void stopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        showPauseState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }

    public void showPlayingState() {
        findViewById(R.id.play_btn).setVisibility(View.GONE);
        findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
    }

    public void showPauseState() {
        findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }


    public void saveVideo() {
        PRDownloader.initialize(this);
        DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.app_folder, item.video_id + ".mp4")
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
                        showProgressDialog();

                    }
                });


        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                dismissProgressDialog();
                audio_file = new File(Variables.app_folder + item.video_id + ".mp4");
                Glide.with(VideoSoundActivity.this)
                        .load(Uri.fromFile(video_file))
                        .into(sound_image);
                loadFFmpeg();
            }

            @Override
            public void onError(Error error) {
                dismissProgressDialog();
            }


        });
    }

    public void loadFFmpeg() {
        showAudioLoading();

        final String[] complexCommand = {"-y", "-i", Variables.app_folder + item.video_id + ".mp4", "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3",
                Variables.app_folder + Variables.SelectedAudio_MP3};


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
                    hideAudioLoading();
                    audio_file = new File(Variables.app_folder + Variables.SelectedAudio_MP3);
                    if (audio_file.exists())
                        playaudio();

                } else if (rc == RETURN_CODE_CANCEL) {
                    Log.d(Variables.tag, "Command execution cancelled by user.");
                    hideAudioLoading();
                } else {
                    Log.d(Variables.tag, String.format("Command execution failed with rc=%d and the output below.", rc));
                    Config.printLastCommandOutput(Log.INFO);
                    hideAudioLoading();
                }

            }
        }.execute();



     /*   ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    hide_audio_loading();
                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFinish() {
                    Extract_sound();

                }
            });
        } catch (FFmpegNotSupportedException e) {
            show_audio_loading();
            Toast.makeText(this, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }

*/
    }

 /*   public void Extract_sound(){


        String[] complexCommand = {"-y", "-i", Variables.app_folder+item.video_id+".mp4", "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3",
                Variables.app_folder+Variables.SelectedAudio_MP3};
        try {
            ffmpeg.execute(complexCommand, new FFmpegExecuteResponseHandler() {

                @Override
                public void onStart() {
                     }

                @Override
                public void onProgress(String message) {

                    Log.d(Variables.tag,message);

                }

                @Override
                public void onFailure(String message) {
                    show_audio_loading();
                    Log.d(Variables.tag,"onFailure "+message);
                   }

                @Override
                public void onSuccess(String message) {
                       }

                @Override
                public void onFinish() {
                    hide_audio_loading();
                    audio_file=new File(Variables.app_folder+Variables.SelectedAudio_MP3);
                    if(audio_file.exists())
                        playaudio();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            hide_audio_loading();
        }
    }
*/

    public void showAudioLoading() {
        findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.play_btn).setVisibility(View.GONE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }

    public void hideAudioLoading() {
        findViewById(R.id.loading_progress).setVisibility(View.GONE);
        findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }


    public void convertMp3ToAcc() {
        Functions.showLoader(this, false, false);
        final String[] complexCommand = new String[]{"-y", "-i", Variables.app_folder + Variables.SelectedAudio_MP3, Variables.app_folder + Variables.SelectedAudio_AAC};

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
                    Functions.cancelLoader();
                    openVideoRecording();

                } else if (rc == RETURN_CODE_CANCEL) {
                    Log.d(Variables.tag, "Command execution cancelled by user.");
                    Functions.cancelLoader();
                } else {
                    Log.d(Variables.tag, String.format("Command execution failed with rc=%d and the output below.", rc));
                    Config.printLastCommandOutput(Log.INFO);
                    Functions.cancelLoader();
                }

            }
        }.execute();


    }


    public void openVideoRecording() {
        Intent intent = new Intent(VideoSoundActivity.this, VideoRecoderActivity.class);
        intent.putExtra("sound_name", sound_name.getText().toString());
        intent.putExtra("sound_id", item.sound_id);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

    }


    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}
