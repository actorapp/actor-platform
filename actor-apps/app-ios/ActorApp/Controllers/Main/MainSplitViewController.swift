//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class MainSplitViewController: UISplitViewController {
    
    init() {
        super.init(nibName: nil, bundle: nil)
        
        preferredDisplayMode = .AllVisible
        if (interfaceOrientation == UIInterfaceOrientation.Portrait || interfaceOrientation == UIInterfaceOrientation.PortraitUpsideDown) {
            minimumPrimaryColumnWidth = CGFloat(300.0)
            maximumPrimaryColumnWidth = CGFloat(300.0)
        } else {
            minimumPrimaryColumnWidth = CGFloat(360.0)
            maximumPrimaryColumnWidth = CGFloat(360.0)
        }
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func willRotateToInterfaceOrientation(toInterfaceOrientation: UIInterfaceOrientation, duration: NSTimeInterval) {
        super.willRotateToInterfaceOrientation(toInterfaceOrientation, duration: duration)
        
        if (toInterfaceOrientation == UIInterfaceOrientation.Portrait || toInterfaceOrientation == UIInterfaceOrientation.PortraitUpsideDown) {
            minimumPrimaryColumnWidth = CGFloat(300.0)
            maximumPrimaryColumnWidth = CGFloat(300.0)
        } else {
            minimumPrimaryColumnWidth = CGFloat(360.0)
            maximumPrimaryColumnWidth = CGFloat(360.0)
        }
    }
    
    override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
}