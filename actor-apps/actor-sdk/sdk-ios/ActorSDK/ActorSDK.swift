//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class ActorSDK {

    //
    // Shared instance
    //
    
    private static let shared =  ActorSDK()
    
    public static func sharedActor() -> ActorSDK {
        return shared
    }
    
    //
    //  Root Objects
    //
    
    /// Main Messenger object
    public var messenger : ACCocoaMessenger!
    
    /// Is Actor Started
    private(set) public var isStarted = false
    
    /// SDK Delegate
    public var delegate: ActorSDKDelegate?
    
    //
    //  Configuration
    //

    /// Server Endpoints
    public var endpoints = [
        "tls://front1-mtproto-api-rev2.actor.im",
        "tls://front2-mtproto-api-rev2.actor.im"
    ]
    
    /// API ID
    public let apiId = 2
    
    /// API Key
    public let apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
    
    /// Push registration mode
    public var autoPushMode = AutoPush.None
    
    /// Push token registration id. Required for sending push tokens
    public let apiPushId: Int? = nil
    
    //
    // User Onlines
    //
    
    /// Is User online
    private(set) public var isUserOnline = false
    
    /// Disable this if you want manually handle online states
    public var automaticOnlineHandling = true

    
    //
    // Internal State
    //
    
    private var binder = Binder()
    private var syncTask: UIBackgroundTaskIdentifier?
    private var completionHandler: ((UIBackgroundFetchResult) -> Void)?
    
    //
    // Initialization
    //
    
    public func createActor() {
        
        if isStarted {
            return
        }
        isStarted = true
        
        ARCocoaStorageProxyProvider.setStorageRuntime(CocoaStorageRuntime())
        ARCocoaHttpProxyProvider.setHttpRuntime(CocoaHttpRuntime())
        ARCocoaFileSystemProxyProvider.setFileSystemRuntime(CocoaFileSystemRuntime())
        ARCocoaNetworkProxyProvider.setNetworkRuntime(CocoaNetworkRuntime())
        
        let builder = ACConfigurationBuilder()
        
        // Api Connections
        let deviceKey = NSUUID().UUIDString
        let deviceName = UIDevice.currentDevice().name
        let appTitle = "Actor iOS"
        for url in endpoints {
            builder.addEndpoint(url)
        }
        builder.setApiConfiguration(ACApiConfiguration(appTitle: appTitle, withAppId: jint(apiId), withAppKey: apiKey, withDeviceTitle: deviceName, withDeviceId: deviceKey))
        
        // Providers
        builder.setPhoneBookProvider(PhoneBookProvider())
        builder.setNotificationProvider(iOSNotificationProvider())
        
        // Stats
        builder.setPlatformType(ACPlatformTypeEnum.values().objectAtIndex(ACPlatformType.IOS.rawValue) as! ACPlatformTypeEnum)
        builder.setDeviceCategory(ACDeviceCategoryEnum.values().objectAtIndex(ACDeviceCategory.MOBILE.rawValue) as! ACDeviceCategoryEnum)
        
        // Logs
        // builder.setEnableFilesLogging(true)
        
        // Creating messenger
        messenger = ACCocoaMessenger(configuration: builder.build())
        
        checkAppState()
        
        initStyles()
        
        // Apply styles
        MainAppTheme.applyAppearance(UIApplication.sharedApplication())
        
        // Bind Messenger LifeCycle
        
        binder.bind(messenger.getAppState().isSyncing, closure: { (value: JavaLangBoolean?) -> () in
            if value!.booleanValue() {
                if self.syncTask == nil {
                    self.syncTask = UIApplication.sharedApplication().beginBackgroundTaskWithName("Background Sync", expirationHandler: { () -> Void in
                        
                    })
                }
            } else {
                if self.syncTask != nil {
                    UIApplication.sharedApplication().endBackgroundTask(self.syncTask!)
                    self.syncTask = nil
                }
                if self.completionHandler != nil {
                    self.completionHandler!(UIBackgroundFetchResult.NewData)
                    self.completionHandler = nil
                }
            }
        })
        
        // Push registration
        
        if autoPushMode == .FromStart {
            requestPush()
        }
    }
    
    func didLoggedIn() {
        
        // Push registration
        
        if autoPushMode == .AfterLogin {
            requestPush()
        }
        
        // TODO: Move UI
    }
    
    //
    // Push support
    //
    
    /// Token need to be with stripped everything except numbers and letters
    func pushRegisterToken(token: String) {
        
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if apiPushId != nil {
            messenger.registerApplePushWithApnsId(jint(apiPushId!), withToken: token)
        }
    }
    
    private func requestPush() {
        let types: UIUserNotificationType = [.Alert, .Badge, .Sound]
        let settings: UIUserNotificationSettings = UIUserNotificationSettings(forTypes: types, categories: nil)
        UIApplication.sharedApplication().registerUserNotificationSettings(settings)
        UIApplication.sharedApplication().registerForRemoteNotifications()
    }

    
    //
    // Presenting Messenger
    //
    
    public func presentMessengerInWindow(window: UIWindow) {
        
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if messenger.isLoggedIn() {
            window.rootViewController = delegate!.actorControllerForStart()
        } else {
            window.rootViewController = delegate!.actorControllerForAuthStart()
        }
    }
    
    //
    // Manual Online handling
    //
    
    public func didBecameOnline() {
        if automaticOnlineHandling {
            fatalError("Manual Online handling not enabled!")
        }
        
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if !isUserOnline {
            isUserOnline = true
            messenger.onAppVisible()
        }
    }
    
    public func didBecameOffline() {
        if automaticOnlineHandling {
            fatalError("Manual Online handling not enabled!")
        }
        
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if isUserOnline {
            isUserOnline = false
            messenger.onAppHidden()
        }
    }
    
    //
    // Automatic Online handling
    //
    
    func checkAppState() {
        if UIApplication.sharedApplication().applicationState == .Active {
            if !isUserOnline {
                isUserOnline = true
                
                // Mark app as visible
                Actor.onAppVisible()
                
                // Notify analytics about visibilibty change
                Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(true))
                
                // Hack for resync phone book
                Actor.onPhoneBookChanged()
            }
        } else {
            if isUserOnline {
                isUserOnline = false
                
                // Mark app as hidden
                Actor.onAppHidden()
                
                // Notify analytics about visibilibty change
                Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(false))
            }
        }
    }
    
    public func applicationDidFinishLaunching(application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    public func applicationDidBecomeActive(application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    public func applicationWillEnterForeground(application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    public func applicationDidEnterBackground(application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
        
        // Keep application running for 40 secs
        if messenger.isLoggedIn() {
            var completitionTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
            
            completitionTask = application.beginBackgroundTaskWithName("Completition", expirationHandler: { () -> Void in
                application.endBackgroundTask(completitionTask)
                completitionTask = UIBackgroundTaskInvalid
            })
            
            // Wait for 40 secs before app shutdown
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(40.0 * Double(NSEC_PER_SEC))), dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) { () -> Void in
                application.endBackgroundTask(completitionTask)
                completitionTask = UIBackgroundTaskInvalid
            }
        }
    }
    
    public func applicationWillResignActive(application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    //
    // Handling remote notifications
    //
    
    public func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject], fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        
        if !messenger.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.NoData)
            return
        }
        
        self.completionHandler = completionHandler
    }
    
    public func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        // Nothing?
    }
    
    //
    // Handling background fetch events
    //
    
    public func application(application: UIApplication, performFetchWithCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        
        if !messenger.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.NoData)
            return
        }
        self.completionHandler = completionHandler
    }
}

public enum AutoPush {
    case None
    case FromStart
    case AfterLogin
}