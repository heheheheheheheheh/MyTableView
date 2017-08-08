package com.wholesale.mytableview_ls14;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //test
        //test
        //test
        setContentView(R.layout.activity_main);
        NewTableView tv = (NewTableView) findViewById(R.id.tv);
        tv.setAdapter(new MyAdapter(MainActivity.this));
    }
}

class MyAdapter implements BaseTableAdapter {
    private Context mContext;

    public MyAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getRowCount() {
        return 50;
    }

    @Override
    public int getColumnCount() {
        return 50;
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {
        if (convertView!=null)
            Log.e("Tag44","convertView"+convertView);
        if (convertView == null) {
            convertView= new TextView(mContext);
        }
        convertView.setBackgroundColor(Color.GREEN);
        ((TextView)convertView).setTextColor(Color.RED);
        ((TextView)convertView).setTextSize(18);
        ((TextView)convertView).setGravity(Gravity.CENTER);
        ((TextView)convertView).setText(row + "行" + column + "列");
        return convertView;
    }

    @Override
    public int getWidth(int column) {
        return 100;
    }

    @Override
    public int getHeight(int row) {
        return 50;
    }

    @Override
    public int getItemViewType(int row, int column) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getColumnView(int row, View convertView, ViewGroup parent) {
        if (convertView!=null)
            Log.e("Tag79","convertView"+convertView);
        if (convertView == null) {
            convertView= new TextView(mContext);
        }
        ((TextView)convertView).setText(row+"行");
        convertView.setBackgroundColor(Color.BLUE);
        return convertView;
    }

    @Override
    public View getRowView(int column, View convertView, ViewGroup parent) {
        if (convertView!=null)
            Log.e("Tag91","convertView"+convertView);
        if (convertView == null) {
            convertView= new TextView(mContext);
        }
        ((TextView)convertView).setText(column+"列");
        convertView.setBackgroundColor(Color.RED);
        return convertView;
    }

    @Override
    public View getFirstView() {
        TextView textView = new TextView(mContext);
        textView.setText("firstview");
        textView.setBackgroundColor(Color.WHITE);
        return textView;
    }

    @Override
    public int getFirstWidth() {
        return 50;
    }

    @Override
    public int getFirstHeight() {
        return 50;
    }

}