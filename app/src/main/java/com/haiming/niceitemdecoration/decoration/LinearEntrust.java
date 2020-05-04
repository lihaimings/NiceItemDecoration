package com.haiming.niceitemdecoration.decoration;

import android.drm.DrmStore;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LinearEntrust extends NiceItemDecorationEntrust {

    public LinearEntrust(int leftRight, int topBottom, int mColor) {
        super(leftRight, topBottom, mColor);
    }

    @Override
    void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            //垂直滑动
            outRect.top = topBottom;
            outRect.left = leftRight;
            outRect.right = leftRight;
            //最后一个子view需要bottom
            if (parent.getChildAdapterPosition(view) == layoutManager.getItemCount() - 1) {
                outRect.bottom = topBottom;
            }
        } else {
            //横向滑动
            outRect.top = topBottom;
            outRect.right = leftRight;
            outRect.bottom = topBottom;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = leftRight;
            }
        }

    }

    @Override
    void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        if (mDivider == null || layoutManager.getChildCount() == 0) {
            return;
        }
        //只画item之间的间隙
        int left, top, right, bottom;
        int childCount = parent.getChildCount();
        //垂直方向，第一排top不画，最后一排bottom不画，左右两边不画
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            for (int i = 0; i < childCount - 1; i++) {
                final View childView = parent.getChildAt(i);
                left = childView.getLeft();
                right = childView.getRight();
                top = childView.getBottom();
                bottom = top + topBottom;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }

        } else {
            //水平方向  第一个left不画，最后一个right不画，上下不画
            for (int i = 0; i < childCount - 1; i++) {
                final View childView = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                left = childView.getRight();
                right = left + layoutManager.getRightDecorationWidth(childView);
                top = childView.getTop();
                bottom = childView.getBottom();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

    }
}
