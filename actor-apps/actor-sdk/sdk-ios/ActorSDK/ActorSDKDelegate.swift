//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public protocol ActorSDKDelegate {
    
    /// User profile controller
    func actorControllerForUser(uid: Int) -> UIViewController
    
    /// Create initial Auth contoller. With Navigation controller (if needed).
    func actorControllerForAuthStart() -> UIViewController
    
    /// Create root logged in controller. With Navigation controller (if needed).
    func actorControllerForStart() -> UIViewController
}

public extension ActorSDKDelegate {
    
    public func actorControllerForUser(uid: Int) -> UIViewController {
        fatalError("Not implemented")
    }
    
    public func actorControllerForAuthStart() -> UIViewController {
        let phoneController = AAAuthPhoneViewController()
        let loginNavigation = AANavigationController(rootViewController: phoneController)
        loginNavigation.navigationBar.tintColor = MainAppTheme.navigation.barColor
        loginNavigation.makeBarTransparent()
        return loginNavigation
    }
    
    public func actorControllerForStart() -> UIViewController {
        fatalError("Not implemented")
//        let rootController : UIViewController
//        if (isIPad) {
//            let splitController = MainSplitViewController()
//            splitController.viewControllers = [MainTabViewController(isAfterLogin: logInViewController != nil), NoSelectionViewController()]
//            
//            rootController = splitController
//        } else {
//            let tabController = MainTabViewController(isAfterLogin: logInViewController != nil)
//            binder.bind(Actor.getAppState().isAppLoaded, valueModel2: Actor.getAppState().isAppEmpty) { (loaded: JavaLangBoolean?, empty: JavaLangBoolean?) -> () in
//                if (empty!.booleanValue()) {
//                    if (loaded!.booleanValue()) {
//                        tabController.showAppIsEmptyPlaceholder()
//                    } else {
//                        tabController.showAppIsSyncingPlaceholder()
//                    }
//                } else {
//                    tabController.hidePlaceholders()
//                }
//            }
//            rootController = tabController
//        }
//        return rootController
    }
}