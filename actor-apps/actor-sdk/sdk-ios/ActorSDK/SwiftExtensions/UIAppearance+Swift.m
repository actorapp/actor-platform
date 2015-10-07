//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

// UIAppearance+Swift.m
@implementation UIView (UIViewAppearance_Swift)
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass {
    return [self appearanceWhenContainedIn:containerClass, nil];
}
@end