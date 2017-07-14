Watch Us Build - Creating an Android Meme Generator App
============
The source code for the Watch Us Build video where we build an Android app for generating Memes from scratch.

Create New Project
--
 * App name - Meme Generator
 * Domain - codeschool.com
 * Package - com.codeschool.memegenerator

Create Layout
--
  * 2 EditText TextViews
  * Button - Create meme - onClick --> createMeme()
  * ImageView
  * 2 TextViews on top of ImageView

Create Method - createMeme()
--
Simply set the top and bottom textviews to the text that was typed in.

~~~java

    public void createMeme(View view) {
        // Find the EditText Views and get the values
        EditText editTextTop = (EditText)findViewById(R.id.edit_text_top);
        EditText editTextBottom = (EditText)findViewById(R.id.edit_text_bottom);

        String topText = editTextTop.getText().toString();
        String bottomText = editTextBottom.getText().toString();

        TextView textViewTop = (TextView) findViewById(R.id.text_view_top);
        TextView textViewBottom = (TextView) findViewById(R.id.text_view_bottom);

        textViewTop.setText(topText);
        textViewBottom.setText(bottomText);
    }
~~~
Instead let's put the EditText on the Image?
--

Or another possibility would be to create method tied to the keyUp event …

 1. We’ll move the edit text fields directly on the image and remove the Create Meme button
 2. To get rid of the lines under edit text, make background - color transparent
 3. Change font style, and center text
 4. Remove our Create meme method

 Choose Picture from Gallery
 --
 In the MainActivity's Layout:
  1. Create a Gallery Button
  2. Add an onClick method - `pickImageFromGallery`

 In `MainActivity.java`:
  1. Create an Intent with ACTION_PICK
  2. Get a URI to the picture directory path
  3. Set the datay and type for the Intent using the URI and "image/*"
  4. Call startActivityForResult()
  5. Add uses-permission to Manifest
  6. **AND** since API 23, we have to explicitly ask for permission

 **MainActivity.java**
 ~~~java
 public void pickImageFromGallery(View view) {
         // https://developer.android.com/reference/android/content/Intent.html#ACTION_PICK
         // "Pick an item from data shown, returning what was selected"
         Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

         // Where do we want to find the data/image?
         File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
         String pictureDirectoryPath = pictureDirectory.getPath();

         // Get the URI of the above path
         Uri galleryUri = Uri.parse(pictureDirectoryPath);

         // Set the data and type - we want all image types
         photoPickerIntent.setDataAndType(galleryUri, "image/*");

         // Explicitly ask for permission beginning in API 23
         askPermission();

         // We will invoke this activity, and get the image back in onActivityResult()
         // The 2nd parameter is a request code that we create in case we are returning from multiple
         // Intents.  We'll create a constant for this.
         startActivityForResult(photoPickerIntent, PHOTO_PICKER);
     }
 ~~~

 Explicitly asking for permission - https://developer.android.com/training/permissions/requesting.html

 **MainActivity.java**
 ~~~java
 private void askPermission() {
         int permissionCheck = ContextCompat.checkSelfPermission(this,
                 Manifest.permission.READ_EXTERNAL_STORAGE);

         if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

             // Should we show an explanation?
             if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                     Manifest.permission.READ_EXTERNAL_STORAGE)) {

                 // Show an explanation to the user *asynchronously* -- don't block
                 // this thread waiting for the user's response! After the user
                 // sees the explanation, try again to request the permission.

             } else {

                 // No explanation needed, we can request the permission.

                 ActivityCompat.requestPermissions(this,
                         new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                         MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                 // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                 // app-defined int constant. The callback method gets the
                 // result of the request.
             }
         }
     }
 ~~~

 Adjust EditText to be multi-line
 --
 ~~~xml
 ...
     <EditText
             android:gravity="bottom|left"
             android:inputType="textMultiLine"
             android:lines="2"
             android:maxLines="2"
             android:minLines="2"            
             ... />
 ...
 ~~~


But what can we do with our Meme?  Let’s Create a Share Button
--

  1. Create Share Button
     * Change text to Share
     * Add OnClick method - shareMeme()
  2. Now we want to create a composite image out of the image combined with the two text fields - so we’ll need a way to combine these 3 Views…
     * Let’s wrap them in a ConstraintLayout - or we could use RelativeLayout.

We Need to Create and Share the Image
--
Creating the Image File
--
Setting up File Sharing, create the file_paths.xml file and add file provider to the Manifest
https://developer.android.com/training/secure-file-sharing/setup-sharing.html

Create the file:
**res/xml/file_paths.xml**
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="my_images" path="Android/data/com.codeschool.memegenerator/files/Pictures"/>
    <cache-path name="shared_images" path="images/"/>
</paths>
~~~

**AndroidManifest.xml**
~~~xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapp">
    <application
        ...>
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.example.myapp.fileprovider"
            android:grantUriPermissions="true"
            android:exported="false">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/filepaths" />
        </provider>
        ...
    </application>
</manifest>
~~~

Get the bitmap image with setDrawingCacheEnabled() and getDrawingCache()
Specify the shareable directory
Compress the Bitmap into a FileOutputStream

**MainActivity.java**
~~~java
private Bitmap getCompositeImage() {
        ConstraintLayout constraintLayout = (ConstraintLayout)findViewById(R.id.image_plus_text);

        // To get the Bitmap image, enable drawing cache and then get drawing cache
        constraintLayout.setDrawingCacheEnabled(true);
        Bitmap bitmap = constraintLayout.getDrawingCache();

        if(bitmap == null) return null;

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

        constraintLayout.setDrawingCacheEnabled(false);
        constraintLayout.destroyDrawingCache();

        return bitmap;
    }
~~~

Share the image:
--
Creating the URI for the Image - Uniform Resource Identifier, just like a URL is a web address a URI identifies the location of our image.
Creating the share Intent for an Image (needs a URI)
https://developer.android.com/training/sharing/send.html#send-binary-content

**MainActivity.java**
~~~java
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
~~~
