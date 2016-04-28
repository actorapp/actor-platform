//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

import ActorSDK

@objc public class AppDelegate : ActorApplicationDelegate {
    
    override init() {
        super.init()
        
        ActorSDK.sharedActor().inviteUrlHost = "quit.email"
        ActorSDK.sharedActor().inviteUrlScheme = "actor"
        
        ActorSDK.sharedActor().style.searchStatusBarStyle = .Default
        
        // Enabling experimental features
        ActorSDK.sharedActor().enableExperimentalFeatures = true
        
        // Setting Development Push Id
        ActorSDK.sharedActor().apiPushId = 868547
        
        ActorSDK.sharedActor().authStrategy = .Username
        
        // Creating Actor
        ActorSDK.sharedActor().createActor()
        
    }
    
    public override func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        return true;
    }
}