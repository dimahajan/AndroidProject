package com.example.amol.tensrflowimagepic;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    public  String FILE_PATH_KEY = "file_path";
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final int PIC_IMAGE = 100;

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Bitmap bitmap;
    private Classifier classifier;

    private Button b2;
    private Button b1;
    private  File destination;
    private TextView info;

    private TextView textViewResult;
    private ImageView iv;
    private static final int asca = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView)findViewById(R.id.imgView);
        initTensorFlowAndLoadModel();
        b2 = (Button)findViewById(R.id.dispInfo);
        info = (TextView)findViewById(R.id.tv2);
        b1 = (Button)findViewById(R.id.takeImg);
        textViewResult = (TextView) findViewById(R.id.tv);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                destination = new   File(Environment.getExternalStorageDirectory(),"image.jpg");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                startActivityForResult(intent, asca);
            }
        });
        b2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent gal = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gal, PIC_IMAGE);
            }
        });

  }


   public void dispInfo(){
       bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
       final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
       String str = String.valueOf(results);
       str=str.replace("[","");
       str=str.replace("]","");
       str=str.replaceAll(",","\n");
       if(str=="")
           textViewResult.setText("No Any Result Found");
       else
           textViewResult.setText(str);

       try {
               ExifInterface exif = new ExifInterface(FILE_PATH_KEY);
               StringBuilder builder = new StringBuilder();

               builder.append("Date & Time: " + getExifTag(exif,ExifInterface.TAG_DATETIME) + "nn\n");
               builder.append("Flash: " + getExifTag(exif,ExifInterface.TAG_FLASH) + "n\n");
               builder.append("Focal Length: " + getExifTag(exif,ExifInterface.TAG_FOCAL_LENGTH) + "nn\n");
               builder.append("GPS Datestamp: " + getExifTag(exif,ExifInterface.TAG_FLASH) + "n\n");
               builder.append("Image Length: " + getExifTag(exif,ExifInterface.TAG_IMAGE_LENGTH) + "n\n");
               builder.append("Image Width: " + getExifTag(exif,ExifInterface.TAG_IMAGE_WIDTH) + "nn\n");
               builder.append("Camera Make: " + getExifTag(exif,ExifInterface.TAG_MAKE) + "n\n");
               builder.append("Camera Model: " + getExifTag(exif,ExifInterface.TAG_MODEL) + "n\n");
               builder.append("Camera Orientation: " + getExifTag(exif,ExifInterface.TAG_ORIENTATION) + "n\n");
               builder.append("Camera White Balance: " + getExifTag(exif,ExifInterface.TAG_WHITE_BALANCE) + "n\n");

               Toast t = Toast.makeText(MainActivity.this,"hii    -"+builder.toString(),Toast.LENGTH_LONG);
               info.setMovementMethod(new ScrollingMovementMethod());
               info.setText(builder.toString());

               builder = null;
           } catch (IOException e) {
               e.printStackTrace();
           }
   }

    private String getExifTag(ExifInterface exif,String tag){
        String attribute = exif.getAttribute(tag);

        return (null != attribute ? attribute : "");
    }

    protected void onActivityResult(int requestCode, int resultCode , Intent data){
       super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK) {
            if (requestCode == asca) {
                try{
                    FileInputStream in = new FileInputStream(destination);
                    FILE_PATH_KEY = destination.getAbsolutePath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 10; //Downsample 10x
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 200, 250, false);
                    iv.setImageBitmap(bitmap);
                } catch (Exception e)
                { e.printStackTrace();
                }
                dispInfo();
            }
            else if(requestCode == PIC_IMAGE){
                Uri u = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), u);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, 200, 250, false);
                iv.setImageBitmap(bitmap);
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(u,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                FILE_PATH_KEY = cursor.getString(columnIndex);
                cursor.close();
                dispInfo();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("txt1",textViewResult.getText().toString());
        outState.putString("txt2",info.getText().toString());
        outState.putParcelable("img1",bitmap);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        textViewResult.setText(savedInstanceState.getString("txt1"));
        info.setText(savedInstanceState.getString("txt2"));
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