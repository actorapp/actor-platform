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
        
        let style = ActorSDK.sharedActor().style
        
        // Default Status Bar style
        style.vcStatusBarStyle = .lightContent
        
        // Navigation colors
        style.navigationBgColor = UIColor(rgb: 0xA43436)
        style.dialogStatusActive = UIColor(rgb: 0xff5882)
        style.welcomeBgColor = UIColor(rgb: 0xA43436)
        
        ActorSDK.sharedActor().style.welcomeSignupTextColor = UIColor(rgb: 0xA43436)
        ActorSDK.sharedActor().style.nextBarColor = UIColor(rgb: 0xA43436)
        
        style.navigationTintColor = UIColor.white
        style.navigationTitleColor = UIColor.white
        style.navigationSubtitleColor = UIColor.white.alpha(0.64)
        style.navigationSubtitleActiveColor = UIColor.white
        // style.navigationHairlineHidden = true
        
        // Full screen placeholder. Set here value that matches UINavigationBar color
        style.placeholderBgColor = UIColor(rgb: 0x528dbe)
        
        // Override User's online/offline statuses in navigation color
        style.userOfflineNavigationColor = UIColor.white.alpha(0.64)
        style.userOnlineNavigationColor = UIColor.white
        
        // Override search status bar style
        style.searchStatusBarStyle = .default
        
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
        
//        ActorSDK.sharedActor().endpoints = ["tcp://api-mtproto.im.xloto.com.br:9070"]
        
        ActorSDK.sharedActor().endpoints = ["tcp://192.168.1.3:9070"]
        
//        ActorSDK.sharedActor().endpoints = ["tcp://api-mtproto.actor.diegosilva.com.br:9070"]
        
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
