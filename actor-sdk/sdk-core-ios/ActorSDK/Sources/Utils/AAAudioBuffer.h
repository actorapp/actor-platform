#import <Foundation/Foundation.h>

struct TGAudioBuffer
{
    NSUInteger capacity;
    uint8_t *data;
    NSUInteger size;
    int64_t pcmOffset;
};

inline TGAudioBuffer *TGAudioBufferWithCapacity(NSUInteger capacity)
{
    TGAudioBuffer *audioBuffer = (TGAudioBuffer *)malloc(sizeof(TGAudioBuffer));
    audioBuffer->capacity = capacity;
    audioBuffer->data = (uint8_t *)malloc(capacity);
    audioBuffer->size = 0;
    audioBuffer->pcmOffset = 0;
    return audioBuffer;
}

inline void TGAudioBufferDispose(TGAudioBuffer *audioBuffer)
{
    if (audioBuffer != NULL)
    {
        free(audioBuffer->data);
        free(audioBuffer);
    }
}