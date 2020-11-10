package com.health.service.face;
import android.content.res.AssetManager;

public class HandSeg {

    public native byte[] HandSeg(byte[] image_, int w, int h, String faceDetectionModelPath);
    static {
        System.loadLibrary("Face");
    }

}
