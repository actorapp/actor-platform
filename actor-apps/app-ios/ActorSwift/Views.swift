//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

extension UIView {
    func hideView() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 0
        })
    }
    
    func showView() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 1
        })
    }
}