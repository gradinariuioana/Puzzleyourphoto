package com.example.puzzleyourphoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static GridView myGridView;
    private static Game myGame;

    private static int numberOfColumns;

    private static String currentPhotoPath;
    private static int REQUEST_TAKE_PHOTO;
    private static int REQUEST_UPLOAD_PHOTO;
    private static int requestedCode;
    private static Uri selectedImage;
    private static int targetH, targetW;

    private static boolean shuffled  = false;
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
                Bitmap bitmap = null;
                myGridView.setNumColumns(numberOfColumns);
                if (REQUEST_TAKE_PHOTO == requestedCode) {
                    bitmap = reduceImageSize();
                }
                else if (REQUEST_UPLOAD_PHOTO == requestedCode) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(con.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Bitmap rotatedBitmap = rotateImage(bitmap);
                Bitmap scaledBitmap = scaleBitmap(rotatedBitmap);
                if (type.equals("Swapping tiles"))
                {
                    myGame = new SwapGame(numberOfColumns);
                    tileList = myGame.getTileList();
                    if (!shuffled) {
                        Collections.shuffle(Arrays.asList(tileList));
                        shuffledTileList = tileList;
                        shuffled = true;
                    }
                    else tileList = shuffledTileList;
                    myGame.setTileList(tileList);
                }
                else
                {
                    myGame = new JigsawGame(numberOfColumns);
                }

                myGame.splitImage(scaledBitmap);
                pieceHeight = myGame.getPieceHeight();
                pieceWidth = myGame.getPieceWidth();

                display(con);

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

        final GameActivity gameActivity = this;

        Button restart = findViewById(R.id.restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartActivity(gameActivity);
            }
        });

    }

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
                textView.setText(getString(R.string.time_left, "" + counter));
                counter = millisUntilFinished / 1000;
                if (counter <= 8)
                {
                    textView.setTextColor(getColor(R.color.Red));
                }
            }
            final GameActivity gameActivity = (GameActivity) context;
            public  void onFinish(){
                textView.setText(getString(R.string.expired_time));
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
            orientation = exifInterface != null ? exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED) : 0;
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

    static void display(Context context){
        if (type.equals("Swapping tiles")){
            ((SwapGame)myGame).prepareForDisplay(context);
            myGridView.setAdapter(new CustomAdapterSwap(((SwapGame)myGame).getButtons(), pieceWidth, pieceHeight));
        }
        else {
            JigsawGame.prepareForDisplay(context);
            myGridView.setAdapter(new CustomAdapterJigsaw(((JigsawGame) myGame).getImageViews(), pieceWidth, pieceHeight));
        }

    }

    static void displayFinish(final Context context, final GameActivity gameActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        myCustomDialog = builder
                .setTitle("Congratulations! You won!")
                .setMessage("Do you want to play another game with the same image?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        restartActivity(gameActivity); }
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
        myCustomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                myCustomDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorPrimaryComplementary));
                myCustomDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getColor(R.color.colorPrimaryComplementary));
            }
        });
        myCustomDialog.show();
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

