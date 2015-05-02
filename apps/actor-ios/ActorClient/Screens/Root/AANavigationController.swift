//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AANavigationController: UINavigationController {
    
    // MARK: -
    // MARK: Methods
    
    func makeBarTransparent() {
        navigationBar.setBackgroundImage(UIImage(), forBarMetrics: UIBarMetrics.Default)
        navigationBar.shadowImage = UIImage()
        navigationBar.translucent = true
    }
    
}
