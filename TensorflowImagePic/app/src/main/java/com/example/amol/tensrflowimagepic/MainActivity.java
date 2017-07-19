package com.example.amol.tensrflowimagepic;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
public class MainActivity extends Activity {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Bitmap bitmap;
    private Classifier classifier;

    private Button b2;
    private Button b1;

    TextView textViewResult;
    private ImageView iv;
    private static final int asca = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView)findViewById(R.id.imgView);
        initTensorFlowAndLoadModel();
        b2 = (Button)findViewById(R.id.dispInfo);
        b1 = (Button)findViewById(R.id.takeImg);
        textViewResult = (TextView) findViewById(R.id.textView);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent callCameraAppIntent = new Intent();
                callCameraAppIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(callCameraAppIntent, asca);
                textViewResult.setText("");
            }
        });
        b2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                String str = String.valueOf(results);
                str=str.replace("[","");
                str=str.replace("]","");
                str=str.replaceAll(",","\n");
                if(str=="")
                    textViewResult.setText("No Any Result Found");
                else
                    textViewResult.setText(str);
            }
        });

  }

    protected void onActivityResult(int requestCode, int resultCode , Intent data){
        if(requestCode == asca && resultCode == RESULT_OK){
            bitmap = (Bitmap) data.getExtras().get("data");
            iv.setImageBitmap(bitmap);
            bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
       }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("txt1",textViewResult.getText().toString());
        outState.putParcelable("img1",bitmap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        textViewResult.setText(savedInstanceState.getString("txt1"));
        bitmap = savedInstanceState.getParcelable("img1");
        iv.setImageBitmap(bitmap);
    }

    private void initTensorFlowAndLoadModel() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
    }
}
