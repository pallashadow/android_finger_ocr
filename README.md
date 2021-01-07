# android离线指读OCR DEMO

## 在android-studio中配置项目

下载解压opencv-4.5.0-android-sdk.zip到根目录
https://sourceforge.net/projects/opencvlibrary/files/4.5.0/opencv-4.5.0-android-sdk.zip/download

依赖NDK版本：android-ndk-r21d。可以在local.properties手动指定

## 图片上传到android测试机

adb push ./t1.jpg /sdcard/DCIM/Camera/t1.jpg

刷新相册使图片可见

adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/DCIM/Camera/t1.jpg

## Linux系统连接android测试机

https://blog.csdn.net/binglumeng/article/details/69525361


# 本地生成ncnn模型（.bin .param）并测试

## 编译ncnn

mkdir build

cd build

cmake ..

make -j4

生成ncnn/build/tools/onnx/onnx2ncnn

如果编译时报错（target pattern contains no '%'）说明需要安装高版本protobuf。https://askubuntu.com/questions/1072683/how-can-i-install-protoc-on-ubuntu-16-04

## 生成ncnn模型文件，param和bin

pth -> onnx

./cv-book-image-recognition/utils/model/pth2trt.py

python utils/model/pth2trt.py

onnx2ncnn *.onnx *.bin *.param

## 本地ncnn inference 测试

本地编译 examples/handseg.cpp

生成ncnn/build/examples/handseg

./handseg t1.jpg

## android工程更新libncnn.a

mkdir build_android

cd build_android

export NDK_ROOT=/media/pallas/69c96109-1b7a-4adc-91e9-e72166a8d823/android/android-ndk-r21d

cmake -DCMAKE_TOOLCHAIN_FILE="${NDK_ROOT}/build/cmake/android.toolchain.cmake"     -DANDROID_ABI="armeabi-v7a" -DANDROID_ARM_NEON=ON  -DNCNN_OPENMP=ON -DCMAKE_BUILD_TYPE=debug -DANDROID_PLATFORM=android-21 ..

make -j4

make install

生成build_android/install/lib/libncnn.a ，替换 handseg_android/app/src/main/jniLibs/armeabi-v7a/libncnn.a

生成build_android/install/include/ncnn , 替换handseg_android/app/src/cpp/include/ncnn


