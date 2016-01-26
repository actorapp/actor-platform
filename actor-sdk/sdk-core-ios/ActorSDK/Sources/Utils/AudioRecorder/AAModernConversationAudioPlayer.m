#import "AAModernConversationAudioPlayer.h"

#import "AATimerTarget.h"

#import "AAModernConversationAudioPlayerContext.h"

#import "AAAudioPlayer.h"

@interface AAModernConversationAudioPlayer () <AAAudioPlayerDelegate>
{
    AAAudioPlayer *_audioPlayer;
    NSTimer *_timer;
    
    AAModernConversationAudioPlayerContext *_inlineMediaContext;
    
    bool _isPaused;
}

@end

@implementation AAModernConversationAudioPlayer

- (instancetype)initWithFilePath:(NSString *)filePath
{
    self = [super init];
    if (self != nil)
    {
        _audioPlayer = [AAAudioPlayer audioPlayerForPath:filePath];
        _audioPlayer.delegate = self;
    }
    return self;
}

- (void)dealloc
{
    [self cleanup];
}

- (void)cleanup
{
    if (_timer != nil)
    {
        [_timer invalidate];
        _timer = nil;
    }
    
    if (_audioPlayer != nil)
    {
        _audioPlayer.delegate = nil;
        [_audioPlayer stop];
        _audioPlayer = nil;
    }
}

- (AAModernViewInlineMediaContext *)inlineMediaContext
{
    if (_inlineMediaContext == nil)
        _inlineMediaContext = [[AAModernConversationAudioPlayerContext alloc] initWithAudioPlayer:self];
    
    return _inlineMediaContext;
}

- (void)play
{
    _isPaused = false;
    
    if (_timer != nil)
    {
        [_timer invalidate];
        _timer = nil;
    }
    
    [_audioPlayer play];
    
    [self updateCurrentTime];
    _timer = [AATimerTarget scheduledMainThreadTimerWithTarget:self action:@selector(updateCurrentTime) interval:0.25 repeat:true];
}

- (void)play:(float)playbackPosition
{
    _isPaused = false;
    
    if (_timer != nil)
    {
        [_timer invalidate];
        _timer = nil;
    }
    
    NSTimeInterval preciseDuration = [_audioPlayer duration];
    if (preciseDuration > 0.1)
    {
        [_audioPlayer playFromPosition:MAX(0.0, MIN(preciseDuration, playbackPosition * preciseDuration))];
        [_inlineMediaContext postUpdatePlaybackPosition:true];
    }
    else
    {
        [_audioPlayer play];
        [self updateCurrentTime];
    }
    
    _timer = [AATimerTarget scheduledMainThreadTimerWithTarget:self action:@selector(updateCurrentTime) interval:0.25 repeat:true];
}

- (void)updateCurrentTime
{
    [_inlineMediaContext postUpdatePlaybackPosition:false];
}

- (void)pause
{
    _isPaused = true;
    
    [_audioPlayer pause];
    
    if (_timer != nil)
    {
        [_timer invalidate];
        _timer = nil;
    }
    
    [_inlineMediaContext postUpdatePlaybackPosition:false];
}

- (void)stop
{
    _isPaused = true;
    
    [_audioPlayer stop];
    
    if (_timer != nil)
    {
        [_timer invalidate];
        _timer = nil;
    }
    
    [self cleanup];
    
}

- (float)playbackPosition
{
    return [self playbackPositionSync:false];
}

- (float)playbackPositionSync:(bool)sync
{
    NSTimeInterval duration = [_audioPlayer duration];
    if (duration > 0.1)
        return (float)([_audioPlayer currentPositionSync:sync] / duration);
    
    return 0.0f;
}

- (NSTimeInterval)duration
{
    return [_audioPlayer duration];
}

- (bool)isPaused
{
    return _isPaused;
}

- (void)audioPlayerDidFinishPlaying:(AAAudioPlayer *)__unused audioPlayer
{
    dispatch_async(dispatch_get_main_queue(), ^
    {
        _isPaused = true;
        
        if (_timer != nil)
        {
            [_timer invalidate];
            _timer = nil;
        }
        
        [_inlineMediaContext postUpdatePlaybackPosition:false];
        
        [self cleanup];
        
        id<AAModernConversationAudioPlayerDelegate> delegate = _delegate;
        if ([delegate respondsToSelector:@selector(audioPlayerDidFinish)])
            [delegate audioPlayerDidFinish];
    });
}

- (void)audioPlayerStopAndFinish
{
    dispatch_async(dispatch_get_main_queue(), ^
                   {
                       
                       _isPaused = true;
                       
                       if (_timer != nil)
                       {
                           [_timer invalidate];
                           _timer = nil;
                       }
                       
                       [_inlineMediaContext postUpdatePlaybackPosition:false];
                       
                       [self cleanup];

                       [_delegate audioPlayerDidFinish];
                       
                       //[_audioPlayer _notifyFinished];
                       
                       //[_inlineMediaContext postUpdatePlaybackPosition:false];
                       
                       //[self cleanup];
                       
//                       id<AAModernConversationAudioPlayerDelegate> delegate = _delegate;
//                       if ([delegate respondsToSelector:@selector(audioPlayerDidFinish)])
//                           [delegate audioPlayerDidFinish];
                   });
}

@end
