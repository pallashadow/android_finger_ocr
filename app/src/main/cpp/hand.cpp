#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <cstring>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include "hand.h"

#define TAG "DetectSo"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
static HandSeg *oHandSeg;


extern "C"{
JNIEXPORT jbyteArray JNICALL
Java_com_health_service_face_HandSeg_HandSeg(JNIEnv *env, jobject instance, jbyteArray image_, jstring faceDetectionModelPath_){

    jbyte *imageData = env->GetByteArrayElements(image_, NULL);

    Mat frameRGBA = cv::Mat(240, 320, CV_8UC4, (uchar*)imageData);
    Mat frameBGR;
    cvtColor(frameRGBA,frameBGR,COLOR_RGBA2BGR);
    oHandSeg = new HandSeg();
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    Mat segimg = oHandSeg->segImg(frameBGR, faceDetectionModelPath);

    Mat alignedRGBA;
    cvtColor(segimg,alignedRGBA,COLOR_BGR2RGBA);
    int len = alignedRGBA.rows*alignedRGBA.cols*4;
    jbyteArray array1 = env->NewByteArray (len);
    env->SetByteArrayRegion (array1, 0, len, (jbyte*)alignedRGBA.data);
    return array1;


    }

}