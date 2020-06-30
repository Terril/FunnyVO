package com.funnyvo.android.settings;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.funnyvo.android.accounts.RequestVerificationFragment;
import com.funnyvo.android.main_menu.MainMenuActivity;
import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;
import com.funnyvo.android.simpleclasses.Variables;
import com.funnyvo.android.simpleclasses.WebviewFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        context = getContext();

        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.request_verification_txt).setOnClickListener(this);
        view.findViewById(R.id.privacy_policy_txt).setOnClickListener(this);
        view.findViewById(R.id.logout_txt).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.request_verification_txt:
                Open_request_verification();
                break;

            case R.id.privacy_policy_txt:
                Open_Privacy_url();
                break;

            case R.id.logout_txt:
                Logout();
                break;
        }
    }


    public void Open_request_verification() {
        RequestVerificationFragment request_verificationFragment = new RequestVerificationFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, request_verificationFragment).commit();
    }

    public void Open_Privacy_url() {
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
    public void Logout() {
        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
        editor.putString(Variables.u_id, "").clear();
        editor.putString(Variables.u_name, "").clear();
        editor.putString(Variables.u_pic, "").clear();
        editor.putBoolean(Variables.islogin, false).clear();
        editor.commit();
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainMenuActivity.class));
    }


}
