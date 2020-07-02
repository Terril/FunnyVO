package com.funnyvo.android.videorecording;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.R;
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

public class PostVideoActivity extends AppCompatActivity implements ServiceCallback, View.OnClickListener {


    ImageView video_thumbnail;
    String video_path;
    ProgressDialog progressDialog;
    ServiceCallback serviceCallback;
    EditText description_edit;

    String draft_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        Intent intent = getIntent();
        if (intent != null) {
            draft_file = intent.getStringExtra("draft_file");
        }

        video_path = Variables.output_filter_file;
        video_thumbnail = findViewById(R.id.video_thumbnail);


        description_edit = findViewById(R.id.description_edit);

        // this will get the thumbnail of video and show them in imageview
        Bitmap bmThumbnail;
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(video_path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        if (bmThumbnail != null) {
            video_thumbnail.setImageBitmap(bmThumbnail);
        } else {
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);


        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        findViewById(R.id.post_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                Start_Service();
            }
        });

        findViewById(R.id.save_draft_btn).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_draft_btn:
                Save_file_in_draft();
                break;
        }
    }


    // this will start the service for uploading the video into database
    public void Start_Service() {
        serviceCallback = this;

        UploadService mService = new UploadService(serviceCallback);
        if (!Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("startservice");
            mServiceIntent.putExtra("uri", "" + Uri.fromFile(new File(video_path)));
            mServiceIntent.putExtra("desc", "" + description_edit.getText().toString());
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
        Stop_Service();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }


    // when the video is uploading successfully it will restart the appliaction
    @Override
    public void showResponse(final String responce) {

        if (mConnection != null)
            unbindService(mConnection);


        if (responce.equalsIgnoreCase("Your Video is uploaded Successfully")) {

            Delete_draft_file();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PostVideoActivity.this, responce, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();

                    startActivity(new Intent(PostVideoActivity.this, MainMenuActivity.class));

                }
            }, 1000);


        } else {
            Toast.makeText(PostVideoActivity.this, responce, Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }


    // this is importance for binding the service to the activity
    UploadService mService;
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // this function will stop the the ruuning service
    public void Stop_Service() {

        serviceCallback = this;

        UploadService mService = new UploadService(serviceCallback);

        if (Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("stopservice");
            startService(mServiceIntent);

        }


    }


    public void Save_file_in_draft() {
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


    public void Delete_draft_file() {
        try {
            if (draft_file != null) {
                File file = new File(draft_file);
                file.delete();
            }
        } catch (Exception e) {

        }


    }

}
