//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class Utils: NSObject {
    
    class func isRetina() -> Bool {
        return UIScreen.mainScreen().scale > 1
    }
    
    class func retinaPixel() -> CGFloat {
        if Utils.isRetina() {
            return 0.5
        }
        return 1.0
    }
    
}

extension UIViewController {
    
    func getNavigationBarHeight() -> CGFloat {
        if (navigationController != nil) {
            return navigationController!.navigationBar.frame.height
        } else {
            return CGFloat(44)
        }
    }
    
    func getStatusBarHeight() -> CGFloat {
        let statusBarSize = UIApplication.sharedApplication().statusBarFrame.size
        return min(statusBarSize.width, statusBarSize.height)
    }
}