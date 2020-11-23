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
    private Bitmap alignedImage = null,segedImage = null, croppedImage = null;
    TextView textView1,textView2,textView3;

    int fingerCropWidth = 200;
    int fingerCropHeight = 64;
    float fingerCropScale = 2.0f;
    int fingerCropShiftY = 20;
    int ocrWidth = (int)(fingerCropWidth*fingerCropScale);
    int ocrHeight = (int)(fingerCropHeight*fingerCropScale);


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
                ActivityCompat.requestPermissions(
                        activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
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
        final int alignHeight=288;
        final int alignWidth=352;

        mHandSeg = new HandSeg(this, alignWidth, alignHeight, 1);
        mOcrEngine = new OcrEngine(this);

        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView1=(TextView)findViewById(R.id.faceInfo1);
        textView2=(TextView)findViewById(R.id.faceInfo2);
        textView3=(TextView)findViewById(R.id.textView1);
        Button buttonImage1 = (Button) findViewById(R.id.select1);
        buttonImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE1);
            }
        });

        Button buttonDetect1 = (Button) findViewById(R.id.detect1);
        buttonDetect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage1 == null)
                    return;

                long timeAlign = System.currentTimeMillis();
                //float[] mtxInner = {285.6716f,0.0f,159.9292f,0.0f,280.291f,144.53f,0.0f,0.0f,1.0f};
                float[] mtxInner = {570.0f ,0.0f,320.0f,0.0f,560.0f,289.9f,0.0f,0.0f,1.0f};
                float[] distort = {0.054286f,-0.697733f,-0.0098f,-0.00233f,1.5394f};
                alignedImage = Bitmap.createBitmap(alignWidth, alignHeight, Bitmap.Config.ARGB_8888);
                mCameraTransform.CameraTransform(
                        yourSelectedImage1, alignedImage,
                        310, 46.0f,400, 283,
                        //240, 320,
                        480, 640,
                        alignHeight, alignWidth, true, 0,
                        mtxInner, distort, true, true);
                timeAlign = System.currentTimeMillis() - timeAlign;
                textView1.setText("pic1 align time:"+timeAlign);
                imageView1.setImageBitmap(alignedImage);

                //展示矫正后图片
                long timeSegHand = System.currentTimeMillis();
                int det1 = mHandSeg.detectFinger(alignedImage);

                if (det1==1){
                    croppedImage = Bitmap.createBitmap(
                            ocrWidth, ocrHeight,
                            Bitmap.Config.ARGB_8888);
                    mHandSeg.cropFingerArea(croppedImage, fingerCropWidth, fingerCropHeight,
                            fingerCropScale, fingerCropShiftY);
                    imageView2.setImageBitmap(croppedImage);
                    timeSegHand = System.currentTimeMillis() - timeSegHand;
                    textView2.setText("handseg time:"+timeSegHand);

                }else{
                    textView2.setText("no finger detected");
                }

            }
        });

        Button buttonDetect2 = (Button) findViewById(R.id.detect2);
        buttonDetect2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 segedImage = Bitmap.createBitmap(alignWidth, alignHeight, Bitmap.Config.ARGB_8888);
                 mHandSeg.debugGetHandSegImage(segedImage);
                 imageView1.setImageBitmap(segedImage);
             }
         });

        Button buttonDetect3 = (Button) findViewById(R.id.detect3);
        buttonDetect3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (croppedImage == null)
                    return;
                Bitmap boxImage = Bitmap.createBitmap(
                        ocrWidth, ocrHeight, Bitmap.Config.ARGB_8888);
                long timeOcr = System.currentTimeMillis();
                OcrResult ocrResult = mOcrEngine.detect(croppedImage, boxImage, 320);
                timeOcr = System.currentTimeMillis() - timeOcr;
                textView2.setText("OCR time:"+timeOcr);
                imageView2.setImageBitmap(ocrResult.getBoxImg());
                textView3.setText(ocrResult.getStrRes());
            }
        });

        Button buttonDetect4 = (Button) findViewById(R.id.detect4);
        buttonDetect4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                imageView1.setImageDrawable(null);
                imageView2.setImageDrawable(null);
                textView1.setText("");
                textView2.setText("");
                textView3.setText("");
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
        BitmapFactory.decodeStream(
                getContentResolver().openInputStream(selectedImage), null, o);

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
        return BitmapFactory.decodeStream(
                getContentResolver().openInputStream(selectedImage), null, o2);
    }


}
