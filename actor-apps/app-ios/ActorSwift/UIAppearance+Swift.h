//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

#ifndef ActorApp_UIAppearance_Swift_h
#define ActorApp_UIAppearance_Swift_h

// UIAppearance+Swift.h
@interface UIView (UIViewAppearance_Swift)
// appearanceWhenContainedIn: is not available in Swift. This fixes that.
+ (instancetype)my_appearanceWhenContainedIn:(Class<UIAppearanceContainer>)containerClass;
@end

#endif
