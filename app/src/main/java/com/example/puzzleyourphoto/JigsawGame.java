package com.example.puzzleyourphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JigsawGame extends Game {
    private static List<PuzzlePiece> puzzlePieces;

    JigsawGame(int numberOfColumns){
        super(numberOfColumns);
    }

    @Override
    void splitImage(@NotNull Bitmap bitmap, Context context) {
        imageParts = new ArrayList<>();
        puzzlePieces = new ArrayList<>();
        int yCoord = 0;
        pieceHeight = bitmap.getHeight() / numberOfColumns;
        pieceWidth = bitmap.getWidth() / numberOfColumns;
        for (int row = 0; row < numberOfColumns; ++row) {
            int xCoord = 0;
            for (int col = 0; col < numberOfColumns; ++col) {
                int offsetX;
                if (col > 0)
                    offsetX = pieceWidth / 3;
                else
                    offsetX = 0;
                int offsetY;
                if (row > 0)
                    offsetY = pieceHeight / 3;
                else
                    offsetY = 0;
                Bitmap currentBitmap = Bitmap.createBitmap(bitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // top side piece
                    path.lineTo(currentBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    float bumpSize = pieceHeight / 4.0f;
                    path.lineTo(offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f, offsetY);
                    path.cubicTo(offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f, offsetY - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f * 5.0f, offsetY - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f * 2.0f, offsetY);
                    path.lineTo(currentBitmap.getWidth(), offsetY);
                }

                if (col == numberOfColumns - 1) {
                    // right side piece
                    path.lineTo(currentBitmap.getWidth(), currentBitmap.getHeight());
                } else {
                    // right bump
                    float bumpSize = pieceWidth / 4.0f;
                    path.lineTo(currentBitmap.getWidth(), offsetY + (currentBitmap.getHeight() - offsetY) / 3.0f);
                    path.cubicTo(currentBitmap.getWidth() - bumpSize, offsetY + (currentBitmap.getHeight() - offsetY) / 6.0f, currentBitmap.getWidth() - bumpSize, offsetY + (currentBitmap.getHeight() - offsetY) / 6.0f * 5.0f, currentBitmap.getWidth(), offsetY + (currentBitmap.getHeight() - offsetY) / 3.0f * 2.0f);
                    path.lineTo(currentBitmap.getWidth(), currentBitmap.getHeight());
                }

                if (row == numberOfColumns - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, currentBitmap.getHeight());
                } else {
                    // bottom bump
                    float bumpSize = pieceHeight / 4.0f;
                    path.lineTo(offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f * 2.0f, currentBitmap.getHeight());
                    path.cubicTo(offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f * 5.0f,currentBitmap.getHeight() - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f, currentBitmap.getHeight() - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f, currentBitmap.getHeight());
                    path.lineTo(offsetX, currentBitmap.getHeight());
                }

                if (col == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
                    float bumpSize = pieceWidth / 4.0f;
                    path.lineTo(offsetX, offsetY + (currentBitmap.getHeight() - offsetY) / 3.0f * 2.0f);
                    path.cubicTo(offsetX - bumpSize, offsetY + (currentBitmap.getHeight() - offsetY) / 6.0f * 5.0f, offsetX - bumpSize, offsetY + (currentBitmap.getHeight() - offsetY) / 6.0f, offsetX, offsetY + (currentBitmap.getHeight() - offsetY) / 3.0f);
                    path.close();
                }

                // mask the piece
                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(currentBitmap, 0, 0, paint);

                // adding a white border
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // adding a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                PuzzlePiece piece = new PuzzlePiece(context);
                piece.setImageBitmap(puzzlePiece);
                piece.xCoord = xCoord - offsetX;
                piece.yCoord = yCoord - offsetY;
                puzzlePieces.add(piece);

                imageParts.add(puzzlePiece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }
    }


    static void prepareForDisplay(RelativeLayout relativeLayout, Context context){
        TouchListener touchListener = new TouchListener(context);
        Collections.shuffle(puzzlePieces);
        for(PuzzlePiece piece : puzzlePieces) {
            piece.setAdjustViewBounds(false);
            piece.setOnTouchListener(touchListener);
            piece.setMinimumHeight(piece.getHeight());
            piece.setMinimumWidth(piece.getWidth());
            relativeLayout.addView(piece);
        }
    }

    static boolean isFinished(){
        for (PuzzlePiece puzzlePiece : puzzlePieces){
            if (puzzlePiece.canMove)
                return false;
        }
        return true;
    }

}
