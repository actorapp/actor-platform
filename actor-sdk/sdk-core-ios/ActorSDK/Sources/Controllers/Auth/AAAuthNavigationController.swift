//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthNavigationController: UINavigationController {
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationBar.setTransparentBackground()
        navigationBar.tintColor = ActorSDK.sharedActor().style.authTintColor
        navigationBar.hairlineHidden = true
        
        view.backgroundColor = UIColor.whiteColor()
    }

    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        UIApplication.sharedApplication().setStatusBarStyle(.Default, animated: true)
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        UIApplication.sharedApplication().setStatusBarStyle(.LightContent, animated: true)
    }
    
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return .Default
    }
}