package com.example.steganography;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

// extractText, isEncoded andd get EncodedLength methods adopted from https://github.com/maniksingh92-xx/simple-steganography

public class DecodeActivity extends AppCompatActivity {

    Button upload;
    Button decode;
    ImageView img;
    TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        upload = findViewById(R.id.uploadEncryptedImage);
        decode = findViewById(R.id.dButton);
        img = findViewById(R.id.uploadedImg);
        msg = findViewById(R.id.decodedMessage);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        decode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                if (isEncoded(bitmap) == false) {
                    msg.setText("");
                    Toast.makeText(getApplicationContext(),"No message detected.",Toast.LENGTH_LONG).show();
                } else {
                    msg.setText(extractText(bitmap));
                }

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
                Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                img.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    String extractText(Bitmap image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int msgLength = getEncodedLength(image);

        StringBuffer decodedMsg = new StringBuffer();
        Deque<Integer> listChar = new ArrayDeque<Integer>();

        int pixel, temp, charOut, ignore = 0, count = 0;
        loop: for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(ignore < 36 + 4*(String.valueOf(msgLength).length()+1)) {
                    ignore++;
                    continue;
                }

                if(count++ == 4*msgLength) {
                    break loop;
                }
                pixel = image.getPixel(i, j); //grab RGB value at specified pixel
                temp = pixel & 0x03; //extract 2 LSB from encoded data

                listChar.add(temp); //add the extracted data to a queue for later processing

                if(listChar.size() >=4) {
                    //once we have 8 bits of data extracted combine them to create a byte, and store this byte as a character
                    charOut = (listChar.pop() << 6) | (listChar.pop() << 4) | (listChar.pop() << 2) | listChar.pop() ;
                    decodedMsg.append((char)charOut);
                }
            }

        }

        String outputMsg = new String(decodedMsg); //generate extracted message
        System.out.println(outputMsg);
        return outputMsg;
    }

    boolean isEncoded(Bitmap image) { //Check for "!encoded!" at starting

        StringBuffer decodedMsg = new StringBuffer();
        Deque<Integer> listChar = new ArrayDeque<Integer>();
        int width = image.getWidth();
        int height = image.getHeight();

        int pixel, temp, charOut, count = 0;
        loop: for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++, count++) {

                if(count == 45) { //remain in loop till first 9 characters are extracted
                    break loop;
                }
                pixel = image.getPixel(i, j); //grab RGB value at specified pixel
                temp = pixel & 0x03; //extract 2 LSB from encoded data

                listChar.add(temp); //add the extracted data to a queue for later processing

                if(listChar.size() >=4) { //once we have 8 bits of data extracted
                    //combine them to create a byte, and store this byte as a character
                    charOut = (listChar.pop() << 6) | (listChar.pop() << 4) | (listChar.pop() << 2) | listChar.pop() ;
                    decodedMsg.append((char)charOut); //else add character to a StringBuffer
                    count++;
                }
            }
        }

        String check = new String(decodedMsg);
        if (check.compareTo("!encoded!") == 0) {
            //System.out.println("true");
            return true;
        } else {
            //System.out.println("false");
            return false;
        }

    }

    int getEncodedLength(Bitmap image) { //method to get actual length of message encoded

        StringBuffer decodedMsg = new StringBuffer();
        Deque<Integer> listChar = new ArrayDeque<Integer>();
        int width = image.getWidth();
        int height = image.getHeight();

        int pixel, temp, charOut, count = 0;
        loop: for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(count < 36) { //ignore the 36 bits or 9 bytes, equal to "!encoded!"
                    count++;
                    continue;
                }

                pixel = image.getPixel(i, j); //grab RGB value at specified pixel
                temp = pixel & 0x03; //extract 2 LSB from encoded data

                listChar.add(temp); //add the extracted data to a queue for later processing

                if(listChar.size() >=4) { //once we have 8 bits of data extracted
                    //combine them to create a byte, and store this byte as a character
                    charOut = (listChar.pop() << 6) | (listChar.pop() << 4) | (listChar.pop() << 2) | listChar.pop() ;
                    if((char)charOut == '!') { //terminate process if character extracted is '!'
                        break loop;
                    } else {
                        decodedMsg.append((char)charOut); //else add character to a StringBuffer
                    }
                }
            }

        }

        String length = new String(decodedMsg);
        System.out.println("length is " + Integer.parseInt(length));
        return Integer.parseInt(length);
    }
}