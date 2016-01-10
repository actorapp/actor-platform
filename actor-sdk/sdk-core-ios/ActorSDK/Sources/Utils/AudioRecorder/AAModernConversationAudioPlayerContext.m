#import "AAModernConversationAudioPlayerContext.h"

#import "AAModernConversationAudioPlayer.h"

@interface AAModernConversationAudioPlayerContext ()
{
    __weak AAModernConversationAudioPlayer *_audioPlayer;
}

@end

@implementation AAModernConversationAudioPlayerContext

- (instancetype)initWithAudioPlayer:(AAModernConversationAudioPlayer *)audioPlayer
{
    self = [super init];
    if (self != nil)
    {
        _audioPlayer = audioPlayer;
    }
    return self;
}

- (bool)isPlaybackActive
{
    return true;
}

- (bool)isPaused
{
    AAModernConversationAudioPlayer *audioPlayer = _audioPlayer;
    return [audioPlayer isPaused];
}

- (float)playbackPosition:(MTAbsoluteTime *)timestamp sync:(bool)sync
{
    if (timestamp != NULL)
        *timestamp = MTAbsoluteSystemTime();
    
    AAModernConversationAudioPlayer *audioPlayer = _audioPlayer;
    return [audioPlayer playbackPositionSync:sync];
}

- (NSTimeInterval)preciseDuration
{
    AAModernConversationAudioPlayer *audioPlayer = _audioPlayer;
    return [audioPlayer duration];
}

- (void)play
{
    AAModernConversationAudioPlayer *audioPlayer = _audioPlayer;
    [audioPlayer play];
}

- (void)play:(float)playbackPosition
{
    AAModernConversationAudioPlayer *audioPlayer = _audioPlayer;
    [audioPlayer play:playbackPosition];
}

- (void)pause
{
    AAModernConversationAudioPlayer *audioPlayer = _audioPlayer;
    [audioPlayer pause];
}

@end
