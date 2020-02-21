package com.example.chenx.aimodel.bottom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.chenx.aimodel.CameraActivity;
import com.example.chenx.aimodel.CvActivity;
import com.example.chenx.aimodel.MainActivity;
import com.example.chenx.aimodel.R;

import java.io.File;
import java.io.IOException;


public class MakeModelFragment extends Fragment {

    private ImageButton openCamera;
    private Uri imageUri;
    public static final int TAKE_PHOTO=1;

    public MakeModelFragment(){}

    public static MakeModelFragment newInstance(){
        MakeModelFragment fragment = new MakeModelFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.makemodelfragment,container,false);
    }

    //设置相机Button
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        openCamera = (ImageButton)getActivity().findViewById(R.id.open_camera);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    //拍照设置
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                break;
            default:
                break;
        }

    }
}
