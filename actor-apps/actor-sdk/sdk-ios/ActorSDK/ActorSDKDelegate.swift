//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

/// Actor SDK Delegate that helps you customize logic of messenger
public protocol ActorSDKDelegate {
    
    /// Create initial Auth contoller. With Navigation controller (if needed).
    func actorControllerForAuthStart() -> UIViewController
    
    /// Create root logged in controller. With Navigation controller (if needed).
    func actorControllerForStart() -> UIViewController
    
    /// User profile controller
    func actorControllerForUser(uid: Int) -> UIViewController
    
    /// User profile controller
    func actorControllerForGroup(gid: Int) -> UIViewController
    
    /// Navigate to Next controller
    func navigateNext(controller: UIViewController)
    
    /// Navigate to Detail controller. On iPhone navigate next, 
    /// on iPad change detail controller in split view.
    func navigateDetail(controller: UIViewController)
}

/// Default values of SDK Delegate
public extension ActorSDKDelegate {
    
    /// Default phone activation
    public func actorControllerForAuthStart() -> UIViewController {
        let phoneController = AAAuthPhoneViewController()
        let loginNavigation = AANavigationController(rootViewController: phoneController)
        loginNavigation.navigationBar.tintColor = MainAppTheme.navigation.barColor
        loginNavigation.makeBarTransparent()
        return loginNavigation
    }
    
    /// Default app layout
    public func actorControllerForStart() -> UIViewController {
        let tab = AARootTabViewController()
        tab.viewControllers = [
            AANavigationController(rootViewController: ContactsViewController()),
            AANavigationController(rootViewController: DialogsViewController()),
            AANavigationController(rootViewController: AASettingsViewController())]
        tab.selectedIndex = 0
        tab.selectedIndex = 1
        
        let rootController : UIViewController
        if (isIPad) {
            let splitController = AARootSplitViewController()
            splitController.viewControllers = [tab, AANoSelectionViewController()]
            rootController = splitController
        } else {
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
            rootController = tab
        }
        return rootController
    }
    
    public func actorControllerForUser(uid: Int) -> UIViewController {
        return AAUserViewController(uid: uid)
    }
    
    func actorControllerForGroup(gid: Int) -> UIViewController {
        return AAGroupViewController(gid: gid)
    }
    
    public func navigateNext(controller: UIViewController) {
        
    }
    
    public func navigateDetail(controller: UIViewController) {
        
    }
}

/// Default empty implementation of SDK Delegate
class ActorSDKDelegateDefault: ActorSDKDelegate {
    
}