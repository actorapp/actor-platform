//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AAImagePickerController: UIImagePickerController {
    public override func preferredStatusBarStyle() -> UIStatusBarStyle {
        return ActorSDK.sharedActor().style.vcStatusBarStyle
    }

    public override func supportedInterfaceOrientations() -> UIInterfaceOrientationMask {
        return UIInterfaceOrientationMask.Landscape
    }

//    public override func childViewControllerForStatusBarStyle() -> UIViewController? {
//        return nil
//    }
//    
//    public override func prefersStatusBarHidden() -> Bool {
//        return true
//    }
//    
//    public override func viewWillAppear(animated: Bool) {
//        super.viewWillAppear(animated)
//        UIApplication.sharedApplication().setStatusBarHidden(true, withAnimation: UIStatusBarAnimation.Fade)
//    }
//    
//    public override func viewWillDisappear(animated: Bool) {
//        super.viewWillDisappear(animated)
//        UIApplication.sharedApplication().setStatusBarHidden(false, withAnimation: UIStatusBarAnimation.Fade)
//    }
}