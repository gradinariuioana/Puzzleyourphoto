package com.example.puzzleyourphoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    static ImageView imageView;
    static String currentPhotoPath;
    static int REQUEST_TAKE_PHOTO;
    static int REQUEST_UPLOAD_PHOTO;
    static int requestedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        imageView = findViewById(R.id.imageView2);
        Intent intent = getIntent();

        currentPhotoPath = intent.getStringExtra("CURRENT_PHOTO_PATH");
        REQUEST_TAKE_PHOTO = intent.getExtras().getInt("REQUEST_TAKE_PHOTO");
        REQUEST_UPLOAD_PHOTO = intent.getExtras().getInt("REQUEST_UPLOAD_PHOTO");
        requestedCode = intent.getExtras().getInt("REQUEST_CODE");

        System.out.println(requestedCode);

        if(REQUEST_TAKE_PHOTO == requestedCode){
        Bitmap reducedPhoto = setReducedImageSize();
        splitImage(rotateImage(reducedPhoto));
    }
        else if(REQUEST_UPLOAD_PHOTO == requestedCode){
        Uri selectedImage = Uri.parse(intent.getStringExtra("URI"));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                splitImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

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

        int targetW = imageView.getLayoutParams().width;
        int targetH = imageView.getLayoutParams().height;

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
            exifInterface = new ExifInterface(MainActivity.currentPhotoPath);
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
    static int chunkWidth;
    static int chunkHeight;

    private void splitImage(Bitmap bitmap){
        chunkedImage = new ArrayList<>(numberOfRows * numberOfColumns);
        int yCoord = 0;
        chunkHeight = bitmap.getHeight()/numberOfRows;
        chunkWidth = bitmap.getWidth()/numberOfColumns;
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

    List<Canvas> canvases = new ArrayList<>(20);
    Bitmap resultBitmap;


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                RectF bounds = new RectF();
                Drawable drawable = imageView.getDrawable();
                if (drawable != null) {
                    imageView.getImageMatrix().mapRect(bounds, new RectF(drawable.getBounds()));
                }

                int[] location = new int[2];
                imageView.getLocationInWindow(location);

                Rect imageViewRectangle = new Rect();
                imageViewRectangle.left = location[0] + (int) bounds.left;
                imageViewRectangle.top = location[1] + (int) bounds.top;
                imageViewRectangle.right = location[0] + (int) bounds.right;
                imageViewRectangle.bottom = location[1] + (int) bounds.bottom;


                System.out.println(imageViewRectangle);
                System.out.println(bounds.right);
                System.out.println(event.getRawX() + "\n" + event.getRawY());

                int xCoordinate = (int) event.getRawX();
                int yCoordinate = (int) event.getRawY();

                if (imageViewRectangle.contains(xCoordinate, yCoordinate)) {
                    //PROBLEMS WITH LAST ROW AND COLUMN!!!
                    int row = Math.round((yCoordinate - imageViewRectangle.top) / chunkHeight);
                    System.out.println(yCoordinate);
                    System.out.println(yCoordinate - imageViewRectangle.top);
                    System.out.println(canvases.get(0).getHeight());
                    int column = Math.round((xCoordinate - imageViewRectangle.left) / chunkWidth);
                    System.out.println(column);
                    Context context = getApplicationContext();
                    CharSequence text = ""+row+" "+column;
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return true;
        }
        return false;
    }

}
