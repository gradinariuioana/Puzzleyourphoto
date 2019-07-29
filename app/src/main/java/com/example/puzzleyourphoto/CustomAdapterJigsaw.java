package com.example.puzzleyourphoto;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

class CustomAdapterJigsaw extends BaseAdapter {
    private final ArrayList<ImageView> imageViews;
    private final int columnWidth, columnHeight;

    CustomAdapterJigsaw(ArrayList<ImageView> imageViews, int columnWidth, int columnHeight) {
        this.imageViews = imageViews;
        this.columnWidth = columnWidth;
        this.columnHeight = columnHeight;
    }

    @Override
    public int getCount() {
        return imageViews.size();
    }

    @Override
    public Object getItem(int position) {return imageViews.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        //If I do not have what to reuse I take the button from the list
        if (convertView == null) {
            imageView = imageViews.get(position);
        }
        //I reuse the convertView
        else {
            imageView = (ImageView) convertView;
        }

        //Provide a place to hold the view type
        imageView.setLayoutParams(new AbsListView.LayoutParams(columnWidth, columnHeight));

        return imageView;
    }
}