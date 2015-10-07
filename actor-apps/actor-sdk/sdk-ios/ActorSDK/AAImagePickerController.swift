//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AAImagePickerController: UIImagePickerController {
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return UIStatusBarStyle.LightContent
    }
    
    public override func childViewControllerForStatusBarStyle() -> UIViewController? {
        return nil
    }
    
    public override func prefersStatusBarHidden() -> Bool {
        return true
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        UIApplication.sharedApplication().setStatusBarHidden(true, withAnimation: UIStatusBarAnimation.Fade)
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.sharedApplication().setStatusBarHidden(false, withAnimation: UIStatusBarAnimation.Fade)
    }
}