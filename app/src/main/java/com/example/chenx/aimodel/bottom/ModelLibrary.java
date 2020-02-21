package com.example.chenx.aimodel.bottom;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chenx.aimodel.CameraActivity;
import com.example.chenx.aimodel.PictureAdapter;
import com.example.chenx.aimodel.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModelLibrary extends Fragment {

//    private List<Bitmap> bmpList = new ArrayList<>();

    private List<String> pathList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private PictureAdapter mpictureAdapter;
    public ModelLibrary(){}

    public static ModelLibrary newInstance(){
        ModelLibrary fragment = new ModelLibrary();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void refreshImages(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initImages();
                        mpictureAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void initImages(){

        String filePathString = Environment.getExternalStorageDirectory().toString() + File.separator+"aimodel";
        File fileAll = new File(filePathString);
        File[] files = fileAll.listFiles();

        pathList.clear();
        if(files != null){
            for (int i=0;i<files.length;i++){
                File filetemp = files[i];
                pathList.add(filetemp.getPath());
//            bitmapTemp = BitmapFactory.decodeFile(filetemp.getPath());
//            bmpList.add(bitmapTemp);
            }
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modellibraryfragment,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorGayPurple);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshImages();
            }
        });

        initImages();
        RecyclerView mrecyclerView = getActivity().findViewById(R.id.recycler_view);
        if (!pathList.isEmpty()) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            mrecyclerView.setLayoutManager(layoutManager);
            mpictureAdapter = new PictureAdapter(pathList);
            mrecyclerView.setAdapter(mpictureAdapter);
        }
        IntentFilter intentFilter = new IntentFilter(CameraActivity.refresh_action);
        getActivity().getApplicationContext().registerReceiver(broadcastReceiver,intentFilter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshImages();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(broadcastReceiver);
    }
}
