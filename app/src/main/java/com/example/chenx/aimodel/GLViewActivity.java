package com.example.chenx.aimodel;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.MatOfPoint3f;

public class GLViewActivity extends AppCompatActivity {

    public static MatOfPoint3f originalStructure;
    private boolean supportEs;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.back);
        toolbar.setTitle("");
        TextView textView = (TextView)findViewById(R.id.toolbar_title_about);
        textView.setText("GLView");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setRenderer(new MyRender(originalStructure));
//        checkSupported();

//        if (supportEs){
//            glSurfaceView.setRenderer(new MyRender());s
//            setContentView(R.layout.activity_glview);
//        }else{
//            setContentView(R.layout.activity_glview);
//            Toast.makeText(this, "当前设备不支持OpenGL ES 2.0!", Toast.LENGTH_SHORT).show();
//        }
    }

//    private void checkSupported(){
//        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        assert activityManager != null;
//        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
//        supportEs = configurationInfo.reqGlEsVersion >= 0x2000;
//
//        boolean isEmulator = (Build.FINGERPRINT.startsWith("generic")
//                || Build.FINGERPRINT.startsWith("unknown")
//                || Build.MODEL.contains("google_sdk")
//                || Build.MODEL.contains("Emulator")
//                || Build.MODEL.contains("Android SDK built for x86"));
//
//        supportEs = supportEs || isEmulator;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView != null){
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null){
            glSurfaceView.onResume();
        }
    }
}
