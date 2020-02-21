package com.example.chenx.aimodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {
    private List<String> mPathList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ViewHolder(View view){
            super(view);
            imageView =view.findViewById(R.id.imgView_item);
        }
    }

    public PictureAdapter(List<String> pathList){
        mPathList = pathList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null)mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Bitmap mbmp = mPictureList.get(position);

        Glide.with(mContext).load(mPathList.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mPathList.size();
    }
}
