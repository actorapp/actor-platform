#import "AAOpusAudioPlayerAU.h"

#import "ASQueue.h"
#import "NSObject+TGLock.h"

#import "AAAudioBuffer.h"

#import "opusfile.h"

#import <AudioUnit/AudioUnit.h>

#import <map>
#import <libkern/OSAtomic.h>

#define kOutputBus 0
#define kInputBus 1

static const int TGOpusAudioPlayerBufferCount = 3;
static const int TGOpusAudioPlayerSampleRate = 48000; // libopusfile is bound to use 48 kHz

static std::map<int, __weak AAOpusAudioPlayerAU *> activeAudioPlayers;

static TG_SYNCHRONIZED_DEFINE(filledBuffersLock) = PTHREAD_MUTEX_INITIALIZER;

static volatile OSSpinLock audioPositionLock = OS_SPINLOCK_INIT;

@interface AAOpusAudioPlayerAU ()
{
@public
    int _playerId;
    
    NSString *_filePath;
    NSInteger _fileSize;
    
    bool _isSeekable;
    int64_t _totalPcmDuration;
    
    bool _isPaused;
    
    OggOpusFile *_opusFile;
    AudioComponentInstance _audioUnit;
    
    AAAudioBuffer *_filledAudioBuffers[TGOpusAudioPlayerBufferCount];
    int _filledAudioBufferCount;
    int _filledAudioBufferPosition;
    
    int64_t _currentPcmOffset;
    bool _finished;
}

@end

@implementation AAOpusAudioPlayerAU

+ (bool)canPlayFile:(NSString *)path
{
    int error = OPUS_OK;
    OggOpusFile *file = op_test_file([path UTF8String], &error);
    if (file != NULL)
    {
        error = op_test_open(file);
        op_free(file);
        
        return error == OPUS_OK;
    }
    return false;
}

- (instancetype)initWithPath:(NSString *)path
{
    self = [super init];
    if (self != nil)
    {
        _filePath = path;
        
        static int nextPlayerId = 1;
        _playerId = nextPlayerId++;
        
        _isPaused = true;
        
        [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
        {
            _fileSize = [[[NSFileManager defaultManager] attributesOfItemAtPath:path error:nil][NSFileSize] integerValue];
            if (_fileSize == 0)
            {
                NSLog(@"[AAOpusAudioPlayer#%p invalid file]", self);
                [self cleanupAndReportError];
            }
        }];
    }
    return self;
}

- (void)dealloc
{
    [self cleanup];
}

- (void)cleanupAndReportError
{
    [self cleanup];
}

- (void)cleanup
{
    TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
    
    activeAudioPlayers.erase(_playerId);
    
    for (int i = 0; i < TGOpusAudioPlayerBufferCount; i++)
    {
        if (_filledAudioBuffers[i] != NULL)
        {
            AAAudioBufferDispose(_filledAudioBuffers[i]);
            _filledAudioBuffers[i] = NULL;
        }
    }
    _filledAudioBufferCount = 0;
    _filledAudioBufferPosition = 0;
    
    TG_SYNCHRONIZED_END(filledBuffersLock);
    
    OggOpusFile *opusFile = _opusFile;
    _opusFile = NULL;
    
    AudioUnit audioUnit = _audioUnit;
    _audioUnit = NULL;
    
    intptr_t objectId = (intptr_t)self;
    
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        if (audioUnit != NULL)
        {
            OSStatus status = noErr;
            status = AudioOutputUnitStop(audioUnit);
            if (status != noErr)
                NSLog(@"[AAOpusAudioPlayer#%lx AudioOutputUnitStop failed: %d]", objectId, (int)status);
            
            status = AudioComponentInstanceDispose(audioUnit);
            if (status != noErr)
                NSLog(@"[AAOpusAudioRecorder#%lx AudioComponentInstanceDispose failed: %d]", objectId, (int)status);
        }
        
        if (opusFile != NULL)
            op_free(opusFile);
    }];
    
    [self _endAudioSessionFinal];
}

static OSStatus TGOpusAudioPlayerCallback(void *inRefCon, __unused AudioUnitRenderActionFlags *ioActionFlags, __unused const AudioTimeStamp *inTimeStamp, __unused UInt32 inBusNumber, __unused UInt32 inNumberFrames, AudioBufferList *ioData)
{
    int playerId = (int)(size_t)inRefCon;
    
    TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
    
    AAOpusAudioPlayerAU *self = nil;
    auto it = activeAudioPlayers.find(playerId);
    if (it != activeAudioPlayers.end())
        self = it->second;
    
    if (self != nil)
    {
        AAAudioBuffer **freedAudioBuffers = NULL;
        int freedAudioBufferCount = 0;
        
        for (int i = 0; i < (int)ioData->mNumberBuffers; i++)
        {
            AudioBuffer *buffer = &ioData->mBuffers[i];
            
            buffer->mNumberChannels = 1;
            
            int requiredBytes = buffer->mDataByteSize;
            int writtenBytes = 0;
            
            while (self->_filledAudioBufferCount > 0 && writtenBytes < requiredBytes)
            {
                OSSpinLockLock(&audioPositionLock);
                self->_currentPcmOffset = self->_filledAudioBuffers[0]->pcmOffset + self->_filledAudioBufferPosition / 2;
                OSSpinLockUnlock(&audioPositionLock);
                
                int takenBytes = MIN((int)self->_filledAudioBuffers[0]->size - self->_filledAudioBufferPosition, requiredBytes - writtenBytes);
                
                if (takenBytes != 0)
                {
                    memcpy(((uint8_t *)buffer->mData) + writtenBytes, self->_filledAudioBuffers[0]->data + self->_filledAudioBufferPosition, takenBytes);
                    writtenBytes += takenBytes;
                }
                
                if (self->_filledAudioBufferPosition + takenBytes >= (int)self->_filledAudioBuffers[0]->size)
                {
                    if (freedAudioBuffers == NULL)
                        freedAudioBuffers = (AAAudioBuffer **)malloc(sizeof(AAAudioBuffer *) * TGOpusAudioPlayerBufferCount);
                    freedAudioBuffers[freedAudioBufferCount] = self->_filledAudioBuffers[0];
                    freedAudioBufferCount++;
                    
                    for (int i = 0; i < TGOpusAudioPlayerBufferCount - 1; i++)
                    {
                        self->_filledAudioBuffers[i] = self->_filledAudioBuffers[i + 1];
                    }
                    self->_filledAudioBuffers[TGOpusAudioPlayerBufferCount - 1] = NULL;
                    
                    self->_filledAudioBufferCount--;
                    self->_filledAudioBufferPosition = 0;
                }
                else
                    self->_filledAudioBufferPosition += takenBytes;
            }
            
            if (writtenBytes < requiredBytes)
                memset(((uint8_t *)buffer->mData) + writtenBytes, 0, requiredBytes - writtenBytes);
        }
        
        if (freedAudioBufferCount != 0)
        {
            [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
            {
                for (int i = 0; i < freedAudioBufferCount; i++)
                {
                    [self fillBuffer:freedAudioBuffers[i]];
                }
                
                free(freedAudioBuffers);
            }];
        }
    }
    else
    {
        for (int i = 0; i < (int)ioData->mNumberBuffers; i++)
        {
            AudioBuffer *buffer = &ioData->mBuffers[i];
            memset(buffer->mData, 0, buffer->mDataByteSize);
        }
    }
    
    TG_SYNCHRONIZED_END(filledBuffersLock);
    
    return noErr;
}

- (void)playFromPosition:(NSTimeInterval)position
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        if (!_isPaused)
            return;
        
        if (_audioUnit == NULL)
        {
            [self _beginAudioSession];
            
            _isPaused = false;
            
            int openError = OPUS_OK;
            _opusFile = op_open_file([_filePath UTF8String], &openError);
            if (_opusFile == NULL || openError != OPUS_OK)
            {
                NSLog(@"[TGOpusAudioPlayer#%p op_open_file failed: %d]", self, openError);
                [self cleanupAndReportError];
                
                return;
            }
            
            _isSeekable = op_seekable(_opusFile);
            _totalPcmDuration = op_pcm_total(_opusFile, -1);
            
            AudioComponentDescription desc;
            desc.componentType = kAudioUnitType_Output;
            desc.componentSubType = kAudioUnitSubType_RemoteIO;
            desc.componentFlags = 0;
            desc.componentFlagsMask = 0;
            desc.componentManufacturer = kAudioUnitManufacturer_Apple;
            AudioComponent inputComponent = AudioComponentFindNext(NULL, &desc);
            AudioComponentInstanceNew(inputComponent, &_audioUnit);
            
            OSStatus status = noErr;
            
            static const UInt32 one = 1;
            status = AudioUnitSetProperty(_audioUnit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Output, kOutputBus, &one, sizeof(one));
            if (status != noErr)
            {
                NSLog(@"[AAOpusAudioPlayer#%@ AudioUnitSetProperty kAudioOutputUnitProperty_EnableIO failed: %d]", self, (int)status);
                [self cleanupAndReportError];
                
                return;
            }
            
            AudioStreamBasicDescription outputAudioFormat;
            outputAudioFormat.mSampleRate = TGOpusAudioPlayerSampleRate;
            outputAudioFormat.mFormatID = kAudioFormatLinearPCM;
            outputAudioFormat.mFormatFlags = kAudioFormatFlagIsSignedInteger | kAudioFormatFlagIsPacked;
            outputAudioFormat.mFramesPerPacket = 1;
            outputAudioFormat.mChannelsPerFrame = 1;
            outputAudioFormat.mBitsPerChannel = 16;
            outputAudioFormat.mBytesPerPacket = 2;
            outputAudioFormat.mBytesPerFrame = 2;
            status = AudioUnitSetProperty(_audioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Input, kOutputBus, &outputAudioFormat, sizeof(outputAudioFormat));
            if (status != noErr)
            {
                NSLog(@"[AAOpusAudioPlayer#%@ AudioUnitSetProperty kAudioUnitProperty_StreamFormat failed: %d]", self, (int)status);
                [self cleanupAndReportError];
                
                return;
            }
            
            AURenderCallbackStruct callbackStruct;
            callbackStruct.inputProc = &TGOpusAudioPlayerCallback;
            callbackStruct.inputProcRefCon = (void *)_playerId;
            if (AudioUnitSetProperty(_audioUnit, kAudioUnitProperty_SetRenderCallback, kAudioUnitScope_Global, kOutputBus, &callbackStruct, sizeof(callbackStruct)) != noErr)
            {
                NSLog(@"[AAOpusAudioPlayer#%@ AudioUnitSetProperty kAudioUnitProperty_SetRenderCallback failed]", self);
                [self cleanupAndReportError];
                
                return;
            }
            
            status = AudioUnitInitialize(_audioUnit);
            if (status != noErr)
            {
                NSLog(@"[AAOpusAudioRecorder#%@ AudioUnitInitialize failed: %d]", self, (int)status);
                [self cleanup];
                
                return;
            }
            
            TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
            activeAudioPlayers[_playerId] = self;
            TG_SYNCHRONIZED_END(filledBuffersLock);
            
            NSUInteger bufferByteSize = [self bufferByteSize];
            for (int i = 0; i < TGOpusAudioPlayerBufferCount; i++)
            {
                _filledAudioBuffers[i] = AAAudioBufferWithCapacity(bufferByteSize);
            }
            _filledAudioBufferCount = TGOpusAudioPlayerBufferCount;
            _filledAudioBufferPosition = 0;
            
            _finished = false;
            
            if (_isSeekable && position >= 0.0)
                op_pcm_seek(_opusFile, (ogg_int64_t)(position * TGOpusAudioPlayerSampleRate));
            
            status = AudioOutputUnitStart(_audioUnit);
            if (status != noErr)
            {
                NSLog(@"[AAOpusAudioRecorder#%@ AudioOutputUnitStart failed: %d]", self, (int)status);
                [self cleanupAndReportError];
            }
        }
        else
        {
            [self _beginAudioSession];
            
            if (_isSeekable && position >= 0.0)
            {
                int result = op_pcm_seek(_opusFile, (ogg_int64_t)(position * TGOpusAudioPlayerSampleRate));
                if (result != OPUS_OK)
                    NSLog(@"[AAOpusAudioPlayer#%p op_pcm_seek failed: %d]", self, result);
                
                ogg_int64_t pcmPosition = op_pcm_tell(_opusFile);
                _currentPcmOffset = pcmPosition;
            
                _isPaused = false;
            }
            else
                _isPaused = false;
            
            _finished = false;
            
            TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
            for (int i = 0; i < _filledAudioBufferCount; i++)
            {
                _filledAudioBuffers[i]->size = 0;
            }
            self->_filledAudioBufferPosition = 0;
            TG_SYNCHRONIZED_END(filledBuffersLock);
        }
    }];
}

- (void)fillBuffer:(AAAudioBuffer *)audioBuffer
{
    if (_opusFile != NULL)
    {
        audioBuffer->pcmOffset = MAX(0, op_pcm_tell(_opusFile));
        
        if (!_isPaused)
        {
            if (_finished)
            {
                bool notifyFinished = false;
                TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
                if (_filledAudioBufferCount == 0)
                    notifyFinished = true;
                TG_SYNCHRONIZED_END(filledBuffersLock);
                
                if (notifyFinished)
                    [self _notifyFinished];
                
                return;
            }
            else
            {
                int availableOutputBytes = (int)audioBuffer->capacity;
                int writtenOutputBytes = 0;
                
                bool endOfFileReached = false;
                
                bool bufferPcmOffsetSet = false;
                
                while (writtenOutputBytes < availableOutputBytes)
                {
                    if (!bufferPcmOffsetSet)
                    {
                        bufferPcmOffsetSet = true;
                        audioBuffer->pcmOffset = MAX(0, op_pcm_tell(_opusFile));
                    }
                    
                    int readSamples = op_read(_opusFile, (opus_int16 *)(audioBuffer->data + writtenOutputBytes), (availableOutputBytes - writtenOutputBytes) / 2, NULL);
                    
                    if (readSamples > 0)
                        writtenOutputBytes += readSamples * 2;
                    else
                    {
                        if (readSamples < 0)
                            NSLog(@"[AAOpusAudioPlayer#%p op_read failed: %d]", self, readSamples);
                        
                        endOfFileReached = true;
                        
                        break;
                    }
                }
                
                audioBuffer->size = writtenOutputBytes;
                
                if (endOfFileReached)
                    _finished = true;
            }
        }
        else
        {
            memset(audioBuffer->data, 0, audioBuffer->capacity);
            audioBuffer->size = audioBuffer->capacity;
            audioBuffer->pcmOffset = _currentPcmOffset;
        }
    }
    else
    {
        memset(audioBuffer->data, 0, audioBuffer->capacity);
        audioBuffer->size = audioBuffer->capacity;
        audioBuffer->pcmOffset = _totalPcmDuration;
    }
    
    TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
    _filledAudioBufferCount++;
    _filledAudioBuffers[_filledAudioBufferCount - 1] = audioBuffer;
    TG_SYNCHRONIZED_END(filledBuffersLock);
}

- (NSUInteger)bufferByteSize
{
    static const NSUInteger maxBufferSize = 0x50000;
    static const NSUInteger minBufferSize = 0x4000;
    
    Float64 seconds = 0.4;
    
    Float64 numPacketsForTime = TGOpusAudioPlayerSampleRate * seconds;
    NSUInteger result = (NSUInteger)(numPacketsForTime * 2);
    
    return MAX(minBufferSize, MIN(maxBufferSize, result));
}

- (void)pause
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        _isPaused = true;
        
        TG_SYNCHRONIZED_BEGIN(filledBuffersLock);
        for (int i = 0; i < _filledAudioBufferCount; i++)
        {
            if (_filledAudioBuffers[i]->size != 0)
                memset(_filledAudioBuffers[i]->data, 0, _filledAudioBuffers[i]->size);
            _filledAudioBuffers[i]->pcmOffset = _currentPcmOffset;
        }
        TG_SYNCHRONIZED_END(filledBuffersLock);
    }];
}

- (void)stop
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        [self cleanup];
    }];
}

- (NSTimeInterval)currentPositionSync:(bool)sync
{
    __block NSTimeInterval result = 0.0;
    
    dispatch_block_t block = ^
    {
        OSSpinLockLock(&audioPositionLock);
        result = _currentPcmOffset / (NSTimeInterval)TGOpusAudioPlayerSampleRate;
        OSSpinLockUnlock(&audioPositionLock);
    };
    
    if (sync)
        [[AAAudioPlayer _playerQueue] dispatchOnQueue:block synchronous:true];
    else
        block();
    
    return result;
}

- (NSTimeInterval)duration
{
    return _totalPcmDuration / (NSTimeInterval)TGOpusAudioPlayerSampleRate;
}

@end
