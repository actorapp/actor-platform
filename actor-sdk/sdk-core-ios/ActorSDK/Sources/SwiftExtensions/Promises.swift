//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MBProgressHUD

extension ARPromise {
    
    func startUserAction(_ ignore: [String] = []) -> ARPromise {
        
        let window = UIApplication.shared.windows[1]
        let hud = MBProgressHUD(window: window)
        hud.mode = MBProgressHUDMode.indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubview(toFront: hud)
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
    
    func then<T>(_ closure: @escaping (T!) -> ()) -> ARPromise {
        then(PromiseConsumer(closure: closure))
        return self
    }
    
    func after(_ closure: @escaping () -> ()) -> ARPromise {
        then(PromiseConsumerEmpty(closure: closure))
        return self
    }
    
    func failure(withClosure closure: @escaping (JavaLangException!) -> ()) -> ARPromise {
        failure(PromiseConsumer(closure: closure))
        return self
    }
}

class PromiseConsumer<T>: NSObject, ARConsumer {
    
    let closure: (T!) -> ()
    
    init(closure: @escaping (T!) -> ()) {
        self.closure = closure
    }

    func apply(withId t: Any!) {
        closure(t as? T)
    }
}

class PromiseConsumerEmpty: NSObject, ARConsumer {
    
    let closure: () -> ()
    
    init(closure: @escaping () -> ()) {
        self.closure = closure
    }
    
    func apply(withId t: Any!) {
        closure()
    }
}
