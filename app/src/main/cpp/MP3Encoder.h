//
// Created by Administrator on 2019/9/25.
//

#ifndef AV_PROJECT_MP3ENCODER_H
#define AV_PROJECT_MP3ENCODER_H
#include<fstream>
#include "libmp3lame/lame.h"

using namespace std;
class MP3Encoder {
private:
    FILE* pcmFile;
    FILE* mp3File;
    lame_t lameClient;
public:
    MP3Encoder();
    ~MP3Encoder();
    int Init(const char* pcmFilePth, const char* mp3FilePath,int sampleRate,int channels,int bitRate);
    void Encode();
    void Destory();
};


#endif //AV_PROJECT_MP3ENCODER_H
