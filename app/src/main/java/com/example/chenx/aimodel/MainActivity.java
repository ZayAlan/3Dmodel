package com.example.chenx.aimodel;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.chenx.aimodel.bottom.BottomNavigationViewHelper;
import com.example.chenx.aimodel.bottom.BottomNavigationViewPagerAdapter;
import com.example.chenx.aimodel.bottom.MakeModelFragment;
import com.example.chenx.aimodel.bottom.ModelLibrary;
import com.example.chenx.aimodel.head.AboutActivity;
import com.example.chenx.aimodel.head.HelpActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,ViewPager.OnPageChangeListener,ViewPager.OnTouchListener{

    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    Unbinder unbinder;
    MakeModelFragment makeModelFragment;
    ModelLibrary modelLibrary;

    BottomNavigationViewPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar设置
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView textView = (TextView)findViewById(R.id.toolbar_title);
        textView.setText(R.string.app_name);
        setSupportActionBar(toolbar);

        unbinder = ButterKnife.bind(this);
        initial();

        //权限申请
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE},1000);
        }
    }


    @SuppressLint("NewApi")
    private void initial(){

        fragments = new ArrayList<>();
        makeModelFragment = MakeModelFragment.newInstance();
        modelLibrary = ModelLibrary.newInstance();
        if (!fragments.contains(makeModelFragment)){
            fragments.add(makeModelFragment);
        }
        if (!fragments.contains(modelLibrary)){
            fragments.add(modelLibrary);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.make_model);

        pagerAdapter = new BottomNavigationViewPagerAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.make_model:
                viewPager.setCurrentItem(0);
                break;
            case R.id.model_library:
                viewPager.setCurrentItem(1);
                break;
        }
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    //实现让Toolbar支持menu的接口

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                Intent intent1 = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent1);
                break;
            case R.id.action_help:
                Intent intent2 = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent2);
                break;
                default:
                    break;
        }
        return true;
    }

}

