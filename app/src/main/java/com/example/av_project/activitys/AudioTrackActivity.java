package com.example.av_project.activitys;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.example.av_project.R;
import com.example.av_project.utils.LogUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioTrackActivity extends AppCompatActivity {
    private static final String TAG = "AudioTrackActivity";
    Button btn_mode_static, btn_mode_stream;
    AudioTrack audioTrack;

    int streamType = AudioManager.STREAM_ALARM;
    int sampleRateInHz = 44100;
    int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);


    int static_mode = AudioTrack.MODE_STATIC;
    int noStaticMode = AudioTrack.MODE_STREAM;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track);
        btn_mode_static = findViewById(R.id.btn_audio_track_mode_static);
        btn_mode_stream = findViewById(R.id.btn_audio_track_mode_stream);

        btn_mode_static.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPCMWithStaticMode();
            }
        });
        btn_mode_stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPCMWithNoStaticMode();
            }
        });
    }
    Thread modeStream_playThread = null;
    private void playPCMWithNoStaticMode() {
        //1.初始化audioTrack
        audioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, noStaticMode);
        //2.开启play
        audioTrack.play();
        //3.开启线程播放
        modeStream_playThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = new byte[bufferSizeInBytes];
                DataInputStream dis = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/1.pcm"));
                    dis = new DataInputStream(new BufferedInputStream(fis));
                    int len;
                    //3.循环写入
                    while ((len = dis.read(bytes)) != -1) {
                        audioTrack.write(bytes, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (dis != null) {
                        try {
                            dis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        modeStream_playThread.start();
    }

    enum AudioTrackStatus {
        UNREADY("unready"), READY("ready"), PLAYING("playing"), END("end");
        private String name;

        AudioTrackStatus(String name) {
            this.name = name;
        }
    }

    private static final int READ_FILE = 0;
    Thread modeStatic_playThread = null;  //用来执行播放的线程
    Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case READ_FILE:
                    try {
                        //开启线程,写入数据
                        LogUtils.i(TAG,"开始写入"+fileSize.length);
                        modeStatic_playThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //2.初始化audioTrack
                                audioTrack = new AudioTrack(streamType,sampleRateInHz,channelConfig,audioFormat,fileSize.length,static_mode);
                                //3.开始写入
                                audioTrack.write(fileSize,0,fileSize.length);
                                //4.开始播放
                                audioTrack.play();
                            }
                        });
                        modeStatic_playThread.start();
                        modeStatic_playThread.join();
                        //5.销毁资源
//                        destroyAudioTrack();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    };
    byte[] fileSize;  //表示文件的内容
    private void playPCMWithStaticMode() {
        //1.先计算要播放文件的大小
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory() + "/1.pcm");
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int b;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    fileSize = out.toByteArray();
                    Message msg = handler.obtainMessage();
                    msg.what = READ_FILE;
                    handler.sendMessage(msg);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopAudioTrack(){
        audioTrack.stop();
    }

    public void destroyAudioTrack() {
        audioTrack.release();
        audioTrack = null;
    }
}
