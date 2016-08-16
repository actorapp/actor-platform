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
        
        ActorSDK.sharedActor().enableCalls = true
        
        ActorSDK.sharedActor().enableVideoCalls = true
        
        ActorSDK.sharedActor().enableSecretChats = true
        
        // Setting Development Push Id
        ActorSDK.sharedActor().apiPushId = 868547
        
        ActorSDK.sharedActor().authStrategy = .PhoneEmail
        
        ActorSDK.sharedActor().style.dialogAvatarSize = 58
        ActorSDK.sharedActor().style.dialogTitleSecureColor = UIColor.greenColor()
        
        ActorSDK.sharedActor().autoJoinGroups = ["actor_news"]
        
        // Creating Actor
        ActorSDK.sharedActor().createActor()
        
    }
    
    public override func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        return true;
    }
    
    public override func actorRootControllers() -> [UIViewController]? {
        return [AAContactsViewController(), AARecentViewController(), AASettingsViewController()]
    }
    
    public override func actorRootInitialControllerIndex() -> Int? {
        return 0
    }
}