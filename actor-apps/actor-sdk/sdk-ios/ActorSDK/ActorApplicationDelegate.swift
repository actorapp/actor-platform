//
//  ActorApplicationDelegate.swift
//  ActorSDK
//
//  Created by Steve Kite on 07.10.15.
//  Copyright © 2015 Steve Kite. All rights reserved.
//

import Foundation

public class ActorApplicationDelegate: NSObject, UIApplicationDelegate, ActorSDKDelegate {
    
    public override init() {
        super.init()
        
        ActorSDK.sharedActor().delegate = self
    }
    
    public func applicationDidFinishLaunching(application: UIApplication) {
        ActorSDK.sharedActor().applicationDidFinishLaunching(application)
    }
    
    public func applicationDidBecomeActive(application: UIApplication) {
        ActorSDK.sharedActor().applicationDidBecomeActive(application)
    }
    
    public func applicationWillEnterForeground(application: UIApplication) {
        ActorSDK.sharedActor().applicationWillEnterForeground(application)
    }
    
    public func applicationDidEnterBackground(application: UIApplication) {
        ActorSDK.sharedActor().applicationDidEnterBackground(application)
    }
    
    public func applicationWillResignActive(application: UIApplication) {
        ActorSDK.sharedActor().applicationWillResignActive(application)
    }
    
    public func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        ActorSDK.sharedActor().application(application, didReceiveRemoteNotification: userInfo)
    }
    
    public func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        let tokenString = "\(deviceToken)".replace(" ", dest: "").replace("<", dest: "").replace(">", dest: "")
        ActorSDK.sharedActor().pushRegisterToken(tokenString)
    }
    
    public func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject], fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        ActorSDK.sharedActor().application(application, didReceiveRemoteNotification: userInfo, fetchCompletionHandler: completionHandler)
    }
    
    public func application(application: UIApplication, performFetchWithCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        ActorSDK.sharedActor().application(application, performFetchWithCompletionHandler: completionHandler)
    }
}