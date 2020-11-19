#include "net.h"

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <stdio.h>
#include <vector>
#include <iostream>
using namespace std;
//using namespace cv;


class HandSeg{

private:
    ncnn::Net Bisenet;
    int threadnum = 1;
    float seg_thr = -1.0f;
    float heat_thr = 0.04f;
public:
    int image_height, image_width;
    cv::Mat bgr;
    cv::Mat seg8U;
    cv::Mat point32F;
    cv::Mat ocrseg;
    cv::Point fingerPoint = cv::Point(-1,-1);


    ~HandSeg(){}
    HandSeg(int image_width_, int image_height_, int numOfThread_):
        image_height(image_height_), image_width(image_width_), threadnum(numOfThread_)
    {
        int ret1 = Bisenet.load_param("handseg.param");
        int ret2 = Bisenet.load_model("handseg.bin");
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
        cout<<imageData32F<<endl;
        cv::threshold(imageData32F, binary32F, seg_thr, 255.0, cv::THRESH_BINARY);
        binary32F.convertTo(seg8U, CV_8UC1);

        memcpy((uchar*)point32F.data, ch2.data, w*h* sizeof(float));
        cv::imwrite("/sdcard/DCIM/camera/aligned.jpg", bgr);
        cv::imwrite("/sdcard/DCIM/camera/seg.jpg", seg8U);

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

    void debugShow(cv::Mat &bgr, cv::Point point, cv::Mat &ocrseg){
        cv::imshow("ocrseg", ocrseg);
        cv::circle(bgr, point, 2, cv::Scalar(0,0,255), 2);
        cv::imshow("bgr", bgr);
        cv::imshow("seg8U", seg8U);
        cv::imshow("point8U", point32F / heat_thr);
        cv::waitKey();
    }
};

int main(int argc, char** argv)
{
    cv::Mat m = cv::imread("t1.jpg", 1);
    cv::Mat bgr;
    cv::resize(m, bgr, cv::Size(352,288));
    //cv::resize(m, bgr, cv::Size(224,224));
    HandSeg *mHandSeg = new HandSeg(352, 288, 1);

    mHandSeg->segImg(bgr);
    int ret1 = mHandSeg->updateFingerPoint();
    cv::Mat ocrseg;
    if (ret1){
        int ret2 = mHandSeg->cropPointedArea(ocrseg, 200, 100);
    }

    mHandSeg->debugShow(bgr, mHandSeg->fingerPoint, ocrseg);

    return 0;
}
