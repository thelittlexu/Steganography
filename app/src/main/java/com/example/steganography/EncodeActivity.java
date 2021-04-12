package com.example.steganography;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

// embedText method adopted from https://github.com/maniksingh92-xx/simple-steganography

public class EncodeActivity extends AppCompatActivity {
    ImageView img;
    Button upload;
    Button encode;
    EditText msg;
    Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        upload = findViewById(R.id.uploadImage);
        img = findViewById(R.id.uploadedImg);
        encode = findViewById(R.id.eButton);
        msg = findViewById(R.id.message);
        download = findViewById(R.id.btDownload);
        download.setVisibility(View.INVISIBLE);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis());
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                OutputStream out;
                try {
                    out = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    Toast.makeText(getApplicationContext(),"Image Downloaded",Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        encode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = msg.getText().toString();
                System.out.println(text);
                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                embedText(bitmap, text);
            }
        });
    }


    private void selectImage() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                img.setImageBitmap(bitmap);
                download.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    // embed secret information/TEXT into a "cover image"
    private void embedText(Bitmap image, String text) {

        Bitmap copy = image.copy(Bitmap.Config.ARGB_8888,true);

        int width = image.getWidth();
        int height = image.getHeight();

        int msgLength = text.length();

        //add overhead "!encoded!" to identify start of encoded message,
        //along with original message length which will instruct when to stop
        //the decoding process, once the encoded message has been extracted
        String message = "!encoded!" + msgLength + "!" + text;
        msgLength = message.length(); //length including the overhead

        //Array that holds the entire message in the form of bits
        //Each byte here is divided into 4 parts of 2 bits
        // Thus, the size of twoBitMessage is 4 times the length of the actual message.
        int[] twoBitMessage = new int[4 * msgLength];

        char currentChar;
        for(int i =0; i < msgLength ; i++) {
            currentChar = message.charAt(i); // extracting character at position i from string
            twoBitMessage[4*i + 0] = (currentChar >> 6) & 0x3; //storing 1st and 2nd bit
            twoBitMessage[4*i + 1] = (currentChar >> 4) & 0x3; //storing 3rd and 4th bit
            twoBitMessage[4*i + 2] = (currentChar >> 2) & 0x3; //storing 5th and 6th bit
            twoBitMessage[4*i + 3] = (currentChar)      & 0x3; //storing 7th and 8th bit
        }

        int pixel, pixOut, count = 0;;
        loop: for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(count < 4*msgLength) { //ensuring that loop only iterates till the entire message has been encoded
                    pixel = image.getPixel(i, j); //Grab the RGB value from the pixel of the source image at position (i,j)

                    //Bit operator AND to convert the two LSB to zero.
                    //Bit operator OR to copy the two bit message in place of these two LSB.
                    //Effectively, 4 pixels, carrying 8 bits of encoded bits will carry the information of one character.
                    pixOut = (pixel & 0xFFFFFFFC) | twoBitMessage[count++];

                    copy.setPixel(i, j, pixOut); //Set the modified RGB value at given pixel.

                } else {
                    break loop;
                }
            }

        }
        img.setImageBitmap(copy);
        img.setImageBitmap(copy);
        Toast.makeText(getApplicationContext(),"Message encoded successfully!",Toast.LENGTH_LONG).show();
    }
}