package com.funnyvo.android.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.customview.SplashScreenCubeTransformation;
import com.funnyvo.android.home.datamodel.Home;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.splash.fragments.FirstFragment;
import com.funnyvo.android.splash.fragments.SecondFragment;
import com.funnyvo.android.splash.fragments.ThirdFragment;
import com.funnyvo.android.videorecording.VideoRecorderActivityNew;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.funnyvo.android.simpleclasses.Variables.API_SUCCESS_CODE;
import static com.funnyvo.android.simpleclasses.Variables.FETCH_SETTINGS;
import static com.funnyvo.android.simpleclasses.Variables.HOME_DATA;
import static com.funnyvo.android.simpleclasses.Variables.IS_FIRST_TIME;
import static com.funnyvo.android.simpleclasses.Variables.PAGE_COUNT_SHOW_ADS_AFTER_VIEWS;
import static com.funnyvo.android.simpleclasses.Variables.SHOW_ADS;
import static com.funnyvo.android.simpleclasses.Variables.SHOW_ALL_VIDEOS;
import static com.funnyvo.android.simpleclasses.Variables.SHOW_ALL_VIDEOS_WITH_ADS;
import static com.funnyvo.android.simpleclasses.Variables.device_id;
import static com.funnyvo.android.simpleclasses.Variables.pref_name;
import static com.funnyvo.android.simpleclasses.Variables.sharedPreferences;

public class SplashActivity extends BaseActivity {
    private SplashPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private MaterialButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        MaterialButton btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);
        sharedPreferences = getSharedPreferences(pref_name, MODE_PRIVATE);

        if (sharedPreferences.getBoolean(IS_FIRST_TIME, false)) {
            ImageView imvLogoImage = findViewById(R.id.imvLogoImage);
            imvLogoImage.setVisibility(View.VISIBLE);
            btnSkip.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            callApiForSettings();
        } else {
            viewPager = findViewById(R.id.viewPager);
            pagerAdapter = new SplashPagerAdapter(getSupportFragmentManager());
            addingFragmentsTOpagerAdapter();
            viewPager.setAdapter(pagerAdapter);
            viewPager.setPageTransformer(true, new SplashScreenCubeTransformation());

            final String androidId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            SharedPreferences.Editor editor2 = sharedPreferences.edit();
            editor2.putString(device_id, androidId).apply();

            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callNextActivity();
                }
            });

            btnNext.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (btnNext.getText().toString().equals(getString(R.string.finish))) {
                        callNextActivity();
                    } else {
                        viewPager.setCurrentItem(getItem(+1), true); //getItem(-1) for previous
                        viewPager.setPageTransformer(true, new SplashScreenCubeTransformation());
                    }

                }
            });
        }
    }

    private void callApiForSettings() {
        ApiRequest.callApi(this, FETCH_SETTINGS, null, new Callback() {
            @Override
            public void response(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.optString("code");
                    if (code.equals(API_SUCCESS_CODE)) {
                        JSONObject msgJson = jsonObject.getJSONObject("msg");
                        String advertisement = msgJson.optString("advertisement");
                        String adAfterVideos = msgJson.optString("ad_after_no_videos");
                        if (advertisement.equals("1")) {
                            SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
                            editor.putBoolean(SHOW_ADS, true);
                            editor.putString(PAGE_COUNT_SHOW_ADS_AFTER_VIEWS, adAfterVideos);
                            editor.apply();
                        }
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }

                String url;
                if (Variables.sharedPreferences.getBoolean(Variables.SHOW_ADS, false)) {
                    url = SHOW_ALL_VIDEOS_WITH_ADS;
                } else {
                    url = SHOW_ALL_VIDEOS;
                }
                callApiForGetAllVideos(url);
            }
        });
    }

    private void callApiForGetAllVideos(String url) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("token", MainMenuActivity.token);
            parameters.put("page_number", 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();
        ApiRequest.callApi(this, url, parameters, new Callback() {
            @Override
            public void response(String response) {
                dismissProgressDialog();
                parseData(response);
            }
        });
    }

    public void parseData(String response) {
        ArrayList dataList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            String code = jsonObject.optString("code");
            if (code.equals(Variables.API_SUCCESS_CODE)) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                int arrayItems = msgArray.length();
                for (int i = 0; i < arrayItems; i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    Home item = new Home();
                    item.fb_id = itemdata.optString("fb_id");

                    JSONObject userInfo = itemdata.optJSONObject("user_info");

                    if (userInfo != null) {
                        item.username = userInfo.optString("username");
                        item.first_name = userInfo.optString("first_name", getResources().getString(R.string.app_name));
                        item.last_name = userInfo.optString("last_name", "User");
                        item.profile_pic = userInfo.optString("profile_pic", "null");
                        item.verified = userInfo.optString("verified");
                    }

                    JSONObject soundData = itemdata.optJSONObject("sound");
                    if (soundData != null) {
                        item.sound_id = soundData.optString("id");
                        item.sound_name = soundData.optString("sound_name");
                        item.sound_pic = soundData.optString("thum");
                        item.soundUrl = soundData.optString("sound_url");
                    }

                    JSONObject count = itemdata.optJSONObject("count");
                    if (count != null) {
                        item.like_count = count.optString("like_count");
                        item.video_comment_count = count.optString("video_comment_count");
                    }

                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");
                    item.video_url = itemdata.optString("video");

                    item.video_description = itemdata.optString("description");

                    item.thum = itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");
                    if (item.video_url.contains(Variables.base_url)) {
                        item.video_url = item.video_url.replace(Variables.base_url + "/", "");
                    }
                    if (item.sound_pic.contains(Variables.base_url)) {
                        item.sound_pic = item.sound_pic.replace(Variables.base_url + "/", "");
                    }
                    if (item.thum.contains(Variables.base_url)) {
                        item.thum = item.thum.replace(Variables.base_url + "/", "");
                    }

                    dataList.add(item);
                }

            } else {
                //   Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);

            intent.putExtra(HOME_DATA, dataList);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
                setIntent(null);
            }

            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void callNextActivity() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_FIRST_TIME, true);
        editor.apply();
        Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);

        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
            setIntent(null);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void addingFragmentsTOpagerAdapter() {
        pagerAdapter.addFragments(new ThirdFragment());
        pagerAdapter.addFragments(new FirstFragment());
        pagerAdapter.addFragments(new SecondFragment());
    }

    public class SplashPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragmentList = new ArrayList<>();

        public SplashPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public void startUpdate(@NonNull ViewGroup container) {
            super.startUpdate(container);
            if (viewPager.getCurrentItem() == getCount() - 1) {
                btnNext.setText(R.string.finish);
            } else {
                btnNext.setText(R.string.next);
            }
        }

        public void addFragments(Fragment fragment) {
            fragmentList.add(fragment);
        }
    }
}
