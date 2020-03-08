package com.practice.tasktracker;

import android.app.Application;

import com.facebook.stetho.Stetho;

import leakcanary.LeakCanary;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
