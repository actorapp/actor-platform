#import "AAAudioPlayer.h"

#import "ASQueue.h"

#import "AAOpusAudioPlayerAU.h"
#import "AANativeAudioPlayer.h"


#import <AVFoundation/AVFoundation.h>

@interface AAAudioPlayer ()
{
    bool _audioSessionIsActive;
    bool _proximityState;
}

@end

@implementation AAAudioPlayer

+ (AAAudioPlayer *)audioPlayerForPath:(NSString *)path
{
    if (path == nil)
        return nil;
    
    if ([AAOpusAudioPlayerAU canPlayFile:path])
        return [[AAOpusAudioPlayerAU alloc] initWithPath:path];
    else
        return [[AANativeAudioPlayer alloc] initWithPath:path];
}

- (instancetype)init
{
    self = [super init];
    if (self != nil)
    {
    }
    return self;
}

- (void)dealloc
{
}

- (void)play
{
    [self playFromPosition:-1.0];
}

- (void)playFromPosition:(NSTimeInterval)__unused position
{
}

- (void)pause
{
}

- (void)stop
{
}

- (NSTimeInterval)currentPositionSync:(bool)__unused sync
{
    return 0.0;
}

- (NSTimeInterval)duration
{
    return 0.0;
}

+ (ASQueue *)_playerQueue
{
    static ASQueue *queue = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^
    {
        queue = [[ASQueue alloc] initWithName:"im.actor.audioPlayerQueue"];
    });
    
    return queue;
}

- (void)_beginAudioSession
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        if (!_audioSessionIsActive)
        {
            __autoreleasing NSError *error = nil;
            AVAudioSession *audioSession = [AVAudioSession sharedInstance];
            bool overridePort = _proximityState && ![AAAudioPlayer isHeadsetPluggedIn];
            if (![audioSession setCategory:overridePort ? AVAudioSessionCategoryPlayAndRecord :AVAudioSessionCategoryPlayback error:&error])
                NSLog(@"[AAAudioPlayer audio session set category failed: %@]", error);
            else if (![audioSession setActive:true error:&error])
                NSLog(@"[AAAudioPlayer audio session activation failed: %@]", error);
            else
            {
//                if (![audioSession overrideOutputAudioPort:overridePort ? AVAudioSessionPortOverrideNone : AVAudioSessionPortOverrideSpeaker error:&error])
//                    NSLog(@"[AAAudioPlayer override route failed: %@]", error);
                _audioSessionIsActive = true;
            }
        }
    }];
}

- (void)_endAudioSession
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        if (_audioSessionIsActive)
        {
            __autoreleasing NSError *error = nil;
            AVAudioSession *audioSession = [AVAudioSession sharedInstance];
            if (![audioSession setActive:false error:&error])
                NSLog(@"[AAAudioPlayer audio session deactivation failed: %@]", error);
            if (![audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:&error])
                NSLog(@"[AAAudioPlayer override route failed: %@]", error);
            
            _audioSessionIsActive = false;
        }
    }];
}

- (void)_endAudioSessionFinal
{
    bool audioSessionIsActive = _audioSessionIsActive;
    _audioSessionIsActive = false;
    
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        if (audioSessionIsActive)
        {
            __autoreleasing NSError *error = nil;
            AVAudioSession *audioSession = [AVAudioSession sharedInstance];
            if (![audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:&error])
                NSLog(@"[AAAudioPlayer override route failed: %@]", error);
            if (![audioSession setActive:false error:&error])
                NSLog(@"[AAAudioPlayer audio session deactivation failed: %@]", error);
        }
    }];
}

- (void)_notifyFinished
{
    id<AAAudioPlayerDelegate> delegate = _delegate;
    if ([delegate respondsToSelector:@selector(audioPlayerDidFinishPlaying:)])
        [delegate audioPlayerDidFinishPlaying:self];
}

+ (BOOL)isHeadsetPluggedIn
{
    AVAudioSessionRouteDescription* route = [[AVAudioSession sharedInstance] currentRoute];
    for (AVAudioSessionPortDescription* desc in [route outputs]) {
        if ([[desc portType] isEqualToString:AVAudioSessionPortHeadphones])
            return YES;
    }
    return NO;
}

@end
