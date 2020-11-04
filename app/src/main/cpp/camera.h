#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
//#include <math.h>

#include <cmath>

//#define M_PI 3.141592653589793


using namespace std;
using namespace cv;


class CameraTransformer{
public:
    float fL,vision_height;
    int H,W, vision_pad, targetHeight, targetWidth, targetCropHeight, targetCropWidth;
    int cropCenterShiftX, cropCenterShiftY;
    //bool fliplr, flipud, rotate;
    Mat mtxInner, perspectiveMat;
    bool cropCenter, largeZoomFlag, distortFlag, dynamicVisionHeightFlag, debug;
    std::vector<float> distort;

    ~CameraTransformer(){} // 析构函数

CameraTransformer(float camera_height, float camera_angle, float vision_height_, float fL240, int image_height, int image_width,
                  int targetHeight_, int targetCropWidth_, bool cropCenter_, int vision_pad_, //bool fliplr, bool flipud, int rotate,
                  Mat mtxInner_, //必须算好再传进来
                  std::vector<float> distort_, //没有的话在外部传5个零进来
                  bool dynamicVisionHeightFlag_=true, bool debug_=true):
H(image_height), W(image_width), vision_height(vision_height_), fL(image_height/240*fL240), targetHeight(targetHeight_),
  mtxInner(mtxInner_), targetCropHeight(targetHeight), targetCropWidth(targetCropWidth_), vision_pad(vision_pad_), distort(distort_),
  cropCenter(cropCenter_), dynamicVisionHeightFlag(dynamicVisionHeightFlag_), debug(debug_)
{
    float y = H/2/mtxInner.at<float>(1,1);
    float y2 = y*y;
    float distortScale_y = ((distort[4]*y2+distort[1])*y2+distort[0])*y2+1+(3*distort[2]+2*distort[3])*y;
    if (abs(distortScale_y-1.0)>0.05) distortFlag=true; else distortFlag=false; // 如果扭曲太小则不修正

    float cy = mtxInner.at<float>(1,2);
    float camera_vision_angle_y_up = atan(cy/fL/distortScale_y);
    float camera_vision_angle_y_down = atan((H-cy)/fL/distortScale_y);
    float camera_vision_angle_y = camera_vision_angle_y_down + camera_vision_angle_y_up;
    float camera_top_angle = camera_angle/180.0*M_PI - camera_vision_angle_y_down;
    float camera_bottom_angle = camera_angle/180.0*M_PI + camera_vision_angle_y_up;
    float camera_angle_fixed = camera_angle;
    if (abs(cy - H/2) > H/15){ // 焦点偏心修正
        camera_angle_fixed = ((camera_bottom_angle + camera_top_angle) / 2)/M_PI*180; // 将角度定义为画面中心角度
        fL = H/2/atan(camera_vision_angle_y / 2); // 根据y方向视场角，将焦距换算为无偏心状况下的焦距
        mtxInner = (Mat_<float>(3,3) << fL, 0, W/2, 0, fL, H/2, 0, 0, 1); //重建无偏心内参矩阵
    }

    float camera_top_distance = tan(camera_top_angle) * camera_height + vision_pad; // 视野近端桌面物理位置
    float camera_bottom_distance = 10000000.0; //inf
    if (camera_bottom_angle < M_PI/2) camera_bottom_distance = tan(camera_bottom_angle) * camera_height; // 视野远端桌面物理位置
    float max_vision_height = camera_bottom_distance - camera_top_distance; // 实际桌面视野
    /*
    largeZoomFlag = false;
    if (max_vision_height < 200){
        vision_height = vision_height * 0.75;
        largeZoomFlag = true;
    }
    */
    if (dynamicVisionHeightFlag) vision_height = vision_height<max_vision_height?vision_height:max_vision_height; // 裁切后桌面视野距离
    float book_width = vision_height*W/H;

    targetWidth = int(W * targetHeight / H);
    cropCenterShiftY = 0;
    if (cropCenter){ // 裁切左右多余边缘
        cropCenterShiftX = (targetWidth - targetCropWidth)/2;
    }else{
        targetCropWidth = targetWidth;
        cropCenterShiftX = 0;
    }

    Mat simulatedPointsCamera = cal_ex_para2(camera_height, camera_angle_fixed, book_width, vision_height,
                 camera_top_distance);// 摄像头坐标系，三维空间中桌面上虚拟4个点

    cameraProject(simulatedPointsCamera);//计算矫正变换矩阵


}

Mat cal_ex_para2(int camera_height, float camera_angle, int book_width, int vision_height, float camera_top_distance){
    float x0 = book_width/2;
    float y0 = -camera_top_distance;
    float y1 = -camera_top_distance - vision_height;
    float z0 = camera_height;
    float data1[12] = {-x0,y0,z0,x0,y0,z0,-x0,y1,z0,x0,y1,z0};
    Mat simulatedPointsWorld = Mat(4,3,CV_32F, data1);
    //Mat simulatedPointsWorld = (Mat_<float>(4,3) << -x0,y0,z0,x0,y0,z0,-x0,y1,z0,x0,y1,z0); // 世界坐标系，三维空间中桌面上虚拟4个点
    float beta = camera_angle / 180.0 * M_PI;
    float data2[9] = {1,0,0,0,cos(beta), sin(beta), 0, -sin(beta), cos(beta)};
    Mat R = Mat(3,3,CV_32F, data2);
    //Mat R = (Mat_<float>(3,3) << 1,0,0,0,cos(beta), sin(beta), 0, -sin(beta), cos(beta)); //x轴旋转beta
    Mat simulatedPointsCamera = simulatedPointsWorld * R.t();
    return simulatedPointsCamera;
}

void cameraProject(Mat &simulatedPointsCamera){
    Mat Y_ = simulatedPointsCamera * mtxInner.t();
    Mat projectedPoints = Mat::zeros(4, 2, CV_32FC1);
    for (int i=0;i<4;i++){
        projectedPoints.at<float>(i,0) = Y_.at<float>(i,0) / Y_.at<float>(i,2);
        projectedPoints.at<float>(i,1) = Y_.at<float>(i,1) / Y_.at<float>(i,2);
    }
    float w = float(targetWidth);
    float h = float(targetHeight);
    float data1[8] = {w,0,0,0,w,h,0,h};
    Mat pt1 = Mat(4,2,CV_32F, data1);
    //Mat pt1 = (Mat_<float>(4,2) << w,0,0,0,w,h,0,h);
    Mat perspectiveMat_ = cv::getPerspectiveTransform(projectedPoints, pt1);
    perspectiveMat_.convertTo(perspectiveMat_, CV_32FC1);

    Mat postTransMat = Mat::eye(Size(3,3), CV_32FC1);
    if (cropCenter){
        postTransMat.at<float>(0,2) -= cropCenterShiftX;
        postTransMat.at<float>(1,2) -= cropCenterShiftY;
    }
    perspectiveMat = postTransMat * perspectiveMat_;
}

Mat perspectiveTransform2(Mat &im, const Scalar borderValue=Scalar(128,128,128), bool debug=false){
    Mat im1;
    //if (distortFlag){
    if (0){
        Mat map1, map2;
        //cv::initUndistortRectifyMap(mtxInner, distort, NULL, mtxInner, Size(W,H), CV_32FC1, map1, map2);
    }else{
        cv::warpPerspective(im, im1, perspectiveMat, Size(targetCropWidth, targetCropHeight),
                            INTER_LINEAR, BORDER_CONSTANT, borderValue);
    }
    return im1;
}


};

