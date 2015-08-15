//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//
#import <UIKit/UIKit.h>

@class ABActionShit;

@protocol ABActionShitDelegate <NSObject>
@optional
- (void)actionShit:(ABActionShit *)actionShit clickedButtonAtIndex:(NSInteger)buttonIndex;
- (void)actionShitClickedCancelButton:(ABActionShit *)actionShit;
@end


@interface ABActionShit : UIView

@property (nonatomic, weak) id<ABActionShitDelegate> delegate;
@property (nonatomic, strong) NSArray *buttonTitles;
@property (nonatomic, assign) NSUInteger destructiveButtonIndex;
@property (nonatomic, strong) NSString *cancelButtonTitle;
@property (nonatomic, assign) BOOL cancelButtonHidden;

- (void)showWithCompletion:(void(^)())completion;
- (void)hideWithCompletion:(void(^)())completion;

@end
