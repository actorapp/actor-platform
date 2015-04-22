//
//  UIBarAppearance+Swift.h
//  ActorApp
//
//  Created by Stepan Korshakov on 14.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

#ifndef ActorApp_UIBarAppearance_Swift_h
#define ActorApp_UIBarAppearance_Swift_h

// UIAppearance+Swift.h
@interface UIBarButtonItem (UIBarAppearance_Swift)
// appearanceWhenContainedIn: is not available in Swift. This fixes that.
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass;
@end

#endif
