//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import JDStatusBarNotification
import PushKit
import SafariServices
import DZNWebViewController

@objc public class ActorSDK: NSObject, PKPushRegistryDelegate {

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
    
    /// SDK Analytics
    public var analyticsDelegate: ActorSDKAnalytics?
    
    //
    //  Configuration
    //

    /// Server Endpoints
    public var endpoints = [
        "tcp://front1-mtproto-api-rev3.actor.im:443",
        "tcp://front2-mtproto-api-rev3.actor.im:443"
    ] {
        didSet {
            trustedKeys = []
        }
    }
    
    /// Trusted Server Keys
    public var trustedKeys = [
        "d9d34ed487bd5b434eda2ef2c283db587c3ae7fb88405c3834d9d1a6d247145b",
        "4bd5422b50c585b5c8575d085e9fae01c126baa968dab56a396156759d5a7b46",
        "ff61103913aed3a9a689b6d77473bc428d363a3421fdd48a8e307a08e404f02c",
        "20613ab577f0891102b1f0a400ca53149e2dd05da0b77a728b62f5ebc8095878",
        "fc49f2f2465f5b4e038ec7c070975858a8b5542aa6ec1f927a57c4f646e1c143",
        "6709b8b733a9f20a96b9091767ac19fd6a2a978ba0dccc85a9ac8f6b6560ac1a"
    ]
    
    /// API ID
    public var apiId = 2
    
    /// API Key
    public var apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
    
    /// Push registration mode
    public var autoPushMode = AAAutoPush.AfterLogin
    
    /// Push token registration id. Required for sending push tokens
    public var apiPushId: Int? = nil
    
    /// Strategy about authentication
    public var authStrategy = AAAuthStrategy.PhoneOnly
    
    /// Enable phone book import
    public var enablePhoneBookImport = true
    
    /// Invitation URL for apps
    public var inviteUrl: String = "https://actor.im/dl"

    /// Privacy Policy URL
    public var privacyPolicyUrl: String? = nil

    /// Privacy Policy Text
    public var privacyPolicyText: String? = nil
    
    /// Terms of Service URL
    public var termsOfServiceUrl: String? = nil
    
    /// Terms of Service Text
    public var termsOfServiceText: String? = nil
    
    /// App name
    public var appName: String = "Actor"
    
    /// Use background on welcome screen
    public var useBackgroundOnWelcomeScreen: Bool? = false
    
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
    
    /// Web Invite Domain host
    public var inviteUrlHost: String? = nil

    /// Enable voice calls feature
    public var enableCalls: Bool = true
    
    
    /// Enable experimental features
    public var enableExperimentalFeatures: Bool = false
    
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
    
    // Reachability
    private var reachability: Reachability!
    
    
    public override init() {
        
        // Auto Loading Application name
        if let name = NSBundle.mainBundle().objectForInfoDictionaryKey(String(kCFBundleNameKey)) as? String {
            self.appName = name
        }
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
        for key in trustedKeys {
            builder.addTrustedKey(key)
        }
        builder.setApiConfiguration(ACApiConfiguration(appTitle: appTitle, withAppId: jint(apiId), withAppKey: apiKey, withDeviceTitle: deviceName, withDeviceId: deviceKey))
        
        // Providers
        builder.setPhoneBookProvider(PhoneBookProvider())
        builder.setNotificationProvider(iOSNotificationProvider())
        builder.setCallsProvider(iOSCallsProvider())
        
        // Stats
        builder.setPlatformType(ACPlatformType.IOS())
        builder.setDeviceCategory(ACDeviceCategory.MOBILE())
        
        // Locale
        for lang in NSLocale.preferredLanguages() {
            log("Found locale :\(lang)")
            builder.addPreferredLanguage(lang)
        }
        
        // TimeZone
        let timeZone = NSTimeZone.defaultTimeZone().name
        log("Found time zone :\(timeZone)")
        builder.setTimeZone(timeZone)
  
        // Logs
        // builder.setEnableFilesLogging(true)
        
        // Application name
        builder.setCustomAppName(appName)
        
        // Config
        builder.setPhoneBookImportEnabled(jboolean(enablePhoneBookImport))
        builder.setVoiceCallsEnabled(jboolean(enableCalls))
        builder.setIsEnabledGroupedChatList(false)
        // builder.setEnableFilesLogging(true)
        
        // Creating messenger
        messenger = ACCocoaMessenger(configuration: builder.build())
        
        // Configure bubbles
        AABubbles.layouters = delegate.actorConfigureBubbleLayouters(AABubbles.builtInLayouters)
        
        checkAppState()
        
        // Bind Messenger LifeCycle
        
        binder.bind(messenger.getGlobalState().isSyncing, closure: { (value: JavaLangBoolean?) -> () in
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
        
        binder.bind(Actor.getGlobalState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            if let v = value {
                UIApplication.sharedApplication().applicationIconBadgeNumber = Int(v.integerValue)
            } else {
                UIApplication.sharedApplication().applicationIconBadgeNumber = 0
            }
        })
        
        // Push registration
        
        if autoPushMode == .FromStart {
            requestPush()
        }
        
        // Subscribe to network changes
        
        do {
            reachability = try Reachability.reachabilityForInternetConnection()
            NSNotificationCenter.defaultCenter().addObserver(self, selector: #selector(ActorSDK.reachabilityChanged(_:)), name: ReachabilityChangedNotification, object: reachability)
            try reachability.startNotifier()
        } catch {
            print("Unable to create Reachability")
            return
        }
    }
    
    @objc func reachabilityChanged(note: NSNotification) {
        print("reachabilityChanged (\(reachability.isReachable()))")
        
        if reachability.isReachable() {
            messenger.forceNetworkCheck()
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
            
            tab.viewControllers = self.getMainNavigations()
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
    
    func pushRegisterKitToken(token: String) {
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if apiPushId != nil {
            messenger.registerApplePushKitWithApnsId(jint(apiPushId!), withToken: token)
        }

    }
    
    private func requestPush() {
        let types: UIUserNotificationType = [.Alert, .Badge, .Sound]
        let settings: UIUserNotificationSettings = UIUserNotificationSettings(forTypes: types, categories: nil)
        UIApplication.sharedApplication().registerUserNotificationSettings(settings)
        UIApplication.sharedApplication().registerForRemoteNotifications()
    }
    
    private func requestPushKit() {
        let voipRegistry = PKPushRegistry(queue: dispatch_get_main_queue())
        voipRegistry.delegate = self        
        voipRegistry.desiredPushTypes = Set([PKPushTypeVoIP])
    }
    
    @objc public func pushRegistry(registry: PKPushRegistry!, didUpdatePushCredentials credentials: PKPushCredentials!, forType type: String!) {
        if (type == PKPushTypeVoIP) {
            let tokenString = "\(credentials.token)".replace(" ", dest: "").replace("<", dest: "").replace(">", dest: "")
            pushRegisterKitToken(tokenString)
        }
    }
    
    @objc public func pushRegistry(registry: PKPushRegistry!, didInvalidatePushTokenForType type: String!) {
        if (type == PKPushTypeVoIP) {
            
        }
    }
    
    @objc public func pushRegistry(registry: PKPushRegistry!, didReceiveIncomingPushWithPayload payload: PKPushPayload!, forType type: String!) {
        if (type == PKPushTypeVoIP) {
            let aps = payload.dictionaryPayload["aps"] as! [NSString: AnyObject]
            if let callId = aps["callId"] as? String {
                if let attempt = aps["attemptIndex"] as? String {
                    Actor.checkCall(jlong(callId)!, withAttempt: jint(attempt)!)
                } else {
                    Actor.checkCall(jlong(callId)!, withAttempt: 0)
                }
            } else if let seq = aps["seq"] as? String {
                Actor.onPushReceivedWithSeq(jint(seq)!)
            }
        }
    }
    
    /// Get main navigations with check in delegate for customize from SDK
    
    private func getMainNavigations() -> [AANavigationController] {
    
        
        var mainNavigations = [AANavigationController]()
        
        ////////////////////////////////////
        // contacts
        ////////////////////////////////////
        
        if let contactsController = self.delegate.actorControllerForContacts() {
            mainNavigations.append(AANavigationController(rootViewController: contactsController))
        } else {
            mainNavigations.append(AANavigationController(rootViewController: AAContactsViewController()))
        }
        
        ////////////////////////////////////
        // recent dialogs
        ////////////////////////////////////
        
        if let recentDialogs = self.delegate.actorControllerForDialogs() {
            mainNavigations.append(AANavigationController(rootViewController: recentDialogs))
        } else {
            mainNavigations.append(AANavigationController(rootViewController: AARecentViewController()))
        }
        
        ////////////////////////////////////
        // settings
        ////////////////////////////////////
        
        if let settingsController = self.delegate.actorControllerForSettings() {
            mainNavigations.append(AANavigationController(rootViewController: settingsController))
        } else {
            mainNavigations.append(AANavigationController(rootViewController: AASettingsViewController()))
        }
        
    
        return mainNavigations;
        
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
                tab.viewControllers = self.getMainNavigations()
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
            let controller: UIViewController! = delegate.actorControllerForAuthStart()
            if controller == nil {
                window.rootViewController = AAWelcomeController()
            } else {
                window.rootViewController = controller
            }
        }
        
        // Bind Status Bar connecting
        
        if !style.statusBarConnectingHidden {
            
            JDStatusBarNotification.setDefaultStyle { (style) -> JDStatusBarStyle! in
                style.barColor = self.style.statusBarConnectingBgColor
                style.textColor = self.style.statusBarConnectingTextColor
                return style
            }
            
            dispatchOnUi { () -> Void in
                self.binder.bind(self.messenger.getGlobalState().isSyncing, valueModel2: self.messenger.getGlobalState().isConnecting) {
                    (isSyncing: JavaLangBoolean?, isConnecting: JavaLangBoolean?) -> () in
                    
                    if isSyncing!.booleanValue() || isConnecting!.booleanValue() {
                        if isConnecting!.booleanValue() {
                            JDStatusBarNotification.showWithStatus(AALocalized("StatusConnecting"))
                        } else {
                            JDStatusBarNotification.showWithStatus(AALocalized("StatusSyncing"))
                        }
                    } else {
                        JDStatusBarNotification.dismiss()
                    }
                }
            }
        }
    }
    
    public func presentMessengerInNewWindow() {
        let window = UIWindow(frame: UIScreen.mainScreen().bounds);
        window.backgroundColor = UIColor.whiteColor()
        presentMessengerInWindow(window)
        window.makeKeyAndVisible()
    }

    //
    // Data Processing
    //
    
    /// Handling URL Opening in application
    func openUrl(url: String) {
        if let u = NSURL(string: url) {
            
            // Handle phone call
            if (u.scheme.lowercaseString == "telprompt") {
                 UIApplication.sharedApplication().openURL(u)
                return
            }
            
            // Handle web invite url
            if (u.scheme.lowercaseString == "http" || u.scheme.lowercaseString == "https") &&  inviteUrlHost != nil {
                
                if u.host == inviteUrlHost {
                    if let token = u.lastPathComponent {
                        joinGroup(token)
                        return
                    }
                }
            }
            
            // Handle custom scheme invite url
            if (u.scheme.lowercaseString == inviteUrlScheme?.lowercaseString) {
                
                if (u.host == "invite") {
                    let token = u.query?.componentsSeparatedByString("=")[1]
                    if token != nil {
                        joinGroup(token!)
                        return
                    }
                }
                
                if let bindedController = bindedToWindow?.rootViewController {
                    let alert = UIAlertController(title: nil, message: AALocalized("ErrorUnableToJoin"), preferredStyle: .Alert)
                    alert.addAction(UIAlertAction(title: AALocalized("AlertOk"), style: .Cancel, handler: nil))
                    bindedController.presentViewController(alert, animated: true, completion: nil)
                }
                
                return
            }
            
            
            
            if (url.isValidUrl()){
                
                if let bindedController = bindedToWindow?.rootViewController {
                    // Dismiss Old Presented Controller to show new one
                    if let presented = bindedController.presentedViewController {
                        presented.dismissViewControllerAnimated(true, completion: nil)
                    }
                    
                    // Building Controller for Web preview
                    let controller: UIViewController
                    if #available(iOS 9.0, *) {
                        controller = SFSafariViewController(URL: u)
                    } else {
                        controller = AANavigationController(rootViewController: DZNWebViewController(URL: u))
                    }
                    if AADevice.isiPad {
                        controller.modalPresentationStyle = .FullScreen
                    }
                    
                    // Presenting controller
                    bindedController.presentViewController(controller, animated: true, completion: nil)
                } else {
                    // Just Fallback. Might never happend
                    UIApplication.sharedApplication().openURL(u)
                }
            }
        }
    }
    
    /// Handling joining group by token
    func joinGroup(token: String) {
        if let bindedController = bindedToWindow?.rootViewController {
            let alert = UIAlertController(title: nil, message: AALocalized("GroupJoinMessage"), preferredStyle: .Alert)
            alert.addAction(UIAlertAction(title: AALocalized("AlertNo"), style: .Cancel, handler: nil))
            alert.addAction(UIAlertAction(title: AALocalized("GroupJoinAction"), style: .Default){ (action) -> Void in
                AAExecutions.execute(Actor.joinGroupViaLinkCommandWithToken(token)!, type: .Safe, ignore: [], successBlock: { (val) -> Void in
                    
                    // TODO: Fix for iPad
                    let groupId = val as! JavaLangInteger
                    let tabBarController = bindedController as! UITabBarController
                    let index = tabBarController.selectedIndex
                    let navController = tabBarController.viewControllers![index] as! UINavigationController
                    if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.groupWithInt(groupId.intValue)) {
                        navController.pushViewController(customController, animated: true)
                    } else {
                        navController.pushViewController(ConversationViewController(peer: ACPeer.groupWithInt(groupId.intValue)), animated: true)
                    }
                    
                }, failureBlock: nil)
            })
            bindedController.presentViewController(alert, animated: true, completion: nil)
        }
    }
    
    /// Tracking page visible
    func trackPageVisible(page: ACPage) {
        analyticsDelegate?.analyticsPageVisible(page)
    }
    
    /// Tracking page hidden
    func trackPageHidden(page: ACPage) {
        analyticsDelegate?.analyticsPageHidden(page)
    }
    
    /// Tracking event
    func trackEvent(event: ACEvent) {
        analyticsDelegate?.analyticsEvent(event)
    }
    
    //
    // File System
    //
    
    public func fullFilePathForDescriptor(descriptor: String) -> String {
        return CocoaFiles.pathFromDescriptor(descriptor)
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
                
                // Notify Audio Manager about app visiblity change
                AAAudioManager.sharedAudio().appVisible()
                
                // Notify analytics about visibilibty change
                // Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(true))
                
                // Hack for resync phone book
                Actor.onPhoneBookChanged()
            }
        } else {
            if isUserOnline {
                isUserOnline = false
                
                // Notify Audio Manager about app visiblity change
                AAAudioManager.sharedAudio().appHidden()
                
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

        //
        // This event is fired when user press power button and lock screeen.
        // In iOS power button also cancel ongoint call.
        //
        // messenger.probablyEndCall()
        
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
    
    public func application(application: UIApplication, didRegisterUserNotificationSettings notificationSettings: UIUserNotificationSettings) {
        requestPushKit()
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
        
        dispatchOnUi { () -> Void in
            self.openUrl(url.absoluteString)
        }

        return true
    }
    
    public func application(application: UIApplication, handleOpenURL url: NSURL) -> Bool {
        
        dispatchOnUi { () -> Void in
            self.openUrl(url.absoluteString)
        }
        
        return true
    }
}

public enum AAAutoPush {
    case None
    case FromStart
    case AfterLogin
}

public enum AAAuthStrategy {
    case PhoneOnly
    case EmailOnly
    case PhoneEmail
}