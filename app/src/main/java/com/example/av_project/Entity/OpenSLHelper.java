package com.example.av_project.Entity;

public class OpenSLHelper {
    static {
        System.loadLibrary("opensles-audio-jni");
    }
    public static native void createEngine();
    public static native boolean createAudioRecorder();
    public static native void startRecording();
    public static native void shutdown();
}
