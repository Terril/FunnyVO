package com.funnyvo.android.videorecording;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.daasuu.gpuv.player.GPUPlayerView;
import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.customview.FunnyVOEditTextView;
import com.funnyvo.android.helper.PlayerEventListener;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.services.ServiceCallback;
import com.funnyvo.android.services.UploadService;
import com.funnyvo.android.services.UploadWorker;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PostVideoActivity extends BaseActivity implements View.OnClickListener {

    private String video_path;
    private ServiceCallback serviceCallback;
    private FunnyVOEditTextView descriptionEdit;
    private UploadService mService;
    private String draft_file;

    private PlayerEventListener eventListener;

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

        video_path = Variables.OUTPUT_FILTER_FILE;
        GPUPlayerView gpuPlayerView = setPlayer(video_path, eventListener);
        ((MovieWrapperView) findViewById(R.id.layout_post_movie_wrapper)).addView(gpuPlayerView);
        gpuPlayerView.onResume();

        findViewById(R.id.btnGoBackPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final FloatingActionButton btnUploadVideo = findViewById(R.id.btnUploadVideo);
        btnUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUploadVideo.setEnabled(false);
                // showProgressDialog();
                startService();

            }
        });

        findViewById(R.id.btnSaveLocal).setOnClickListener(this);
        descriptionEdit = findViewById(R.id.edtDescriptionAndHashTags);
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
    private void startService() {
        OneTimeWorkRequest.Builder uploadWork = new OneTimeWorkRequest.Builder(UploadWorker.class);
        Data.Builder data = new Data.Builder();
//Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
        data.putString("uri", "" + Uri.fromFile(new File(video_path)));
        data.putString("desc", descriptionEdit.getText().toString().trim());
//Set Input Data
        uploadWork.setInputData(data.build());
        WorkRequest uploadWorkRequest = uploadWork.build();
        WorkManager
                .getInstance(getApplicationContext())
                .enqueue(uploadWorkRequest);

        Toast.makeText(PostVideoActivity.this, R.string.continue_using_app, Toast.LENGTH_LONG).show();
        if (player != null) {
            player.removeListener(eventListener);
            player.release();
            player = null;
        }

        deleteDraftFile();

        startActivity(new Intent(PostVideoActivity.this, MainMenuActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    // when the video is uploading successfully it will restart the application
    private void saveFileInDraft() {
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

                Toast.makeText(PostVideoActivity.this, R.string.file_saved_in_draft, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PostVideoActivity.this, MainMenuActivity.class));

            } else {
                Toast.makeText(PostVideoActivity.this, R.string.save_failed_into_draft, Toast.LENGTH_SHORT).show();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void deleteDraftFile() {
        try {
            if (draft_file != null) {
                File file = new File(draft_file);
                file.delete();
            }
        } catch (Exception e) {

        }
    }
}
