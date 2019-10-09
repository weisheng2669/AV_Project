package com.example.av_project;

import android.app.Application;

import com.example.av_project.utils.CrashHandler;
import com.example.av_project.utils.LogUtils;

public class AVProjectApplication extends Application {
    private static AVProjectApplication sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);

        LogUtils.getInstance();
    }
}
