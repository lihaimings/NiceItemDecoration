package com.haiming.niceitemdecoration.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class NiceItemDecoration extends RecyclerView.ItemDecoration {

    private int mColor;
    private int leftRight;
    private int topBottom;
    private NiceItemDecorationEntrust mEntrust;

    public NiceItemDecoration(int leftRight, int topBottom) {
        this.leftRight = leftRight;
        this.topBottom = topBottom;
    }

    public NiceItemDecoration(int leftRight, int topBottom, int mColor) {
        this(leftRight, topBottom);
        this.mColor = mColor;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mEntrust == null) {
            mEntrust = getEntrust(parent);
        }
        mEntrust.getItemOffsets(outRect,view,parent,state);
    }


    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mEntrust == null) {
            mEntrust = getEntrust(parent);
        }
        mEntrust.onDraw(c,parent,state);
        super.onDraw(c, parent, state);
    }

    private NiceItemDecorationEntrust getEntrust(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            mEntrust = new GridEntrust(leftRight, topBottom, mColor);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mEntrust = new StaggeredGridEntrust(leftRight, topBottom, mColor);
        } else if (layoutManager instanceof LinearLayoutManager) {
            mEntrust = new LinearEntrust(leftRight, topBottom, mColor);
        }
        return mEntrust;
    }

}
