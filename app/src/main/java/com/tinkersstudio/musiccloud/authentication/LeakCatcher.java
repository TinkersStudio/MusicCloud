package com.tinkersstudio.musiccloud.authentication;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Using Canary Leak to detect leak memory
 */
public class LeakCatcher extends Application {
    private RefWatcher mRefWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        return ((LeakCatcher) context.getApplicationContext()).mRefWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        //mRefWatcher = LeakCanary.install(this);
    }
}
