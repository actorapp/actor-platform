//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MBProgressHUD

public extension ARPromise {
    
    public func startUserAction(_ ignore: [String] = []) -> ARPromise {
        
        let window = UIApplication.shared.windows[0]
       // let hud = MBProgressHUD(window: window)
        
        let hud = MBProgressHUD(view: window)
        hud.mode = MBProgressHUDMode.indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubview(toFront: hud)
        //hud.show(true)
        hud.show(animated: true)
        
        then { (t: AnyObject!) -> () in
            hud.hide(animated:true)
        }
        
        failure { (e) -> () in
            hud.hide(animated:true)
            if let rpc = e as? ACRpcException {
                if ignore.contains(rpc.tag) {
                    return
                }
            }
            AAExecutions.errorWithError(e)
        }
        
        return self
    }
    
    public func then<T>(_ closure: @escaping (T!) -> ()) -> ARPromise {
        then(PromiseConsumer(closure: closure))
        return self
    }
    
    public func after(_ closure: @escaping () -> ()) -> ARPromise {
        then(PromiseConsumerEmpty(closure: closure))
        return self
    }
    
    public func failure(withClosure closure: @escaping (JavaLangException!) -> ()) -> ARPromise {
        failure(PromiseConsumer(closure: closure))
        return self
    }
}

open class PromiseConsumer<T>: NSObject, ARConsumer {
    
    let closure: (T!) -> ()
    
    init(closure: @escaping (T!) -> ()) {
        self.closure = closure
    }

    open func apply(withId t: Any!) {
        closure(t as? T)
    }
}

open class PromiseConsumerEmpty: NSObject, ARConsumer {
    
    let closure: () -> ()
    
    init(closure: @escaping () -> ()) {
        self.closure = closure
    }
    
    open func apply(withId t: Any!) {
        closure()
    }
}
