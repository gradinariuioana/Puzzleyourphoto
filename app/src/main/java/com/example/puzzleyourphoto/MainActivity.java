package com.example.puzzleyourphoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.scwang.wave.MultiWaveHeader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLook();

        //Action buttons
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

        DrawerLayout drawer = findViewById(R.id.activityMainLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initLook(){
        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //WaveHeader
        MultiWaveHeader waveHeader = findViewById(R.id.waveHeader);
        waveHeader.start();
        waveHeader.isRunning();

        //Plus button
        FloatingActionsMenu pls = findViewById(R.id.plusbtn);
        pls.bringToFront();

    }

    static String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_UPLOAD_PHOTO = 2;
    static int requestedCode;
    File photoFile;

    //Take Picture
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
                requestedCode = REQUEST_TAKE_PHOTO;
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //Create file for photo taken
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

    //Upload picture
    public void dispatchUploadPictureIntent(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        requestedCode = REQUEST_UPLOAD_PHOTO;
        startActivityForResult(intent, REQUEST_UPLOAD_PHOTO);
    }

    //Send the info to GameActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            System.out.println(requestedCode);
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("REQUEST_UPLOAD_PHOTO", REQUEST_UPLOAD_PHOTO);
            intent.putExtra("REQUEST_TAKE_PHOTO", REQUEST_TAKE_PHOTO);
            intent.putExtra("REQUEST_CODE", requestedCode);
            if (selectedMode != null)
                intent.putExtra("MODE", selectedMode.getTitle());
            else
                intent.putExtra("MODE", "Classic Mode");
            if (selectedDifficulty != null)
                intent.putExtra("DIFFICULTY", selectedDifficulty.getTitle());
            else
                intent.putExtra("DIFFICULTY", "Easy");
            if (requestCode == REQUEST_TAKE_PHOTO)
                intent.putExtra("CURRENT_PHOTO_PATH", currentPhotoPath);
            if (requestCode == REQUEST_UPLOAD_PHOTO){
                intent.putExtra("URI", data.getData().toString());
            }
            startActivity(intent);
        }
    }

    //Main Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    //Drawer options

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.activityMainLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    MenuItem selectedMode;
    MenuItem selectedDifficulty;

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.getGroupId() == R.id.group_mode) {
            if (selectedMode != null) {
                selectedMode.setChecked(false);
            }
            else{
                NavigationView nv = findViewById(R.id.nav_view);
                Menu menu = nv.getMenu();
                selectedMode = menu.findItem(R.id.cmode);
                selectedMode.setChecked(false);
            }
            selectedMode = item;
            selectedMode.setChecked(true);
        } else if (item.getGroupId() == R.id.group_difficulty) {
            if (selectedDifficulty != null) {
                selectedDifficulty.setChecked(false);
            }
            else{
                NavigationView nv = findViewById(R.id.nav_view);
                Menu menu = nv.getMenu();
                selectedDifficulty = menu.findItem(R.id.ediff);
                selectedDifficulty.setChecked(false);
            }
            selectedDifficulty = item;
            selectedDifficulty.setChecked(true);
        }

        DrawerLayout drawer = findViewById(R.id.activityMainLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
