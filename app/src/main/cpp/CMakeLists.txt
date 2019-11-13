
cmake_minimum_required(VERSION 3.4.1)

set(CURRENT_DIR ${CMAKE_SOURCE_DIR})
add_library(
        ffmpeg-handler
        SHARED
        ${CMAKE_SOURCE_DIR}/../jni/ffmpeg-handler.c)

add_library(
        decode-video
        SHARED
        ${CMAKE_SOURCE_DIR}/../jni/decode_video.cpp)

find_library( # Sets the name of the path variable.
        log-lib
        log)
add_library(avutil
        SHARED
        IMPORTED )
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavutil-56.so )

add_library(swresample
        SHARED
        IMPORTED )
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libswresample-3.so )

add_library(swscale
        SHARED
        IMPORTED )
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libswscale-5.so )

add_library(avcodec
        SHARED
        IMPORTED )
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavcodec-58.so )

add_library(avformat
        SHARED
        IMPORTED )
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavformat-58.so )

add_library(avfilter
        SHARED
        IMPORTED )
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavfilter-7.so )

add_library(avdevice
        SHARED
        IMPORTED )
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavdevice-58.so )

include_directories(${CMAKE_SOURCE_DIR}/ffmpeg/include)

target_link_libraries(decode-video
        android
        avutil swresample swscale avcodec avformat avfilter avdevice
        ${log-lib} ${android-lib})

target_link_libraries(ffmpeg-handler
        android
        avutil swresample swscale avcodec avformat avfilter avdevice
        ${log-lib} ${android-lib} )