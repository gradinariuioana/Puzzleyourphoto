package com.example.puzzleyourphoto;

import android.content.Context;

import androidx.appcompat.widget.AppCompatImageView;

public class PuzzlePiece extends AppCompatImageView {

    public int xCoord;
    public int yCoord;
    public boolean canMove = true;


    public PuzzlePiece(Context context) {
        super(context);
    }
}
