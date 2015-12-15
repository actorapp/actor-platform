#import <Foundation/Foundation.h>

@class ASQueue;
@class AAAudioPlayer;

@protocol AAAudioPlayerDelegate <NSObject>

@optional

- (void)audioPlayerDidFinishPlaying:(AAAudioPlayer *)audioPlayer;

@end

@interface AAAudioPlayer : NSObject

@property (nonatomic, weak) id<AAAudioPlayerDelegate> delegate;

+ (AAAudioPlayer *)audioPlayerForPath:(NSString *)path;

- (void)play;
- (void)playFromPosition:(NSTimeInterval)position;
- (void)pause;
- (void)stop;
- (NSTimeInterval)currentPositionSync:(bool)sync;
- (NSTimeInterval)duration;

+ (ASQueue *)_playerQueue;
- (void)_beginAudioSession;
- (void)_endAudioSession;
- (void)_endAudioSessionFinal;
- (void)_notifyFinished;

@end
