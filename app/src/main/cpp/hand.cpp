#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <cstring>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include "hand.h"

static HandSeg *mHandSeg;


extern "C" {

JNIEXPORT void JNICALL
Java_com_health_service_face_HandSeg_init(JNIEnv *env, jobject instance, jobject assetManager,
                                          jint image_width, jint image_height, jint numOfThread) {
    mHandSeg = new HandSeg(env, assetManager, image_width, image_height, numOfThread);
    return;
}

JNIEXPORT jint JNICALL
Java_com_health_service_face_HandSeg_detectFinger(JNIEnv *env, jobject instance, jobject inputBitmap) {
    Mat frameRGBA, frameBGR;
    bitmapToMat(env, inputBitmap, frameRGBA);
    cvtColor(frameRGBA, frameBGR, COLOR_RGBA2BGR);

    mHandSeg->segImg(frameBGR);
    int ret = mHandSeg->updateFingerPoint();
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_health_service_face_HandSeg_cropFingerArea(JNIEnv *env, jobject instance,
        jobject outputBitmap, jint w, jint h, jfloat scale, jint shiftY) {
    Mat ocrseg, cropedRGBA;
    int ret = mHandSeg->cropPointedArea(ocrseg, w, h, scale, shiftY);
    cvtColor(ocrseg, cropedRGBA, COLOR_BGR2RGBA);
    matToBitmap(env, cropedRGBA, outputBitmap);
    return 1;
}

JNIEXPORT jint JNICALL
Java_com_health_service_face_HandSeg_debugGetHandSegImage(JNIEnv *env, jobject instance,
        jobject outputBitmap){
    Mat cropedRGBA;
    cvtColor(mHandSeg->seg8U, cropedRGBA,COLOR_GRAY2RGBA);
    matToBitmap(env, cropedRGBA, outputBitmap);
    return 1;
}

JNIEXPORT jint JNICALL
Java_com_health_service_face_HandSeg_debugGetFingerHeatmap(JNIEnv *env, jobject instance, jobject outputBitmap){
    Mat cropedRGBA;
    Mat point8U;
    Mat point32F = mHandSeg->point32F / 0.1 *255.0;
    point32F.convertTo(point8U, CV_8UC1);
    cvtColor(point8U, cropedRGBA,COLOR_GRAY2RGBA);
    matToBitmap(env, cropedRGBA, outputBitmap);
    return 1;
}

JNIEXPORT jintArray JNICALL
Java_com_health_service_face_HandSeg_getFingerPoint(JNIEnv *env, jobject instance){
    int len = 2*sizeof(int);
    jintArray point = env->NewIntArray (len);
    int x = mHandSeg->fingerPoint.x;
    int y = mHandSeg->fingerPoint.y;
    int point_[2] = {x,y};
    env->SetIntArrayRegion (point, 0, len, (jint*)point_);
    return point;
}

}