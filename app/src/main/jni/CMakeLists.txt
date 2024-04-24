project(CartoonStyle)

cmake_minimum_required(VERSION 3.4.1)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(CartoonStyle SHARED OnnxInference_jni.cpp)

target_link_libraries(CartoonStyle ncnn)
