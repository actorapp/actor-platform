#import <Foundation/Foundation.h>

struct AAAudioBuffer
{
    NSUInteger capacity;
    uint8_t *data;
    NSUInteger size;
    int64_t pcmOffset;
};

inline AAAudioBuffer *AAAudioBufferWithCapacity(NSUInteger capacity)
{
    AAAudioBuffer *audioBuffer = (AAAudioBuffer *)malloc(sizeof(AAAudioBuffer));
    audioBuffer->capacity = capacity;
    audioBuffer->data = (uint8_t *)malloc(capacity);
    audioBuffer->size = 0;
    audioBuffer->pcmOffset = 0;
    return audioBuffer;
}

inline void AAAudioBufferDispose(AAAudioBuffer *audioBuffer)
{
    if (audioBuffer != NULL)
    {
        free(audioBuffer->data);
        free(audioBuffer);
    }
}