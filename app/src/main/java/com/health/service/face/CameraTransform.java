package com.health.service.face;

import android.graphics.Bitmap;

public class CameraTransform {

    public native int CameraTransform(
            Bitmap input, Bitmap output,
            float camera_height, float camera_angle, float vision_height, float fL240,
            int image_height, int image_width, int targetHeight, int targetCropWidth,
            boolean cropCenter, int vision_pad, float[] mtxInner_, float[] distort_,
            boolean dynamicVisionHeightFlag, boolean debug);
    static {
        System.loadLibrary("Face");
    }

}

