package com.health.service.face;

public class CameraTransform {

    public native byte[] CameraTransform(
            byte[] image_,
            float camera_height, float camera_angle, float vision_height, float fL240,
            int image_height, int image_width, int targetHeight, int targetCropWidth,
            boolean cropCenter, int vision_pad, float[] mtxInner_, float[] distort_,
            boolean dynamicVisionHeightFlag, boolean debug);
    static {
        System.loadLibrary("Face");
    }

}

