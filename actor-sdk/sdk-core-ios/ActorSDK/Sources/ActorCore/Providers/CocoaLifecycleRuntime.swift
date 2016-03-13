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
    
    private var background: UIBackgroundTaskIdentifier?
    
    override init() {
        background = UIApplication.sharedApplication().beginBackgroundTaskWithExpirationHandler(nil)
        super.init()
    }
    
    func releaseLock() {
        if background != nil {
            UIApplication.sharedApplication().endBackgroundTask(background!)
            background = nil
        }
    }
}