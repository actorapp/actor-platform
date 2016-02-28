//
//  CocoaLifecycleProvider.swift
//  ActorSDK
//
//  Created by Steve Kite on 28.02.16.
//  Copyright © 2016 Steve Kite. All rights reserved.
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