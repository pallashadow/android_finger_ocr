#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
//#include <math.h>
#include "ncnn/net.h"
#include <cmath>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
using namespace std;
using namespace cv;


class HandSeg{

private:
    ncnn::Net Bisenet;
    int threadnum = 1;
public:
    Mat img;
    ~HandSeg(){}
    HandSeg(){}


Mat segImg(Mat img, const std::string &model_path){
    std::string param_files = model_path+"bisenet.param";
    std::string bin_files = model_path+"bisenet.bin";

    Bisenet.load_param(param_files.c_str());
    Bisenet.load_model(bin_files.c_str());
    __android_log_print(ANDROID_LOG_INFO, "lclclc", "模型加载成功bisenet");
    ncnn::Mat in;
    int w = img.cols;
    int h = img.rows;

    in = ncnn::Mat::from_pixels(img.data, ncnn::Mat::PIXEL_BGR2RGB, w, h);
    ncnn::Extractor ex = Bisenet.create_extractor();
    ex.set_num_threads(threadnum);
    ex.set_light_mode(true);

    ex.input("input", in);
    ncnn::Mat out;
    ex.extract("output", out);
//    ncnn::Mat ch1 = out.channel(0);
    ncnn::Mat ch1 = out.channel(0);
    __android_log_print(ANDROID_LOG_INFO, "lclclc", "获取网络结果%d,%d,%f",ch1.c, ch1.w, ch1.h);

    cv::Mat out8U(h,w, CV_8UC1);
    float mean_val = -1.0f;
    float norm_val = 3.0f;
    ch1.substract_mean_normalize(&mean_val, &norm_val);
    ch1.to_pixels(out8U.data,ncnn::Mat::PIXEL_GRAY);
    /*
    cv::Mat imageData32F(h, w, CV_32FC1);
    cv::Mat binary32F(h, w, CV_32FC1);
    memcpy((uchar*)imageData32F.data, ch1.data, w*h* sizeof(float));
    cv::threshold(imageData32F, binary32F, -1.0, 255.0, cv::THRESH_BINARY);
    binary32F.convertTo(out8U, CV_8UC1);
    */
    //__android_log_print(ANDROID_LOG_INFO, "lclclc", "取得第n个ch");
    // cv::imwrite("/storage/emulated/0/apks/123.jpg",imageDate);
    return out8U;

}


};