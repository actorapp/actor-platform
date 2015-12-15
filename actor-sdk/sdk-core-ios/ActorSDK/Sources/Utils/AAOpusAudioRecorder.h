#import <Foundation/Foundation.h>

@interface AAOpusAudioRecorder : NSObject

- (instancetype)initWithFileEncryption:(bool)fileEncryption;

- (void)record;
- (NSString *)stop:(NSTimeInterval *)recordedDuration;
- (NSTimeInterval)currentDuration;

@end
