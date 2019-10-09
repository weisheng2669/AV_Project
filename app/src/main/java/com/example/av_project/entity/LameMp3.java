package com.example.av_project.entity;

public class LameMp3 {

    static {
        System.loadLibrary("lame-mp3");
    }
    public native String getVersion();

    public native int init(String pcmPath, int audioChannels, int bitRate, int sampleRate, String mp3Path);

    public native void encode();

    public native void destroy();
}
