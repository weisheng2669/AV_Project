package com.example.av_project.activitys;

import android.graphics.ImageFormat;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.example.av_project.R;
import com.example.av_project.entity.FFmpegHandler;
import com.example.av_project.utils.camera_relation.CameraPreView;

public class DecodeYUVActivity extends AppCompatActivity {
    private static final String PULL_URL_XINSHEN = "rtmp://123.183.160.249:11935/app/test";
    private static final String PUSH_URL_XINSHEN = "rtmp://123.183.160.249:19350/app/test";

    private static final String PUSH_URL_ALIYUN = "rtmp://39.107.138.4:1935/myapp/test";
    private static final String PULL_URL_ALIYUN = "rtmp://39.107.138.4:1935/myapp/test";

    static {
        System.loadLibrary("decode-video");
    }

    SurfaceView surfaceView;
    Surface surface;
    SurfaceHolder surfaceViewHolder;
    Button btn_push_video_aliyun, btn_push_video_xinshen,btn_pull_video_aliyun,btn_pull_video_xinshen,btn_switch_camera;

    //用于预览的TextureView
    TextureView preViewTexture;
    //用于保存数据的ImageReader
    ImageReader mImageReader;
    CameraPreView.Builder builder;
    CameraPreView cameraPreView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_layout);
        surfaceView = findViewById(R.id.surface_video);
        surfaceViewHolder = surfaceView.getHolder();
        mImageReader = ImageReader.newInstance(640, 480,
                ImageFormat.YUV_420_888, 1);
        btn_pull_video_aliyun = findViewById(R.id.start_play);
        btn_pull_video_xinshen = findViewById(R.id.start_play_xinshen);
        btn_push_video_aliyun = findViewById(R.id.start_push);
        btn_push_video_xinshen  = findViewById(R.id.start_push_xinshen);
        btn_switch_camera = findViewById(R.id.btn_switch_camera);
        preViewTexture = findViewById(R.id.preview_container);



        surfaceViewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surface = holder.getSurface();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        btn_push_video_aliyun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FFmpegHandler.getInstance().init(PUSH_URL_ALIYUN);
                startCameraWork();
                cameraPreView.pushToServer(PUSH_URL_ALIYUN);
            }
        });

        btn_push_video_xinshen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FFmpegHandler.getInstance().init(PUSH_URL_XINSHEN);
                startCameraWork();
                cameraPreView.pushToServer(PUSH_URL_ALIYUN);
            }
        });
        btn_pull_video_aliyun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
                startDecode(PULL_URL_ALIYUN, surface);
                destroy();
            }
        });

        btn_pull_video_xinshen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
                startDecode(PULL_URL_XINSHEN, surface);
                destroy();
            }
        });
        btn_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreView.switchCamera();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FFmpegHandler.getInstance().close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startCameraWork() {
        builder = new CameraPreView.Builder(this,preViewTexture,mImageReader, CameraPreView.CAMERA_FACING.FACING_BACK);
        cameraPreView = builder.build();
        //开启
        cameraPreView.start();
    }

    public native void init();

    public native int startDecode(String videoUrl, Surface surface);

    public native void destroy();
}
