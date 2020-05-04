package com.haiming.niceitemdecoration.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class NiceItemDecorationEntrust {

    protected int leftRight;
    protected int topBottom;
    protected Drawable mDivider;

    public NiceItemDecorationEntrust(int leftRight, int topBottom, int mColor){
        this.leftRight = leftRight;
        this.topBottom = topBottom;
        if (mColor != 0){
            mDivider = new ColorDrawable(mColor);
        }
    }

    abstract void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state);

    abstract void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state);
}
