#import "AAAudioPlayer.h"

@interface AAOpusAudioPlayerAU : AAAudioPlayer

+ (bool)canPlayFile:(NSString *)path;

- (instancetype)initWithPath:(NSString *)path;

@end
