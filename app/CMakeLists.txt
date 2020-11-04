cmake_minimum_required(VERSION 3.4.1)

# ##################### OpenCV 环境 ############################
#设置OpenCV-android-sdk路径
set( OpenCV_DIR /media/pallas/69c96109-1b7a-4adc-91e9-e72166a8d823/softwares/OpenCV-3.2.0-android-sdk/sdk/native/jni )

find_package(OpenCV REQUIRED )
if(OpenCV_FOUND)
    include_directories(${OpenCV_INCLUDE_DIRS})
    message(STATUS "OpenCV library status:")
    message(STATUS "    version: ${OpenCV_VERSION}")
    message(STATUS "    libraries: ${OpenCV_LIBS}")
    message(STATUS "    include path: ${OpenCV_INCLUDE_DIRS}")
else(OpenCV_FOUND)
    message(FATAL_ERROR "OpenCV library not found")
endif(OpenCV_FOUND)

#include头文件目录
include_directories(src/main/cpp/include
                    src/main/cpp/)

#source directory源文件目录
file(GLOB FACE_SRC src/main/cpp/*.h
                   src/main/cpp/*.cpp)

set(FACE_COMPILE_CODE ${FACE_SRC})

#添加ncnn库
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
  PROPERTIES IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#编译为动态库
add_library(Face SHARED ${FACE_COMPILE_CODE})

#添加工程所依赖的库
find_library(  log-lib log )
target_link_libraries( Face
                       ${OpenCV_LIBS}
                       libncnn
                       jnigraphics
                       z
                       ${log-lib})