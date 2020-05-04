# NiceItemDecoration
自定义一个RV的分割线

### 分割线源码分析  
```
/**
 * Measure a child view using standard measurement policy, taking the padding
 * of the parent RecyclerView and any added item decorations into account.
 *
 * <p>If the RecyclerView can be scrolled in either dimension the caller may
 * pass 0 as the widthUsed or heightUsed parameters as they will be irrelevant.</p>
 *
 * @param child Child view to measure
 * @param widthUsed Width in pixels currently consumed by other views, if relevant
 * @param heightUsed Height in pixels currently consumed by other views, if relevant
 */
public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    1.//下面三行代码就是把分割线的距离增加到每个view上
    final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
    widthUsed += insets.left + insets.right;
    heightUsed += insets.top + insets.bottom;

    final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
            getPaddingLeft() + getPaddingRight() + widthUsed, lp.width,
            canScrollHorizontally());
    final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
            getPaddingTop() + getPaddingBottom() + heightUsed, lp.height,
            canScrollVertically());
    if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
        child.measure(widthSpec, heightSpec);
    }

Rect getItemDecorInsetsForChild(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.mInsetsDirty) {
            return lp.mDecorInsets;
        }

        if (mState.isPreLayout() && (lp.isItemChanged() || lp.isViewInvalid())) {
            // changed/invalid items should not be updated until they are rebound.
            return lp.mDecorInsets;
        }
        final Rect insets = lp.mDecorInsets;
        insets.set(0, 0, 0, 0);
        2.//拿到所有的Item
        final int decorCount = mItemDecorations.size();
        for (int i = 0; i < decorCount; i++) {
            mTempRect.set(0, 0, 0, 0);
  // 调用 getItemOffsets（）方法，给mTempRect赋值    
  mItemDecorations.get(i).getItemOffsets(mTempRect, child, this, mState);
            insets.left += mTempRect.left;
            insets.top += mTempRect.top;
            insets.right += mTempRect.right;
            insets.bottom += mTempRect.bottom;
        }
        lp.mInsetsDirty = false;
        return insets;
    }
  ```
绘制分割线:
  ```
final ArrayList<ItemDecoration> mItemDecorations = new ArrayList<>();

@Override
public void onDraw(Canvas c) {
    super.onDraw(c);
    final int count = mItemDecorations.size();
    for (int i = 0; i < count; i++) {
        1.//会回调分割线方法
        mItemDecorations.get(i).onDraw(c, this, mState);
    }
}

public abstract static class ItemDecoration {
      
        //通过重写此onDraw方法绘制分割线
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
            onDraw(c, parent);
        }

         //此方法已过时
        @Deprecated
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent) {
        }
}
  ```

### 从上面的分割线源码分析可知：
1.在getItemOffset()方法中，设置分割线的大小最终是加在itemView的宽高上。这会造成一个问题，就是每个itemView的大小可能不一致
2. 在确定了分割线大小后，在绘制itemView时，会调用分割线的onDraw()方法。我们可以重写分割线的onDraw()方法绘制分割线。

### 2.自定义一个分割线NiceItemDecoration

 大致思路：定义三个变量(leftRight,topBottom,mColor)，然后根据不同的layoutManager使用不同的引擎确定分割线的大小和绘制分割线 

  ```
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

    //由不同的引擎确定分割线的大小
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mEntrust == null) {
            mEntrust = getEntrust(parent);
        }
        mEntrust.getItemOffsets(outRect,view,parent,state);
    }

    //由不同的引擎绘制分割线
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mEntrust == null) {
            mEntrust = getEntrust(parent);
        }
        mEntrust.onDraw(c,parent,state);
        super.onDraw(c, parent, state);
    }

   //根据layoutManager确定不同的引擎
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
  ```
**注意：**在判断时，要先判断GridLayoutManager再判断LinearLayoutManager，因为GridLayoutManager是继承LinearLayoutManager。**getEntrust（）**方法根据不同的layoutManager分别生成LinearEntrust、GridEntrust、StaggeredGridEntrust引擎来确定分割线大小和绘制分割线。

####一些基础知识：
**1.关于LinearLayoutManager**
1、 在getItemOffsets()方法中，可以获得childView在设配器中的位置，和childView的数量
  ```
@Override
    void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
       //获得childView所在的位置
       int position = parent.getChildAdapterPosition(view);
       //获得itemView的数量
       int ItemSize = layoutManager.getItemCount();
    }
  ```
2、在onDraw()方法中 可以获得每个childView的分割线的上下的高度和左右的宽度
  ```
    //获得itemView的数量
   int itemSize = layoutManager.getChildCount();
   for (int i = 0; i < itemSize - 1; i++) {
   final View childView = parent.getChildAt(i);
    //获得childView分割线左右的宽度
   layoutManager.getLeftDecorationWidth(childView);
   layoutManager.getRightDecorationWidth(childView);
   //获得childView分割线上下的高度
   layoutManager.getTopDecorationHeight(childView);
 layoutManager.getBottomDecorationHeight(childView);
}
  ```
**2.关于GridLayoutManager**
1.可以知道每个childView所在的行数，在行里的位置，每行的chilView比重
  ```
GridLayoutManager.LayoutParams.getSpanSize() //childView所占的比重
GridLayoutManager.LayoutParams.getSpanIndex() //childView在所在行的第几个
GridLayoutManager.getSpanCount() //每行多少个childView
GridLayoutManager.getSpanSizeLookup().getSpanSize(i); //childView所占的比重
GridLayoutManager.getSpanSizeLookup().getSpanIndex(childPosition,spanCount); //childView在所在行的第几个
GridLayoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition,spanCount) //childPosition的view处于第几排或第几列
  ```
2.可以设置每个view的比重
  ```
 mLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
                final GridLayoutManager manager = (GridLayoutManager) mLayoutManager;
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return position % (manager.getSpanCount() + 1) == 0 ? manager.getSpanCount() : 1;
                    }
                });
  ```
****
###1.写一个抽象引擎，让它们共有属性和要重写的方法封装起来
  ```
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
  ```
### 2.LinearEntrust引擎
分析：**在确定分割线大小时**，分两种情况：垂直滑动：每个childView都有上左右，但最后一个childView时要下；水平滑动：每个childView都有上下右，但第一个又左。
**在绘制分割线时只绘制ChildView的之间的距离**，分两种情况：垂直滑动：每个childView画bottom,但最后一个不画（i<childCount - 1）；水平滑动：每个childView画right,但最后一个不画（i<childCount - 1）。
  ```
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
  ```

### 3.GridEntrust引擎
分析：**确定分割线大小**,分垂直和水平滑动。垂直滑动：每个childView都有下，但第一排有上。左右分两种情况：如果一个childView占满一行，则直接设置左右；如果不是，则按比例分配左右，因为分割线算在childView的区域，分割线分配不均会导致childView的大小不一样。 
水平滑动：每个childView都有右，但第一列有左。上下分两种情况：如果一个childView占满一列，则直接设置上下，如果不是，则按比例分配上下，因为分割线算在childView的区域，分割线分配不均会导致childView的大小不一样。
**绘制分割线**,垂直滑动：画水平方向分割线：在不是第一个行，且childView在行的位置是0时，在chidView上方绘制一条分割线。画垂直方向:在不是最后列中，画childView右边的分割线。
水平滑动：画垂直方向分割线：在不是第一列中画ChildView的左边的垂直分割线。画水平方向分割线：在不是最后一行中，绘制每个childView的下方的分割线。

  ```
public class GridEntrust extends NiceItemDecorationEntrust {

    public GridEntrust(int leftRight, int topBottom, int mColor) {
        super(leftRight, topBottom, mColor);
    }


    @Override
    void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        final GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        //在适配器中的位置 
        final int childPosition = parent.getChildAdapterPosition(view);
        //每行或列的比重
        final int spanCount = layoutManager.getSpanCount();
   
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
  ```

