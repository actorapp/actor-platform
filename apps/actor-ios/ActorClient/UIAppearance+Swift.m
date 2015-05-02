//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

#import <Foundation/Foundation.h>

// UIAppearance+Swift.m
@implementation UIView (UIViewAppearance_Swift)
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass {
    return [self appearanceWhenContainedIn:containerClass, nil];
}
@end