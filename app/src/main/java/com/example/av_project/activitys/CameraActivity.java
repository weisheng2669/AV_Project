package com.example.av_project.activitys;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.av_project.R;
import com.example.av_project.utils.LogUtils;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CameraActivity";

    HandlerThread handlerThread = new HandlerThread(TAG);
    Handler mCameraHandler;

    private static final int MESSAGE_GET_DEVICE = 0;
    private static final int MESSAGE_GET_SESSION = 1;

    final int facingBack = 0, facingFront = 1;

    //用于展示数据
    Surface mPreviewSurface;
    SurfaceTexture mPreviewSurfaceTexture;
    //预览的Size
    Size mFrontPreviewSize, mBackPreviewSize;

    CameraManager manager;
    //前后的Camera数据
    CameraCharacteristics frontCameraCharacteristics, backCameraCharacteristics;
    //前后的CameraIDString
    String frontCameraId, backCameraId;

    Button btn_open_back, btn_open_front, btn_close_back;
    TextureView textureView;
    //开启Session
    CameraCaptureSession mPreviewCaptureSession;
    //Request
    CaptureRequest.Builder mPreviewRequestBuilder;
    //CameraDevice
    CameraDevice device;

    //Camera打开的监听方法
    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            LogUtils.i(TAG,"Camera Opened");
            device = camera;
            Message getDeviceMessage = mCameraHandler.obtainMessage();
            getDeviceMessage.what = MESSAGE_GET_DEVICE;
            mCameraHandler.sendMessage(getDeviceMessage);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            LogUtils.i(TAG,"Camera onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            LogUtils.i(TAG,"Camera onError");
        }
    };
    //CameraCaptureSession的监听方法
    private CameraCaptureSession.StateCallback captureSessionStateCallBack = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            LogUtils.i(TAG,"CameraCaptureSession onConfigured");
            mPreviewCaptureSession = session;
            Message getMySessionMessage = mCameraHandler.obtainMessage();
            getMySessionMessage.what = MESSAGE_GET_SESSION;
            mCameraHandler.sendMessage(getMySessionMessage);
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            LogUtils.i(TAG,"CameraCaptureSession onConfigureFailed");
        }
    };
    //开始Request的监听方法
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogUtils.i(TAG,"打开预览失败 ==== onCaptureFailed");
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initViews();
        initData();

    }

    private void initData() {
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGE_GET_DEVICE:
                        createCameraDeviceCaptureSession();
                        //创建Request
                        createCameraRequest();
                        break;
                    case MESSAGE_GET_SESSION:
                        startPreview();
                        break;
                }
            }
        };

    }
    private void initViews() {
        btn_open_back = findViewById(R.id.btn_open_camera_back);
        btn_open_back.setOnClickListener(this);
        btn_open_front = findViewById(R.id.btn_open_camera_front);
        btn_close_back = findViewById(R.id.btn_close_camera_back);
        btn_close_back.setOnClickListener(this);
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //获取用于展示的画面的Surface
                mPreviewSurfaceTexture = surface;
                mPreviewSurface = new Surface(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_camera_back:
                LogUtils.i(TAG,"开启预览");
                preViewBackCamera();
                break;
            case R.id.btn_close_camera_back:
                if (mPreviewCaptureSession != null) {
                    try {
                        mPreviewCaptureSession.stopRepeating();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void preViewBackCamera() {
        //初始化CameraManager和Camera的参数
        initCameraCharacteristics();
        //得到前后的预览Size
        initPreViewSize();
        //设置预览画面的尺寸
        setSurfaceViewSize(facingBack);
        //打开Camera,并回调
        openCameraWithConfig();


    }

    private void startPreview() {
        if(mPreviewCaptureSession!=null) {
            LogUtils.i(TAG,"开始预览");
            try {
                mPreviewCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), captureCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCameraRequest() {
        //创建请求
        try {
            LogUtils.i(TAG,"开始创建Request");
            mPreviewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置输出Surface
            mPreviewRequestBuilder.addTarget(mPreviewSurface);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void createCameraDeviceCaptureSession() {
        if(device!=null){
            LogUtils.i(TAG,"开始创建Session");
            try {
                device.createCaptureSession(Arrays.asList(mPreviewSurface),captureSessionStateCallBack,mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "设备未获取到", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCameraWithConfig() {
        LogUtils.i(TAG,"打开照相机");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //摄像机ID,监听回调方法,处理的Handler
        try {
            manager.openCamera(backCameraId, deviceStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initPreViewSize() {
        /**
         * LEGACY：向后兼容的级别，处于该级别的设备意味着它只支持 Camera1 的功能，不具备任何 Camera2 高级特性。
         * LIMITED：除了支持 Camera1 的基础功能之外，还支持部分 Camera2 高级特性的级别。
         * FULL：支持所有 Camera2 的高级特性。
         * LEVEL_3：新增更多 Camera2 高级特性，例如 YUV 数据的后处理等。
         */
        //设置前摄像头的参数
        int frontLevel = frontCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (frontLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            LogUtils.i(TAG, "前置摄像头不支持新特性");
        }
        StreamConfigurationMap frontMap = frontCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] frontSupportedSize = frontMap.getOutputSizes(SurfaceTexture.class);
        mFrontPreviewSize = getMyPreViewSize(frontSupportedSize);

        //设置后摄像头的参数
        int backLevel = backCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (backLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            LogUtils.i(TAG, "前置摄像头不支持新特性");
        }
        StreamConfigurationMap backMap = backCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] backSupportedSize = backMap.getOutputSizes(SurfaceTexture.class);
        mBackPreviewSize = getMyPreViewSize(backSupportedSize);
    }

    private void initCameraCharacteristics() {
        try {
            //1.初始化CameraManager
            manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            //2.得到CameraList
            String[] cameraIdList = manager.getCameraIdList();
            //3.拿到每个Camera的数据
            for (String cameraId : cameraIdList) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId;
                    frontCameraCharacteristics = characteristics;
                } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraId;
                    backCameraCharacteristics = characteristics;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setSurfaceViewSize(int facing) {
        Size size = null;
        switch (facing) {
            case facingBack:
                size = mBackPreviewSize;
                break;
            case facingFront:
                size = mFrontPreviewSize;
                break;
        }
        mPreviewSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
    }

    private Size getMyPreViewSize(Size[] supportedSize) {
        Size size = null;
        int max = 0;
        for (Size item : supportedSize) {
            if (item.getHeight() == item.getWidth() && item.getWidth() > max) {
                size = item;
            }
        }
        if (size == null) {
            size = new Size(200, 200);
        }
        return size;
    }
}
