package com.health.service.face;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class HandSeg {
    public HandSeg(Context context, int image_width, int image_height, int numOfThreads){
        init(context.getAssets(), image_width, image_height, numOfThreads);
    }

    public native int detectFinger(Bitmap image);

    public native int cropFingerArea(Bitmap croppedImage, int w, int h, float scale, int shiftY);

    public native int debugGetHandSegImage(Bitmap handSegImage);

    public native int debugGetFingerHeatmap(Bitmap fingerHeatmap);

    public native int[] getFingerPoint();

    public native void init(AssetManager assets, int image_width, int image_height, int numOfThreads);
    static {
        System.loadLibrary("Face");
    }

}
