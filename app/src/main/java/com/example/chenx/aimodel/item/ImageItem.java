package com.example.chenx.aimodel.item;

import android.content.Context;
import android.util.AttributeSet;

public class ImageItem extends android.support.v7.widget.AppCompatImageView {
    public ImageItem(Context context) {
        super(context);
    }
    public ImageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
