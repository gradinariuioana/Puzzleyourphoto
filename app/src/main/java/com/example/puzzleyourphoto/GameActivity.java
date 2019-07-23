package com.example.puzzleyourphoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static GestureDetectGridView myGridView;

    private static int numberOfColumns = 3;
    private static int numberOfRows = 4;
    private static final int numberOfSubImages = numberOfColumns * numberOfRows;

    public static final String up = "up";
    public static final String down = "down";
    public static final String left = "left";
    public static final String right = "right";

    static String currentPhotoPath;
    static int REQUEST_TAKE_PHOTO;
    static int REQUEST_UPLOAD_PHOTO;
    static int requestedCode;
    static Uri selectedImage;
    private static int targetH, targetW;


    private static List<Bitmap> imageParts;
    private static int buttonHeight, buttonWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initLook();
        getIntentInfo();

        final Context con = this;

        myGridView.post(new Runnable() {
            @Override
            public void run() {
                if (REQUEST_TAKE_PHOTO == requestedCode) {
                    //Resize the image
                    Bitmap reduced = reduceImageSize();
                    Bitmap rotated = rotateImage(reduced);
                    splitImage(scaleBitmap(rotated));
                    //Display split image
                    display(con);
                } else if (REQUEST_UPLOAD_PHOTO == requestedCode) {
                    try {
                        //Split the image
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(con.getContentResolver(), selectedImage);
                        Bitmap rotated = rotateImage(bitmap);
                        splitImage(scaleBitmap(rotated));
                        //Display the split image
                        display(con);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //We use this string array to verify whether the game is finished or not
    private static String[] tileList;

    //Get the initial look of the activity
    private void initLook() {
        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myGridView = findViewById(R.id.grid);
        myGridView.setVerticalScrollBarEnabled(false);
        myGridView.setNumColumns(numberOfColumns);

        tileList = new String[numberOfSubImages];
        for (int i = 0; i < numberOfSubImages; i++) {
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

    //Reduce image size
    private Bitmap reduceImageSize() {
        targetH = myGridView.getHeight();
        targetW = myGridView.getWidth();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoH = bmOptions.outHeight;
        int photoW = bmOptions.outWidth;

        bmOptions.inJustDecodeBounds = false;
        int scaleFactor = Math.max(photoH / targetH, photoW / targetW);
        bmOptions.inSampleSize = scaleFactor;

        Bitmap reducedBitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        return reducedBitmap;
    }

    //Rotate the image if it's the case
    private Bitmap rotateImage(Bitmap bitmap) {

        int orientation;

        ExifInterface exifInterface = null;
        if (requestedCode == REQUEST_TAKE_PHOTO) {
            try {
                exifInterface = new ExifInterface(MainActivity.currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } else {
            targetH = myGridView.getHeight();
            targetW = myGridView.getWidth();
            String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
            Cursor cur = getApplicationContext().getContentResolver().query(selectedImage, orientationColumn, null, null, null);
            orientation = -1;
            if (cur != null && cur.moveToFirst()) {
                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            }
        }
        System.out.println(orientation);
        Matrix matrix = new Matrix();
        switch (orientation) {
            //Take cases
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            //Upload cases
            case 90:
                matrix.setRotate(90);
                break;
            case 180:
                matrix.setRotate(180);
                break;
            case 270:
                matrix.setRotate(270);
                break;
            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    //Reduce image size to fit the gridView
    private Bitmap scaleBitmap(Bitmap bitmap) {

        Bitmap reducedPhoto = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
        return reducedPhoto;
    }


    //Get the imageParts from the image
    private void splitImage(Bitmap bitmap) {
        imageParts = new ArrayList<>();
        int yCoord = 0;
        buttonHeight = bitmap.getHeight() / numberOfRows;
        buttonWidth = bitmap.getWidth() / numberOfColumns;
        for (int y = 0; y < numberOfRows; ++y) {
            int xCoord = 0;
            for (int x = 0; x < numberOfColumns; ++x) {
                Bitmap currentBitmap = Bitmap.createBitmap(bitmap, xCoord, yCoord, buttonWidth, buttonHeight);
                imageParts.add(currentBitmap);
                xCoord += buttonWidth;
            }
            yCoord += buttonHeight;
        }
        //Shuffle the tileList for random order of imageParts
        Collections.shuffle(Arrays.asList(tileList));
        System.out.println(bitmap.getHeight());
        System.out.println(imageParts.get(0).getHeight());
    }

    //Display imageParts as button backgrounds in the gridView
    private static void display(Context context) {
        ArrayList<Button> buttons = new ArrayList<>();
        Button button;

        for (int i = 0; i < tileList.length; i++) {
            button = new Button(context);
            for (int j = 0; j < numberOfSubImages; j++)
                //each tileList[i] contains a string with a number and we display the imagePart related to that number
                if (tileList[i].equals("" + j))
                    button.setBackground(new BitmapDrawable(context.getResources(), imageParts.get(j)));
            buttons.add(button);
        }
        myGridView.setAdapter(new CustomAdapter(buttons, buttonWidth, buttonHeight));
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numberOfColumns = 4;
            numberOfRows = 3;

            if (REQUEST_TAKE_PHOTO == requestedCode) {
                //Resize the image
                Bitmap reduced = reduceImageSize();
                Bitmap rotated = rotateImage(reduced);
                splitImage(scaleBitmap(rotated));
                //Display split image
                display(this);
            } else if (REQUEST_UPLOAD_PHOTO == requestedCode) {
                try {
                    //Split the image
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    Bitmap rotated = rotateImage(bitmap);
                    splitImage(scaleBitmap(rotated));
                    //Display the split image
                    display(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

