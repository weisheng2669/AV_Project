package com.example.av_project.activitys;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.av_project.R;
import com.example.av_project.utils.LogUtils;

public class DirectPlayActivity extends AppCompatActivity {

    private static final String TAG = "DirectPlayActivity";

    private AudioTrack audioTrack = null;
    private AudioRecord audioRecord = null;
    Button btn_play,btn_stop,btn_destroy;
    /* AudioRecord 的配置*/
    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private int sampleRateInHz = 44100;
    private int audioRecordChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int audioRecordBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,audioRecordChannelConfig,audioFormat);

    /* AudioTrack的配置 */
    private int streamType = AudioManager.STREAM_ALARM;
    private int audioTrackChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private int audioTrackBufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz,audioTrackChannelConfig,audioFormat);
    private int mode =  AudioTrack.MODE_STREAM;

    static volatile boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_play);
        btn_play = findViewById(R.id.btn_play);
        btn_stop = findViewById(R.id.btn_stop);
        btn_destroy = findViewById(R.id.btn_destroy);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAudio();
                startRecordAndPlay();
            }
        });

    }

    private void startRecordAndPlay() {
        if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
            audioRecord.startRecording();
            isRecording = true;
        }
        if(audioTrack.getState() == AudioTrack.STATE_INITIALIZED){
            audioTrack.play();
        }
        LogUtils.d(TAG,"开始写入");
        Thread recordAndPlay = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] pcmData = new byte[1024];
                while (isRecording){

                    audioRecord.read(pcmData,0,pcmData.length);
                    audioTrack.write(pcmData,0,pcmData.length);
                }
            }
        });
        recordAndPlay.start();
    }

    private void initAudio() {
        audioRecord = new AudioRecord(audioSource,sampleRateInHz,audioRecordChannelConfig,audioFormat,audioRecordBufferSizeInBytes);
        audioTrack = new AudioTrack(streamType,sampleRateInHz,audioTrackChannelConfig,audioFormat,audioTrackBufferSizeInBytes,mode);
    }
}
