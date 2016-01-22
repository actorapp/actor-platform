//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

import Fabric
import Crashlytics
import ActorSDK

@objc public class AppDelegate : ActorApplicationDelegate {
    
    override init() {
        super.init()
        
        // Even when Fabric/Crashlytics not configured
        // this method doesn't crash
        Fabric.with([Crashlytics.self()])
        
        ActorSDK.sharedActor().inviteUrlHost = "quit.email"
        ActorSDK.sharedActor().inviteUrlScheme = "actor"
        
        ActorSDK.sharedActor().style.searchStatusBarStyle = .Default
        
        // Enabling experimental features
        ActorSDK.sharedActor().enableExperimentalFeatures = true
        
        // Creating Actor
        ActorSDK.sharedActor().createActor()
        
    }
    
    public override func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        return true;
    }
}