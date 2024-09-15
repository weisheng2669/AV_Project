package com.example.av_project.activitys;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.av_project.R;
import com.example.av_project.entity.FFmpegHandler;

public class PullFFMPEGActivity extends AppCompatActivity {
    TextureView textureView;
    Button btn_play_pull;
    SurfaceTexture surfaceTexture;
    Surface surface;
    boolean isAvailable = false;
    static {
            System.loadLibrary("ffmpeg-handler");
    }
    Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    surface = new Surface(surfaceTexture);
                    isAvailable = true;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_ffmpeg);
        textureView = findViewById(R.id.surface_pull);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                surfaceTexture = surface;
                Message msg = handler.obtainMessage();
                msg.what = 0;
                handler.sendMessage(msg);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        btn_play_pull = findViewById(R.id.btn_play_rtmp);
        btn_play_pull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAvailable) {
                    FFmpegHandler.getInstance().pullRTMPSoruce("rtmp://39.107.138.4:1935/myapp/test".toCharArray(), textureView);
                }else{
                    Toast.makeText(PullFFMPEGActivity.this, "初始化未完成!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
