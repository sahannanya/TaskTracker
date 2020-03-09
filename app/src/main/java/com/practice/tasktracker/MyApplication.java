package com.practice.tasktracker;

import android.app.Application;
//import android.content.Context;
//
//import com.facebook.  stetho.Stetho;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

@SuppressWarnings("ALL")
public class MyApplication extends Application {
    //    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
//        Stetho.initializeWithDefaults(this);
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        refWatcher = LeakCanary.install(this);
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        MyApplication application = (MyApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }
//
}
