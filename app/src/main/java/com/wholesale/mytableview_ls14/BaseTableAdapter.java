package com.wholesale.mytableview_ls14;

import android.view.View;
import android.view.ViewGroup;

/**
 * @version ${Rev}
 * @auther liucz
 * @time 2017/6/7 11:06
 * Created by dell on 2017/6/7.
 */

public interface BaseTableAdapter {
    int getRowCount();
    int getColumnCount();
    View getView(int row, int column, View convertView, ViewGroup parent);
    int getWidth(int column);
    int getHeight(int row);
    int getItemViewType(int row,int column);
    int getViewTypeCount();
    View getColumnView(int row,View convertView,ViewGroup parent);
    View getRowView(int column,View convertView,ViewGroup parent);
    View getFirstView();
    int getFirstWidth();
    int getFirstHeight();
}
