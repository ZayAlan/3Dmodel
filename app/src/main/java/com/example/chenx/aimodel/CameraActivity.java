package com.example.chenx.aimodel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chenx.aimodel.bottom.ModelLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {


    public static final String refresh_action = "jason.broadcast.action";
    private static String TAG = "CameraActivity";
    private android.hardware.Camera mCamera;
    //拍照、保存、继续拍
    private ImageButton mButton, mButton1, mButton2;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    private String path = "aimodel";//图片所在文件夹名
    private String path1;
    private Bitmap bmp;

    //为内参矩阵准备
//    public static float zoom;
//    public static float pictureHeight;
//    public static float pictureWidth;
//    public static float sensorSizeHeight;
//    public static float sensorSizeWidth;

    String strPictureName=null;
    Date date=null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 隐藏状态栏 */
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* 隐藏标题栏 */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /* 屏幕显示可转换 */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_camera);
        /* SurfaceHolder设定 */
        mSurfaceView = findViewById(R.id.mSurfaceView);
        holder = mSurfaceView.getHolder();
        holder.addCallback(CameraActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /* Button初始化 */
        mButton = findViewById(R.id.take_picture);
        mButton1 = findViewById(R.id.camera_sure);
        mButton2 = findViewById(R.id.camera_back);



        //设置内参矩阵相关参数
//        pictureHeight = mCamera.getParameters().getPictureSize().height;
//        pictureWidth = mCamera.getParameters().getPictureSize().width;
//        SizeF sizeF = new SizeF(0,0);
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String[] cameraIds = manager.getCameraIdList();
//            CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[0]);
//            sizeF = character.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        sensorSizeHeight = sizeF.getHeight();
//        sensorSizeWidth = sizeF.getWidth();



        /* 拍照Button的事件处理 */
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /* 自动对焦后拍照 */
                mCamera.autoFocus(mAutoFocusCallback);
            }
        });

        /* Button的事件处理 */
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i(TAG,"click button2");
                Intent intent = new Intent(CameraActivity.this, CvActivity.class);
                CvActivity.pictureHeight = mCamera.getParameters().getPictureSize().height;
                CvActivity.pictureWidth = mCamera.getParameters().getPictureSize().width;
                SizeF sizeF = new SizeF(0,0);
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                try {
                    String[] cameraIds = manager.getCameraIdList();
                    CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[0]);
                    sizeF = character.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                CvActivity.sensorSizeHeight = sizeF.getHeight();
                CvActivity.sensorSizeWidth = sizeF.getWidth();
                CvActivity.zoom = mCamera.getParameters().getMaxZoom();         //获取的焦距可能有问题
                startActivity(intent);
                finish();
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(refresh_action);
//                sendBroadcast(intent);
                finish();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
            /* 打开相机， */
            mCamera = android.hardware.Camera.open();
            mCamera.setPreviewDisplay(holder);
            Log.i(TAG,"create camera---");
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
                               int h) {
        /* 相机初始化 */
        Log.i(TAG,"init camera");
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.i(TAG,"destoryed camera");
        stopCamera();
        mCamera.release();
        mCamera = null;
    }

    /* 拍照的method */
    private void takePicture() {
        if (mCamera != null) {
            Log.i(TAG,"takePicture");
            mCamera.takePicture(null, rawCallback, jpegCallback);
        }
    }

    private android.hardware.Camera.ShutterCallback shutterCallback = new android.hardware.Camera.ShutterCallback() {
        public void onShutter() {

        }
    };

    private android.hardware.Camera.PictureCallback rawCallback = new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

        }
    };


    private android.hardware.Camera.PictureCallback jpegCallback = new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            bmp = rotateBitmapByDegree(bmp, 90);
            save();
            initCamera();
        }
    };

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    private void save(){
        if (bmp != null) {
            /* 检查SDCard是否存在 */
            if (!Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                /* SD卡不存在，显示Toast信息 */
                Toast.makeText(CameraActivity.this,
                        "SD卡不存在!无法保存相片,请插入SD卡。", Toast.LENGTH_LONG).show();
            } else {
                try {
                    /* 文件不存在就创建 */
                    File f = new File(Environment
                            .getExternalStorageDirectory(), path);
                    Log.i(TAG,"click button2:" + f.getAbsolutePath());
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    /* 保存相片文件 */
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");//获取当前时间，进一步转化为字符串
                    date =new Date();
                    strPictureName=format.format(date);
                    path1 = strPictureName+ ".jpg";
                    File n = new File(f, path1);
                    FileOutputStream bos = new FileOutputStream(n
                            .getAbsolutePath());
                    /* 文件转换 */
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                    Toast.makeText(CameraActivity.this,
                            path1 + "保存成功!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /* 告定义class AutoFocusCallback */
    public final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, android.hardware.Camera camera) {
            if (success) {
                takePicture();
            }
        }
    };

    /* 相机初始化的method */
    private void initCamera() {
        if (mCamera != null) {
            try {
                android.hardware.Camera.Parameters parameters = mCamera.getParameters();

                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setPictureSize(1920, 1080);
                mCamera.setParameters(parameters);
                /* 开启预览画面 */
                int degrees = getDisplayOritation(getDispalyRotation(),1);
                mCamera.setDisplayOrientation(degrees);
                mCamera.startPreview();
                Log.i(TAG, "init camera");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getDisplayOritation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private int getDispalyRotation() {
        int i = getWindowManager().getDefaultDisplay().getRotation();
        switch (i) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    /* 停止相机的method */
    private void stopCamera() {
        if (mCamera != null) {
            try {
                /* 停止预览 */
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(refresh_action);
        sendBroadcast(intent);
    }
}
