//
// Created by pallas on 20-11-3.
//
#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <cstring>
#include <assert.h>
#include <opencv2/opencv.hpp>
#include "camera.h"
#include "BitmapUtils.h"


#define TAG "CameraTransfrom"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
static CameraTransformer *mCameraTransformer;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_health_service_face_CameraTransform_CameraTransform(
        JNIEnv *env, jobject instance,
        //jbyteArray image_,
        jobject inputBitmap, jobject outputBitmap,
        jfloat camera_height, jfloat camera_angle, jfloat vision_height, jfloat fL240,
        jint image_height, jint image_width,
        jint targetHeight, jint targetCropWidth,
        jboolean cropCenter, jint vision_pad, jfloatArray mtxInner_, jfloatArray distort_,
        jboolean dynamicVisionHeightFlag, jboolean debug

) {
    jfloat *mtxInnerData = env->GetFloatArrayElements(mtxInner_, NULL);
    jfloat *distortData = env->GetFloatArrayElements(distort_, NULL);
    Mat frameRGBA, frameBGR;
    bitmapToMat(env, inputBitmap, frameRGBA);

    cvtColor(frameRGBA, frameBGR,COLOR_RGBA2BGR);
    //Mat img0 = cv::imread("/sdcard/DCIM/Camera/t1.jpg");
    //Mat img1;
    //cv::resize(img0, img1, Size(320,240));

    if (image_height != frameBGR.rows || image_width != frameBGR.cols){
        Mat frameBGR_;
        frameBGR.copyTo(frameBGR_);
        cv::resize(frameBGR_, frameBGR, Size(image_width, image_height));
    }
    Mat mtxInner = cv::Mat(3,3,CV_32F, mtxInnerData);
    std::vector<float> distort = std::vector<float>(distortData, distortData+5*sizeof(float));
    mCameraTransformer = new CameraTransformer(
            camera_height, camera_angle, vision_height, fL240,
            image_height, image_width, targetHeight, targetCropWidth,
            cropCenter, vision_pad, mtxInner, distort, dynamicVisionHeightFlag, debug);
    Mat alignedBGR = mCameraTransformer->perspectiveTransform2(frameBGR);

    //cv::imwrite("/sdcard/DCIM/Camera/t2_.jpg", frameBGR);
    //cv::imwrite("/sdcard/DCIM/Camera/t2.jpg", alignedBGR);

    Mat alignedRGBA;
    cvtColor(alignedBGR,alignedRGBA,COLOR_BGR2RGBA);
    matToBitmap(env, alignedRGBA, outputBitmap);

    env->ReleaseFloatArrayElements(mtxInner_, mtxInnerData, 0);
    env->ReleaseFloatArrayElements(distort_, distortData, 0);
    return 1;
}

} // end extern "C"

