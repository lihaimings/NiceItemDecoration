package com.haiming.niceitemdecoration.decoration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import java.sql.Struct;
import java.util.IllegalFormatCodePointException;
import java.util.regex.Pattern;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {

    private static final String DEFAULT_COLOR = "#e2e5e8";
    private static final int MODE_HORIZONTAL = 1;
    private static final int MODE_VERTICAL = 2;


    private int mDrawableRid = 0;

    private int mColor = Color.parseColor(DEFAULT_COLOR);

    private int mThickness;

    private int mDashWidth = 0;

    private int mDashGap = 0;
    private boolean mFirstLineVisible;
    private boolean mLastLineVisible;
    private int mPaddingStart = 0;
    private int mPaddingEnd = 0;

    private boolean mGridLeftVisible;
    private boolean mGridRightVisible;
    private boolean mGridTopVisible;
    private boolean mGridBottomVisible;

    private int mGridHorizontalSpacing;
    private int mGridVerticalSpacing;

    private int mMode;

    private Paint mPaint;

    private Bitmap mBitmap;

    private NinePatch mNinePatch;

    private int mCurrentThickness;

    private boolean hasNicePath = false;

    private boolean hasGetParentLayoutMode = false;

    private Context mContext;

    public RecyclerViewItemDecoration() {

    }

    public void setParams(Context context, Param params) {
        this.mContext = context;

        this.mDrawableRid = params.drawableRid;
        this.mColor = params.color;
        this.mThickness = params.thickness;
        this.mDashGap = params.dashGap;
        this.mDashWidth = params.dashWidth;
        this.mPaddingStart = params.paddingStart;
        this.mPaddingEnd = params.paddingEnd;
        this.mFirstLineVisible = params.firstLineVisible;
        this.mLastLineVisible = params.lastLineVisible;
        this.mGridLeftVisible = params.gridLeftVisible;
        this.mGridRightVisible = params.gridRightVisible;
        this.mGridTopVisible = params.gridTopVisible;
        this.mGridBottomVisible = params.gridBottomVisible;
        this.mGridHorizontalSpacing = params.gridHorizontalSpacing;
        this.mGridVerticalSpacing = params.gridVerticalSpacing;
    }

    private void initPaint(Context context) {
        this.mBitmap = BitmapFactory.decodeResource(context.getResources(), mDrawableRid);

        if (mBitmap.getNinePatchChunk() != null) {
            hasNicePath = true;
            mNinePatch = new NinePatch(mBitmap, mBitmap.getNinePatchChunk(), null);
        }

        //画水平，取高度
        if (mMode == RecyclerItemDecorationConst.MODE_HORIZONTAL) {
            mCurrentThickness = mThickness == 0 ? mBitmap.getHeight() : mThickness;
        }

        //画垂直，取宽度
        if (mMode == RecyclerItemDecorationConst.MODE_VERTICAL) {
            mCurrentThickness = mThickness == 0 ? mBitmap.getWidth() : mThickness;
        }

        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mThickness);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getChildCount() == 0) {
            return;
        }
        mPaint.setColor(mColor);
        if (mMode == RecyclerItemDecorationConst.MODE_HORIZONTAL) {
            drawHorinzontal(c, parent);
        } else if (mMode == RecyclerItemDecorationConst.MODE_VERTICAL) {
            drawVertical(c, parent);
        } else if (mMode == RecyclerItemDecorationConst.MODE_GRID) {
            drawGrid(c, parent);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        //如果没有设置画的模式，则开始设置
        if (!hasGetParentLayoutMode) {
            compatibleWithLayoutManager(parent);
            hasGetParentLayoutMode = true;
        }
        //view的位置
        int viewPosition = parent.getChildAdapterPosition(view);
        //如果模式是MODE_HORIZONTAL，即竖直滑动
        if (mMode == RecyclerItemDecorationConst.MODE_HORIZONTAL) {
            //最后一行可见，不是最后一个view
            if (!(!mLastLineVisible && viewPosition == parent.getAdapter().getItemCount() - 1)) {
                if (mDrawableRid != 0) {
                    outRect.set(0, 0, 0, mCurrentThickness);
                } else {
                    outRect.set(0, 0, 0, mThickness);
                }
            }
            //第一行可见，且是第一个view
            if (mFirstLineVisible && viewPosition == 0) {
                if (mDrawableRid != 0) {
                    outRect.set(0, mCurrentThickness, 0, mCurrentThickness);
                }
            }
        } else if (mMode == RecyclerItemDecorationConst.MODE_VERTICAL) {
            //MODE_VERTICAL,横向滑动
            //最后一个VIEW可见或不是最后一个view
            if (!(!mLastLineVisible && viewPosition == parent.getAdapter().getItemCount() - 1)) {
                if (mDrawableRid != 0) {
                    outRect.set(0, 0, mCurrentThickness, 0);
                } else {
                    outRect.set(0, 0, mThickness, 0);
                }
            }

            //第一行可见，且在最后一行时
            if (mFirstLineVisible && viewPosition == 0) {
                if (mDrawableRid != 0) {
                    outRect.set(mCurrentThickness, 0, mCurrentThickness, 0);
                } else {
                    outRect.set(mThickness, 0, mThickness, 0);
                }
            }
        } else if (mMode == RecyclerItemDecorationConst.MODE_GRID) {
            //每行或列有多少比重
            int columnSize = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            //全部view的数量
            int itemSize = parent.getAdapter().getItemCount();

            if (mDrawableRid != 0) {
                setGridOffsets(outRect, viewPosition, columnSize, itemSize, 0);
            } else {
                setGridOffsets(outRect, viewPosition, columnSize, itemSize, 1);
            }
        }
    }

    //画水平线
    private void drawHorinzontal(Canvas c, RecyclerView parent) {
        int childrenCount = parent.getChildCount();

        if (mDrawableRid != 0) {
            if (mFirstLineVisible) {
                View childView = parent.getChildAt(0);
                int myY = childView.getTop();

                if (hasNicePath) {
                    Rect rect = new Rect(mPaddingStart, myY - mCurrentThickness,
                            parent.getWidth() - mPaddingEnd, myY);
                    mNinePatch.draw(c, rect);
                } else {
                    c.drawBitmap(mBitmap, mPaddingStart, myY - mCurrentThickness, mPaint);
                }
            }

            for (int i = 0; i < childrenCount; i++) {

                if (!mLastLineVisible && i == childrenCount - 1) {
                    break;
                }
                View childView = parent.getChildAt(i);
                int myY = childView.getBottom();

                if (hasNicePath) {
                    Rect rect = new Rect(mPaddingStart, myY,
                            parent.getWidth() - mPaddingEnd, myY + mCurrentThickness);
                    mNinePatch.draw(c, rect);
                } else {
                    c.drawBitmap(mBitmap, mPaddingStart, myY, mPaint);
                }

            }
        } else {
            //mDrawableRid为空
            boolean isPureLine = isPureLine();
            if (!isPureLine) {
                PathEffect effect = new DashPathEffect(new float[]{0, 0, mDashWidth, mThickness}, mDashGap);
                mPaint.setPathEffect(effect);
            }

            if (mFirstLineVisible) {
                View childView = parent.getChildAt(0);
                int myY = childView.getTop() - mThickness / 2;

                if (isPureLine) {
                    c.drawLine(mPaddingStart, myY, parent.getWidth() - mPaddingEnd, myY, mPaint);
                } else {
                    Path path = new Path();
                    path.moveTo(mPaddingStart, myY);
                    path.lineTo(parent.getWidth() - mPaddingEnd, myY);
                    c.drawPath(path, mPaint);
                }
            }

            for (int i = 0; i < childrenCount; i++) {
                if (!mLastLineVisible && i == childrenCount - 1) {
                    break;
                }
                View childView = parent.getChildAt(i);
                int myY = childView.getBottom() + mThickness / 2;

                if (isPureLine) {
                    c.drawLine(mPaddingStart, myY, parent.getWidth() - mPaddingEnd, myY, mPaint);
                } else {
                    Path path = new Path();
                    path.moveTo(mPaddingStart, myY);
                    path.lineTo(parent.getWidth() - mPaddingEnd, myY);
                    c.drawPath(path, mPaint);
                }
            }

        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        int childrenCount = parent.getChildCount();
        if (mDrawableRid != 0) {
            if (mFirstLineVisible) {
                View childView = parent.getChildAt(0);
                int myX = childView.getLeft();
                if (hasNicePath) {
                    Rect rect = new Rect(myX - mCurrentThickness, mPaddingStart
                            , myX, parent.getHeight() - mPaddingEnd);
                    mNinePatch.draw(c, rect);
                } else {
                    c.drawBitmap(mBitmap, myX - mCurrentThickness, mPaddingStart, mPaint);
                }
            }
            for (int i = 0; i < childrenCount; i++) {
                if (!mLastLineVisible && i == childrenCount - 1) {
                    break;
                }
                View childView = parent.getChildAt(i);
                int myX = childView.getRight();
                if (hasNicePath) {
                    Rect rect = new Rect(myX, mPaddingStart, myX + mCurrentThickness
                            , parent.getHeight() - mPaddingEnd);
                    mNinePatch.draw(c, rect);
                } else {
                    c.drawBitmap(mBitmap, myX, mPaddingStart, mPaint);
                }
            }
        } else {
            boolean isPureLine = isPureLine();
            if (!isPureLine) {
                PathEffect effect = new DashPathEffect(new float[]{0, 0, mDashWidth, mThickness}, mDashGap);
                mPaint.setPathEffect(effect);
            }

            if (mFirstLineVisible) {
                View childView = parent.getChildAt(0);
                int myX = childView.getLeft() - mThickness / 2;
                if (isPureLine) {
                    c.drawLine(myX, mPaddingStart, myX,
                            parent.getHeight() - mPaddingEnd, mPaint);

                }else {
                    Path path = new Path();
                    path.moveTo(myX, mPaddingStart);
                    path.lineTo(myX, parent.getHeight() - mPaddingEnd);
                    c.drawPath(path, mPaint);
                }
            }

        }
    }

    private void drawGrid(Canvas c, RecyclerView parent) {

    }

    private boolean isPureLine() {
        return mDashGap == 0 && mDashWidth == 0;
    }

    private void setGridOffsets(Rect outRect, int viewPosition, int columnSize, int itemSize, int tag) {
        int x, y;
        int borderThichness = mThickness;
        //有drawableId
        if (tag == 0) {
            x = mBitmap.getWidth();
            y = mBitmap.getHeight();
        } else {
            if (mGridHorizontalSpacing != 0) {
                x = mGridHorizontalSpacing;
            } else {
                x = mThickness;
            }

            if (mGridVerticalSpacing != 0) {
                y = mGridVerticalSpacing;
            } else {
                y = mThickness;
            }
        }

        // 如果在第一行 也在第一列
        if (isFirstGridColumn(viewPosition, columnSize)
                && isFirstGridRow(viewPosition, columnSize)) {
            // 上边可见和左边可见
            if (mGridTopVisible && mGridLeftVisible) {
                outRect.set(borderThichness, borderThichness, 0, 0);

            } else if (mGridTopVisible) {
                outRect.set(0, borderThichness, 0, 0);
            } else {
                outRect.set(0, 0, 0, 0);
            }
        } else if (isFirstGridRow(viewPosition, columnSize)
                && isLastGridColumn(viewPosition, itemSize, columnSize)) {
            // 在第一行也在第一列
            if (mGridRightVisible && mGridBottomVisible) {
                outRect.set(x, y, borderThichness, borderThichness);
            } else if (mGridRightVisible) {
                outRect.set(x, y, borderThichness, 0);
            } else if (mGridBottomVisible) {
                outRect.set(0, y, 0, borderThichness);
            } else {
                outRect.set(0, y, 0, 0);
            }
        } else if (isLastGridColumn(viewPosition, itemSize, columnSize)
                && isLastGridRow(viewPosition, itemSize, columnSize)) {
            //最后一行和最后一列
            if (mGridLeftVisible && mGridBottomVisible) {
                outRect.set(borderThichness, y, 0, borderThichness);
            } else if (mGridLeftVisible) {
                outRect.set(borderThichness, y, 0, 0);
            } else if (mGridBottomVisible) {
                outRect.set(0, y, 0, borderThichness);
            } else {
                outRect.set(0, y, 0, 0);
            }
        } else if (isFirstGridColumn(viewPosition, columnSize)) {
            //第一列
            if (mGridTopVisible) {
                outRect.set(x, borderThichness, 0, 0);
            } else {
                outRect.set(x, 0, 0, 0);
            }
        } else if (isLastGridColumn(viewPosition, itemSize, columnSize)) {
            //是否为最后一列
            if (mGridRightVisible) {
                outRect.set(x, y, borderThichness, 0);
            } else {
                outRect.set(x, y, 0, 0);
            }
        } else {
            outRect.set(x, y, 0, 0);
        }

    }


    /**
     * 是否是为第一个列，其实这样判断不准，可以用lp.getSpanIndex()
     *
     * @param viewPosition
     * @param columnSize
     * @return
     */
    private boolean isFirstGridColumn(int viewPosition, int columnSize) {
        return viewPosition % columnSize == 0;
    }

    /**
     * 是否第一行,这种判断也不准，可用layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition,spanCount)
     *
     * @param viewPosition
     * @param columnSize
     * @return
     */
    private boolean isFirstGridRow(int viewPosition, int columnSize) {
        return viewPosition < columnSize;
    }

    /**
     * 是否为最后一列 lp.getSpanIndex()+spanSizeLookup.getSpanSize(i) == columnSize
     *
     * @param viewPosition
     * @param itemSize
     * @param columnSize
     * @return
     */
    private boolean isLastGridColumn(int viewPosition, int itemSize, int columnSize) {
        boolean isLast = false;
        if (((viewPosition + 1) % columnSize == 0 || (itemSize <= columnSize && viewPosition == itemSize - 1))) {
            isLast = true;
        }
        return isLast;
    }

    /**
     * 是否为网格的最后一行
     *
     * @param viewPosition
     * @param itemSize
     * @param columnSize
     * @return
     */
    private boolean isLastGridRow(int viewPosition, int itemSize, int columnSize) {
        int temp = itemSize % columnSize;
        return temp == 0 && viewPosition >= itemSize - columnSize ||
                viewPosition >= itemSize / columnSize * columnSize;

    }

    /**
     * 是否为最后一项
     *
     * @param position
     * @param itemSize
     * @return
     */
    private boolean isLastItem(int position, int itemSize) {
        return position == itemSize - 1;
    }


    @SuppressLint("WrongConstant")
    private void compatibleWithLayoutManager(RecyclerView parent) {

        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof GridLayoutManager) {
                //Grid声明为MODE_GRID
                mMode = RecyclerItemDecorationConst.MODE_GRID;
            } else if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                //Linear
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayout.HORIZONTAL) {
                    //水平滑动，设置为MODE_VERTICAL
                    mMode = RecyclerItemDecorationConst.MODE_VERTICAL;
                } else {
                    //竖直滑动，设置为MODE_HORIZONTAL
                    mMode = RecyclerItemDecorationConst.MODE_HORIZONTAL;
                }
            } else {
                //将Stagger设置为未知
                mMode = RecyclerItemDecorationConst.MODE_UNKNOWN;
            }
            //初始化画笔
            initPaint(mContext);
        }
    }

    static boolean isColorString(String colorStr) {
        return Pattern.matches("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$", colorStr);
    }


    public static class Build {
        private Param mParam;
        private Context mContext;

        public Build(Context context) {
            this.mParam = new Param();
            this.mContext = context;
        }

        public RecyclerViewItemDecoration create() {
            RecyclerViewItemDecoration recyclerViewItemDecoration = new RecyclerViewItemDecoration();
            recyclerViewItemDecoration.setParams(mContext, mParam);
            return recyclerViewItemDecoration;
        }

        public Build drawableId(@DrawableRes int drawableId) {
            mParam.drawableRid = drawableId;
            return this;
        }

        public Build color(@ColorInt int color) {
            mParam.color = color;
            return this;
        }

        public Build color(String color) {
            if (isColorString(color)) {
                mParam.color = Color.parseColor(color);
            }
            return this;
        }

        public Build thickness(int thickness) {
            mParam.thickness = thickness;
            return this;
        }

        public Build dashWidth(int dashWidth) {
            mParam.dashWidth = dashWidth;
            return this;
        }

        public Build dashGap(int dashGap) {
            mParam.dashGap = dashGap;
            return this;
        }

        public Build lastLineVisible(boolean visible) {
            mParam.lastLineVisible = visible;
            return this;
        }

        public Build firstLineVisible(boolean visible) {
            mParam.firstLineVisible = visible;
            return this;
        }

        public Build paddingStart(int padding) {
            mParam.paddingStart = padding;
            return this;
        }

        public Build paddingEnd(int padding) {
            mParam.paddingEnd = padding;
            return this;
        }

        public Build gridLeftVisible(boolean visible) {
            mParam.gridLeftVisible = visible;
            return this;
        }

        public Build gridRightVisible(boolean visible) {
            mParam.gridRightVisible = visible;
            return this;
        }

        public Build gridTopVisible(boolean visible) {
            mParam.gridTopVisible = visible;
            return this;
        }

        public Build gridBottomVisible(boolean visible) {
            mParam.gridBottomVisible = visible;
            return this;
        }

        public Build gridHorizontalSpacing(int spacing) {
            mParam.gridHorizontalSpacing = spacing;
            return this;
        }

        public Build gridVerticalSpacing(int spacing) {
            mParam.gridVerticalSpacing = spacing;
            return this;
        }


    }

    private static class Param {

        public int drawableRid = 0;
        public int color = Color.parseColor(DEFAULT_COLOR);
        public int thickness;
        public int dashWidth = 0;
        public int dashGap = 0;
        public boolean lastLineVisible;
        public boolean firstLineVisible;
        public int paddingStart;
        public int paddingEnd;
        public boolean gridLeftVisible;
        public boolean gridRightVisible;
        public boolean gridTopVisible;
        public boolean gridBottomVisible;
        public int gridHorizontalSpacing;
        public int gridVerticalSpacing;
    }
}
