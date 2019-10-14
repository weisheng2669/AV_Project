package com.example.av_project.activitys;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.example.av_project.R;
import com.example.av_project.entity.FFmpegHandler;
import com.example.av_project.utils.CameraPreView;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FFMpegActivity extends AppCompatActivity {
    Button btn_start_pusher,btn_stop_pusher;

    //用于预览的TextureView
    TextureView preViewTexture;
    //用于保存数据的ImageReader
    ImageReader mImageReader;
    CameraPreView.Builder builder;
    CameraPreView cameraPreView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);
        btn_start_pusher= findViewById(R.id.btn_start_pusher);
        btn_stop_pusher = findViewById(R.id.btn_stop_pusher);
        preViewTexture = findViewById(R.id.preview_container);
        mImageReader = ImageReader.newInstance(640, 480,
                ImageFormat.YUV_420_888, 1);
        btn_start_pusher.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
               startCameraWork();
            }
        });
        btn_stop_pusher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreView.stop();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startCameraWork() {
        builder = new CameraPreView.Builder(this,preViewTexture,mImageReader, CameraPreView.CAMERA_FACING.FACING_BACK);
        cameraPreView = builder.build();
        //开启
        cameraPreView.start();
    }



    @Override
    protected void onResume() {
        super.onResume();
        FFmpegHandler.getInstance().init("rtmp://39.107.138.4:1935/myapp/test");
    }

    @Override
    protected void onPause() {
        super.onPause();
        FFmpegHandler.getInstance().close();
        super.onPause();
    }
}
