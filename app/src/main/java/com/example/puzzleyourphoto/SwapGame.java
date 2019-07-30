package com.example.puzzleyourphoto;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

class SwapGame extends Game {

    private ArrayList<Button> buttons;

    static final String up = "up";
    static final String down = "down";
    static final String left = "left";
    static final String right = "right";

    SwapGame(int numberOfColumns){
        super(numberOfColumns);
    }

    void splitImage(@NotNull Bitmap bitmap) {
        imageParts = new ArrayList<>();
        int yCoord = 0;
        pieceHeight = bitmap.getHeight() / numberOfColumns;
        pieceWidth = bitmap.getWidth() / numberOfColumns;
        for (int y = 0; y < numberOfColumns; ++y) {
            int xCoord = 0;
            for (int x = 0; x < numberOfColumns; ++x) {
                Bitmap currentBitmap = Bitmap.createBitmap(bitmap, xCoord, yCoord, pieceWidth, pieceHeight);
                imageParts.add(currentBitmap);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }
    }

    //Display imageParts as button backgrounds in the gridView
    void prepareForDisplay(Context context) {
        buttons = new ArrayList<>();
        Button button;

        for (String s : tileList) {
            System.out.println(s);
            button = new Button(context);
            button.setBackground(new BitmapDrawable(context.getResources(), imageParts.get(Integer.parseInt(s))));
            buttons.add(button);
        }
    }


    //Swap two tiles in the tileList -> swap two images backgrounds -> the prepareForDisplay to see the changes
    private static void swapTiles(Context context, int currentPosition, int positionsToSwapWith) {
        String newPosition = tileList[currentPosition + positionsToSwapWith];
        tileList[currentPosition + positionsToSwapWith] = tileList[currentPosition];
        tileList[currentPosition] = newPosition;
        GameActivity.display(context);
    }

    static void moveTiles(Context context, @org.jetbrains.annotations.NotNull String direction, int position) {
        final GameActivity gameActivity = (GameActivity) context;
        switch (direction) {
            case up:
                //Upper row cannot move up
                if (position - numberOfColumns >= 0) swapTiles(context, position, -numberOfColumns);
                else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
                break;
            case left:
                //Left column cannot move left
                if (position % numberOfColumns != 0) swapTiles(context, position, -1);
                else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
                break;
            case right:
                //Right column cannot move rigth
                if ((position + 1) % numberOfColumns != 0) swapTiles(context, position, 1);
                else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
                break;
            default:
                //Down row cannot move down
                if (position <= numberOfColumns * (numberOfColumns - 1))
                    swapTiles(context, position, numberOfColumns);
                else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
                break;
        }
        if (isSolved())
            GameActivity.displayFinish(context, gameActivity);
    }

    private static boolean isSolved(){
        boolean solved = true;
        for (int i = 0; i < tileList.length && solved; i++)
            if (!(tileList[i].equals(String.valueOf(i))))
                solved = false;
        return solved;
    }

    ArrayList<Button> getButtons() {
        return buttons;
    }
}
