//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

extension UIViewController {
    func navigateDetail(controller: UIViewController) {
        if (isIPad) {
            var split = self.splitViewController;
            if (split == nil) {
                split = navigationController?.splitViewController
            }
            var master = split!.viewControllers[0]
            var detail = AANavigationController()
            detail.viewControllers = [controller]
            split!.viewControllers = [master, detail]
        } else {
            controller.hidesBottomBarWhenPushed = true
            navigationController?.pushViewController(controller, animated: true);
        }
    }
}

extension UIViewController {
    func navigateNext(controller: UIViewController, removeCurrent: Bool = false) {
        controller.hidesBottomBarWhenPushed = true
        if (!removeCurrent) {
            self.navigationController!.pushViewController(controller, animated: true);
        } else {
            var nControllers : [AnyObject] = []
            var oldControllers = self.navigationController!.viewControllers
            if (oldControllers.count >= 1) {
                for i in 0...(oldControllers.count - 2) {
                    nControllers.append(oldControllers[i])
                }
            }
            nControllers.append(controller)
            self.navigationController!.setViewControllers(nControllers, animated: true);
        }
    }
    
    func navigateBack() {
        self.navigationController!.popViewControllerAnimated(true)
    }
}