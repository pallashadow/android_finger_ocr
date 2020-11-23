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

/*
JNIEXPORT void JNICALL
Java_com_health_service_face_HandSeg_init(JNIEnv *env, jobject instance, jstring faceDetectionModelPath_){
const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
mHandSeg = new HandSeg(faceDetectionModelPath);
return;
}
*/
JNIEXPORT void JNICALL
Java_com_health_service_face_HandSeg_init(JNIEnv *env, jobject instance, jobject assetManager,
                                          jint image_width, jint image_height, jint numOfThread) {
    mHandSeg = new HandSeg(env, assetManager, image_width, image_height, numOfThread);
    return;
}

JNIEXPORT jint JNICALL
Java_com_health_service_face_HandSeg_detectFinger(JNIEnv *env, jobject instance,
                                                  jbyteArray image_) {
    //bitmapToMat(env, input, imgRGBA);
    jbyte *imageData = env->GetByteArrayElements(image_, NULL);
    int h = mHandSeg->image_height;
    int w = mHandSeg->image_width;

    Mat frameRGBA = cv::Mat(h, w, CV_8UC4, (uchar *) imageData);
    Mat frameBGR;
    cvtColor(frameRGBA, frameBGR, COLOR_RGBA2BGR);

    mHandSeg->segImg(frameBGR);
    int ret = mHandSeg->updateFingerPoint();
    env->ReleaseByteArrayElements(image_, imageData, 0);
    //matToBitmap(env, imgOut, output);
    return ret;
}

JNIEXPORT jbyteArray JNICALL
Java_com_health_service_face_HandSeg_cropFingerArea(JNIEnv *env, jobject instance, jint w, jint h) {
    Mat ocrseg;
    int ret = mHandSeg->cropPointedArea(ocrseg, w, h);
    Mat cropedRGBA;
    cvtColor(ocrseg, cropedRGBA, COLOR_BGR2RGBA);
    int len = h * w * 4;
    jbyteArray array1 = env->NewByteArray(len);
    env->SetByteArrayRegion(array1, 0, len, (jbyte *) cropedRGBA.data);
    return array1;
}

JNIEXPORT jbyteArray JNICALL
Java_com_health_service_face_HandSeg_debugGetHandSegImage(JNIEnv *env, jobject instance){
    int h = mHandSeg->image_height;
    int w = mHandSeg->image_width;
    Mat cropedRGBA;
    cvtColor(mHandSeg->seg8U, cropedRGBA,COLOR_GRAY2RGBA);
    int len = h*w*4;
    jbyteArray array1 = env->NewByteArray (len);
    env->SetByteArrayRegion (array1, 0, len, (jbyte*)cropedRGBA.data);
    return array1;
}

JNIEXPORT jbyteArray JNICALL
Java_com_health_service_face_HandSeg_debugGetFingerHeatmap(JNIEnv *env, jobject instance){
    int h = mHandSeg->image_height;
    int w = mHandSeg->image_width;
    Mat cropedRGBA;
    Mat point8U;
    Mat point32F = mHandSeg->point32F / 0.1 *255.0;
    point32F.convertTo(point8U, CV_8UC1);
    cvtColor(point8U, cropedRGBA,COLOR_GRAY2RGBA);
    int len = h*w*4;
    jbyteArray array1 = env->NewByteArray (len);
    env->SetByteArrayRegion (array1, 0, len, (jbyte*)cropedRGBA.data);
    return array1;
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