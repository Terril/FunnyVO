package com.funnyvo.android.watchvideos;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.FragmentTransaction;

import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.simpleclasses.Variables;

import java.util.ArrayList;

public class WatchVideosActivity extends BaseActivity {

    private ArrayList<Home> data_list = new ArrayList<>();
    private int position = 0;
    private String video_id;
    private String link = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Variables.sharedPreferences == null) {
            Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE);
        }

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        if (intent != null) {

            Uri appLinkData = intent.getData();
            video_id = intent.getStringExtra("video_id");

            if (appLinkData == null) {
                data_list = (ArrayList<Home>) intent.getSerializableExtra("arraylist");
                position = intent.getIntExtra("position", 0);
            } else {
                link = appLinkData.toString();
                String[] parts = link.split("=");
                video_id = parts[1];
            }

            bundle.putString("video_id", video_id);
            bundle.putString("link", link);
            bundle.putInt("position", position);
            bundle.putSerializable("arraylist", data_list);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        WatchVideosFragment fragment = WatchVideosFragment.Companion.getInstance();
        fragment.setArguments(bundle);
        ft.add(R.id.frameLayoutWatchVideo, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (video_id != null && link != null) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        WatchVideosFragment fragment = WatchVideosFragment.Companion.getInstance();
        ft.remove(fragment);
        ft.commit();
    }
}
