package com.funnyvo.android.settings;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.funnyvo.android.accounts.RequestVerificationFragment;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.ApiRequest;
import com.funnyvo.android.simpleclasses.Callback;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.simpleclasses.WebviewFragment;

import org.json.JSONException;
import org.json.JSONObject;

import static com.funnyvo.android.simpleclasses.Variables.API_SUCCESS_CODE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends RootFragment implements View.OnClickListener {

    private View view;
    private Context context;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        context = getContext();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.request_verification_txt).setOnClickListener(this);
        view.findViewById(R.id.privacy_policy_txt).setOnClickListener(this);
        view.findViewById(R.id.logout_txt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.request_verification_txt:
                openRequestVerification();
                break;

            case R.id.privacy_policy_txt:
                openPrivacyUrl();
                break;

            case R.id.logout_txt:
                logout();
                break;
        }
    }


    private void openRequestVerification() {
        RequestVerificationFragment request_verificationFragment = new RequestVerificationFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, request_verificationFragment).commit();
    }

    private void openPrivacyUrl() {
        WebviewFragment webview_fragment = new WebviewFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        bundle.putString("url", Variables.privacy_policy);
        bundle.putString("title", "Privacy Policy");
        webview_fragment.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, webview_fragment).commit();
    }

    // this will erase all the user info store in locally and logout the user
    private void logout() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.callApi(getActivity(), Variables.Logout, parameters, new Callback() {
            @Override
            public void response(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.optString("code");
                    if (code.equals(API_SUCCESS_CODE)) {
                        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
                        editor.putString(Variables.u_id, "").clear();
                        editor.putString(Variables.u_name, "").clear();
                        editor.putString(Variables.u_pic, "").clear();
                        editor.putBoolean(Variables.islogin, false).clear();
                        editor.apply();
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), MainMenuActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


}
