package com.example.puzzleyourphoto;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private ArrayList<Button> buttons;
    private int columnWidth, columnHeight;

    public CustomAdapter(ArrayList<Button> buttons, int columnWidth, int columnHeight) {
        this.buttons = buttons;
        this.columnWidth = columnWidth;
        this.columnHeight = columnHeight;
    }

    @Override
    public int getCount() {
        return buttons.size();
    }

    @Override
    public Object getItem(int position) {return (Object) buttons.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button button;

        if (convertView == null) {
            button = buttons.get(position);
        }
        else {
            button = (Button) convertView;
        }

        android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(columnWidth, columnHeight);
        button.setLayoutParams(params);

        return button;
    }
}