package com.health.service.face;
import android.content.Context;
import android.content.res.AssetManager;

public class HandSeg {
    //public HandSeg(String faceDetectionModelPath){
        //init(faceDetectionModelPath);
    private int numOfThreads = 1;

    public HandSeg(Context context){
        init(context.getAssets(), numOfThreads);
    }

    public native byte[] HandSeg(byte[] image_, int w, int h);
    //public native void init(String faceDetectionModelPath);
    public native void init(AssetManager assets, int numOfThreads);
    static {
        System.loadLibrary("Face");
    }

}
