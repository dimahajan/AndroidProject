package com.example.amol.picapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.IOException

class MainActivity:AppCompatActivity() {
    private var classifier: Classifier? = null
    var FILE_PATH_KEY = "file_path"
    private val INPUT_SIZE = 224
    private val IMAGE_MEAN = 117
    private val IMAGE_STD = 1f
    private val INPUT_NAME = "input"
    private val OUTPUT_NAME = "output"
    private val MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb"
    private val LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt"


    private var b1: Button? = null
    private var b2: Button? = null
    private var iv: ImageView? = null
    private var imgLoc : String? = null
    private var bitmap: Bitmap? = null
    private var textViewResult : TextView? = null
    private var info : TextView? = null


    protected override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTensorFlowAndLoadModel()
        b1 = findViewById(R.id.takeImg) as Button
        b2 = findViewById(R.id.dispInfo) as Button
        iv = findViewById(R.id.imgView) as ImageView
        info = findViewById(R.id.tv2) as TextView
        textViewResult = findViewById(R.id.tv) as TextView

        b1!!.setOnClickListener ( View.OnClickListener() {
            var takePic: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var stor: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            var img: File = File.createTempFile("img", ".jpg", stor)
            imgLoc = img.absolutePath
            takePic.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img))
            startActivityForResult(takePic, 1)
        })
        b2!!.setOnClickListener ( View.OnClickListener() {
            val gal = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gal, 2)
        })
    }

    fun dispInfo() {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        val results = classifier!!.recognizeImage(bitmap)
        var str = results.toString()
        str = str.replace("[", "")
        str = str.replace("]", "")
        str = str.replace(",".toRegex(), "\n")
        if (str === "")
            textViewResult!!.setText("No Any Result Found")
        else
            textViewResult!!.setText(str)
        try {
            val exif = ExifInterface(imgLoc)
            var builder: StringBuilder? = StringBuilder()

            builder!!.append("Date & Time: " + getExifTag(exif, ExifInterface.TAG_DATETIME) + "\n")
            builder.append("Flash: " + getExifTag(exif, ExifInterface.TAG_FLASH) + "\n")
            builder.append("Focal Length: " + getExifTag(exif, ExifInterface.TAG_FOCAL_LENGTH) + "\n")
            builder.append("GPS Datestamp: " + getExifTag(exif, ExifInterface.TAG_FLASH) + "n\n")
            builder.append("Image Length: " + getExifTag(exif, ExifInterface.TAG_IMAGE_LENGTH) + "\n")
            builder.append("Image Width: " + getExifTag(exif, ExifInterface.TAG_IMAGE_WIDTH) + "\n")
            builder.append("Camera Make: " + getExifTag(exif, ExifInterface.TAG_MAKE) + "\n")
            builder.append("Camera Model: " + getExifTag(exif, ExifInterface.TAG_MODEL) + "\n")
            builder.append("Camera Orientation: " + getExifTag(exif, ExifInterface.TAG_ORIENTATION) + "\n")
            builder.append("Camera White Balance: " + getExifTag(exif, ExifInterface.TAG_WHITE_BALANCE) + "\n")

            info!!.setMovementMethod(ScrollingMovementMethod())
            info!!.setText(builder.toString())

            builder = null
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getExifTag(exif: ExifInterface, tag: String): String {
        val attribute = exif.getAttribute(tag)

        return attribute ?: ""
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("txt1", textViewResult!!.getText().toString())
        outState.putString("txt2", info!!.getText().toString())
        outState.putParcelable("img1", bitmap)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        textViewResult!!.setText(savedInstanceState.getString("txt1"))
        info!!.setText(savedInstanceState.getString("txt2"))
        bitmap = savedInstanceState.getParcelable<Bitmap>("img1")
        iv!!.setImageBitmap(bitmap)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1){
                bitmap = BitmapFactory.decodeFile(imgLoc)
                bitmap = Bitmap.createScaledBitmap(bitmap, 200,250, false)
                iv!!.setImageBitmap(bitmap)
                dispInfo()
            } else if (requestCode == 2) {
                val u = data!!.getData()
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, u)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                bitmap = Bitmap.createScaledBitmap(bitmap, 200, 250, false)
                iv!!.setImageBitmap(bitmap)
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(u, filePathColumn, null, null, null)
                cursor!!.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                imgLoc = cursor.getString(columnIndex)
                cursor.close()
                dispInfo()
            }
        }
    }


    private fun initTensorFlowAndLoadModel() {
        try {
            classifier = TensorFlowImageClassifier.create(
                    assets,
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE,
                    IMAGE_MEAN,
                    IMAGE_STD,
                    INPUT_NAME,
                    OUTPUT_NAME)
        } catch (e: Exception) {
            throw RuntimeException("Error initializing TensorFlow!", e)
        }

    }
}
