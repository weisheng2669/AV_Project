package com.example.av_project.utils;


import android.content.Context;
import java.lang.Thread.*;

public class CrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private volatile static CrashHandler sInstance;
    private UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;


    public CrashHandler() {

    }

    public static CrashHandler getInstance(){
        if(sInstance == null){
            synchronized (CrashHandler.class){
                if(sInstance == null){
                    sInstance = new CrashHandler();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context){
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
    }
}
