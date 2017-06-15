package com.wholesale.mytableview_ls14;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * @version ${Rev}
 * @auther liucz
 * @time 2017/6/12 9:42
 * Created by dell on 2017/6/12.
 */

public class NewTableView extends ViewGroup {
    private static final String VERTICAL = "VERTICAL";
    private static final String HORIZONTAL = "HORIZONTAL";
    private static final String BOTH = "BOTH";
    private static final String CHANGEFIRSTWIDTH = "CHANGEFIRSTWIDTH";
    private static final String CHANGEFIRSTHEIGHT = "CHANGEFIRSTHEIGHT";
    private static final String CHANGEBOTH = "CHANGEBOTH";
    private int maximumVelocity;
    private int minimumVelocity;
    private int touchSlop;
    private BaseTableAdapter adapter;
    private Recycler recycler;
    private int firstColumn;
    private int firstRow;
    private int mRowCount;
    private int mColumnCount;
    private int[] widths;
    private int[] heights;
    private int firstWidth;
    private int firstHeight;
    private int width;
    private int height;
    private int mScrollY;
    private int mScrollX;
    private List<View> rowViewList;
    private List<View> columnViewList;
    private List<List<View>> bodyViewTable;
    private VelocityTracker velocityTracker;
    private int startX;
    private int startY;
    private boolean needRelayout = true;
    private Scroller mScroller;
    private int lastX;
    private int lastY;
    private String mDirection;

    public NewTableView(Context context) {
        this(context, null);
    }

    public NewTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.rowViewList = new ArrayList<>();
        this.columnViewList = new ArrayList<>();
        this.bodyViewTable = new ArrayList<>();
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setAdapter(BaseTableAdapter baseTableAdapter) {
        this.adapter = baseTableAdapter;
        this.recycler = new Recycler(baseTableAdapter.getViewTypeCount());
        firstColumn = 0;
        firstRow = 0;
        setFirstHeight();
        setFirstWidth();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int w = 0;
        int h = 0;
        if (adapter != null) {
            mRowCount = adapter.getRowCount();
            mColumnCount = adapter.getColumnCount();
            widths = new int[mColumnCount];
            for (int i = 0; i < mColumnCount; i++) {
                //数组每一个元素  存放着    控件宽度
                widths[i] = adapter.getWidth(i);
            }
            heights = new int[mRowCount];
            for (int i = 0; i < mRowCount; i++) {
                heights[i] = adapter.getHeight(i);
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                w = Math.min(widthSize, sumArray(widths) + getFirstWidth());
            } else {
                w = widthSize;
            }
            if (heightMode == MeasureSpec.AT_MOST) {
                h = Math.min(heightSize, sumArray(heights) + getFirstHeight());
            } else {
                h = heightSize;
            }
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            needRelayout = false;//防止removeview..时的重复绘制
            if (adapter != null) {
                rowViewList.clear();
                columnViewList.clear();
                bodyViewTable.clear();
                removeAllViews();
                width = r - l;
                height = b - t;
                int left = 0;
                int top = getFirstHeight() - mScrollY;
                for (int i = firstRow; i < mRowCount && top < b; i++) {//排列表
                    left = getFirstWidth() - mScrollX;
                    List<View> list = new ArrayList<>();
                    for (int j = firstColumn; j < mColumnCount && left < r; j++) {
                        View view = makeAndStep(i, j, left, top, left + widths[j], top + heights[i]);
                        left += widths[j];
                        list.add(view);
                    }
                    bodyViewTable.add(list);
                    top += heights[i];
                }

                left = getFirstWidth() - mScrollX;
                for (int i = firstColumn; i < mColumnCount && left < r; i++) {//排第一行
                    View view = makeAndSetupRow(true, i, left, 0, left + widths[i], getFirstHeight());
                    rowViewList.add(view);
                    left += widths[i];
                }
                top = getFirstHeight() - mScrollY;
                for (int i = firstRow; i < mRowCount && top < b; i++) {//排第一列
                    View view = makeAndSetupRow(false, i, 0, top, getFirstWidth(), top + heights[i]);
                    columnViewList.add(view);
                    top += heights[i];
                }
                layoutfirst();//排第一个
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x2 = Math.abs(startX - (int) ev.getX());
                int y2 = Math.abs(startY - (int) ev.getY());
                if (x2 > touchSlop || y2 > touchSlop) {
                    intercept = true;
                }
                break;
            }
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) { // If we do not have velocity tracker
            velocityTracker = VelocityTracker.obtain(); // then get one
        }
        velocityTracker.addMovement(event); // add this movement to it
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) { // If scrolling, then stop now
                    mScroller.forceFinished(true);
                }
                startX = (int) event.getX();
                startY = (int) event.getY();
                if (startX > getFirstWidth() - touchSlop && startX < getFirstWidth() + touchSlop && startY > getFirstHeight() - touchSlop && startY < getFirstHeight() + touchSlop) {
                    mDirection = CHANGEBOTH;
                } else if (startX > getFirstWidth() - touchSlop / 2 && startX < getFirstWidth() + touchSlop / 2) {
                    mDirection = CHANGEFIRSTWIDTH;
                } else if (startY > getFirstHeight() - touchSlop / 2 && startY < getFirstHeight() + touchSlop / 2) {
                    mDirection = CHANGEFIRSTHEIGHT;
                } else if (startX <= getFirstWidth() && startY <= getFirstHeight()) {
                    return false;
                } else if (startX <= getFirstWidth() - touchSlop / 2) {
                    mDirection = VERTICAL;
                } else if (startY <= getFirstHeight() - touchSlop / 2) {
                    mDirection = HORIZONTAL;
                } else {
                    mDirection = BOTH;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int diffY = 0;
                int diffX = 0;
                int x2 = (int) event.getX();
                int y2 = (int) event.getY();
                switch (mDirection) {
                    case VERTICAL:
                        diffY = startY - y2;
                        scrollBy(diffX, diffY);
                        break;
                    case HORIZONTAL:
                        diffX = startX - x2;
                        scrollBy(diffX, diffY);
                        break;
                    case BOTH:
                        //   diffX>0    手指往左划
                        diffX = startX - x2;
                        diffY = startY - y2;
                        scrollBy(diffX, diffY);
                        break;
                    case CHANGEFIRSTWIDTH:
                        diffX = startX - x2;
                        firstWidth -= diffX;
                        needRelayout = true;
                        requestLayout();
                        break;
                    case CHANGEFIRSTHEIGHT:
                        diffY = startY - y2;
                        firstHeight -= diffY;
                        needRelayout = true;
                        requestLayout();
                        break;
                    case CHANGEBOTH:
                        diffX = startX - x2;
                        firstWidth -= diffX;
                        diffY = startY - y2;
                        firstHeight -= diffY;
                        needRelayout = true;
                        requestLayout();
                        break;
                }
                startX = x2;
                startY = y2;
                break;
            case MotionEvent.ACTION_UP:
                //判断当ev事件是MotionEvent.ACTION_UP时：计算速率
                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);//设置maxVelocity值为0.1时，速率大于0.01时，显示的速率都是0.01,速率小于0.01时，显示正常
                int velocityX = (int) velocityTracker.getXVelocity();
                int velocityY = (int) velocityTracker.getYVelocity();
                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                    mScroller.fling(getActualScrollX(), getActualScrollY(), velocityX, velocityY, 0, getMaxScrollX(), 0, getMaxScrollY());
                    lastX = getActualScrollX();
                    lastY = getActualScrollY();
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            int diffX = lastX - x;
            int diffY = lastY - y;
            if (diffX != 0 || diffY != 0) {
                scrollBy(diffX, diffY);
                lastX = x;
                lastY = y;
            }
        }
    }

    public void scrollBy(int x, int y) {
        //   >0    手指往左划,往上滑
        mScrollX += x;
        mScrollY += y;
        scrollBounds();//修正scroll的值
        if (mScrollX == 0) {
            //如果 等于 什么都不做
        } else if (mScrollX > 0) {
            while (mScrollX > widths[firstColumn]) {
                removeLeft();
                mScrollX -= widths[firstColumn];
                firstColumn++;
            }
            while (getFilledWidth() < width) {
                addRight();
            }
        } else {
            //scrillX<0
            //手指往右滑
            //移除右边的Item的临界值
            while (!rowViewList.isEmpty() && getFilledWidth() - widths[firstColumn + rowViewList.size() - 1] >= width) {
                removeRight();
            }
            //添加左边的临界值
            while (mScrollX < 0) {
                addLeft();
                //更新firstColumn
                firstColumn--;
                mScrollX += widths[firstColumn];
            }
        }
        if (mScrollY == 0) {
            // no op
        } else if (mScrollY > 0) {
            while (!columnViewList.isEmpty() && heights[firstRow] < mScrollY) {
                removeTop();
                mScrollY -= heights[firstRow];
                firstRow++;
            }
            while (getFilledHeight() < height) {
                addBottom();
            }
        } else {
            while (!columnViewList.isEmpty() && getFilledHeight() - heights[firstRow + columnViewList.size() - 1] >= height) {
                removeBottom();
            }
            while (0 > mScrollY) {
                addTop();
                firstRow--;
                mScrollY += heights[firstRow];
            }
        }
        repositionViews();
    }

    public int getActualScrollX() {
        return mScrollX + sumArray(widths, 1, firstColumn);
    }

    public int getActualScrollY() {
        return mScrollY + sumArray(heights, 1, firstRow);
    }

    private int getMaxScrollX() {
        return Math.max(0, sumArray(widths) - width);
    }

    private int getMaxScrollY() {
        return Math.max(0, sumArray(heights) - height);
    }

    private void addTop() {
        addTopAndBottom(firstRow - 1, 0);
    }

    private void addBottom() {
        final int size = columnViewList.size();
        addTopAndBottom(firstRow + size, size);
    }

    private void addTopAndBottom(int row, int index) {
        View view = layoutColumnRow(false, row, getFirstWidth(), heights[row]);
        columnViewList.add(index, view);

        List<View> list = new ArrayList<>();
        final int size = rowViewList.size() + firstColumn;
        for (int i = firstColumn; i < size; i++) {
            view = obtainView(row, i, widths[i], heights[row]);
            list.add(view);
        }
        bodyViewTable.add(index, list);
    }

    private void removeTop() {
        removeTopOrBottom(0);
    }

    private void removeBottom() {
        removeTopOrBottom(columnViewList.size() - 1);
    }

    private void removeTopOrBottom(int position) {
        removeColumnView(columnViewList.remove(position));
        List<View> remove = bodyViewTable.remove(position);
        for (View view : remove) {
            removeView(view);
        }
    }

    private void repositionViews() {
        int left, top, right, bottom, i;
        top = getFirstHeight() - mScrollY;
        i = firstRow;
        for (List<View> list : bodyViewTable) {
            bottom = top + heights[i++];
            left = getFirstWidth() - mScrollX;
            int j = firstColumn;
            for (View view : list) {
                right = left + widths[j++];
                view.layout(left, top, right, bottom);
                left = right;
            }
            top = bottom;
        }

        left = getFirstWidth() - mScrollX;
        i = firstColumn;
        for (View view : rowViewList) {
            right = left + widths[i++];
            view.layout(left, 0, right, getFirstHeight());
            left = right;
        }

        top = getFirstHeight() - mScrollY;
        i = firstRow;
        for (View view : columnViewList) {
            bottom = top + heights[i++];
            view.layout(0, top, getFirstWidth(), bottom);
            top = bottom;
        }
        layoutfirst();
        invalidate();
    }

    //移除最后一列
    private void removeRight() {
        removeLeftOrRight(rowViewList.size() - 1);
    }

    private void addRight() {
        int size = rowViewList.size();
        addLeftOrRight(firstColumn + size, size);
    }

    private void addLeft() {
        addLeftOrRight(firstColumn - 1, 0);
    }

    private void addLeftOrRight(int column, int index) {
        View view = layoutColumnRow(true, column, widths[column], getHeight());
        rowViewList.add(index, view);
        int i = firstRow;
        for (List<View> list : bodyViewTable) {
            view = obtainView(i, column, widths[column], heights[i]);
            list.add(index, view);
            i++;
        }
    }

    private void removeLeft() {
        removeLeftOrRight(0);
    }

    private void removeLeftOrRight(int i) {
        removeRowView(rowViewList.remove(i));
        //移除
        for (List<View> list : bodyViewTable) {
            removeView(list.remove(i));
        }
    }

    private void removeColumnView(View view) {
        super.removeView(view);
        recycler.addColumnView(view);
    }

    private void removeRowView(View view) {
        super.removeView(view);
        recycler.addRowView(view);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        final int typeView = (Integer) view.getTag(R.id.tag_type_view);
        //添加到回收池
        recycler.addRecycledView(view, typeView);
    }

    private void scrollBounds() {
        mScrollX = scrollBoundsX(mScrollX, firstColumn, widths, width);
        mScrollY = scrollBoundsY(mScrollY, firstRow, heights, height);
    }

    private int scrollBoundsX(int desiredScroll, int firstCell, int sizes[], int viewSize) {
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, 1, firstCell));
        } else {
            //修整左滑的临界值
            int i = Math.max(0, sumArray(sizes, firstCell, sizes.length - firstCell) + getFirstWidth() - viewSize);
            desiredScroll = Math.min(desiredScroll, Math.max(0, sumArray(sizes, firstCell, sizes.length - firstCell) + getFirstWidth() - viewSize));
        }
        return desiredScroll;
    }

    private int scrollBoundsY(int desiredScroll, int firstCell, int sizes[], int viewSize) {
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, 1, firstCell));
        } else {
            //修整左滑的临界值
            desiredScroll = Math.min(desiredScroll, Math.max(0, sumArray(sizes, firstCell, sizes.length - firstCell) + getFirstHeight() - viewSize));
        }
        return desiredScroll;
    }

    private void layoutfirst() {
        View firstView = adapter.getFirstView();
        addView(firstView);
        firstView.layout(0, 0, getFirstWidth(), getFirstHeight());
    }

    private View makeAndStep(int row, int colmun, int left, int top, int right, int bottom) {
        View view = obtainView(row, colmun, right - left, bottom - top);
        view.layout(left, top, right, bottom);
        return view;
    }

    private View obtainView(int row, int colmun, int width, int height) {
        int itemType = adapter.getItemViewType(row, colmun);
        View reclyView = recycler.getRecycleView(itemType);
        View view = adapter.getView(row, colmun, reclyView, this);
        view.setTag(R.id.tag_type_view, itemType);
        view.setTag(R.id.tag_column, colmun);
        view.setTag(R.id.tag_row, row);
        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        addView(view, 0);
        return view;
    }

    private View makeAndSetupRow(boolean isRow, int num, int left, int top, int right, int bottom) {
        View view;
        if (isRow) {
            view = layoutColumnRow(isRow, num, widths[num], getFirstHeight());
        } else {
            view = layoutColumnRow(isRow, num, getFirstWidth(), heights[num]);
        }
        view.layout(left, top, right, bottom);
        return view;
    }

    private View layoutColumnRow(boolean isRow, int num, int width, int height) {
        View view = null;
        if (isRow) {
            View reclyView = recycler.getRowView();
            view = adapter.getRowView(num, reclyView, this);
            view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getFirstHeight(), MeasureSpec.EXACTLY));
        } else {
            View reclyView = recycler.getColumnView();
            view = adapter.getColumnView(num, reclyView, this);
            view.measure(MeasureSpec.makeMeasureSpec(getFirstWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        addView(view);
        return view;
    }

    //计算数组的  总和
    private int sumArray(int array[]) {
        return sumArray(array, 0, array.length);
    }

    private int sumArray(int array[], int start, int end) {
        int sum = 0;
        end += start;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    public int getFirstWidth() {
        return firstWidth;
    }

    public void setFirstWidth() {
        firstWidth = adapter.getFirstWidth();
    }

    public int getFirstHeight() {
        return firstHeight;
    }

    public void setFirstHeight() {
        firstHeight = adapter.getFirstHeight();
    }

    public int getFilledWidth() {
        return getFirstWidth() + sumArray(widths, firstColumn, rowViewList.size()) - mScrollX;
    }

    private int getFilledHeight() {
        return getFirstHeight() + sumArray(heights, firstRow, columnViewList.size()) - mScrollY;
    }
}
