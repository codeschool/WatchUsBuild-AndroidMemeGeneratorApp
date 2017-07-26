Android WUB Script
============
Resources to have open:
 * https://developer.android.com/reference/android/content/Intent.html
 * https://developer.android.com/reference/android/support/v4/content/FileProvider.html
 * https://developer.android.com/training/permissions/requesting.html
 * https://developer.android.com/training/sharing/send.html#send-binary-content
 * https://developer.android.com/training/camera/photobasics.html

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

Prevent Rotation
--
Since the image won't fit on the screen in landscape view, we're going to permanently set the orientation to portrait.  If you've used Instagram before you know they also do this.
Add `android:screenOrientation="portrait"` to the `<activity>` element in the manifest.

Choose Picture from Gallery
--
In the MainActivity's Layout:
1. Create a Gallery Button
2. Add an onClick method - `pickImageFromGallery`

In `MainActivity.java`:
1. Create an Intent with ACTION_PICK
2. Get a URI to the picture directory path
3. Set the data and type for the Intent using the URI and "image/*"
4. Call startActivityForResult()
5. Add uses-permission to Manifest
6. **AND** since API 23, we have to explicitly ask for permission

 **URI** is super set of URL that means its a path to file

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

**MainActivity.java**
~~~java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // requestCode is the PHOTO_PICKER constant we created, resultCode if it's successful
        // And data is where our picture should be

        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_PICKER) {
                // Get our picture
                Uri imageUri = data.getData();

                // Declare a stream to read the image data from the SD card
                InputStream inputStream;

                try {
                    // The content resolver provides applications access to persistent data
                    // Then openInputStream() opens a stream on to the content associated with a content URI.
                    inputStream = getContentResolver().openInputStream(imageUri);

                    // Get a bitmap from the stream
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // Show a message to the user indicating that the image is unavailable
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
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
 1. Defining a FileProvider
 2. Specifying Available files
 3. Retrieving the Content Path and URI for a File

1. Defining a FileProvider
--
**FileProvider** is a special subclass of ContentProvider that lets you share files securely.
First, we'll define a FileProvider by adding it to the app's Manifest file.
https://developer.android.com/reference/android/support/v4/content/FileProvider.html
https://developer.android.com/training/secure-file-sharing/setup-sharing.html

The name is the FileProvider under the support library.  The authorities is based on the domain we control, so com.codeschool.memegenerator.fileprovider. Grant Uri permissions is true, to allow you to grant temporary access to files. Exported to false, since the provider doesn't need to be public.  Then to link the file we are going to create next to this FileProvider, add a <meta-data> element as a child of the <provider> element that defines the FileProvider. Set the <meta-data> element's "android:name" attribute to android.support.FILE_PROVIDER_PATHS. Set the element's "android:resource" attribute to @xml/file_paths (notice that you don't specify the .xml extension).
**AndroidManifest.xml**
~~~xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapp">
    <application
        ...>
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.codeschool.memegenerator.fileprovider"
            android:grantUriPermissions="true"
            android:exported="false">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
        ...
    </application>
</manifest>
~~~

2. Specifying Available files
Setting up File Sharing: create the file_paths.xml file.  A FileProvider can only generate a content URI for files in directories that you specify beforehand. To specify a directory, specify the its storage area and path in XML, using child elements of the <paths> element.
The `cache-path` child, represents files in the cache subdirectory of your app's internal storage area. The root path of this subdirectory is the same as the value returned by `getCacheDir()``.
The `name="name"` is a URI path segment separate from the subdirectory path for security.
`path="path"`` is the subdirectory you're sharing.
Create the file:
**res/xml/file_paths.xml**
~~~xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="my_images" path="Android/data/com.codeschool.memegenerator/files/Pictures"/>
    <cache-path name="shared_images" path="images/"/>
</paths>
~~~


3. Retrieving the Content Path and URI for a File
Also need to -- Get the bitmap image with setDrawingCacheEnabled() and getDrawingCache()
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

Launch the Camera:
--
 1. Request camera permission in the manifest
 2. We want to save the photo to external storage - so get permission for external storage
 3. Add an external storage directory to our file_paths.xml file (that we created earlier for sharing our meme).
 4. Create the Intent with MediaStore.ACTION_IMAGE_CAPTURE
 5. Create a file and then a Uri to save the image to
 6. Call startActivityForResult() with our Intent
 7. In onActivityResult() set our ImageView to the Image Uri of our image

  1. Request camera permission in the manifest
~~~xml
<uses-feature android:name="android.hardware.camera"
        android:required="true" />
~~~

  2. We want to save the photo to external storage - so get permission for external storage
 ~~~xml
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 ~~~

  3. Add an external storage directory to our file_paths.xml file (that we created earlier for sharing our meme).
 ~~~xml
 <?xml version="1.0" encoding="utf-8"?>
 <paths xmlns:android="http://schemas.android.com/apk/res/android">
     <external-path name="my_images" path="Android/data/com.codeschool.memegenerator/files/Pictures"/>
     <cache-path name="shared_images" path="images/"/>
 </paths>
 ~~~

  4. Create the Intent with MediaStore.ACTION_IMAGE_CAPTURE
 ~~~java
 static final int REQUEST_TAKE_PHOTO = 1;
    Uri mPhotoURI = null;
    public void launchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mPhotoURI = FileProvider.getUriForFile(this,
                        "com.codeschool.memegenerator.fileprovider",
                        photoFile);
                // MediaStore.EXTRA_OUTPUT, indicates that the Uri passed in is
                // where to store the photo or video
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                // REQUEST_TAKE_PHOTO is a constant we created so we can tell which request
                // we are handling in onActivityResult()
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
 ~~~

  5. Create a file and then a Uri to save the image to
 ~~~java
 private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Use the method File.createTempFile() to create a new empty file in the specified directory
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
~~~

  6. Call startActivityForResult() with our Intent

  7. In onActivityResult() set our ImageView to the Image Uri of our image
~~~java
// Returning the image taken
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the photo - the photo isn't passed in with the intent, it's saved at the Uri
            if (mPhotoURI!=null) {
                imageViewMeme.setImageURI(mPhotoURI);
                imageViewMeme.setAdjustViewBounds(false);
                imageViewMeme.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }
    }
~~~

For Large Camera Photo Sizes - we can set a Max Height on the ImageView
--
Since the image and the buttons won't fit on the screen if the image is too big, let's set a max height on the ImageView.  We can use the property `android:maxHeight="425dp"`.  Also let's constrain the buttons to the bottom of the screen instead of the bottom of the image.
