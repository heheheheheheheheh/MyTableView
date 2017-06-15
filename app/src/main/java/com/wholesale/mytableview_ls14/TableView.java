package com.wholesale.mytableview_ls14;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @version ${Rev}
 * @auther liucz
 * @time 2017/6/7 11:05
 * Created by dell on 2017/6/7.
 */

public class TableView extends ViewGroup {

    private static final String VERTICAL = "VERTICAL";
    private static final String HORIZONTAL = "HORIZONTAL";
    private static final String BOTH = "BOTH";
    //第一行
    private int firstRow;
    //第一列
    private int firstColumn;
    private int[] widths;
    private int[] heights;

    private int width;
    private int height;
    private Recycler recycler;
    private BaseTableAdapter adapter;
    private int mRowCount;
    private int mColumnCount;
    private int firstWidth = 100;
    private int firstHeight = 40;
        private String mDirection;
    private float mStartX;
    private float mStartY;
    private int mScrollY;
    private int mScrollX;
    private int CurrentScrollX;
    private int CurrentScrollY;

    public TableView(Context context) {
        this(context, null);
    }

    public TableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAdapter(BaseTableAdapter baseTableAdapter) {
        this.adapter = baseTableAdapter;
        this.recycler = new Recycler(baseTableAdapter.getViewTypeCount());
        firstColumn = 0;
        firstRow = 0;
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
        removeAllViews();
        width = r - l;
        height = b - t;
        //        makeAndStep(0, 0, 0, 0, widths[0], heights[0]);
        int left = scopeX(r);;
        int top = 0;
        for (int i = 0; i < mColumnCount && left < r; i++) {
            top = scopeY(b);
            for (int j = 0; j < mRowCount && top < b; j++) {
                makeAndStep(j, i, left, top, left + widths[i], top + heights[i]);
                top += heights[i];
            }
            left += widths[i];
        }
        left = scopeX(r);;
        for (int i = 0; i < mColumnCount && left < r; i++) {//排第一行
            layoutColumnRow(true, i, left, 0, left + widths[i], heights[0]);
            left += widths[i];
        }
        top =scopeY(b);
        for (int i = 0; i < mRowCount && top < b; i++) {//排第一列
            layoutColumnRow(false, i, 0, top, widths[0], top + heights[i]);
            top += heights[i];
        }
        layoutfirst();//排第一个
    }

    private int scopeX(int r) {
        int x = getFirstWidth()+mScrollX + CurrentScrollX;
        if (x>=getFirstWidth()){
            x = getFirstWidth();
            CurrentScrollX=0;
        }else if (x<r-sumArray(widths)){
            x=r-sumArray(widths);
            CurrentScrollX = r-sumArray(widths)-getFirstWidth();
        }
        return x;
    }
    private int scopeY(int b){
        int x = getFirstHeight()+mScrollY + CurrentScrollY;
        if (x>=getFirstHeight()){
            x = getFirstHeight();
            CurrentScrollY=0;
        }else if (x<b-sumArray(heights)){
            x=b-sumArray(heights);
            CurrentScrollY=b-sumArray(heights)-getFirstHeight();
        }
        return x;
    }

    private void layoutfirst() {
        TextView view = new TextView(getContext());
        view.setText("表格");
        view.setBackgroundColor(Color.BLACK);
        view.setTextSize(18);
        view.setTextColor(Color.WHITE);
        addView(view);
        view.layout(0, 0, 100, 40);
    }

    private void layoutColumnRow(boolean isRow, int num, int left, int top, int right, int bottom) {
        TextView view = null;
        if (isRow) {
            view = new TextView(getContext());
            view.setText(num + "行");
        } else {
            view = new TextView(getContext());
            view.setText(num + "列");
        }
        view.setBackgroundColor(Color.BLACK);
        view.setTextSize(18);
        view.setTextColor(Color.WHITE);
        addView(view);
        view.layout(left, top, right, bottom);
    }

    private void makeAndStep(int row, int colmun, int left, int top, int right, int bottom) {
        View view = obtainView(row, colmun, right, right - left, bottom - top);
        view.layout(left, top, right, bottom);
    }

    private View obtainView(int row, int colmun, int right, int width, int height) {
        int type = adapter.getItemViewType(row, colmun);
        View reclyView = recycler.getRecycleView(type);
        View view = adapter.getView(row, colmun, reclyView, this);
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

    public void setFirstWidth(int firstWidth) {
        this.firstWidth = firstWidth;
    }

    public int getFirstHeight() {
        return firstHeight;
    }

    public void setFirstHeight(int firstHeight) {
        this.firstHeight = firstHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean canotHorizontal = false;
        boolean canotVertical = false;
        if (sumArray(widths)+getFirstWidth()<getWidth()){
            canotHorizontal = true;
        }
        if (sumArray(heights)+getFirstHeight()<getHeight()){
            canotVertical = true;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x <= getFirstWidth() && y <= getFirstHeight()) {
                    return false;
                } else if (x <= getFirstWidth()||canotHorizontal) {
                    mDirection = VERTICAL;
                } else if (y <= getFirstHeight()||canotVertical) {
                    mDirection = HORIZONTAL;
                } else {
                    mDirection = BOTH;
                }
                mStartX = x;
                mStartY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mDirection) {
                    case VERTICAL:
                        mScrollY = (int) (y - mStartY);
                        break;
                    case HORIZONTAL:
                        mScrollX = (int) (x - mStartX);
                        break;
                    case BOTH:
                        mScrollX = (int) (x - mStartX);
                        mScrollY = (int) (y - mStartY);
                        break;
                }
                //                mStartX = x;
                //                mStartY = y;
                //                scrollBy(mScrollX,mScrollY);
                requestLayout();
                break;
            case MotionEvent.ACTION_UP:
                CurrentScrollX += mScrollX;
                CurrentScrollY += mScrollY;
                break;
        }
        return true;
    }
}
