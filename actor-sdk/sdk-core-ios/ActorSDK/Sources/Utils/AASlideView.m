//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//


#import "AASlideView.h"

@implementation AASlideView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    
    if (self) {
        [self createSubViews];
    }
    
    return self;
}

- (void)createSubViews
{
    
    self.clipsToBounds = YES;
    
    UILabel *label = [[UILabel alloc] initWithFrame:self.bounds];
    CGRect labelframe = label.frame;
    labelframe.origin.x += 10;
    [label setFrame:labelframe];
    
    label.text = @"slide to cancel";
    label.font = [UIFont systemFontOfSize:16.0f];
    label.textAlignment = NSTextAlignmentCenter;
    label.backgroundColor = [UIColor clearColor];
    [self addSubview:label];
    self.textLabel = label;
    
    UIImageView *bkimageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"vm_slidearrow"]];
    CGRect frame = bkimageView.frame;
    frame.origin.x = 2;
    frame.origin.y += 10;
    [bkimageView setFrame:frame];
    [self addSubview:bkimageView];
    self.arrowImageView = bkimageView;
    
}

- (void)updateLocation:(CGFloat)offsetX
{
    CGRect labelFrame = self.textLabel.frame;
    labelFrame.origin.x += offsetX;
    self.textLabel.frame = labelFrame;
    
    CGRect imageFrame = self.arrowImageView.frame;
    imageFrame.origin.x += offsetX;
    self.arrowImageView.frame = imageFrame;
}

@end
