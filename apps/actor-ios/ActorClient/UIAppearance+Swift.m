//
//  UIAppearance+Swift.m
//  ActorApp
//
//  Created by Stepan Korshakov on 14.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

#import <Foundation/Foundation.h>

// UIAppearance+Swift.m
@implementation UIView (UIViewAppearance_Swift)
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass {
    return [self appearanceWhenContainedIn:containerClass, nil];
}
@end