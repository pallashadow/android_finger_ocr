package com.health.service.face;
import android.content.res.AssetManager;

public class HandSeg {

    public native byte[] HandSeg(byte[] image_, String faceDetectionModelPath);
    public native boolean HandModelInit();
    static {
        System.loadLibrary("Face");
    }

}
