package com.example.av_project.activitys;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.av_project.R;
import com.example.av_project.Utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorderActivity extends AppCompatActivity {
    private static final String TAG = "AppCompatActivity";
    AudioRecord audioRecord = null;
    Button btn_record,btn_stop;
    File file = new File(Environment.getExternalStorageDirectory()+"/2.pcm");
    FileOutputStream fos = null;
    int audioSource = MediaRecorder.AudioSource.MIC;
    int sampleRateInHz = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
    static volatile boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);
        btn_record = findViewById(R.id.btn_start_record);
        btn_stop = findViewById(R.id.btn_stop_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //构造audioRecord
                if(audioRecord == null||audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED){
                    audioRecord = new AudioRecord(audioSource,sampleRateInHz,channelConfig,audioFormat,bufferSizeInBytes);
                }
                if(file.exists()){
                    file.delete();
                }
                try {
                    file.createNewFile();
                    LogUtils.i(TAG,"创建新文件");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //2.开始记录
                if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                    audioRecord.startRecording();
                    isRecording = true;
                }
                LogUtils.i(TAG,"开始记录");
                Thread recordThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] pcmData = new byte[1024];
                        while(isRecording){
                            audioRecord.read(pcmData,0,pcmData.length);
                            try {
                                if(fos != null) {
                                    fos.write(pcmData, 0, pcmData.length);
                                }else{
                                    LogUtils.d(TAG,"fos == null");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
                recordThread.start();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 isRecording = false;
                 LogUtils.i(TAG,"停止记录");
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 destroyRecord();
            }
        });

    }

    private void destroyRecord() {
        if(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if(audioRecord !=null){
            audioRecord = null;
        }
    }
}
