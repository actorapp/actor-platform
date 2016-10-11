//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

import ActorSDK

open class AppDelegate : ActorApplicationDelegate {
    
    override init() {
        super.init()
        
        ActorSDK.sharedActor().inviteUrlHost = "quit.email"
        ActorSDK.sharedActor().inviteUrlScheme = "actor"
        
        ActorSDK.sharedActor().style.searchStatusBarStyle = .default
        
        // Enabling experimental features
        ActorSDK.sharedActor().enableExperimentalFeatures = true
        
        ActorSDK.sharedActor().enableCalls = true
        
        ActorSDK.sharedActor().enableVideoCalls = true
        
        // Setting Development Push Id
        ActorSDK.sharedActor().apiPushId = 868547
        
        ActorSDK.sharedActor().authStrategy = .phoneEmail
        
        ActorSDK.sharedActor().style.dialogAvatarSize = 58
        
        ActorSDK.sharedActor().autoJoinGroups = ["actor_news"]
        
        // Creating Actor
        ActorSDK.sharedActor().createActor()
        
    }
    
    open override func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]?) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
        
        ActorSDK.sharedActor().presentMessengerInNewWindow()
        
        return true
    }
    
    open override func actorRootControllers() -> [UIViewController]? {
        return [AAContactsViewController(), AARecentViewController(), AASettingsViewController()]
    }
    
    open override func actorRootInitialControllerIndex() -> Int? {
        return 0
    }
}
