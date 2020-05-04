package com.haiming.niceitemdecoration.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridEntrust extends NiceItemDecorationEntrust {

    public GridEntrust(int leftRight, int topBottom, int mColor) {
        super(leftRight, topBottom, mColor);
    }

    /**
     * 分析：在格布局中，每个item的比重不一定是1，可能一个item就暂用一行，这种情况左右布局就会就是正常leftRight
     * 假如一行中每个item比重都是1，那么每个item之间的right+left=leftRight,第一个和最后一个的左右为正常leftRight
     * <p>
     * 基础知识：
     * GridLayoutManager.LayoutParams.getSpanSize() //childView所占的比重
     * GridLayoutManager.LayoutParams.getSpanIndex() //childView在所在行的第几个
     * <p>
     * GridLayoutManager.getSpanCount() //每行多少个childView
     * <p>
     * GridLayoutManager.getSpanSizeLookup().getSpanSize(i); //childView所占的比重
     * GridLayoutManager.getSpanSizeLookup().getSpanIndex(childPosition,spanCount); //childView在所在行的第几个
     * GridLayoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition,spanCount) //childPosition的view处于第几排或第几列
     */
    @Override
    void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        final GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        final int childPosition = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();
        Log.d("大小", "spanCount=" + spanCount);

        //垂直方向，第一排还有top，每排都有bottom，每列都有left,最后一列有right
        if (layoutManager.getOrientation() == GridLayoutManager.VERTICAL) {
            outRect.bottom = topBottom;
            //是否在第一排
            if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {
                outRect.top = topBottom;
            }

            if (layoutParams.getSpanSize() == spanCount) {
                //一个item占满一行
                outRect.left = leftRight;
                outRect.right = leftRight;
            } else {

                outRect.left = (int)
                        (((float) (spanCount - layoutParams.getSpanIndex())) / spanCount * leftRight);

                outRect.right = (int)
                        (((float) leftRight * (spanCount + 1) / spanCount) - outRect.left);
            }

        } else {
            //水平 第一列有left，全部列有right, 全部有top，最后一个有bottom
            outRect.right = leftRight;
            if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {
                outRect.left = leftRight;
            }
            if (layoutParams.getSpanSize() == spanCount) {//占满
                outRect.top = topBottom;
                outRect.bottom = topBottom;
            } else {
                outRect.top = (int) (((float) (spanCount - layoutParams.getSpanIndex())) / spanCount * topBottom);
                outRect.bottom = (int) (((float) topBottom * (spanCount + 1) / spanCount) - outRect.top);
            }
        }
    }

    @Override
    void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        final GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
        if (mDivider == null || layoutManager.getChildCount() == 0) {
            return;
        }
        int spanCount = layoutManager.getSpanCount();
        int chileCount = layoutManager.getChildCount();
        int left, top, right, bottom;
        //垂直方向，每排画bottom，但最后一排不画bottom，只画right，但最后一列不画right
        if (layoutManager.getOrientation() == GridLayoutManager.VERTICAL) {
            for (int i = 0; i < chileCount; i++) {
                final View childView = parent.getChildAt(i);
                int childPosition = parent.getChildAdapterPosition(childView);

                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
                int spanSize = spanSizeLookup.getSpanSize(i);
                int spanIndex = spanSizeLookup.getSpanIndex(childPosition, spanCount);

                //画水平方向，除了最后一排，每排都画bottom边的
                boolean isLast = spanSizeLookup.getSpanGroupIndex(childPosition, spanCount) == 0;
                if (!isLast && spanIndex == 0) {
                    left = childView.getLeft();
                    right = parent.getWidth() - leftRight;
                    top = childView.getTop() - topBottom;
                    bottom = top + layoutManager.getBottomDecorationHeight(childView);

                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }

                //画垂直方向，每列都有right ，除了最右边

                boolean isRight = spanIndex + spanSize == spanCount;
                if (!isRight) {
                    left = childView.getRight();
                    right = left + leftRight;
                    top = childView.getTop();
                    bottom = parent.getBottom() - layoutManager.getBottomDecorationHeight(childView);
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }
        } else {
            //水平方向
            for (int i = 0; i < chileCount; i++) {
                final View childView = parent.getChildAt(i);
                int childPosition = parent.getChildAdapterPosition(childView);
                int spanSize = spanSizeLookup.getSpanSize(childPosition);
                int spanIndex = spanSizeLookup.getSpanIndex(childPosition, spanCount);

                //垂直
                boolean isFirst = spanSizeLookup.getSpanGroupIndex(childPosition, spanCount) == 0;
                if (!isFirst && spanIndex == 0) {
                    left = childView.getLeft() - leftRight;
                    right = left + leftRight;
                    top = layoutManager.getTopDecorationHeight(childView);
                    bottom = parent.getHeight() - layoutManager.getTopDecorationHeight(childView);
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }

                //画水平
                boolean isRight = spanIndex + spanSize == spanCount;
                if (!isRight) {
                    left = childView.getLeft();
                    right = childView.getRight();
                    top = childView.getBottom();
                    bottom = top + leftRight;
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }

            }
        }

    }
}
