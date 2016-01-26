#import <Foundation/Foundation.h>

@class AAAudioRecorder;

@protocol AAAudioRecorderDelegate <NSObject>

@required

- (void)audioRecorderDidStartRecording;

@end

@interface AAAudioRecorder : NSObject

@property (nonatomic, weak) id<AAAudioRecorderDelegate> delegate;
@property (nonatomic, strong) id activityHolder;

- (void)start;
- (NSTimeInterval)currentDuration;
- (void)cancel;
- (void)finish:(void (^)(NSString *, NSTimeInterval))completion;

@end
