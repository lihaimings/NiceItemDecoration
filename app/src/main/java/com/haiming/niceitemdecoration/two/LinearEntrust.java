package com.haiming.niceitemdecoration.two;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：请叫我百米冲刺 on 2016/12/6 上午11:32
 * 邮箱：mail@hezhilin.cc
 */

public class LinearEntrust extends SpacesItemDecorationEntrust {

    public LinearEntrust(int leftRight, int topBottom, int mColor) {
        super(leftRight, topBottom, mColor);
    }

    //绘制没有颜色的
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        //竖直方向的
        if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
            //最后一项需要 bottom
            if (parent.getChildAdapterPosition(view) == layoutManager.getItemCount() - 1) {
                outRect.bottom = topBottom;
            }
            outRect.top = topBottom;
            outRect.left = leftRight;
            outRect.right = leftRight;
            // Log.d("左边距",""+layoutParams.leftMargin);
        } else {
            //最后一项需要right
            if (parent.getChildAdapterPosition(view) == layoutManager.getItemCount() - 1) {
                outRect.right = leftRight;
            }
            outRect.top = topBottom;
            outRect.left = leftRight;
            outRect.bottom = topBottom;
        }
    }

    //绘制有颜色的
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        //没有子view或者没有没有颜色直接return
        if (mDivider == null || layoutManager.getChildCount() == 0) {
            return;
        }
        int left;
        int right;
        int top;
        int bottom;
        final int childCount = parent.getChildCount();
        if (layoutManager.getOrientation() == GridLayoutManager.VERTICAL) {
            for (int i = 0; i < childCount - 1; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                Log.d("左边距", "" + params.leftMargin);
                //将有颜色的分割线处于中间位置
                final float center = (layoutManager.getTopDecorationHeight(child) + 1 - topBottom) / 2;
                //计算下边的
              //  Log.d("距离", "layoutManager.getTopDecorationHeight(child)=" + layoutManager.getTopDecorationHeight(child) + "\t layoutManager.getLeftDecorationWidth(child)=" + layoutManager.getLeftDecorationWidth(child));

                left = layoutManager.getLeftDecorationWidth(child);

                right = parent.getRight() - left;

                top = (int) (child.getBottom() + center);
                bottom = top + topBottom;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        } else {
            //水平方向
            for (int i = 0; i < childCount - 1; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                //将有颜色的分割线处于中间位置
                // layoutManager.getLeftDecorationWidth(child)相当于每个view的rect的left
                Log.d("距离", "layoutManager.getLeftDecorationWidth(child)=" + layoutManager.getLeftDecorationWidth(child) + "\t leftRight=" + leftRight);
              //  final float center = (layoutManager.getLeftDecorationWidth(child) + 1 - leftRight) / 2;
                //计算右边的
                left = (int) (child.getRight());
                right = left + leftRight;
                top = layoutManager.getTopDecorationHeight(child);
                bottom = parent.getBottom() - layoutManager.getTopDecorationHeight(child);
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

    }

}