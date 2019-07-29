package com.example.puzzleyourphoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static GridView myGridView;

    private static int numberOfColumns;
    private static int numberOfSubImages;

    public static final String up = "up";
    public static final String down = "down";
    public static final String left = "left";
    public static final String right = "right";

    private static String currentPhotoPath;
    private static int REQUEST_TAKE_PHOTO;
    private static int REQUEST_UPLOAD_PHOTO;
    private static int requestedCode;
    private static Uri selectedImage;
    private static int targetH, targetW;

    private static boolean shuffled  = false;

    private static List<Bitmap> imageParts;
    private static int pieceHeight, pieceWidth;

    //We use this string array to verify whether the game is finished or not
    private static String[] tileList;
    private static String[] shuffledTileList;

    private static CountDownTimer countDownTimer;
    private static long defaultTime;
    private static long timeLeftOnTimer = -1;
    private static long counter;
    private static String mode;

    private static String type;
    private static AlertDialog myCustomDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getIntentInfo();
        initLook();

        //Create a copy of the context to use in run method
        final Context con = this;

        //To be able to measure the grid view after it is drawn -> add these actions to the end of the running queue
        myGridView.post(new Runnable() {
            @Override
            public void run() {
                myGridView.setNumColumns(numberOfColumns);
                if (REQUEST_TAKE_PHOTO == requestedCode) {
                    //Resize the image
                    Bitmap reduced = reduceImageSize();
                    Bitmap rotated = rotateImage(reduced);
                    splitImage(scaleBitmap(rotated));
                } else if (REQUEST_UPLOAD_PHOTO == requestedCode) {
                    try {
                        //Split the image
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(con.getContentResolver(), selectedImage);
                        Bitmap rotated = rotateImage(bitmap);
                        splitImage(scaleBitmap(rotated));
                        //Display the split image
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (type.equals("Swapping tiles"))
                    displaySwap(con);
                else displayJigsaw(con);
                startCounter(con);
            }
        });
    }

    //Get the initial look of the activity
    private void initLook() {

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myGridView.setVerticalScrollBarEnabled(false);

        tileList = new String[numberOfSubImages];
        for (int i = 0; i < numberOfSubImages; i++) {
            tileList[i] = String.valueOf(i);
        }

        final GameActivity gameActivity = this;

        Button restart = findViewById(R.id.restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartActivity(gameActivity);
            }
        });

    }

    //Get info from intent
    private void getIntentInfo() {
        Intent intent = getIntent();
        String difficulty;
        try{
        REQUEST_TAKE_PHOTO = intent.getExtras().getInt("REQUEST_TAKE_PHOTO");
        REQUEST_UPLOAD_PHOTO = intent.getExtras().getInt("REQUEST_UPLOAD_PHOTO");
        requestedCode = intent.getExtras().getInt("REQUEST_CODE");
        type = intent.getExtras().getString("TYPE");
            System.out.println(type);
        ViewFlipper viewFlipper = findViewById(R.id.content);
        if (type.equals("Swapping tiles")) {
            viewFlipper.setDisplayedChild(0);
            myGridView = findViewById(R.id.grid_gesture_detector);
        }else {
            viewFlipper.setDisplayedChild(1);
            myGridView = findViewById(R.id.simple_grid);
        }

        difficulty = intent.getExtras().getString("DIFFICULTY");
            if (difficulty.equals("Easy")) {
                numberOfColumns = 3;
                defaultTime = 30000;
            }
            else if (difficulty.equals("Medium")) {
                numberOfColumns = 5;
                defaultTime = 50000;
            }
            else{
                numberOfColumns = 7;
                defaultTime = 70000;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (timeLeftOnTimer == -1)
            timeLeftOnTimer = defaultTime;

        mode = intent.getExtras().getString("MODE");

        if (mode.equals("Zen Mode")) {
            findViewById(R.id.timer).setVisibility(View.INVISIBLE);
            findViewById(R.id.restart).setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }

        numberOfSubImages = numberOfColumns * numberOfColumns;

        if (REQUEST_TAKE_PHOTO == requestedCode)
            currentPhotoPath = intent.getStringExtra("CURRENT_PHOTO_PATH");
        if (REQUEST_UPLOAD_PHOTO == requestedCode)
            selectedImage = Uri.parse(intent.getStringExtra("URI"));
    }

    private void startCounter(Context con){
        final Context context = con;
        counter = timeLeftOnTimer / 1000;
        final TextView textView = findViewById(R.id.timer);
        countDownTimer = new CountDownTimer(timeLeftOnTimer, 1000){
            public void onTick(long millisUntilFinished){
                timeLeftOnTimer = millisUntilFinished;
                textView.setText("Time left: " + counter);
                counter = millisUntilFinished / 1000;
                if (counter <= 8)
                {
                    textView.setTextColor(getColor(R.color.Red));
                }
            }
            final GameActivity gameActivity = (GameActivity) context;
            public  void onFinish(){
                textView.setText("Time expired!!");
                if (mode.equals("Classic Mode")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        myCustomDialog = builder
                                .setTitle("Oh no! Time has expired!")
                                .setMessage("Do you want to play another game with the same image?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        restartActivity(gameActivity);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        timeLeftOnTimer = -1;
                                        shuffled = false;
                                        countDownTimer.cancel();
                                        finish();
                                    }
                                })
                                .setIcon(R.drawable.ic_star).create();
                        myCustomDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                myCustomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorPrimaryComplementary));
                                myCustomDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getColor(R.color.colorPrimaryComplementary));
                            }});
                        myCustomDialog.show();
                    }
                }
        }.start();
    }

    //Reduce image size to be able to work with it
    private Bitmap reduceImageSize() {
        targetH = myGridView.getHeight();
        targetW = myGridView.getWidth();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoH = bmOptions.outHeight;
        int photoW = bmOptions.outWidth;

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = Math.max(photoH / targetH, photoW / targetW);

        return BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
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
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //Scale image to fit the gridView
    private Bitmap scaleBitmap(Bitmap bitmap) {

        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
    }


    //Get the imageParts from the image
    private void splitImage(@NotNull Bitmap bitmap) {
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
        //Shuffle the tileList for random order of imageParts
        if (!shuffled) {
            Collections.shuffle(Arrays.asList(tileList));
            shuffledTileList = tileList;
            shuffled = true;
        }
        else tileList = shuffledTileList;
    }

    //Display imageParts as button backgrounds in the gridView
    private static void displaySwap(Context context) {
        ArrayList<Button> buttons = new ArrayList<>();
        Button button;

        for (String s : tileList) {
            button = new Button(context);
            for (int j = 0; j < numberOfSubImages; j++)
                //each tileList[i] contains a string with a number and we display the imagePart related to that number
                if (s.equals("" + j))
                    button.setBackground(new BitmapDrawable(context.getResources(), imageParts.get(j)));
            buttons.add(button);
        }
        myGridView.setAdapter(new CustomAdapterSwap(buttons, pieceWidth, pieceHeight));
    }

    private static void displayJigsaw(Context context) {
        ArrayList<ImageView> imageViews = new ArrayList<>();
        ImageView imageView;
        for (String s : tileList) {
            imageView = new ImageView(context);
            for (int j = 0; j < numberOfSubImages; j++)
                //each tileList[i] contains a string with a number and we display the imagePart related to that number
                if (s.equals("" + j))
                    imageView.setBackground(new BitmapDrawable(context.getResources(), imageParts.get(j)));
            imageViews.add(imageView);
        }
        myGridView.setAdapter(new CustomAdapterJigsaw(imageViews, pieceWidth, pieceHeight));

    }

    //Swap two tiles in the tileList -> swap two images backgrounds -> the display to see the changes
    private static void swapTiles(final Context context, int currentPosition, int positionsToSwapWith) {
        final GameActivity gameActivity = (GameActivity) context;
        String newPosition = tileList[currentPosition + positionsToSwapWith];
        tileList[currentPosition + positionsToSwapWith] = tileList[currentPosition];
        tileList[currentPosition] = newPosition;
        displaySwap(context);

        if (isSolved())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            myCustomDialog = builder
                    .setTitle("Congratulations! You won!")
                    .setMessage("Do you want to play another game with the same image?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            restartActivity(gameActivity);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            timeLeftOnTimer = -1;
                            shuffled = false;
                            countDownTimer.cancel();
                            ((GameActivity) context).finish();
                        }
                    })
                    .setIcon(R.drawable.ic_star).create();
            myCustomDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    myCustomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorPrimaryComplementary));
                    myCustomDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getColor(R.color.colorPrimaryComplementary));
                }});
            myCustomDialog.show();
        }
    }

    //Move tiles
    public static void moveTiles(Context context, @org.jetbrains.annotations.NotNull String direction, int position) {
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
    }

    //Verify whether each photoPiece is its right place
    private static boolean isSolved() {
        boolean solved = true;
        for (int i = 0; i < tileList.length && solved; i++)
            if (!(tileList[i].equals(String.valueOf(i))))
                solved = false;
        return solved;
    }

    private static void restartActivity(GameActivity gameActivity)
    {
        if (myCustomDialog != null)
            myCustomDialog.dismiss();
        timeLeftOnTimer = defaultTime;
        shuffled = false;
        countDownTimer.cancel();
        gameActivity.recreate();
    }

    @Override
    public void finish(){
        if (myCustomDialog != null)
            myCustomDialog.dismiss();
        super.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String TIME_KEY = "TIME_KEY";
        outState.putLong(TIME_KEY, timeLeftOnTimer);
        countDownTimer.cancel();
    }

    @Override
    public void onBackPressed() {
        timeLeftOnTimer = -1;
        shuffled = false;
        countDownTimer.cancel();
        super.onBackPressed();
    }
}

