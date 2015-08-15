//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

#ifndef ActorApp_UIBarAppearance_Swift_h
#define ActorApp_UIBarAppearance_Swift_h

// UIAppearance+Swift.h
@interface UIBarButtonItem (UIBarAppearance_Swift)
// appearanceWhenContainedIn: is not available in Swift. This fixes that.
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass;
@end

#endif
