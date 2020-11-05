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

    Mat frameRGBA = cv::Mat(320, 240, CV_8UC4, (uchar*)imageData);
    Mat frameBGR;
    cvtColor(frameRGBA,frameBGR,CV_RGBA2BGR);
    oHandSeg = new HandSeg();
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    __android_log_print(ANDROID_LOG_INFO, "lclclc", "segggggggggggggggggggggggggggggggggggg");
    Mat segimg = oHandSeg->segImg(frameBGR, faceDetectionModelPath);


 //   int len = segimg.rows*segimg.cols*4;
//    jbyteArray array1 = env->NewByteArray (len);
//    env->SetByteArrayRegion (array1, 0, len, (jbyte*)segimg.data);

 //   return array1;


    }

}