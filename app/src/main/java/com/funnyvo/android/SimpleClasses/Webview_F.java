package com.funnyvo.android.SimpleClasses;


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

import com.funnyvo.android.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.funnyvo.android.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Webview_F extends RootFragment {

    View view;
    Context context;

    ProgressBar progress_bar;
    WebView webView;
    String url = "www.google.com";
    String title;
    TextView title_txt;

    public Webview_F() {

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

        webView = view.findViewById(R.id.webview);
        progress_bar = view.findViewById(R.id.progress_bar);
        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 80) {
                    progress_bar.setVisibility(View.GONE);
                }
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
