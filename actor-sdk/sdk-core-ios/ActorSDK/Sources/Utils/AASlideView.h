//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//


#import <UIKit/UIKit.h>

@interface AASlideView : UIView

@property (nonatomic, strong) UILabel *textLabel;
@property (nonatomic, strong) UIImageView *arrowImageView;

- (void)updateLocation:(CGFloat)offsetX;

@end
