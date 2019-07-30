package com.example.puzzleyourphoto;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

import java.util.List;

abstract class Game {

    static int numberOfColumns;
    static List<Bitmap> imageParts;
    static int pieceHeight, pieceWidth;
    static int numberOfSubImages;
    static String[] tileList;

    Game(int numberOfColumns){
        Game.numberOfColumns = numberOfColumns;
        Game.numberOfSubImages = numberOfColumns * numberOfColumns;
        tileList = new String[numberOfSubImages];
        for (int i = 0; i < numberOfSubImages; i++) {
            tileList[i] = String.valueOf(i);
        }
    }

    void splitImage(@NotNull Bitmap bitmap) {
    }

    int getPieceWidth() {
        return pieceWidth;
    }

    int getPieceHeight() {
        return pieceHeight;
    }
    String[] getTileList() {
        return tileList;
    }

    void setTileList(String[] tileList) {
        Game.tileList = tileList;
    }

}
