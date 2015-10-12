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
    
    // Actor Style
    public let style = ActorStyle()
    
    /// SDK Delegate
    public var delegate: ActorSDKDelegate = ActorSDKDelegateDefault()
    
    //
    //  Configuration
    //

    /// Server Endpoints
    public var endpoints = [
        "tls://front1-mtproto-api-rev2.actor.im",
        "tls://front2-mtproto-api-rev2.actor.im"
    ]
    
    /// API ID
    public var apiId = 2
    
    /// API Key
    public var apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
    
    /// Push registration mode
    public var autoPushMode = AAAutoPush.None
    
    /// Push token registration id. Required for sending push tokens
    public var apiPushId: Int? = nil
    
    /// Invitation URL for apps
    public var inviteUrl: String = "https://actor.im/dl"
    
    /// Support email
    public var supportEmail: String? = nil
    
    /// Support email
    public var supportActivationEmail: String? = nil
    
    /// Support account
    public var supportAccount: String? = nil
    
    /// Support home page
    public var supportHomepage: String? = "https://actor.im"

    /// Support account
    public var supportTwitter: String? = "actorapp"

    /// Invite url scheme
    public var inviteUrlScheme: String? = nil
    
    /// Extensions
    private var extensions = [ActorExtension]()
    
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
    
    /// Is Actor Started
    private(set) public var isStarted = false
    
    private var binder = AABinder()
    private var syncTask: UIBackgroundTaskIdentifier?
    private var completionHandler: ((UIBackgroundFetchResult) -> Void)?
    
    // View Binding info
    private(set) public var bindedToWindow: UIWindow!
    
    //
    // Initialization
    //
    
    public func registerExtension(key: String, ext: ACExtension) {
        extensions.append(ActorExtension(key: key, ext: ext))
    }
    
    public func createActor() {
        
        if isStarted {
            return
        }
        isStarted = true
        
        AAActorRuntime.configureRuntime()
        
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
  
        // Extensions
        for ex in extensions {
            builder.addExtensionWithNSString(ex.key, withACExtension: ex.ext)
        }
        
        // Logs
        // builder.setEnableFilesLogging(true)
        
        // Creating messenger
        messenger = ACCocoaMessenger(configuration: builder.build())
        
        // Configure bubbles
        AABubbles.layouters = delegate.actorConfigureBubbleLayouters(AABubbles.builtInLayouters)
        
        checkAppState()
        
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
        
        // Bind badge counter
        
        binder.bind(Actor.getAppState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            UIApplication.sharedApplication().applicationIconBadgeNumber = Int((value!).integerValue)
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
        
        var controller: UIViewController! = delegate.actorControllerAfterLogIn()
        if controller == nil {
            controller = delegate.actorControllerForStart()
        }
        if controller == nil {
            let tab = AARootTabViewController()
            tab.viewControllers = [
                AANavigationController(rootViewController: AAContactsViewController()),
                AANavigationController(rootViewController: AARecentViewController()),
                AANavigationController(rootViewController: AASettingsViewController())]
            tab.selectedIndex = 0
            tab.selectedIndex = 1
            
            if (AADevice.isiPad) {
                let splitController = AARootSplitViewController()
                splitController.viewControllers = [tab, AANoSelectionViewController()]
                controller = splitController
            } else {
                controller = tab
            }
        }
        bindedToWindow.rootViewController = controller!
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
        
        self.bindedToWindow = window
        
        if messenger.isLoggedIn() {
            
            if autoPushMode == .AfterLogin {
                requestPush()
            }
            
            var controller: UIViewController! = delegate.actorControllerForStart()
            if controller == nil {
                let tab = AARootTabViewController()
                tab.viewControllers = [
                    AANavigationController(rootViewController: AAContactsViewController()),
                    AANavigationController(rootViewController: AARecentViewController()),
                    AANavigationController(rootViewController: AASettingsViewController())]
                tab.selectedIndex = 0
                tab.selectedIndex = 1
                
                if (AADevice.isiPad) {
                    let splitController = AARootSplitViewController()
                    splitController.viewControllers = [tab, AANoSelectionViewController()]
                    controller = splitController
                } else {
                    controller = tab
                }
            }
            window.rootViewController = controller!
        } else {
            var controller: UIViewController! = delegate.actorControllerForAuthStart()
            if controller == nil {
                controller = AAAuthNavigationController(rootViewController: AAAuthPhoneViewController())
            }
            window.rootViewController = controller!
        }
    }
    
    public func presentMessengerInNewWindow() {
        let window = UIWindow(frame: UIScreen.mainScreen().bounds);
        window.backgroundColor = UIColor.whiteColor()
        presentMessengerInWindow(window)
        window.makeKeyAndVisible()
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
                messenger.onAppVisible()
                
                // Notify analytics about visibilibty change
                // Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(true))
                
                // Hack for resync phone book
                Actor.onPhoneBookChanged()
            }
        } else {
            if isUserOnline {
                isUserOnline = false
                
                // Mark app as hidden
                messenger.onAppHidden()
                
                // Notify analytics about visibilibty change
                // Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(false))
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
    
    //
    // Handling invite url
    //
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        
        if (url.scheme == "actor") {
            if (url.host == "invite") {
                if (Actor.isLoggedIn()) {
                    let token = url.query?.componentsSeparatedByString("=")[1]
                    if token != nil {
//                        UIAlertView.showWithTitle(nil, message: AALocalized("GroupJoinMessage"), cancelButtonTitle: localized("AlertNo"), otherButtonTitles: [AALocalized("GroupJoinAction")], tapBlock: { (view, index) -> Void in
//                            if (index == view.firstOtherButtonIndex) {
//                                Executions.execute(Actor.joinGroupViaLinkCommandWithUrl(token), successBlock: { (val) -> Void in
//                                    let groupId = val as! JavaLangInteger
//                                    self.openChat(ACPeer.groupWithInt(groupId.intValue))
//                                    }, failureBlock: { (val) -> Void in
//                                        
//                                        if let res = val as? ACRpcException {
//                                            if res.getTag() == "USER_ALREADY_INVITED" {
//                                                UIAlertView.showWithTitle(nil, message: localized("ErrorAlreadyJoined"), cancelButtonTitle: localized("AlertOk"), otherButtonTitles: nil, tapBlock: nil)
//                                                return
//                                            }
//                                        }
//                                        
//                                        UIAlertView.showWithTitle(nil, message: localized("ErrorUnableToJoin"), cancelButtonTitle: localized("AlertOk"), otherButtonTitles: nil, tapBlock: nil)
//                                })
//                            }
//                        })
                    }
                }
                
                return true
            }
        }
        return false
    }
}

public enum AAAutoPush {
    case None
    case FromStart
    case AfterLogin
}

class ActorExtension {
    let key: String
    let ext: ACExtension
    
    init(key: String, ext: ACExtension) {
        self.key = key
        self.ext = ext
    }
}