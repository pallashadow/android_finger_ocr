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
    private Bitmap faceImage1 = null,faceImage2 = null;
    TextView textView1,textView2,cmpResult;


    private CameraTransform mCameraTransform = new CameraTransform();
    private HandSeg mHandSeg = new HandSeg();


    private Face mFace = new Face();
    private FaceAlign mFaceAlign = new FaceAlign();
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

        try {
            //copyBigDataToSD("det1.bin");
            //copyBigDataToSD("det2.bin");
            //copyBigDataToSD("det3.bin");
            //copyBigDataToSD("det1.param");
            //copyBigDataToSD("det2.param");
            //copyBigDataToSD("det3.param");
            copyBigDataToSD("recognition.bin");
            copyBigDataToSD("recognition.param");
            copyBigDataToSD("centerface.bin");
            copyBigDataToSD("centerface.param");
            copyBigDataToSD("bisenet.bin");
            copyBigDataToSD("bisenet.param");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //model init
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        String sdPath = sdDir.toString() + "/facem/";
        mFace.FaceModelInit(sdPath);

        //Retrival init
        String retrievalFilePath = "/storage/sdcard0/faceDataset.txt";
        final Retrieval mRetrieval = tryLoadRetrivalDataFromSD(retrievalFilePath);

        //LEFT IMAGE 身份录入流程，包含1.人脸检测 2.人脸矫正裁切 3.人脸身份特征抽取 4.数据录入
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        textView1=(TextView)findViewById(R.id.faceInfo1);
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
                faceImage1=null;
                //人脸检测 detect
                int width = yourSelectedImage1.getWidth();
                int height = yourSelectedImage1.getHeight();
                byte[] imageData = bitmap2byte(yourSelectedImage1); // 图片转换

                long timeDetectFace = System.currentTimeMillis();
                int targetHeight=288;
                int targetWidth=352;
                float[] mtxInner = {285.6716f,0.0f,159.9292f,0.0f,280.29103f,144.53004f,0.0f,0.0f,1.0f};
                float[] distort = {0.054286f,-0.697733f,-0.0098f,-0.00233f,1.5394f};
//                byte[] alignedData = mHandSeg.HandSeg(imageData);
                byte[] alignedData = mCameraTransform.CameraTransform(
                        imageData, 310, 46.0f,400, 283,
                        yourSelectedImage1.getHeight(), yourSelectedImage1.getWidth(),
                        targetHeight, targetWidth, true, 0,
                        mtxInner, distort, true, true);
                timeDetectFace = System.currentTimeMillis() - timeDetectFace;
                //展示矫正后图片
                File sdDir = Environment.getExternalStorageDirectory();//get directory
                String sdPath = sdDir.toString() + "/apks/";
                byte[] segedData = mHandSeg.HandSeg(alignedData, sdPath);

                faceImage1 = byte2bitmap(segedData, 320, 240);
                textView1.setText("pic1 align time:"+timeDetectFace);
                imageView1.setImageBitmap(faceImage1);


            }
        });

        //RIGHT IMAGE 单纯人脸检测和展示
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView2=(TextView)findViewById(R.id.faceInfo2);
        Button buttonImage2 = (Button) findViewById(R.id.select2);
        buttonImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE2);
            }
        });


        Button buttonDetect2 = (Button) findViewById(R.id.detect2);
        buttonDetect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage2 == null)
                    return;
                //detect
                faceImage2=null;
                int width = yourSelectedImage2.getWidth();
                int height = yourSelectedImage2.getHeight();
                byte[] imageData2 = bitmap2byte(yourSelectedImage2);

                long timeDetectFace = System.currentTimeMillis();
                //int[] detectInfo2 = mFace.FaceDetect(imageData2,width,height,4);
                float[] detectInfo2=mFace.CenterFaceDetect(imageData2,width,height,4);
                timeDetectFace = System.currentTimeMillis() - timeDetectFace;

                if(detectInfo2.length>1){
                    int faceNum = (int)detectInfo2[0];
                    Log.i(TAG, "pic width："+width+"height："+height+" face num：" + faceNum );

                    Bitmap drawBitmap = yourSelectedImage2.copy(Bitmap.Config.ARGB_8888, true);
                    for(int i=0;i<detectInfo2[0];i++) {
                        float left, top, right, bottom;
                        Canvas canvas = new Canvas(drawBitmap);
                        Paint paint = new Paint();
                        left = detectInfo2[1+14*i];
                        top = detectInfo2[2+14*i];
                        right = detectInfo2[3+14*i];
                        bottom = detectInfo2[4+14*i];
                        paint.setColor(Color.GREEN);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(5);
                        canvas.drawRect(left, top, right, bottom, paint);
                        for (int j=0;j<5;j++){
                            canvas.drawCircle(detectInfo2[14*i+5+j], detectInfo2[14*i+10+j], 2, paint);
                        }
                    }

                    mFaceAlign.setImage(imageData2, height, width);
                    mFaceAlign.faceAlignLargest(detectInfo2);
                    boolean validFace = mFaceAlign.checkValid4Recognition();
                    int sharpness = Math.round(mFaceAlign.sharpness);
                    int rotud = Math.round(mFaceAlign.orientations[0]);
                    int rotlr = Math.round(mFaceAlign.orientations[1]);
                    String faceValVerb = "\n清晰："+sharpness+"\n上下旋："+rotud+"\n左右旋："+rotlr;
                    if (validFace==false){
                        faceValVerb += "\n人脸不合格";
                    }
                    byte[] faceAlignedData2 = mFaceAlign.getAlignedData();
                    faceImage2 = byte2bitmap(faceAlignedData2, 112, 112);

                    //展示人脸检测结果
                    textView2.setText("pic2 detect time:"+timeDetectFace+faceValVerb);
                    imageView2.setImageBitmap(drawBitmap);
                    //faceImage2 = Bitmap.createBitmap(yourSelectedImage2,faceInfo[1],faceInfo[2],faceInfo[3]-faceInfo[1],faceInfo[4]-faceInfo[2]);
                }else{
                    textView2.setText("no face");
                }

            }
        });

        //人脸识别流程， 1、人脸检测 2、人脸矫正裁切 3.人脸身份特征提取 4.数据查询
        cmpResult=(TextView)findViewById(R.id.textView1);
        Button cmpImage = (Button) findViewById(R.id.facecmp);
        cmpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0){
                if (faceImage2 == null){
                    cmpResult.setText("no enough face,return");
                    return;
                }

                byte[] faceAlignedData2 = bitmap2byte(faceImage2);
                long timeRecognizeFace = System.currentTimeMillis();
                //人脸身份特征提取
                float[] face2Feature128 = mFace.FaceRecognizeFeature(faceAlignedData2, 112, 112);
                //特征查询
                FaceInfo faceInfo1 = mRetrieval.retrievalByFeature128(face2Feature128);//查询特征最相似的条目
                timeRecognizeFace = System.currentTimeMillis() - timeRecognizeFace;
                imageView1.setImageBitmap(faceInfo1.faceImage);
                String recogVerb="cosin:"+faceInfo1.similarity;
                if (faceInfo1.similarity<0.6){
                    recogVerb += " 最优相似度小于阈值，查询失败";
                }else{
                    recogVerb += " 查询成功，姓名："+faceInfo1.name;
                }
                cmpResult.setText(recogVerb+"\ncmp time:"+timeRecognizeFace);
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

    private Retrieval tryLoadRetrivalDataFromSD(String filePath){
        Retrieval mRetrieval = new Retrieval(filePath);
        /* TODO 从SD读取人脸库
        try {
            FileInputStream fi = new FileInputStream(new File(filePath));
            ObjectInputStream oi = new ObjectInputStream(fi);
            // Write objects to file
            mRetrieval = (Retrieval) oi.readObject();
            oi.close();
            fi.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        */
        return mRetrieval;
    }

}
