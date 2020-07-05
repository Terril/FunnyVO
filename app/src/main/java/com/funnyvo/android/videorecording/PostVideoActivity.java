package com.funnyvo.android.videorecording;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.daasuu.gpuv.player.GPUPlayerView;
import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.helper.PlayerEventListener;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.services.ServiceCallback;
import com.funnyvo.android.services.UploadService;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PostVideoActivity extends BaseActivity implements ServiceCallback, View.OnClickListener {

    String video_path;
    ServiceCallback serviceCallback;
    EditText description_edit;
    UploadService mService;
    String draft_file;

    PlayerEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigation();
        setContentView(R.layout.activity_post_video);

        Intent intent = getIntent();
        if (intent != null) {
            draft_file = intent.getStringExtra("draft_file");
        }

        eventListener = new PlayerEventListener();

        video_path = Variables.output_filter_file;
        GPUPlayerView gpuPlayerView = setPlayer(video_path, eventListener);
        ((MovieWrapperView) findViewById(R.id.layout_post_movie_wrapper)).addView(gpuPlayerView);
        gpuPlayerView.onResume();

        findViewById(R.id.btnGoBackPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnUploadVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                startService();
            }
        });

        findViewById(R.id.btnSaveLocal).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveLocal:
                saveFileInDraft();
                break;
        }
    }

    // this will start the service for uploading the video into database
    public void startService() {
        serviceCallback = this;

        UploadService mService = new UploadService(serviceCallback);
        if (!Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("startservice");
            mServiceIntent.putExtra("uri", "" + Uri.fromFile(new File(video_path)));
            mServiceIntent.putExtra("desc", "");
            startService(mServiceIntent);

            Intent intent = new Intent(this, UploadService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        } else {
            Toast.makeText(this, "Please wait video already in uploading progress", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService();
        if (player != null) {
            player.setPlayWhenReady(false);
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }


    // when the video is uploading successfully it will restart the appliaction
    @Override
    public void showResponse(final String response) {

        if (mConnection != null) {
            unbindService(mConnection);
        }

        if (response.equalsIgnoreCase("Your Video is uploaded Successfully")) {
            deleteDraftFile();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PostVideoActivity.this, response, Toast.LENGTH_LONG).show();
                    dismissProgressDialog();

                    startActivity(new Intent(PostVideoActivity.this, MainMenuActivity.class));

                }
            }, 1000);

        } else {
            Toast.makeText(PostVideoActivity.this, response, Toast.LENGTH_LONG).show();
            dismissProgressDialog();
        }
    }


    // this is importance for binding the service to the activity
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            UploadService.LocalBinder binder = (UploadService.LocalBinder) service;
            mService = binder.getService();
            mService.setCallbacks(PostVideoActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    // this function will stop the the ruuning service
    public void stopService() {
        serviceCallback = this;
        UploadService mService = new UploadService(serviceCallback);

        if (Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("stopservice");
            startService(mServiceIntent);

        }
    }


    public void saveFileInDraft() {
        File source = new File(video_path);
        File destination = new File(Variables.draft_app_folder + Functions.getRandomString() + ".mp4");
        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Toast.makeText(PostVideoActivity.this, "File saved in Draft", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PostVideoActivity.this, MainMenuActivity.class));

            } else {
                Toast.makeText(PostVideoActivity.this, "File failed to saved in Draft", Toast.LENGTH_SHORT).show();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void deleteDraftFile() {
        try {
            if (draft_file != null) {
                File file = new File(draft_file);
                file.delete();
            }
        } catch (Exception e) {

        }
    }

}
