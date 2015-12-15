#import <Foundation/Foundation.h>

typedef double MTAbsoluteTime;
MTAbsoluteTime MTAbsoluteSystemTime();

@protocol AAModernViewInlineMediaContextDelegate <NSObject>

@optional

- (void)inlineMediaPlaybackStateUpdated:(bool)isPaused playbackPosition:(float)playbackPosition timestamp:(NSTimeInterval)timestamp preciseDuration:(NSTimeInterval)preciseDuration;

@end

@interface AAModernViewInlineMediaContext : NSObject

@property (nonatomic, weak) id<AAModernViewInlineMediaContextDelegate> delegate;

- (void)setDelegate:(id<AAModernViewInlineMediaContextDelegate>)delegate;
- (void)removeDelegate:(id<AAModernViewInlineMediaContextDelegate>)delegate;

- (bool)isPlaybackActive;
- (bool)isPaused;
- (float)playbackPosition:(NSTimeInterval *)timestamp;
- (float)playbackPosition:(NSTimeInterval *)timestamp sync:(bool)sync;
- (NSTimeInterval)preciseDuration;

- (void)play;
- (void)play:(float)playbackPosition;
- (void)pause;

- (void)postUpdatePlaybackPosition:(bool)sync;

@end
