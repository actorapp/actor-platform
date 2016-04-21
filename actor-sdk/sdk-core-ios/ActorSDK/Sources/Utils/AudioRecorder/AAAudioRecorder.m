#import "AAAudioRecorder.h"
#import "ASQueue.h"
#import "AATimer.h"

#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>

#import "AAAlertView.h"

#define TGUseModernAudio true

#import "AAOpusAudioRecorder.h"

@interface AAAudioRecorder () <AVAudioRecorderDelegate>
{
    AATimer *_timer;
    
    AAOpusAudioRecorder *_modernRecorder;
    
    BOOL sessionCanceled;
}

@end

@implementation AAAudioRecorder

- (void)dealloc
{
    [self cleanup];
}

+ (ASQueue *)audioRecorderQueue
{
    static ASQueue *queue = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^
    {
        queue = [[ASQueue alloc] initWithName:"org.actor.audioRecorderQueue"];
    });
    return queue;
}

static NSMutableDictionary *recordTimers()
{
    static NSMutableDictionary *dict = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^
    {
        dict = [[NSMutableDictionary alloc] init];
    });
    
    return dict;
}

static int currentTimerId = 0;

static void playSoundCompleted(__unused SystemSoundID ssID, __unused void *clientData)
{
    [[AAAudioRecorder audioRecorderQueue] dispatchOnQueue:^
    {
        int timerId = currentTimerId;
        AATimer *timer = (AATimer *)recordTimers()[@(timerId)];
        if ([timer isScheduled])
            [timer resetTimeout:0.05];
    }];
}

- (void)start
{
    sessionCanceled = false;
    NSLog(@"[AAAudioRecorder start]");
    
    [[AAAudioRecorder audioRecorderQueue] dispatchOnQueue:^
    {
        void (^recordBlock)(bool) = ^(bool granted)
        {
            if (granted)
            {
                _modernRecorder = [[AAOpusAudioRecorder alloc] initWithFileEncryption:false];
                NSTimeInterval prepareStart = CFAbsoluteTimeGetCurrent();
                
                [_timer invalidate];
                
                static int nextTimerId = 0;
                int timerId = nextTimerId++;
                
                __weak AAAudioRecorder *weakSelf = self;
                NSTimeInterval timeout = MIN(1.0, MAX(0.1, 1.0 - (CFAbsoluteTimeGetCurrent() - prepareStart)));
                _timer = [[AATimer alloc] initWithTimeout:timeout repeat:false completion:^
                {
                    __strong AAAudioRecorder *strongSelf = weakSelf;
                    [strongSelf _commitRecord];
                } queue:[AAAudioRecorder audioRecorderQueue].nativeQueue];
                recordTimers()[@(timerId)] = _timer;
                [_timer start];
                
                currentTimerId = timerId;
                
                AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
                
//                static SystemSoundID soundId;
//                static dispatch_once_t onceToken;
//                dispatch_once(&onceToken, ^
//                {
//                    NSString *path = [NSString stringWithFormat:@"%@/%@", [[NSBundle mainBundle] resourcePath], @"begin_record.caf"];
//                    NSURL *filePath = [NSURL fileURLWithPath:path isDirectory:false];
//                    AudioServicesCreateSystemSoundID((__bridge CFURLRef)filePath, &soundId);
//                    if (soundId != 0)
//                        AudioServicesAddSystemSoundCompletion(soundId, NULL, kCFRunLoopCommonModes, &playSoundCompleted, NULL);
//                });
//                
//                AudioServicesPlaySystemSound(soundId);
            }
            else
            {
                [[[AAAlertView alloc] initWithTitle:nil message:@"We needs access to your microphone for voice messages. Please go to Settings — Privacy — Microphone and set to ON" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
            }
        };
        
        if ([[AVAudioSession sharedInstance] respondsToSelector:@selector(requestRecordPermission:)])
        {
            [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted)
            {
                dispatch_async(dispatch_get_main_queue(), ^
                {
                    recordBlock(granted);
                });
            }];
        }
        else
            recordBlock(true);
    }];
}

- (NSTimeInterval)currentDuration
{
    return [_modernRecorder currentDuration];
}

- (void)_commitRecord
{
    if(!sessionCanceled){
        [_modernRecorder record];
        
        dispatch_async(dispatch_get_main_queue(), ^
                       {
                           id<AAAudioRecorderDelegate> delegate = _delegate;
                           [_delegate audioRecorderDidStartRecording];
                       });
    }
}

- (void)cleanup
{
    AAOpusAudioRecorder *modernRecorder = _modernRecorder;
    _modernRecorder = nil;
    
    AATimer *timer = _timer;
    _timer = nil;
    
    [[AAAudioRecorder audioRecorderQueue] dispatchOnQueue:^
    {
        [timer invalidate];
        
        if (modernRecorder != nil)
            [modernRecorder stop:NULL];
    }];
}

- (void)cancel
{
    sessionCanceled = true;
    
    [[AAAudioRecorder audioRecorderQueue] dispatchOnQueue:^
    {
        [self cleanup];
    }];
}

- (void)finish:(void (^)(NSString *, NSTimeInterval))completion
{
    [[AAAudioRecorder audioRecorderQueue] dispatchOnQueue:^
    {
        NSString *resultPath = nil;
        NSTimeInterval resultDuration = 0.0;
        
        if (_modernRecorder != nil)
        {
            NSTimeInterval recordedDuration = 0.0;
            NSString *path = [_modernRecorder stop:&recordedDuration];
            if (path != nil && recordedDuration > 0.5)
            {
                resultPath = path;
                resultDuration = recordedDuration;
            }
        }
        
        if (completion != nil)
            completion(resultPath, resultDuration);
    }];
}


@end
