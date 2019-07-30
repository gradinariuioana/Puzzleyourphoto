package com.example.puzzleyourphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class JigsawGame extends Game {
    static private List<PuzzlePiece> puzzlePieces;
    private static ArrayList<ImageView> imageViews;

    JigsawGame(int numberOfColumns){
        super(numberOfColumns);
    }

    @Override
    void splitImage(@NotNull Bitmap bitmap) {
        imageParts = new ArrayList<>();
        puzzlePieces = new ArrayList<>();
        int yCoord = 0;
        pieceHeight = bitmap.getHeight() / numberOfColumns;
        pieceWidth = bitmap.getWidth() / numberOfColumns;
        for (int y = 0; y < numberOfColumns; ++y) {
            int xCoord = 0;
            for (int x = 0; x < numberOfColumns; ++x) {
                int offsetX = 0;
                int offsetY = 0;
                if (x > 0)
                    offsetX = pieceWidth / 3;
                if (y > 0)
                    offsetY = pieceHeight / 3;
                Bitmap currentBitmap = Bitmap.createBitmap(bitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                float bumpSize = pieceHeight / 4.0f;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (y == 0) {
                    // top side piece
                    path.lineTo(currentBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    path.lineTo(offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f, offsetY);
                    path.cubicTo(offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f, offsetY - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f * 5.0f, offsetY - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f * 2.0f, offsetY);
                    path.lineTo(currentBitmap.getWidth(), offsetY);
                }

                if (x == numberOfColumns - 1) {
                    // right side piece
                    path.lineTo(currentBitmap.getWidth(), currentBitmap.getHeight());
                } else {
                    // right bump
                    path.lineTo(currentBitmap.getWidth(), offsetY + (currentBitmap.getHeight() - offsetY) / 3.0f);
                    path.cubicTo(currentBitmap.getWidth() - bumpSize,offsetY + (currentBitmap.getHeight() - offsetY) / 6.0f, currentBitmap.getWidth() - bumpSize, offsetY + (currentBitmap.getHeight() - offsetY) / 6.0f * 5.0f, currentBitmap.getWidth(), offsetY + (currentBitmap.getHeight() - offsetY) / 3.0f * 2.0f);
                    path.lineTo(currentBitmap.getWidth(), currentBitmap.getHeight());
                }

                if (y == numberOfColumns - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, currentBitmap.getHeight());
                } else {
                    // bottom bump
                    path.lineTo(offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f * 2.0f, currentBitmap.getHeight());
                    path.cubicTo(offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f * 5.0f,currentBitmap.getHeight() - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 6.0f, currentBitmap.getHeight() - bumpSize, offsetX + (currentBitmap.getWidth() - offsetX) / 3.0f, currentBitmap.getHeight());
                    path.lineTo(offsetX, currentBitmap.getHeight());
                }

                if (x == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
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

                // draw a white border
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // draw a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                PuzzlePiece puzzlePiece1 = new PuzzlePiece(xCoord, yCoord, puzzlePiece);
                puzzlePieces.add(puzzlePiece1);

                imageParts.add(puzzlePiece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }
    }

    static void prepareForDisplay(Context context) {
        imageViews = new ArrayList<>();
        ImageView imageView;
        for (String s : tileList) {
            imageView = new ImageView(context);
            TouchListener touchListener = new TouchListener();
            imageView.setBackground(new BitmapDrawable(context.getResources(), imageParts.get(Integer.parseInt(s))));
            imageViews.add(imageView);
        }
    }


    ArrayList<ImageView> getImageViews() {
        return imageViews;
    }
}
