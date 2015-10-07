//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

// UIAppearance+Swift.m
@implementation UIBarButtonItem (UIBarAppearance_Swift)
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass {
    return [self appearanceWhenContainedIn:containerClass, nil];
}
@end
