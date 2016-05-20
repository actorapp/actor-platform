//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MBProgressHUD

extension ARPromise {
    
    func startUserAction(ignore: [String] = []) -> ARPromise {
        
        let window = UIApplication.sharedApplication().windows[1]
        let hud = MBProgressHUD(window: window)
        hud.mode = MBProgressHUDMode.Indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubviewToFront(hud)
        hud.show(true)
        
        then { (t: AnyObject!) -> () in
            hud.hide(true)
        }
        
        failure { (e) -> () in
            hud.hide(true)
            if let rpc = e as? ACRpcException {
                if ignore.contains(rpc.tag) {
                    return
                }
            }
            AAExecutions.errorWithError(e)
        }
        
        return self
    }
    
    func then<T>(closure: (T!) -> ()) -> ARPromise {
        then(PromiseConsumer(closure: closure))
        return self
    }
    
    func failure(withClosure closure: (JavaLangException!) -> ()) -> ARPromise {
        failure(PromiseConsumer(closure: closure))
        return self
    }
}

class PromiseConsumer<T>: NSObject, ARConsumer {
    
    let closure: (T!) -> ()
    
    init(closure: (T!) -> ()) {
        self.closure = closure
    }

    func applyWithId(t: AnyObject!) {
        closure(t as? T)
    }
}