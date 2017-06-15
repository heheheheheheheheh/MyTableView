package com.wholesale.mytableview_ls14;

import android.view.View;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @version ${Rev}
 * @auther liucz
 * @time 2017/6/7 10:56
 * Created by dell on 2017/6/7.
 */

public class Recycler {
    private Stack<View>[] views;
    private ArrayList<View>[] views2;
    private ArrayList<View> viewColumn;
    private ArrayList<View> viewRow;

    public Recycler(int type) {
        viewColumn = new ArrayList<>();
        viewRow = new ArrayList<>();
        views2 = new ArrayList[type];
        for (int i = 0; i < type; i++) {
            views2[i] = new ArrayList<>();
        }
    }

    public void addRecycledView(View view, int type) {
        views2[type].add(view);
    }

    public View getRecycleView(int type) {
        try {
            int size = views2[type].size();
            return views2[type].remove(0);
        } catch (Exception e) {
            return null;
        }
    }

    public void addRowView(View view) {
        viewRow.add(view);
    }

    public View getRowView() {
        try {
            int size = viewRow.size();
            return viewRow.remove(0);
        } catch (Exception e) {
            return null;
        }
    }

    public void addColumnView(View view) {
        viewColumn.add(view);
    }

    public View getColumnView() {
        try {
            int size = viewColumn.size();
            return viewColumn.remove(0);
        } catch (Exception e) {
            return null;
        }
    }
}
