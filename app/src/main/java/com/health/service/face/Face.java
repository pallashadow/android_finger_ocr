package com.health.service.face;

/**
 * Created by L on 2018/6/11.
 */

public class Face {

    public float FaceRecognize(byte[] faceDate1,int w1,int h1,byte[] faceDate2,int w2,int h2){
        float[] feature1 = FaceRecognizeFeature(faceDate1, w1, h1);
        float[] feature2 = FaceRecognizeFeature(faceDate2, w1, h1);
        float sim = calcSimilar(feature1, feature2);
        return sim;
    }

    private static float calcSimilar(float[] feature1, float[] feature2) {
        assert(feature1.length == feature2.length);
        float ret = 0;
        for (int i=0;i<128;i++){
            ret += feature1[i] * feature2[i];
        }
        return ret;
    }

    public native boolean FaceModelInit(String faceDetectionModelPath);

    //public native float[] FaceDetect(byte[] imageDate, int imageWidth , int imageHeight, int imageChannel);

    public native float[] CenterFaceDetect(byte[] imageDate, int imageWidth , int imageHeight, int imageChannel);

    //public native boolean FaceModelUnInit();

    public native float[] FaceRecognizeFeature(byte[] faceDate1,int w1,int h1);

    static {
        System.loadLibrary("Face");

    }
}
