package com.example.puzzleyourphoto;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab1 = findViewById(R.id.takephotobtn);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.uploadphotobtn);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchUploadPictureIntent();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    String currentPhotoPath;
    ImageView imageView;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_UPLOAD_PHOTO = 2;
    File photoFile;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void dispatchUploadPictureIntent(){
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_UPLOAD_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView = findViewById(R.id.imageView2);
        if(this.REQUEST_TAKE_PHOTO == requestCode && resultCode == RESULT_OK){
            Bitmap reducedPhoto = setReducedImageSize();
            splitImage(rotateImage(reducedPhoto));
        }
        else if(this.REQUEST_UPLOAD_PHOTO == requestCode && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            imageView.setImageURI(selectedImage);

            /* //IF THAT DOESN'T WORK TRY:
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));*/
        }
    }


    private Bitmap setReducedImageSize() {

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap reducedPhoto = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(reducedPhoto);

        return  reducedPhoto;
    }

    private Bitmap rotateImage(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(currentPhotoPath);
        }catch (IOException e){
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

    static final int numberOfColumns = 3;
    static final int numberOfRows = 4;
    private static List<Bitmap> chunkedImage;

    private void splitImage(Bitmap bitmap){
        chunkedImage = new ArrayList<>(numberOfRows * numberOfColumns);
        int yCoord = 0;
        int chunkHeight = bitmap.getHeight()/numberOfRows;
        int chunkWidth = bitmap.getWidth()/numberOfColumns;
        for (int y = 0; y < numberOfRows; ++y) {
            int xCoord = 0;
            for (int x = 0; x < numberOfColumns; ++x) {
                chunkedImage.add(Bitmap.createBitmap(bitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }

        Collections.shuffle(chunkedImage);

        //create a bitmap of a size which can hold the complete image after merging
        resultBitmap = Bitmap.createBitmap(chunkWidth * numberOfColumns, chunkHeight * numberOfRows,  Bitmap.Config.ARGB_8888);

        //create a canvas for drawing all those small images
        Canvas canvas = new Canvas(resultBitmap);
        int count = 0;
        for(int rows = 0; rows < numberOfRows; rows++){
            for(int cols = 0; cols < numberOfColumns; cols++){
                canvas.drawBitmap(chunkedImage.get(count), chunkWidth * cols, chunkHeight * rows, null);
                canvases.add(new Canvas(chunkedImage.get(count)));
                count++;
            }
        }
        imageView.setImageBitmap(resultBitmap);
    }

    List<Canvas> canvases = new ArrayList<>(0);
    Bitmap resultBitmap;


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //Create a rectangle representing the imageView
        Rect imageViewRectangle = new Rect();
        int[] location = new int[2];

        imageView.getLocationOnScreen(location);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();

        imageViewRectangle.left = location[0] + lp.leftMargin;
        imageViewRectangle.top = location[1] + lp.topMargin;
        imageViewRectangle.right = imageViewRectangle.left + imageView.getWidth() + lp.leftMargin - lp.rightMargin;
        imageViewRectangle.bottom = imageViewRectangle.top + imageView.getHeight() + lp.topMargin - lp.bottomMargin;

        //The x and y coordinates of the touch -> relative to the screen i.e. top left corner of imageView has coordinates location[0], location[1] !!!NOT (0,0)
        int xCoordinate = (int) (event.getX() + imageView.getX());
        int yCoordinate = (int) (event.getY() + imageView.getY());


        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (imageViewRectangle.contains(xCoordinate, yCoordinate)) {
                    //PROBLEMS WITH LAST ROW AND COLUMN!!!
                    int row = (yCoordinate - imageViewRectangle.top ) / chunkedImage.get(0).getHeight();
                    System.out.println(row);
                    int column = (xCoordinate - imageViewRectangle.left) / chunkedImage.get(0).getWidth();
                    System.out.println(column);
                    Context context = getApplicationContext();
                    CharSequence text = "ceva";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
