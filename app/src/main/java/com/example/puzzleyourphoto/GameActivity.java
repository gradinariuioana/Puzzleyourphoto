package com.example.puzzleyourphoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static GestureDetectGridView myGridView;
    private static final int numberOfColumns = 3;
    private static final int numberOfRows = 4;
    private static final int numberOfChunks = numberOfColumns * numberOfRows;

    public static final String up = "up";
    public static final String down = "down";
    public static final String left = "left";
    public static final String right = "right";

    static String currentPhotoPath;
    static int REQUEST_TAKE_PHOTO;
    static int REQUEST_UPLOAD_PHOTO;
    static int requestedCode;
    static Uri selectedImage;


    private static List<Bitmap> imageParts;
    private static int chunkHeight, chunkWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initLook();
        getIntentInfo();

        if (REQUEST_TAKE_PHOTO == requestedCode) {
            //Split the image
            Bitmap reducedPhoto = setReducedImageSize();
            splitImage(rotateImage(reducedPhoto));
            //Display split image
            display(this);
        } else if (REQUEST_UPLOAD_PHOTO == requestedCode) {
            try {
                //Split the image
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                splitImage(bitmap);
                //Display the split image
                display(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //We use this string array to verify whether the game is finished or not
    private static String[] tileList;

    private void initLook() {
        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myGridView = (GestureDetectGridView) findViewById(R.id.grid);
        myGridView.setNumColumns(numberOfColumns);

        tileList = new String[numberOfChunks];
        for (int i = 0; i < numberOfChunks; i++) {
            tileList[i] = String.valueOf(i);
        }
    }

    //Get info from intent
    private void getIntentInfo() {
        Intent intent = getIntent();

        currentPhotoPath = intent.getStringExtra("CURRENT_PHOTO_PATH");
        REQUEST_TAKE_PHOTO = intent.getExtras().getInt("REQUEST_TAKE_PHOTO");
        REQUEST_UPLOAD_PHOTO = intent.getExtras().getInt("REQUEST_UPLOAD_PHOTO");
        requestedCode = intent.getExtras().getInt("REQUEST_CODE");
        if (REQUEST_UPLOAD_PHOTO == requestedCode)
            selectedImage = Uri.parse(intent.getStringExtra("URI"));
    }

    //Reduce image size to fit the gridView
    private Bitmap setReducedImageSize() {

        int targetW = myGridView.getLayoutParams().width;
        int targetH = myGridView.getLayoutParams().height;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        System.out.println(targetW);
        System.out.println(targetW);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap reducedPhoto = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        //imageView.setImageBitmap(reducedPhoto);

        return reducedPhoto;
    }

    //Rotate the image if it's the case
    private Bitmap rotateImage(Bitmap bitmap) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(MainActivity.currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //imageView.setImageBitmap(rotatedBitmap);
        return rotatedBitmap;
    }

    //Get the imageParts from the image
    private void splitImage(Bitmap bitmap) {
        imageParts = new ArrayList<>();
        int yCoord = 0;
        chunkHeight = bitmap.getHeight() / numberOfRows;
        System.out.println(chunkHeight);
        chunkWidth = bitmap.getWidth() / numberOfColumns;
        System.out.println(chunkWidth);
        for (int y = 0; y < numberOfRows; ++y) {
            int xCoord = 0;
            for (int x = 0; x < numberOfColumns; ++x) {
                Bitmap currentBitmap = Bitmap.createBitmap(bitmap, xCoord, yCoord, chunkWidth, chunkHeight);
                imageParts.add(currentBitmap);
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        //Shuffle the tileList for random order of imageParts
        Collections.shuffle(Arrays.asList(tileList));
    }

    //Display imageParts as button backgrounds in the gridView
    private static void display(Context context) {
        ArrayList<Button> buttons = new ArrayList<>();
        Button button;

        for (int i = 0; i < tileList.length; i++) {
            button = new Button(context);
            for (int j = 0; j < numberOfChunks; j++)
                //each tileList[i] contains a string with a number and we display the imagePart related to that number
                if (tileList[i].equals("" + j))
                    button.setBackground(new BitmapDrawable(context.getResources(), imageParts.get(j)));
            buttons.add(button);
        }
        myGridView.setAdapter(new CustomAdapter(buttons, chunkWidth, chunkHeight));
    }

    //Swap two tiles in the tileList -> swap two images backgrounds -> the display to see the changes
    private static void swap(Context context, int currentPosition, int positionsToSwapWith) {
        String newPosition = tileList[currentPosition + positionsToSwapWith];
        tileList[currentPosition + positionsToSwapWith] = tileList[currentPosition];
        tileList[currentPosition] = newPosition;
        display(context);

        if (isSolved()) Toast.makeText(context, "YOU WIN!", Toast.LENGTH_SHORT).show();
    }


    public static void moveTiles(Context context, String direction, int position) {
        if (direction.equals(up)) {
            //Upper row cannot move up
            if (position - numberOfColumns >= 0) swap(context, position, -numberOfColumns);
            else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
        } else if (direction.equals(left)) {
            //Left column cannot move left
            if (position % numberOfColumns != 0) swap(context, position, -1);
            else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
        } else if (direction.equals(right)) {
            //Right column cannot move rigth
            if ((position + 1) % numberOfColumns != 0) swap(context, position, 1);
            else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
        } else {
            //Down row cannot move down
            if (position <= numberOfColumns * (numberOfRows - 1))
                swap(context, position, numberOfColumns);
            else Toast.makeText(context, "Invalid move!", Toast.LENGTH_SHORT).show();
        }
    }

    //Verify whether each photoPiece is its right place
    private static boolean isSolved() {
        boolean solved = true;
        for (int i = 0; i < tileList.length && solved; i++)
            if (!(tileList[i].equals(String.valueOf(i))))
                solved = false;
        return solved;
    }
}

