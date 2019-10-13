package com.example.av_project.utils;

import android.Manifest;
import android.content.Context;
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
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import com.example.av_project.entity.FFmpegHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CAMERA_SERVICE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraPreView {

    private static final String TAG = CameraPreView.class.getSimpleName();
    private static volatile HandlerThread cameraHandlerThread = null;
    private static Handler mCameraHandler;
    private Context context;
    private TextureView mPreViewTexture;
    private ImageReader mImageReader;
    private MediaRecorder mediaRecorder;

    private static final int MESSAGE_GET_DEVICE = 0;
    private static final int MESSAGE_GET_SESSION = 1;

    List<Surface> surfaceList = new ArrayList<>();

    //预览的Size
    Size mFrontPreviewSize, mBackPreviewSize;
    //用于预览的SurfaceTexture
    SurfaceTexture mPerViewSurfaceTexture;

    CameraManager manager;
    //前后的Camera数据
    CameraCharacteristics frontCameraCharacteristics, backCameraCharacteristics;
    //前后的CameraIDString
    String frontCameraId, backCameraId;

    //开启Session
    CameraCaptureSession mPreviewCaptureSession;
    //Request
    CaptureRequest.Builder mPreviewRequestBuilder;
    //CameraDevice
    CameraDevice device;

    CAMERA_FACING facing;

    OP_NAME op_name;

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
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

//            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            Image image = reader.acquireLatestImage();
            //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
            if (image == null) {
                return;
            }

            final Image.Plane[] planes = image.getPlanes();

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = image.getWidth();
            int height = image.getHeight();

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yBytes = new byte[width * height];
            //目标数组的装填到的位置
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                ByteBuffer buffer = planes[i].getBuffer();

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }
           // FFmpegHandler.getInstance().pushCameraData(yBytes, yBytes.length, uBytes, uBytes.length, vBytes, vBytes.length);
            image.close();
        }

    };

    public void stop() {
        if (mPreviewCaptureSession != null) {
            try {
                mPreviewCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy(){
        try {
            mPreviewCaptureSession.abortCaptures();
            mPreviewCaptureSession.close();
            mPreviewCaptureSession = null;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    public enum CAMERA_FACING{
        FACING_FRONT("front"),FACING_BACK("back"),FACING_OUT_ADD("out_add");
        private String name;
        CAMERA_FACING(String name){
            this.name = name;
        }
    }
    public enum OP_NAME{
        RECORDER("recorder"),PUSHER_STREAM("pusher");
        private String name;
        OP_NAME(String name){
            this.name = name;
        }
    }

    public final static class Builder{

        private final CameraPreView mCameraPreView;
        //记录数据
        public Builder(Context context, TextureView mPreViewTextrueView, ImageReader imageReader,CAMERA_FACING facing){
            mCameraPreView = new CameraPreView(context,mPreViewTextrueView,imageReader,facing);
        }
        public Builder(Context context, TextureView mPreViewTextrueView, MediaRecorder mediaRecorder,CAMERA_FACING facing){
            mCameraPreView = new CameraPreView(context,mPreViewTextrueView,mediaRecorder,facing);
        }
        public Builder init(){
            return this;
        }

        public CameraPreView build(){
            return mCameraPreView;
        }

    }
    public CameraPreView(Context context, TextureView mPreViewTexture, ImageReader imageReader, CAMERA_FACING facing){
        this.context = context;
        this.mPreViewTexture = mPreViewTexture;
        this.mPerViewSurfaceTexture = mPreViewTexture.getSurfaceTexture();
        surfaceList.add(new Surface(mPerViewSurfaceTexture));
        this.mImageReader = imageReader;
        this.facing = facing;
        init(OP_NAME.PUSHER_STREAM);
    }

    public CameraPreView(Context context, TextureView mPerViewTexture, MediaRecorder mediaRecorder, CAMERA_FACING facing){
        this.context = context;
        this.mPreViewTexture = mPerViewTexture;
        this.mediaRecorder = mediaRecorder;
        this.facing = facing;
        init(OP_NAME.RECORDER);
    }

    private void init(OP_NAME op) {
        setHandlerThread();
        setHandler();
        mPreViewTexture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
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
        switch (op){
            case RECORDER:
                op_name = OP_NAME.RECORDER;
                surfaceList.add(mediaRecorder.getSurface());
                break;
            case PUSHER_STREAM:
                op_name = OP_NAME.PUSHER_STREAM;
//                surfaceList.add(mImageReader.getSurface());
//                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mCameraHandler);
                break;
        }

    }

    private void setHandler() {
        if(mCameraHandler == null) {
            mCameraHandler = new Handler(cameraHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
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
    }
    private void startPreview() {
        if(mPreviewCaptureSession!=null) {
            LogUtils.i(TAG,"开始预览");
            try {
                if(op_name.equals(OP_NAME.RECORDER)){
                    mediaRecorder.start();
                }else if(op_name.equals(OP_NAME.PUSHER_STREAM)){
//                    mImageReader
                }
                mPreviewCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), captureCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        //初始化CameraManager和Camera的参数
        initCameraCharacteristics();
        //得到前后的预览Size
        initPreViewSize();
        //设置预览画面的尺寸
        setSurfaceViewSize(facing);
        //打开Camera,并回调
        openCameraWithConfig();
    }

    private void createCameraRequest() {
        //创建请求
        try {
            LogUtils.i(TAG,"开始创建Request");
            mPreviewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置输出Surface
            for(Surface target:surfaceList) {
                LogUtils.i(TAG,target.toString());
                mPreviewRequestBuilder.addTarget(target);
            }
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCameraWithConfig() {
        LogUtils.i(TAG,"打开照相机");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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

    private void setSurfaceViewSize(CAMERA_FACING facing) {
        Size size = null;
        switch (facing) {
            case FACING_BACK:
                size = mBackPreviewSize;
                break;
            case FACING_FRONT:
                size = mFrontPreviewSize;
                break;
            case FACING_OUT_ADD:
                //TODO外接摄像头
                break;
        }
        mPerViewSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
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

    private void initCameraCharacteristics() {
        try {
            //1.初始化CameraManager
            manager = (CameraManager)context.getSystemService(CAMERA_SERVICE);
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



    public static void setHandlerThread() {
        if(cameraHandlerThread == null){
            synchronized (HandlerThread.class){
                if(cameraHandlerThread == null){
                    cameraHandlerThread = new HandlerThread(TAG);
                    cameraHandlerThread.start();

                }
            }
        }
    }
    private void createCameraDeviceCaptureSession() {
        if(device!=null){
            LogUtils.i(TAG,"开始创建Session");
            try {
                device.createCaptureSession(surfaceList,captureSessionStateCallBack,mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context, "设备未获取到", Toast.LENGTH_SHORT).show();
        }
    }
}
