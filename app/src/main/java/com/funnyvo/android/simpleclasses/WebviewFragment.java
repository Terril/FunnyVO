package com.funnyvo.android.simpleclasses;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.funnyvo.android.main_menu.relatetofragment_onback.RootFragment;
import com.funnyvo.android.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebviewFragment extends RootFragment {

    View view;
    Context context;

    WebView webView;
    String url = "www.google.com";
    String title;
    TextView title_txt;

    public WebviewFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_webview, container, false);
        context = getContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            url = bundle.getString("url");
            title = bundle.getString("title");
        }

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        title_txt = view.findViewById(R.id.title_txt);
        title_txt.setText(title);

        showProgressDialog();
        webView = view.findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                dismissProgressDialog();
            }
        });


        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(url);


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return false;
            }
        });

        return view;
    }

}
