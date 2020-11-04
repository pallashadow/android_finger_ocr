#pragma once
#ifndef NCNN_CENTERFACE_H_
#define NCNN_CENTERFACE_H_
#include <string>
#include <iostream>
#include "net.h"
#include<vector>
#include<algorithm>
#include<numeric>
#include<map>


#define NMS_UNION 1
#define NMS_MIN  2

namespace Face{

    typedef struct FaceInfo {
        float x1;
        float y1;
        float x2;
        float y2;
        float score;
        float area;
        float landmarks[10];
    };

    class Centerface {
    public:
        Centerface(const std::string model_path);
        ~Centerface();


        void SetThreadNum(int threadNum);

        //You can change the shape of input image by setting params :resized_w and resized_h
        int detect(ncnn::Mat &inblob, std::vector<FaceInfo>&faces, int resized_w,int resized_h,
            float scoreThresh = 0.5, float nmsThresh = 0.3);

    private:
        void dynamicScale(float in_w, float in_h);
        void squareBox(std::vector<FaceInfo> &faces);
        void genIds(float *heatmap, int h, int w, float thresh, std::vector<int> &ids);
        void nms(std::vector<FaceInfo>& input, std::vector<FaceInfo>& output, float nmsthreshold = 0.3,int type=NMS_MIN);
        void decode(ncnn::Mat &heatmap, ncnn::Mat &scale, ncnn::Mat &offset, ncnn::Mat &landmarks,
            std::vector<FaceInfo>&faces, float scoreThresh, float nmsThresh);
    private:
        ncnn::Net net;

        int d_h;
        int d_w;
        float d_scale_h;
        float d_scale_w;

        float scale_h;
        float scale_w;

        int image_h;
        int image_w;

        int threadnum = 1;
    };
}
#endif // NCNN_CENTERFACE_H_
