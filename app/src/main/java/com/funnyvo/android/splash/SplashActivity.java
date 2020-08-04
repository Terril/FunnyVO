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
import com.funnyvo.android.customview.SplashScreenCubeTransformation;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.splash.fragments.FirstFragment;
import com.funnyvo.android.splash.fragments.SecondFragment;
import com.funnyvo.android.splash.fragments.ThirdFragment;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    private SplashPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private MaterialButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);

        if (Variables.sharedPreferences.getBoolean(Variables.IS_FIRST_TIME, false)) {
            ImageView imvLogoImage = findViewById(R.id.imvLogoImage);
            imvLogoImage.setVisibility(View.VISIBLE);
            callApiForSettings();
        }

        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new SplashPagerAdapter(getSupportFragmentManager());
        addingFragmentsTOpagerAdapter();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(true, new SplashScreenCubeTransformation());

        final String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SharedPreferences.Editor editor2 = Variables.sharedPreferences.edit();
        editor2.putString(Variables.device_id, androidId).apply();

        MaterialButton btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNextActivity();
            }
        });

        btnNext = findViewById(R.id.btnNext);
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

    private void callApiForSettings() {
        ApiRequest.callApi(this, Variables.FETCH_SETTINGS, null, new Callback() {
            @Override
            public void response(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.optString("code");
                    if (code.equals(Variables.API_SUCCESS_CODE)) {
                        JSONObject msgJson = jsonObject.getJSONObject("msg");
                        String advertisement = msgJson.optString("advertisement");
                        if (advertisement.equals("1")) {
                            SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
                            editor.putBoolean(Variables.SHOW_ADS, true);
                            editor.apply();
                        }
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
                Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);

                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                    setIntent(null);
                }

                startActivity(intent);
                finish();
            }
        });
    }

    private void callNextActivity() {
        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
        editor.putBoolean(Variables.IS_FIRST_TIME, true);
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
