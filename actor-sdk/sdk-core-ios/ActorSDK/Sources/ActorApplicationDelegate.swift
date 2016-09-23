//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class ActorApplicationDelegate: ActorSDKDelegateDefault, UIApplicationDelegate {
    
    public override init() {
        super.init()
        
        ActorSDK.sharedActor().delegate = self
    }
    
    open func applicationDidFinishLaunching(_ application: UIApplication) {
        ActorSDK.sharedActor().applicationDidFinishLaunching(application)
    }
    
    open func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        ActorSDK.sharedActor().applicationDidFinishLaunching(application)
        return true
    }
    
    open func applicationDidBecomeActive(_ application: UIApplication) {
        ActorSDK.sharedActor().applicationDidBecomeActive(application)
    }
    
    open func applicationWillEnterForeground(_ application: UIApplication) {
        ActorSDK.sharedActor().applicationWillEnterForeground(application)
    }
    
    open func applicationDidEnterBackground(_ application: UIApplication) {
        ActorSDK.sharedActor().applicationDidEnterBackground(application)
    }
    
    open func applicationWillResignActive(_ application: UIApplication) {
        ActorSDK.sharedActor().applicationWillResignActive(application)
    }
    
    open func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
        ActorSDK.sharedActor().application(application, didReceiveRemoteNotification: userInfo)
    }
    
    open func application(_ application: UIApplication, didRegister notificationSettings: UIUserNotificationSettings) {
        ActorSDK.sharedActor().application(application, didRegisterUserNotificationSettings: notificationSettings)
    }
    
    open func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenString = "\(deviceToken)".replace(" ", dest: "").replace("<", dest: "").replace(">", dest: "")
        ActorSDK.sharedActor().pushRegisterToken(tokenString)
    }
    
    open func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        ActorSDK.sharedActor().application(application, didReceiveRemoteNotification: userInfo, fetchCompletionHandler: completionHandler)
    }
    
    open func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        ActorSDK.sharedActor().application(application, performFetchWithCompletionHandler: completionHandler)
    }
    
    open func application(_ application: UIApplication, open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        return ActorSDK.sharedActor().application(application, openURL: url, sourceApplication: sourceApplication, annotation: annotation as AnyObject)
    }
    
    open func application(_ application: UIApplication, handleOpen url: URL) -> Bool {
        return ActorSDK.sharedActor().application(application, handleOpenURL: url)
    }
}
