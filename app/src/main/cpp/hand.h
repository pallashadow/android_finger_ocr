#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
//#include <math.h>
#include "net.h"
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
    std::string param_files = model_path+"BiseNet.param";
    std::string bin_files = model_path+"BiseNet.bin";

    Bisenet.load_param(param_files.c_str());
    Bisenet.load_model(bin_files.c_str());
    __android_log_print(ANDROID_LOG_INFO, "lclclc", "模型加载成功bisenet");
    ncnn::Mat in;
    //cv::imwrite("/storage/emulated/0/apks/123.jpg",img);

    in = ncnn::Mat::from_pixels(img.data, ncnn::Mat::PIXEL_BGR2RGB,
                                img.cols, img.rows);
    ncnn::Extractor ex = Bisenet.create_extractor();
    ex.set_num_threads(threadnum);
    //ex.set_light_mode(true);
    ex.input("input", in);
    ncnn::Mat out;
    ex.extract("output", out);

    ncnn::Mat ch1 = out.channel(0);
    //__android_log_print(ANDROID_LOG_INFO, "lclclc", "获取网络结果%d,%d",ch2.c, ch2.w);

    cv::Mat imageDate(ch1.h, ch1.w, CV_8UC1);
    ch1.to_pixels(imageDate.data,ncnn::Mat::PIXEL_GRAY);
    //__android_log_print(ANDROID_LOG_INFO, "lclclc", "取得第n个ch");
    return imageDate;

}


};