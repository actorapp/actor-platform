//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AARootSplitViewController: UISplitViewController {
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        
        preferredDisplayMode = .allVisible
        if (interfaceOrientation == UIInterfaceOrientation.portrait || interfaceOrientation == UIInterfaceOrientation.portraitUpsideDown) {
            minimumPrimaryColumnWidth = CGFloat(300.0)
            maximumPrimaryColumnWidth = CGFloat(300.0)
        } else {
            minimumPrimaryColumnWidth = CGFloat(360.0)
            maximumPrimaryColumnWidth = CGFloat(360.0)
        }
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func willRotate(to toInterfaceOrientation: UIInterfaceOrientation, duration: TimeInterval) {
        super.willRotate(to: toInterfaceOrientation, duration: duration)
        
        if (toInterfaceOrientation == UIInterfaceOrientation.portrait || toInterfaceOrientation == UIInterfaceOrientation.portraitUpsideDown) {
            minimumPrimaryColumnWidth = CGFloat(300.0)
            maximumPrimaryColumnWidth = CGFloat(300.0)
        } else {
            minimumPrimaryColumnWidth = CGFloat(360.0)
            maximumPrimaryColumnWidth = CGFloat(360.0)
        }
    }
    
    open override var preferredStatusBarStyle : UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }
}
