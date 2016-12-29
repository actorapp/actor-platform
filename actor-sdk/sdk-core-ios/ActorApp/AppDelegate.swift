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
        
        ActorSDK.sharedActor().enableVideoCalls = false
        
        // Setting Development Push Id
        ActorSDK.sharedActor().apiPushId = 868547
        ActorSDK.sharedActor().autoPushMode = .afterLogin
        
        ActorSDK.sharedActor().authStrategy = .phoneEmail
        
        ActorSDK.sharedActor().style.dialogAvatarSize = 58
        
       // ActorSDK.sharedActor().autoJoinGroups = ["canalxloto"]
        
        //ActorSDK.sharedActor().endpoints = ["tcp://api-mtproto.im.xloto.com.br:9070"]
        
        ActorSDK.sharedActor().endpoints = ["tcp://192.168.1.8:9070"]
        
        //AppCocoaHttpRuntime.getMethod("")
        
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
        return 1
    }
    
    open override func showStickersButton() -> Bool{
        return false
    }
    
    open override func useOnClientPrivacy() -> Bool{
        return true
    }
}
