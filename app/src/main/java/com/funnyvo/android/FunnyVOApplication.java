package com.funnyvo.android;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class FunnyVOApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        FirebaseApp.initializeApp(this);
    }

}
