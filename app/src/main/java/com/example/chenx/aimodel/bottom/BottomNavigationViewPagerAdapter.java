package com.example.chenx.aimodel.bottom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class BottomNavigationViewPagerAdapter extends FragmentPagerAdapter {

    //fragment集合
    private List<Fragment> fragments;

    public BottomNavigationViewPagerAdapter(FragmentManager fm,List<Fragment> fragments){
        super(fm);
        this.fragments = fragments;
    }

    //返回fragments中的一个fragment
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    //fragment数量
    @Override
    public int getCount() {
        return fragments.size();
    }
}
