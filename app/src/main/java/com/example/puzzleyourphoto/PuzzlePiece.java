package com.example.puzzleyourphoto;

import android.graphics.Bitmap;

public class PuzzlePiece {

    private int originalXCoord;
    private int originalYCoord;
    private Bitmap bitmap;


    PuzzlePiece(int originalXCoord, int originalYCoord, Bitmap bitmap){
        setBitmap(bitmap);
        setOriginalXCoord(originalXCoord);
        setOriginalYCoord(originalYCoord);
    }


    public int getOriginalXCoord() {
        return originalXCoord;
    }

    private void setOriginalXCoord(int originalXCoord) {
        this.originalXCoord = originalXCoord;
    }

    public int getOriginalYCoord() {
        return originalYCoord;
    }

    private void setOriginalYCoord(int originalYCoord) {
        this.originalYCoord = originalYCoord;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    private void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
