package com.funnyvo.android.main_menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.funnyvo.android.R;
import com.funnyvo.android.base.BaseActivity;
import com.funnyvo.android.chat.ChatActivity;
import com.funnyvo.android.simpleclasses.Functions;
import com.funnyvo.android.simpleclasses.Variables;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.funnyvo.android.simpleclasses.Variables.HOME_DATA;


public class MainMenuActivity extends BaseActivity {
    public static MainMenuActivity mainMenuActivity;
    private MainMenuFragment mainMenuFragment;
    long mBackPressed;
    public static String token;
    public static Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mainMenuActivity = this;
        intent = getIntent();
        setIntent(null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Variables.screen_height = displayMetrics.heightPixels;
        Variables.screen_width = displayMetrics.widthPixels;

        Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);

        Variables.user_id = Variables.sharedPreferences.getString(Variables.u_id, "");
        Variables.user_name = Variables.sharedPreferences.getString(Variables.u_name, "");
        Variables.user_pic = Variables.sharedPreferences.getString(Variables.u_pic, "");


        token = FirebaseInstanceId.getInstance().getToken();
        if (token == null || (token.equals("") || token.equals("null")))
            token = Variables.sharedPreferences.getString(Variables.device_token, "null");


        if (savedInstanceState == null) {
            initScreen();
        } else {
            mainMenuFragment = (MainMenuFragment) getSupportFragmentManager().getFragments().get(0);
        }

        Functions.makeDirectory(Variables.APP_FOLDER);
        Functions.makeDirectory(Variables.draft_app_folder);

    }


    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            String type = intent.getStringExtra("type");
            if (type != null && type.equalsIgnoreCase("message")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ChatActivity chat_activity = new ChatActivity();
                        FragmentTransaction transaction = MainMenuActivity.mainMenuActivity.getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);

                        Bundle args = new Bundle();
                        args.putString("user_id", intent.getStringExtra("user_id"));
                        args.putString("user_name", intent.getStringExtra("user_name"));
                        args.putString("user_pic", intent.getStringExtra("user_pic"));

                        chat_activity.setArguments(args);
                        transaction.addToBackStack(null);
                        transaction.replace(R.id.MainMenuFragment, chat_activity).commit();
                    }
                }, 2000);

            }
        }

    }

    private void initScreen() {
        mainMenuFragment = new MainMenuFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mainMenuFragment)
                .commit();

        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (!mainMenuFragment.onBackPressed()) {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    Toast.makeText(getBaseContext(), "Tap Again To Exit", Toast.LENGTH_SHORT).show();
                    mBackPressed = System.currentTimeMillis();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

}
