package com.example.sky.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private ImageView iv;
    private static final  int asca = 100;
    private Button b2,b1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView)findViewById(R.id.iv);
        b2 = (Button)findViewById(R.id.button);
        b1 = (Button)findViewById(R.id.button1);

        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent callCameraAppIntent = new Intent();
                callCameraAppIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(callCameraAppIntent, 1);
            }
        });
        b2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent gal = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(gal, 100);

                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode , Intent data){
        if(resultCode == RESULT_OK) {
            if (requestCode == asca ) {
                Uri u = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), u);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iv.setImageBitmap(bitmap);
            } else if (requestCode == 1  ) {
                bitmap = (Bitmap) data.getExtras().get("data");
                bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
                iv.setImageBitmap(bitmap);
            }
        }
    }
}
