#import "AAOpusAudioRecorder.h"

#import "ASQueue.h"

#import <AVFoundation/AVFoundation.h>
#import <AudioUnit/AudioUnit.h>

#import "opus.h"
#import "opusenc.h"

static const int TGOpusAudioRecorderSampleRate = 16000;

typedef struct
{
    AudioComponentInstance audioUnit;
    bool audioUnitStarted;
    bool audioUnitInitialized;
    
    int globalAudioRecorderId;
} TGOpusAudioRecorderContext;

static TGOpusAudioRecorderContext globalRecorderContext = { .audioUnit = NULL, .audioUnitStarted = false, .audioUnitInitialized = false, .globalAudioRecorderId = -1};
static __weak AAOpusAudioRecorder *globalRecorder = nil;

static dispatch_semaphore_t playSoundSemaphore = nil;

@interface AAOpusAudioRecorder ()
{
    NSString *_tempFilePath;
    
    TGOggOpusWriter *_oggWriter;
    
    NSMutableData *_audioBuffer;
    NSFileHandle *_fileHandle;
}

@property (nonatomic) int recorderId;

@end

@implementation AAOpusAudioRecorder

- (instancetype)initWithFileEncryption:(bool)fileEncryption
{
    self = [super init];
    if (self != nil)
    {
        int64_t randomId = 0;
        arc4random_buf(&randomId, 8);
        _tempFilePath = [NSTemporaryDirectory() stringByAppendingString:[[NSString alloc] initWithFormat:@"%" PRIx64 ".ogg", randomId]];
        
        [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
        {
            static int nextRecorderId = 1;
            _recorderId = nextRecorderId++;
            globalRecorderContext.globalAudioRecorderId = _recorderId;
            
            globalRecorder = self;
            
        }];
    }
    return self;
}

- (void)dealloc
{
    [self cleanup];
}

+ (ASQueue *)processingQueue
{
    static ASQueue *queue = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^
    {
        queue = [[ASQueue alloc] initWithName:"vkm.kio.opusAudioRecorderQueue"];
    });
    
    return queue;
}

- (void)cleanup
{
    intptr_t objectId = (intptr_t)self;
    int recorderId = _recorderId;
    
    NSFileHandle *fileHandle = _fileHandle;
    _fileHandle = nil;
    
    globalRecorder = nil;
    
    _oggWriter = nil;
    
    [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
    {
        [fileHandle closeFile];
        
        if (globalRecorderContext.globalAudioRecorderId == recorderId)
        {
            globalRecorderContext.globalAudioRecorderId++;
            
            if (globalRecorderContext.audioUnitStarted && globalRecorderContext.audioUnit != NULL)
            {
                OSStatus status = noErr;
                status = AudioOutputUnitStop(globalRecorderContext.audioUnit);
                if (status != noErr)
                    NSLog(@"[TGOpusAudioRecorder#%lx AudioOutputUnitStop failed: %d]", objectId, (int)status);
                
                globalRecorderContext.audioUnitStarted = false;
            }
            
            if (globalRecorderContext.audioUnit != NULL)
            {
                OSStatus status = noErr;
                status = AudioComponentInstanceDispose(globalRecorderContext.audioUnit);
                if (status != noErr)
                    NSLog(@"[AAOpusAudioRecorder#%lx AudioComponentInstanceDispose failed: %d]", objectId, (int)status);
                
                globalRecorderContext.audioUnit = NULL;
            }
        }
    }];
    
    [self _endAudioSession];
}

- (void)_beginAudioSession:(bool)begin
{
    [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
    {
        __autoreleasing NSError *error = nil;
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
        if (![audioSession setCategory:AVAudioSessionCategoryPlayAndRecord error:&error])
            NSLog(@"[AAAudioRecorder audio session set category failed: %@]", error);
        if (begin)
        {
            if (![audioSession setActive:true error:&error])
                NSLog(@"[AAAudioRecorder audio session activation failed: %@]", error);
        }
    }];
}

- (void)_endAudioSession
{
    [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
    {
        __autoreleasing NSError *error = nil;
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
       // [audioSession setActive:NO error:&error];
        if (![audioSession setCategory:AVAudioSessionCategoryPlayback error:&error])
            NSLog(@"[AAAudioRecorder audio session set category failed: %@]", error);
        if (![audioSession setActive:NO error:&error])
            NSLog(@"[AAAudioRecorder audio session deactivation failed: %@]", error);
    }];
}

- (void)record
{
    [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
    {
        [self _beginAudioSession:true];
        
        AudioComponentDescription desc;
        desc.componentType = kAudioUnitType_Output;
        desc.componentSubType = kAudioUnitSubType_RemoteIO;
        desc.componentFlags = 0;
        desc.componentFlagsMask = 0;
        desc.componentManufacturer = kAudioUnitManufacturer_Apple;
        AudioComponent inputComponent = AudioComponentFindNext(NULL, &desc);
        AudioComponentInstanceNew(inputComponent, &globalRecorderContext.audioUnit);
        
        OSStatus status = noErr;
        
        static const UInt32 one = 1;
        status = AudioUnitSetProperty(globalRecorderContext.audioUnit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Input, 1, &one, sizeof(one));
        if (status != noErr)
        {
            NSLog(@"[AAOpusAudioRecorder#%@ AudioUnitSetProperty kAudioOutputUnitProperty_EnableIO failed: %d]", self, (int)status);
            [self cleanup];
            
            return;
        }
        
        AudioStreamBasicDescription inputAudioFormat;
        inputAudioFormat.mSampleRate = TGOpusAudioRecorderSampleRate;
        inputAudioFormat.mFormatID = kAudioFormatLinearPCM;
        inputAudioFormat.mFormatFlags = kAudioFormatFlagIsSignedInteger | kAudioFormatFlagIsPacked;
        inputAudioFormat.mFramesPerPacket = 1;
        inputAudioFormat.mChannelsPerFrame = 1;
        inputAudioFormat.mBitsPerChannel = 16;
        inputAudioFormat.mBytesPerPacket = 2;
        inputAudioFormat.mBytesPerFrame = 2;
        status = AudioUnitSetProperty(globalRecorderContext.audioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Output, 1, &inputAudioFormat, sizeof(inputAudioFormat));
        if (status != noErr)
        {
            NSLog(@"[TGOpusAudioRecorder#%@ AudioUnitSetProperty kAudioUnitProperty_StreamFormat failed: %d]", self, (int)status);
            [self cleanup];
            
            return;
        }
        
        AudioStreamBasicDescription audioFormat;
        audioFormat.mSampleRate = TGOpusAudioRecorderSampleRate;
        audioFormat.mFormatID = kAudioFormatLinearPCM;
        audioFormat.mFormatFlags = kAudioFormatFlagIsSignedInteger | kAudioFormatFlagIsPacked;
        audioFormat.mFramesPerPacket = 1;
        audioFormat.mChannelsPerFrame = 1;
        audioFormat.mBitsPerChannel = 16;
        audioFormat.mBytesPerPacket = 2;
        audioFormat.mBytesPerFrame = 2;
        status = AudioUnitSetProperty(globalRecorderContext.audioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Input, 0, &audioFormat, sizeof(audioFormat));
        if (status != noErr)
        {
            NSLog(@"[TGOpusAudioRecorder#%@ AudioUnitSetProperty kAudioUnitProperty_StreamFormat failed: %d]", self, (int)status);
            [self cleanup];
            
            return;
        }
        
        AURenderCallbackStruct callbackStruct;
        callbackStruct.inputProc = &TGOpusRecordingCallback;
        callbackStruct.inputProcRefCon = (void *)_recorderId;
        if (AudioUnitSetProperty(globalRecorderContext.audioUnit, kAudioOutputUnitProperty_SetInputCallback, kAudioUnitScope_Global, 0, &callbackStruct, sizeof(callbackStruct)) != noErr)
        {
            NSLog(@"[TGOpusAudioRecorder#%@ AudioUnitSetProperty kAudioOutputUnitProperty_SetInputCallback failed]", self);
            [self cleanup];
            
            return;
        }
        
        static const UInt32 zero = 0;
        if (AudioUnitSetProperty(globalRecorderContext.audioUnit, kAudioUnitProperty_ShouldAllocateBuffer, kAudioUnitScope_Output, 0, &zero, sizeof(zero)) != noErr)
        {
            NSLog(@"[TGOpusAudioRecorder#%@ AudioUnitSetProperty kAudioUnitProperty_ShouldAllocateBuffer failed]", self);
            [self cleanup];
            
            return;
        }
        
        [[NSFileManager defaultManager] createFileAtPath:_tempFilePath contents:nil attributes:nil];
        _fileHandle = [NSFileHandle fileHandleForWritingAtPath:_tempFilePath];
        
        _oggWriter = [[TGOggOpusWriter alloc] init];
        if (![_oggWriter begin:_fileHandle])
        {
            NSLog(@"[TGOpusAudioRecorder#%@ error initializing ogg opus writer]", self);
            [self cleanup];
            
            return;
        }
        
        CFAbsoluteTime startTime = CFAbsoluteTimeGetCurrent();
        
        status = AudioUnitInitialize(globalRecorderContext.audioUnit);
        if (status == noErr)
            globalRecorderContext.audioUnitInitialized = true;
        else
        {
            NSLog(@"[TGOpusAudioRecorder#%@ AudioUnitInitialize failed: %d]", self, (int)status);
            [self cleanup];
            
            return;
        }
        
        NSLog(@"[TGOpusAudioRecorder#%@ setup time: %f ms]", self, (CFAbsoluteTimeGetCurrent() - startTime) * 1000.0);
        
        //if (playSoundSemaphore != nil)
        //    dispatch_semaphore_wait(playSoundSemaphore, dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.0 * NSEC_PER_SEC)));
        
        status = AudioOutputUnitStart(globalRecorderContext.audioUnit);
        if (status == noErr)
        {
            NSLog(@"[TGOpusAudioRecorder#%@ initialization time: %f ms]", self, (CFAbsoluteTimeGetCurrent() - startTime) * 1000.0);
            NSLog(@"[TGOpusAudioRecorder#%@ started]", self);
            globalRecorderContext.audioUnitStarted = true;
        }
        else
        {
            NSLog(@"[TGOpusAudioRecorder#%@ AudioOutputUnitStart failed: %d]", self, (int)status);
            [self cleanup];
        }
    }];
}

static OSStatus TGOpusRecordingCallback(void *inRefCon, AudioUnitRenderActionFlags *ioActionFlags, const AudioTimeStamp *inTimeStamp, UInt32 inBusNumber, UInt32 inNumberFrames, __unused AudioBufferList *ioData)
{
    @autoreleasepool
    {
        if (globalRecorderContext.globalAudioRecorderId != (int)inRefCon)
            return noErr;
        
        AudioBuffer buffer;
        buffer.mNumberChannels = 1;
        buffer.mDataByteSize = inNumberFrames * 2;
        buffer.mData = malloc(inNumberFrames * 2);
        
        AudioBufferList bufferList;
        bufferList.mNumberBuffers = 1;
        bufferList.mBuffers[0] = buffer;
        OSStatus status = AudioUnitRender(globalRecorderContext.audioUnit, ioActionFlags, inTimeStamp, inBusNumber, inNumberFrames, &bufferList);
        if (status == noErr)
        {
            [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
            {
                AAOpusAudioRecorder *recorder = globalRecorder;
                if (recorder != nil && recorder.recorderId == (int)inRefCon)
                    [recorder _processBuffer:&buffer];
                
                free(buffer.mData);
            }];
        }
    }
    
    return noErr;
}

- (void)_processBuffer:(AudioBuffer const *)buffer
{
    @autoreleasepool
    {
        if (_oggWriter == nil)
            return;
        
        static const int millisecondsPerPacket = 60;
        static const int encoderPacketSizeInBytes = TGOpusAudioRecorderSampleRate / 1000 * millisecondsPerPacket * 2;
        
        unsigned char currentEncoderPacket[encoderPacketSizeInBytes];
        
        int bufferOffset = 0;
        
        while (true)
        {
            int currentEncoderPacketSize = 0;
            
            while (currentEncoderPacketSize < encoderPacketSizeInBytes)
            {
                if (_audioBuffer.length != 0)
                {
                    int takenBytes = MIN((int)_audioBuffer.length, encoderPacketSizeInBytes - currentEncoderPacketSize);
                    if (takenBytes != 0)
                    {
                        memcpy(currentEncoderPacket + currentEncoderPacketSize, _audioBuffer.bytes, takenBytes);
                        [_audioBuffer replaceBytesInRange:NSMakeRange(0, takenBytes) withBytes:NULL length:0];
                        currentEncoderPacketSize += takenBytes;
                    }
                }
                else if (bufferOffset < (int)buffer->mDataByteSize)
                {
                    int takenBytes = MIN((int)buffer->mDataByteSize - bufferOffset, encoderPacketSizeInBytes - currentEncoderPacketSize);
                    if (takenBytes != 0)
                    {
                        memcpy(currentEncoderPacket + currentEncoderPacketSize, ((const char *)buffer->mData) + bufferOffset, takenBytes);
                        bufferOffset += takenBytes;
                        currentEncoderPacketSize += takenBytes;
                    }
                }
                else
                    break;
            }
            
            if (currentEncoderPacketSize < encoderPacketSizeInBytes)
            {
                if (_audioBuffer == nil)
                    _audioBuffer = [[NSMutableData alloc] initWithCapacity:encoderPacketSizeInBytes];
                [_audioBuffer appendBytes:currentEncoderPacket length:currentEncoderPacketSize];
                
                break;
            }
            else
            {
                NSUInteger previousBytesWritten = [_oggWriter encodedBytes];
                [_oggWriter writeFrame:currentEncoderPacket frameByteCount:(NSUInteger)currentEncoderPacketSize];
                NSUInteger currentBytesWritten = [_oggWriter encodedBytes];
                if (currentBytesWritten != previousBytesWritten)
                {
                    [_fileHandle synchronizeFile];
                    
//                    [ActionStageInstance() dispatchOnStageQueue:^
//                    {
//                        TGLiveUploadActor *actor = (TGLiveUploadActor *)[ActionStageInstance() executingActorWithPath:_liveUploadPath];
//                        [actor updateSize:currentBytesWritten];
//                    }];
                }
            }
        }
    }
}

- (NSString *)stop:(NSTimeInterval *)recordedDuration
{
    __block NSString *pathResult = nil;
    __block NSTimeInterval durationResult = 0.0;
    
    __block NSUInteger totalBytes = 0;
    
    [[AAOpusAudioRecorder processingQueue] dispatchOnQueue:^
    {
        if (_oggWriter != nil && [_oggWriter writeFrame:NULL frameByteCount:0])
        {
            pathResult = _tempFilePath;
            durationResult = [_oggWriter encodedDuration];
            totalBytes = [_oggWriter encodedBytes];
        }
        
        [self cleanup];
    } synchronous:true];
    
    if (recordedDuration != NULL)
        *recordedDuration = durationResult;
    
    return pathResult;
}

- (NSTimeInterval)currentDuration
{
    return [_oggWriter encodedDuration];
}

@end
