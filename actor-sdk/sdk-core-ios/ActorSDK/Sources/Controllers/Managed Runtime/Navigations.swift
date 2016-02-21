//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension UIViewController {
    public func navigateDetail(controller: UIViewController) {
        if (AADevice.isiPad) {
            let split = UIApplication.sharedApplication().keyWindow?.rootViewController as! UISplitViewController
            let master = split.viewControllers[0]
            let detail = AANavigationController()
            detail.viewControllers = [controller]
            split.viewControllers = [master, detail]
        } else {
            let tabBar = UIApplication.sharedApplication().keyWindow?.rootViewController as! UITabBarController
            controller.hidesBottomBarWhenPushed = true
            (tabBar.selectedViewController as! AANavigationController).pushViewController(controller, animated: true)
        }
    }
}

public extension UIViewController {
    public func navigateNext(controller: UIViewController, removeCurrent: Bool = false) {
        if let aaC = controller as? AAViewController, let aaSelf = self as? AAViewController  {
            aaC.popover = aaSelf.popover
        }
        
        controller.hidesBottomBarWhenPushed = true
        if (!removeCurrent) {
            self.navigationController!.pushViewController(controller, animated: true);
        } else {
            var nControllers = [UIViewController]()
            var oldControllers = self.navigationController!.viewControllers
            if (oldControllers.count >= 2) {
                for i in 0...(oldControllers.count - 2) {
                    nControllers.append(oldControllers[i])
                }
            }
            nControllers.append(controller)
            self.navigationController!.setViewControllers(nControllers, animated: true);
        }
    }
    
    public func navigateBack() {
        if (self.navigationController!.viewControllers.last != nil) {
            if (self.navigationController!.viewControllers.last! == self) {
                self.navigationController!.popViewControllerAnimated(true)
            } else {
            
            }
        } else {
            var nControllers = [UIViewController]()
            var oldControllers = self.navigationController!.viewControllers
            for i in 0..<oldControllers.count {
                if (oldControllers[i] != self) {
                    nControllers.append(oldControllers[i])
                }
            }
            self.navigationController!.setViewControllers(nControllers, animated: true);
        }
    }
}