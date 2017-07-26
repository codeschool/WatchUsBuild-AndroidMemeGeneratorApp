package com.codeschool.memegenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int PHOTO_PICKER_REQUEST_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;
    public static final int REQUEST_TAKE_PHOTO_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Make sure there's a Camera Activity to handle the Intent
        // Do this by calling resolveActivity() ,
        // which returns the Activity component that should be used to handle this intent
        // Takes in - PackageManager - application packages that are currently installed on the device
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (mPhotoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO_CODE);
            }
        }
    }

    Uri mPhotoUri = null;
    private void createImageFile() throws IOException {
        mPhotoUri = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mPhotoUri = FileProvider.getUriForFile(this,
                "com.codeschool.memegenerator.fileprovider",
                image);
    }

    public void shareMeme(View view) {
        // First we need to create a composite image and save it to a file
        createCompositeImage();
        
        // Then we can share the image with an Intent
        shareImage();
    }

    private void shareImage() {
        File imagePath = new File(this.getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");
        Uri contentUri = FileProvider.getUriForFile(this, "com.codeschool.memegenerator.fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }
    }

    private void createCompositeImage() {
        // Crete the image
        View view = findViewById(R.id.image_frame_layout);
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();

        // Now we need to save it to a file
        // save bitmap to cache directory
        try {
            // Get the cache directory, and the images subdirectory
            File cachePath = new File(this.getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
            // 100 is 100% quality
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
    }

    public void pickImageFromGallery(View view) {
        // Pick an item from the data, returning what was selected
        Intent intent = new Intent(Intent.ACTION_PICK);

        // Now we want to create a URI of the picture directory path
        // First, get the path
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // Then get the path as a String
        String pictureDirectoryPath = pictureDirectory.getPath();
        // Create Uri
        Uri galleryUri = Uri.parse(pictureDirectoryPath);

        // Now we can set the data and type of the Intent with the Uri
        intent.setDataAndType(galleryUri, "image/*");

        askPermission();

        // Start the Intent, get the result in onActivityResult()
        startActivityForResult(intent, PHOTO_PICKER_REQUEST_CODE);
    }

    private void askPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // First we'll check that our result code is RESULT_OK (standard Activity result),
        // and the request code matches the photo picker code
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_PICKER_REQUEST_CODE) {
                // Now we can get the Uri from the passed in data
                Uri uri = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // Show an error message to user
                    Toast.makeText(this, "Unable to read image", Toast.LENGTH_LONG);
                }
            }
            else if (requestCode == REQUEST_TAKE_PHOTO_CODE) {
                if (mPhotoUri != null) {
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageURI(mPhotoUri);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        }
    }
}
