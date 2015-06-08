//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

extension UIViewController {
    
    func execute(command: AMCommand) {
        execute(command, successBlock: nil, failureBlock: nil)
    }
    
    func execute(command: AMCommand, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        var window = UIApplication.sharedApplication().windows[1] as! UIWindow
        var hud = MBProgressHUD(window: window)
        hud.mode = MBProgressHUDMode.Indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubviewToFront(hud)
        hud.show(true)
        command.startWithCallback(CocoaCallback(result: { (val:Any?) -> () in
            dispatch_async(dispatch_get_main_queue(), {
                hud.hide(true)
                successBlock?(val: val)
            })
            }, error: { (val) -> () in
                dispatch_async(dispatch_get_main_queue(), {
                    hud.hide(true)
                    failureBlock?(val: val)
                })
        }))
    }
    
}