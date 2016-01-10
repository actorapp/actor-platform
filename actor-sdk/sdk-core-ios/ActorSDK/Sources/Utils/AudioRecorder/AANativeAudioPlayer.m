#import "AANativeAudioPlayer.h"

#import "ASQueue.h"

#import <AVFoundation/AVFoundation.h>

@interface AANativeAudioPlayer () <AVAudioPlayerDelegate>
{
    AVAudioPlayer *_audioPlayer;
}

@end

@implementation AANativeAudioPlayer

- (instancetype)initWithPath:(NSString *)path
{
    self = [super init];
    if (self != nil)
    {
        __autoreleasing NSError *error = nil;
        _audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL fileURLWithPath:path] error:&error];
        _audioPlayer.delegate = self;
        
        if (_audioPlayer == nil || error != nil)
        {
            [self cleanupWithError];
        }
    }
    return self;
}

- (void)dealloc
{
    [self cleanup];
}

- (void)cleanupWithError
{
    [self cleanup];
}

- (void)cleanup
{
    AVAudioPlayer *audioPlayer = _audioPlayer;
    _audioPlayer.delegate = nil;
    _audioPlayer = nil;
    
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        [audioPlayer stop];
    }];
    
    [self _endAudioSessionFinal];
}

- (void)playFromPosition:(NSTimeInterval)position
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        [self _beginAudioSession];
        
        if (position >= 0.0)
            [_audioPlayer setCurrentTime:position];
        [_audioPlayer play];
    }];
}

- (void)pause
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        [_audioPlayer pause];
    }];
}

- (void)stop
{
    [[AAAudioPlayer _playerQueue] dispatchOnQueue:^
    {
        [_audioPlayer stop];
    }];
}

- (NSTimeInterval)currentPositionSync:(bool)sync
{
    __block NSTimeInterval result = 0.0;
    
    dispatch_block_t block = ^
    {
        result = [_audioPlayer currentTime];
    };
    
    if (sync)
        [[AAAudioPlayer _playerQueue] dispatchOnQueue:block synchronous:true];
    else
        block();
    
    return result;
}

- (NSTimeInterval)duration
{
    return [_audioPlayer duration];
}

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)__unused player successfully:(BOOL)__unused flag
{
    [self _notifyFinished];
}

@end
