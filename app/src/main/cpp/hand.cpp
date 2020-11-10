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
Java_com_health_service_face_HandSeg_HandSeg(JNIEnv *env, jobject instance, jbyteArray image_, jint w, jint h, jstring faceDetectionModelPath_){

    jbyte *imageData = env->GetByteArrayElements(image_, NULL);

    Mat frameRGBA = cv::Mat(h, w, CV_8UC4, (uchar*)imageData);
    Mat frameBGR;
    cvtColor(frameRGBA,frameBGR,COLOR_RGBA2BGR);
    Mat frameBGR_;
    cv::resize(frameBGR, frameBGR_, Size(320, 240));
    oHandSeg = new HandSeg();
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    Mat segimg = oHandSeg->segImg(frameBGR_, faceDetectionModelPath);

    Mat alignedRGBA;
    //cvtColor(segimg,alignedRGBA,COLOR_BGR2RGBA);
    cvtColor(segimg,alignedRGBA,COLOR_GRAY2RGBA);
    int len = 320*240*4;//h*w*4;
    jbyteArray array1 = env->NewByteArray (len);
    env->SetByteArrayRegion (array1, 0, len, (jbyte*)alignedRGBA.data);

    cv::imwrite("/sdcard/DCIM/Camera/t3.jpg", segimg);

    return array1;


    }

}