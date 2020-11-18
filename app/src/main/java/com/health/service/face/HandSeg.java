package com.health.service.face;
import android.content.Context;
import android.content.res.AssetManager;

public class HandSeg {
    public HandSeg(Context context, int image_width, int image_height, int numOfThreads){
        init(context.getAssets(), image_width, image_height, numOfThreads);
    }

    public native int detectFinger(byte[] image);

    public native byte[] cropFingerArea(int w, int h);

    public native byte[] debugGetHandSegImage();

    public native byte[] debugGetFingerHeatmap();

    public native int[] getFingerPoint();

    //public native void init(String faceDetectionModelPath);
    public native void init(AssetManager assets, int image_width, int image_height, int numOfThreads);
    static {
        System.loadLibrary("Face");
    }

}
