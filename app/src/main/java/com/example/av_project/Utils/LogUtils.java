package com.example.av_project.Utils;

import android.util.Log;

public class LogUtils {

    private volatile static LogUtils instance = null;
    private static boolean isDeBug = true;

    private LogUtils(){

    }

    public static LogUtils getInstance(){
        if(instance == null){
            synchronized (LogUtils.class){
                if(instance == null){
                    instance = new LogUtils();
                }
            }
        }
        return instance;
    }

    public static void d(String className,String msg){
        if(isDeBug){
            Log.d(className,msg);
        }
    }
    public static void i(String className,String msg){
        Log.i(className,msg);
    }

    public static void e(String className,String msg){
        Log.e(className,msg);
    }
    public static void w(String className,String msg){
        Log.w(className,msg);
    }

}
