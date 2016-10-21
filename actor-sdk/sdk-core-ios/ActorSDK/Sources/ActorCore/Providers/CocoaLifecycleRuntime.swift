//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

@objc class CocoaLifecycleRuntime: NSObject, ARLifecycleRuntime {
    
    func killApp() {
        [][100]
    }
    
    func makeWakeLock() -> ARWakeLock! {
        return CocoaWakeLock()
    }
}

@objc class CocoaWakeLock: NSObject, ARWakeLock {
    
    fileprivate var background: UIBackgroundTaskIdentifier?
    
    override init() {
        background = UIApplication.shared.beginBackgroundTask(expirationHandler: nil)
        super.init()
    }
    
    public func close() {
        if background != nil {
            UIApplication.shared.endBackgroundTask(background!)
            background = nil
        }
    }
}
