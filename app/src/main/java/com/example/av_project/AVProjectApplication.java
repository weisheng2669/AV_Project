package com.example.av_project;

import android.app.Application;

import com.example.av_project.utils.CrashHandler;
import com.example.av_project.utils.LogUtils;
import com.tencent.bugly.crashreport.CrashReport;

public class AVProjectApplication extends Application {
    private static AVProjectApplication sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;


        CrashReport.initCrashReport(getApplicationContext(), "91c5f8c4ee", false);
        LogUtils.getInstance();
    }
}
