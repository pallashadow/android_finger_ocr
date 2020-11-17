# android离线指读OCR DEMO

## 在android-studio中配置项目

下载解压opencv-4.5.0-android-sdk.zip到根目录
https://sourceforge.net/projects/opencvlibrary/files/4.5.0/opencv-4.5.0-android-sdk.zip/download

依赖NDK版本：android-ndk-r21d。可以在local.properties手动指定

## 图片上传到android测试机

adb push ./t1.jpg /sdcard/DCIM/Camera/t1.jpg

刷新相册使图片可见

adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/DCIM/Camera/t1.jpg


## pytorch2onnx

./cv-book-image-recognition/utils/model/pth2trt.py 将pth模型转换为onnx模型

## onnx2ncnn

编译ncnn from source

如果编译时报错（target pattern contains no '%'）说明需要安装高版本protobuf。https://askubuntu.com/questions/1072683/how-can-i-install-protoc-on-ubuntu-16-04

在build/tools/onnx下出现可执行文件 onnx2ncnn

./onnx2ncnn /home/pallas/PROJECTS/cv-book-image-recognition/resources/model_store/BiSeNet_shufflenetv2x1_hand.4.7.pth handseg.param handseg.bin

## Linux系统连接android测试机

TODO
