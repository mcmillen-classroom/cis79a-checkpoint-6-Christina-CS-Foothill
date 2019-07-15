package com.christinahunter.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri mCurrentPhotoPath;
    private ImageView mImageView;
    private Button picButton;
    private Button shareButton;
    private Button emailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view);
        picButton = (Button) findViewById(R.id.camera_button);
        shareButton = (Button) findViewById(R.id.share_button);
        emailButton = (Button) findViewById(R.id.email_button);

        picButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        emailButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.camera_button){

            dispatchTakePictureIntent();
        }
        else if(view.getId() == R.id.share_button){
            dispatchSharePicture();
        }
        else if(view.getId() == R.id.email_button){
            dispatchEmailPicture();
        }


    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //ensure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //create a file where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch (IOException e){
                //error occurred while creating the File
                System.out.println(e);
            }
            //continue only if the File was successfully created
            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    private void dispatchSharePicture()
    {
        File image = new File(mCurrentPhotoPath.getPath());
        Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", image);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        shareIntent.setType("image/jpeg");

        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_pic)));
    }

    private void dispatchEmailPicture()
    {
        File image = new File(mCurrentPhotoPath.getPath());
        Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", image);

        Intent emailIntent = new Intent();
        //must use ACTION_SEND not ACTION_SENDTO, the latter cannot handle attachments
        emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out my pic!");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Taken using my CameraApp.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        //also, set the type to text/plain so attachment works(not sure why, just know
        //that it works when you set the type to this)
        emailIntent.setType("text/plain");

        if (emailIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(emailIntent);
        }
        else
        {
            Toast.makeText(this, "No email app configured.", Toast.LENGTH_LONG).show();
        }
    }



    //this method is automatically called by android following the dispatchTakePictureIntent() method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //galleryAddPic();
            setPic();
        }
    }



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
        mCurrentPhotoPath = Uri.fromFile(image);
        return image;
    }

    private void galleryAddPic() {
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), mCurrentPhotoPath.getPath(), "Foo", "Bar");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.getPath(), bmOptions);
        mImageView.setImageBitmap(bitmap);
    }


}
