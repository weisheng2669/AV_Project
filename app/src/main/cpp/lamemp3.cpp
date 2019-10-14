#include <jni.h>
#include <string>
#include "libmp3lame/lame.h"
#include "MP3Encoder.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

MP3Encoder *encoder;

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_av_1project_entity_LameMp3_init(JNIEnv *env, jobject thiz, jstring pcmPathParam,
                                          jint audio_channels, jint bit_rate, jint sample_rate,
                                          jstring mp3PathParam) {
    // TODO: implement init()
    const char *pcmPath = env->GetStringUTFChars(pcmPathParam, NULL);
    const char *mp3Path = env->GetStringUTFChars(mp3PathParam, NULL);
    encoder = new MP3Encoder();
    encoder->Init(pcmPath, mp3Path, sample_rate, audio_channels, bit_rate);
    env->ReleaseStringUTFChars(mp3PathParam, mp3Path);
    env->ReleaseStringUTFChars(pcmPathParam, pcmPath);
    return 0;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_av_1project_entity_LameMp3_encode(JNIEnv *env, jobject thiz) {
    // TODO: implement encode()
    encoder->Encode();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_av_1project_entity_LameMp3_destroy(JNIEnv *env, jobject thiz) {
    // TODO: implement destroy()
    encoder->Destory();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_av_1project_entity_LameMp3_getVersion(JNIEnv *env, jobject thiz) {
    // TODO: implement getVersion()
    return env->NewStringUTF(get_lame_version());
}