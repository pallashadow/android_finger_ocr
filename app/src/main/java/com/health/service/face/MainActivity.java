package com.health.service.face;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//import com.example.l.mobilefacenet.R;
import com.benjaminwan.ocrlibrary.OcrEngine;
import com.benjaminwan.ocrlibrary.OcrResult;
import com.health.service.face.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE1 = 1,SELECT_IMAGE2 = 2;
    private ImageView imageView1,imageView2;
    private Bitmap yourSelectedImage1 = null,yourSelectedImage2 = null;
    private Bitmap alignedImage = null,segedImage = null;
    TextView textView1,textView2,cmpResult;


    private CameraTransform mCameraTransform = new CameraTransform();
    private HandSeg mHandSeg = null;
    private OcrEngine mOcrEngine = null;


    //private Face mFace = new Face();
    //private FaceAlign mFaceAlign = new FaceAlign();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        final File sdDir = Environment.getExternalStorageDirectory();//get directory
        final String sdPath = sdDir.toString() + "/apks/";
        mHandSeg = new HandSeg(this);
        //mOcrEngine = new OcrEngine(getResources().getAssets());
        mOcrEngine = new OcrEngine(this);
        /*
        try {
            copyBigDataToSD("bisenet.bin");
            copyBigDataToSD("bisenet.param");
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        //LEFT IMAGE 身份录入流程，包含1.人脸检测 2.人脸矫正裁切 3.人脸身份特征抽取 4.数据录入
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView1=(TextView)findViewById(R.id.faceInfo1);
        textView2=(TextView)findViewById(R.id.faceInfo2);
        Button buttonImage1 = (Button) findViewById(R.id.select1);
        buttonImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE1);
            }
        });
        final int targetHeight=288;
        final int targetWidth=352;

        Button buttonDetect1 = (Button) findViewById(R.id.detect1);
        buttonDetect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage1 == null)
                    return;
                int width = yourSelectedImage1.getWidth();
                int height = yourSelectedImage1.getHeight();
                byte[] imageData = bitmap2byte(yourSelectedImage1); // 图片转换

                long timeAlign = System.currentTimeMillis();
                float[] mtxInner = {285.6716f,0.0f,159.9292f,0.0f,280.29103f,144.53004f,0.0f,0.0f,1.0f};
                float[] distort = {0.054286f,-0.697733f,-0.0098f,-0.00233f,1.5394f};
//                byte[] alignedData = mHandSeg.HandSeg(imageData);
                byte[] alignedData = mCameraTransform.CameraTransform(
                        imageData, 310, 46.0f,400, 283,
                        yourSelectedImage1.getHeight(), yourSelectedImage1.getWidth(),
                        targetHeight, targetWidth, true, 0,
                        mtxInner, distort, true, true);
                timeAlign = System.currentTimeMillis() - timeAlign;
                alignedImage = byte2bitmap(alignedData, targetWidth, targetHeight);
                textView1.setText("pic1 align time:"+timeAlign);
                imageView1.setImageBitmap(alignedImage);

                //展示矫正后图片
                long timeSegHand = System.currentTimeMillis();
                byte[] segedData = mHandSeg.HandSeg(alignedData, targetWidth, targetHeight);
                timeSegHand = System.currentTimeMillis() - timeSegHand;

                segedImage = byte2bitmap(segedData, targetWidth, targetHeight);
                //segedImage = byte2bitmap(segedData, 320, 240);
                textView2.setText("pic1 align time:"+timeSegHand);
                imageView2.setImageBitmap(segedImage);

            }
        });

        Button buttonDetect2 = (Button) findViewById(R.id.detect2);
        buttonDetect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (alignedImage == null)
                    return;
                Bitmap boxImage = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                long timeOcr = System.currentTimeMillis();
                OcrResult ocrResult = mOcrEngine.detect(alignedImage, boxImage, 320);
                timeOcr = System.currentTimeMillis() - timeOcr;
                textView2.setText("OCR time:"+timeOcr);
                imageView2.setImageBitmap(ocrResult.getBoxImg());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                if (requestCode == SELECT_IMAGE1) {
                    Bitmap bitmap = decodeUri(selectedImage);
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yourSelectedImage1 = rgba;
                    imageView1.setImageBitmap(yourSelectedImage1);
                }
                else if (requestCode == SELECT_IMAGE2) {
                    Bitmap bitmap = decodeUri(selectedImage);
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yourSelectedImage2 = rgba;
                    imageView2.setImageBitmap(yourSelectedImage2);
                }
            } catch (FileNotFoundException e) {
                Log.e("MainActivity", "FileNotFoundException");
                return;
            }
        }
    }


    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    //get pixels
    private static byte[] bitmap2byte(final Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bytes = byteBuffer.array();
        return bytes;
    }

    private static Bitmap byte2bitmap(final byte[] data, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        File file = new File(sdDir.toString()+"/facem/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString()+"/facem/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString()+"/facem/"+ strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "end copy file " + strOutFileName);

    }

}
