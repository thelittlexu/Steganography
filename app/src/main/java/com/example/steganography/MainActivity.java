package com.example.steganography;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btEncode;
    private Button btDecode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btEncode = findViewById(R.id.EncodeButton);
        btDecode = findViewById(R.id.DecodeButton);


        btEncode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                encode();
            }
        });

        btDecode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                decode();
            }
        });
    }
     public void encode() {
        Intent intent = new Intent(this, EncodeActivity.class);
        startActivity(intent);
     }

    public void decode() {
        Intent intent = new Intent(this, DecodeActivity.class);
        startActivity(intent);
    }

}