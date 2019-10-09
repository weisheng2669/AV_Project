//
// Created by Administrator on 2019/10/7.
//
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <assert.h>
#include <jni.h>
#include <string.h>
#include <pthread.h>
// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

static SLObjectItf outputMixObject = NULL;
static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;

// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
        SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_av_1project_Entity_OpenSLHelper_createAudioRecorder(JNIEnv *env, jclass clazz) {
    // TODO: implement createAudioRecorder()
    return JNI_TRUE;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_av_1project_Entity_OpenSLHelper_startRecording(JNIEnv *env, jclass clazz) {
    // TODO: implement startRecording()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_av_1project_Entity_OpenSLHelper_shutdown(JNIEnv *env, jclass clazz) {
    // TODO: implement shutdown()
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_av_1project_Entity_OpenSLHelper_createEngine(JNIEnv *env, jclass clazz) {
    // TODO: implement createEngine()

}