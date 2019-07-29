package com.example.puzzleyourphoto;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

class CustomAdapterSwap extends BaseAdapter {
    private final ArrayList<Button> buttons;
    private final int columnWidth, columnHeight;

    CustomAdapterSwap(ArrayList<Button> buttons, int columnWidth, int columnHeight) {
        this.buttons = buttons;
        this.columnWidth = columnWidth;
        this.columnHeight = columnHeight;
    }

    @Override
    public int getCount() {
        return buttons.size();
    }

    @Override
    public Object getItem(int position) {return buttons.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button button;

        //If I do not have what to reuse I take the button from the list
        if (convertView == null) {
            button = buttons.get(position);
        }
        //I reuse the convertView
        else {
            button = (Button) convertView;
        }

        //Provide a place to hold the view type
        button.setLayoutParams(new AbsListView.LayoutParams(columnWidth, columnHeight));

        return button;
    }
}