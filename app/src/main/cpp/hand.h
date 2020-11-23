#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
//#include <math.h>
#include "ncnn/net.h"
#include <cmath>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "BitmapUtils.h"

#define TAG "hand.h"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
using namespace std;
using namespace cv;


class HandSeg{

private:
    ncnn::Net Bisenet;

public:
    cv::Mat bgr;
    cv::Mat seg8U;
    cv::Mat point32F;
    int image_height, image_width;
    cv::Point fingerPoint = cv::Point(-1,-1);
    int threadnum = 1;
    float seg_thr = -1.0f;
    float heat_thr = 0.04f;

    ~HandSeg(){}
    //HandSeg(const std::string &model_path){
    HandSeg(JNIEnv *env, jobject assetManager, int image_width_, int image_height_, int numOfThread):
            image_height(image_height_), image_width(image_width_), threadnum(numOfThread)
    {
        threadnum = numOfThread;
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        if (mgr == NULL) {
            LOGE("AAssetManager==NULL");
        }
        int ret1 = Bisenet.load_param(mgr, "handseg.param");
        int ret2 = Bisenet.load_model(mgr, "handseg.bin");
        if (ret1!=0 || ret2!=0){
            LOGE("模型加载失败bisenet");
        }else{
            LOGI("模型加载成功bisenet");
        }
        seg8U.create(cv::Size(image_width,image_height), CV_8UC1);
        point32F.create(cv::Size(image_width,image_height), CV_32FC1);
    }


    int segImg(const cv::Mat& input)
    {
        bgr = input;
        int w = image_width;
        int h = image_height;
        assert(w == bgr.cols && h == bgr.rows);

        ncnn::Mat in = ncnn::Mat::from_pixels(bgr.data, ncnn::Mat::PIXEL_BGR2RGB, w, h);
        const float mean_vals[3] = {128.f, 128.f, 128.f};
        const float norm_vals[3] = {1/128.f, 1/128.f, 1/128.f};
        in.substract_mean_normalize(mean_vals, norm_vals);

        ncnn::Extractor ex = Bisenet.create_extractor();
        ex.set_num_threads(threadnum);
        ex.set_light_mode(true);

        ex.input("input", in);
        ncnn::Mat out;
        ex.extract("output", out);
        ncnn::Mat ch1 = out.channel(0);
        ncnn::Mat ch2 = out.channel(1);

        cv::Mat imageData32F(h, w, CV_32FC1);
        cv::Mat binary32F(h, w, CV_32FC1);
        memcpy((uchar*)imageData32F.data, ch1.data, w*h* sizeof(float));
        cv::threshold(imageData32F, binary32F, seg_thr, 255.0, cv::THRESH_BINARY);
        binary32F.convertTo(seg8U, CV_8UC1);

        memcpy((uchar*)point32F.data, ch2.data, w*h* sizeof(float));

        //cv::imwrite("/sdcard/DCIM/camera/aligned.jpg", bgr);
        //cv::imwrite("/sdcard/DCIM/camera/seg.jpg", seg8U);

        return 1;
    }

    int updateFingerPoint(){
        double minVal, maxVal;
        cv::Point minLoc, maxLoc;
        cv::minMaxLoc(point32F, &minVal, &maxVal, &minLoc, &maxLoc );
        if (maxVal > heat_thr) {
            fingerPoint = maxLoc;
            return 1;
        }else return 0;
    }

    int cropPointedArea(cv::Mat &seg, int w, int h){
        int x = fingerPoint.x;
        int y = fingerPoint.y;
        int l = max(x-w/2, 0);
        int r = min(x+w/2, image_width-1);
        int t = max(y-h/2, 0);
        int b = min(y+h/2, image_height-1);
        cv::Rect roi(l,t, r-l, b-t);
        seg = bgr(roi);
        return 1;
    }




};