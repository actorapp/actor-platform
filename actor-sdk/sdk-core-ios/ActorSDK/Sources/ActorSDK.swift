//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import JDStatusBarNotification
import PushKit
import SafariServices
import DZNWebViewController
import ReachabilitySwift

@objc open class ActorSDK: NSObject, PKPushRegistryDelegate {

    //
    // Shared instance
    //
    
    fileprivate static let shared =  ActorSDK()
    
    open static func sharedActor() -> ActorSDK {
        return shared
    }
    
    //
    //  Root Objects
    //
    
    /// Main Messenger object
    open var messenger : ACCocoaMessenger!
    
    // Actor Style
    open let style = ActorStyle()
    
    /// SDK Delegate
    open var delegate: ActorSDKDelegate = ActorSDKDelegateDefault()
    
    /// SDK Analytics
    open var analyticsDelegate: ActorSDKAnalytics?
    
    //
    //  Configuration
    //

    /// Server Endpoints
    open var endpoints = [
        "tcp://front1-mtproto-api-rev3.actor.im:443",
        "tcp://front2-mtproto-api-rev3.actor.im:443"
    ] {
        didSet {
            trustedKeys = []
        }
    }
    
    /// Trusted Server Keys
    open var trustedKeys = [
        "d9d34ed487bd5b434eda2ef2c283db587c3ae7fb88405c3834d9d1a6d247145b",
        "4bd5422b50c585b5c8575d085e9fae01c126baa968dab56a396156759d5a7b46",
        "ff61103913aed3a9a689b6d77473bc428d363a3421fdd48a8e307a08e404f02c",
        "20613ab577f0891102b1f0a400ca53149e2dd05da0b77a728b62f5ebc8095878",
        "fc49f2f2465f5b4e038ec7c070975858a8b5542aa6ec1f927a57c4f646e1c143",
        "6709b8b733a9f20a96b9091767ac19fd6a2a978ba0dccc85a9ac8f6b6560ac1a"
    ]
    
    /// API ID
    open var apiId = 2
    
    /// API Key
    open var apiKey = "2ccdc3699149eac0a13926c77ca84e504afd68b4f399602e06d68002ace965a3"
    
    /// Push registration mode
    open var autoPushMode = AAAutoPush.afterLogin
    
    /// Push token registration id. Required for sending push tokens
    open var apiPushId: Int? = nil
    
    /// Strategy about authentication
    open var authStrategy = AAAuthStrategy.phoneOnly
    
    /// Enable phone book import
    open var enablePhoneBookImport = true
    
    /// Invitation URL for apps
    open var inviteUrl: String = "https://actor.im/dl"
    
    /// Invitation URL for apps
    open var invitePrefix: String? = "https://actor.im/join/"
    
    /// Invitation URL for apps
    open var invitePrefixShort: String? = "actor.im/join/"

    /// Privacy Policy URL
    open var privacyPolicyUrl: String? = nil

    /// Privacy Policy Text
    open var privacyPolicyText: String? = nil
    
    /// Terms of Service URL
    open var termsOfServiceUrl: String? = nil
    
    /// Terms of Service Text
    open var termsOfServiceText: String? = nil
    
    /// App name
    open var appName: String = "Actor"
    
    /// Use background on welcome screen
    open var useBackgroundOnWelcomeScreen: Bool? = false
    
    /// Support email
    open var supportEmail: String? = nil
    
    /// Support email
    open var supportActivationEmail: String? = nil
    
    /// Support account
    open var supportAccount: String? = nil
    
    /// Support home page
    open var supportHomepage: String? = "https://actor.im"

    /// Support account
    open var supportTwitter: String? = "actorapp"

    /// Invite url scheme
    open var inviteUrlScheme: String? = nil
    
    /// Web Invite Domain host
    open var inviteUrlHost: String? = nil

    /// Enable voice calls feature
    open var enableCalls: Bool = false
    
    /// Enable video calls feature
    open var enableVideoCalls: Bool = false
    
    /// Enable custom sound on Groups and Chats
    open var enableChatGroupSound: Bool = false
    
    /// Enable experimental features
    open var enableExperimentalFeatures: Bool = false
    
    /// Auto Join Groups
    open var autoJoinGroups = [String]()
    
    /// Should perform auto join only after first message or contact
    open var autoJoinOnReady = true
    
    //
    // User Onlines
    //
    
    /// Is User online
    fileprivate(set) open var isUserOnline = false
    
    /// Disable this if you want manually handle online states
    open var automaticOnlineHandling = true
    
    
    //
    // Local Settings
    //
    
    // Local Shared Settings
    fileprivate static var udStorage = UDPreferencesStorage()
    
    open var isPhotoAutoDownloadGroup: Bool = udStorage.getBoolWithKey("local.photo_download.group", withDefault: true) {
        willSet(v) {
            ActorSDK.udStorage.putBool(withKey: "local.photo_download.group", withValue: v)
        }
    }
    
    open var isPhotoAutoDownloadPrivate: Bool = udStorage.getBoolWithKey("local.photo_download.private", withDefault: true) {
        willSet(v) {
            ActorSDK.udStorage.putBool(withKey: "local.photo_download.private", withValue: v)
        }
    }
    
    open var isAudioAutoDownloadGroup: Bool = udStorage.getBoolWithKey("local.audio_download.group", withDefault: true) {
        willSet(v) {
            ActorSDK.udStorage.putBool(withKey: "local.audio_download.group", withValue: v)
        }
    }

    open var isAudioAutoDownloadPrivate: Bool = udStorage.getBoolWithKey("local.audio_download.private", withDefault: true) {
        willSet(v) {
            ActorSDK.udStorage.putBool(withKey: "local.audio_download.private", withValue: v)
        }
    }
    
    open var isGIFAutoplayEnabled: Bool = udStorage.getBoolWithKey("local.autoplay_gif", withDefault: true) {
        willSet(v) {
            ActorSDK.udStorage.putBool(withKey: "local.autoplay_gif", withValue: v)
        }
    }
    
    
    //
    // Internal State
    //
    
    /// Is Actor Started
    fileprivate(set) open var isStarted = false
    
    fileprivate var binder = AABinder()
    fileprivate var syncTask: UIBackgroundTaskIdentifier?
    fileprivate var completionHandler: ((UIBackgroundFetchResult) -> Void)?
    
    // View Binding info
    fileprivate(set) open var bindedToWindow: UIWindow!
    
    // Reachability
    fileprivate var reachability: Reachability!
    
    public override init() {
        
        // Auto Loading Application name
        if let name = Bundle.main.object(forInfoDictionaryKey: String(kCFBundleNameKey)) as? String {
            self.appName = name
        }
    }
    
    open func createActor() {
        
        if isStarted {
            return
        }
        isStarted = true
        
        AAActorRuntime.configureRuntime()
        
        let builder = ACConfigurationBuilder()!
        
        // Api Connections
        let deviceKey = UUID().uuidString
        let deviceName = UIDevice.current.name
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
        builder.setPlatformType(ACPlatformType.ios())
        builder.setDeviceCategory(ACDeviceCategory.mobile())
        
        // Locale
        for lang in Locale.preferredLanguages {
            log("Found locale :\(lang)")
            builder.addPreferredLanguage(lang)
        }
        
        // TimeZone
        let timeZone = TimeZone.current.identifier
        log("Found time zone :\(timeZone)")
        builder.setTimeZone(timeZone)
  
        // AutoJoin
        for s in autoJoinGroups {
            builder.addAutoJoinGroup(withToken: s)
        }
        if autoJoinOnReady {
            builder.setAutoJoinType(ACAutoJoinType.after_INIT())
        } else {
            builder.setAutoJoinType(ACAutoJoinType.immediately())
        }
        
        // Logs
        // builder.setEnableFilesLogging(true)
        
        // Application name
        builder.setCustomAppName(appName)
        
        // Config
        builder.setPhoneBookImportEnabled(jboolean(enablePhoneBookImport))
        builder.setVoiceCallsEnabled(jboolean(enableCalls))
        builder.setVideoCallsEnabled(jboolean(enableCalls))
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
                    self.syncTask = UIApplication.shared.beginBackgroundTask(withName: "Background Sync", expirationHandler: { () -> Void in
                        
                    })
                }
            } else {
                if self.syncTask != nil {
                    UIApplication.shared.endBackgroundTask(self.syncTask!)
                    self.syncTask = nil
                }
                if self.completionHandler != nil {
                    self.completionHandler!(UIBackgroundFetchResult.newData)
                    self.completionHandler = nil
                }
            }
        })
        
        // Bind badge counter
        
        binder.bind(Actor.getGlobalState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            if let v = value {
                UIApplication.shared.applicationIconBadgeNumber = Int(v.intValue)
            } else {
                UIApplication.shared.applicationIconBadgeNumber = 0
            }
        })
        
        // Push registration
        
        if autoPushMode == .fromStart {
            requestPush()
        }
        
        // Subscribe to network changes
        
        reachability = Reachability()!
        if reachability != nil {
            reachability.whenReachable = { reachability in
                self.messenger.forceNetworkCheck()
            }
            
            do {
                try reachability.startNotifier()
            } catch {
                print("Unable to start Reachability")
            }
        } else {
            print("Unable to create Reachability")
        }
    }
    
    func didLoggedIn() {
        
        // Push registration
        
        if autoPushMode == .afterLogin {
            requestPush()
        }
        
        var controller: UIViewController! = delegate.actorControllerAfterLogIn()
        if controller == nil {
            controller = delegate.actorControllerForStart()
        }
        if controller == nil {
            let tab = AARootTabViewController()
            
            tab.viewControllers = self.getMainNavigations()
            
            if let index = self.delegate.actorRootInitialControllerIndex() {
                tab.selectedIndex = index
            } else {
                tab.selectedIndex = 1
            }
            
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
    func pushRegisterToken(_ token: String) {
        
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if apiPushId != nil {
            messenger.registerApplePush(withApnsId: jint(apiPushId!), withToken: token)
        }
    }
    
    func pushRegisterKitToken(_ token: String) {
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        if apiPushId != nil {
            messenger.registerApplePushKit(withApnsId: jint(apiPushId!), withToken: token)
        }

    }
    
    fileprivate func requestPush() {
        let types: UIUserNotificationType = [.alert, .badge, .sound]
        let settings: UIUserNotificationSettings = UIUserNotificationSettings(types: types, categories: nil)
        UIApplication.shared.registerUserNotificationSettings(settings)
        UIApplication.shared.registerForRemoteNotifications()
    }
    
    fileprivate func requestPushKit() {
        let voipRegistry = PKPushRegistry(queue: DispatchQueue.main)
        voipRegistry.delegate = self        
        voipRegistry.desiredPushTypes = Set([PKPushType.voIP])
    }
    
    @objc open func pushRegistry(_ registry: PKPushRegistry, didUpdate credentials: PKPushCredentials, forType type: PKPushType) {
        if (type == PKPushType.voIP) {
            let tokenString = "\(credentials.token)".replace(" ", dest: "").replace("<", dest: "").replace(">", dest: "")
            pushRegisterKitToken(tokenString)
        }
    }
    
    @objc open func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenForType type: PKPushType) {
        if (type == PKPushType.voIP) {
            
        }
    }
    
    @objc open func pushRegistry(_ registry: PKPushRegistry, didReceiveIncomingPushWith payload: PKPushPayload, forType type: PKPushType) {
        if (type == PKPushType.voIP) {
            let aps = payload.dictionaryPayload["aps"] as! [NSString: AnyObject]
            if let callId = aps["callId"] as? String {
                if let attempt = aps["attemptIndex"] as? String {
                    Actor.checkCall(jlong(callId)!, withAttempt: jint(attempt)!)
                } else {
                    Actor.checkCall(jlong(callId)!, withAttempt: 0)
                }
            } else if let seq = aps["seq"] as? String {
                Actor.onPushReceived(withSeq: jint(seq)!, withAuthId: 0)
            }
        }
    }
    
    /// Get main navigations with check in delegate for customize from SDK
    fileprivate func getMainNavigations() -> [AANavigationController] {
    
        let allControllers = self.delegate.actorRootControllers()
        
        if let all = allControllers {
            
            var mainNavigations = [AANavigationController]()
            
            for controller in all {
                mainNavigations.append(AANavigationController(rootViewController: controller))
            }
            
            return mainNavigations
        } else {

            var mainNavigations = [AANavigationController]()
        
            ////////////////////////////////////
            // Contacts
            ////////////////////////////////////
        
            if let contactsController = self.delegate.actorControllerForContacts() {
                mainNavigations.append(AANavigationController(rootViewController: contactsController))
            } else {
                mainNavigations.append(AANavigationController(rootViewController: AAContactsViewController()))
            }
        
            ////////////////////////////////////
            // Recent dialogs
            ////////////////////////////////////
        
            if let recentDialogs = self.delegate.actorControllerForDialogs() {
                mainNavigations.append(AANavigationController(rootViewController: recentDialogs))
            } else {
                mainNavigations.append(AANavigationController(rootViewController: AARecentViewController()))
            }
        
            ////////////////////////////////////
            // Settings
            ////////////////////////////////////
        
            if let settingsController = self.delegate.actorControllerForSettings() {
                mainNavigations.append(AANavigationController(rootViewController: settingsController))
            } else {
                mainNavigations.append(AANavigationController(rootViewController: AASettingsViewController()))
            }
        
    
            return mainNavigations
        }
    }

    
    //
    // Presenting Messenger
    //
    
    open func presentMessengerInWindow(_ window: UIWindow) {
        if !isStarted {
            fatalError("Messenger not started")
        }
        
        self.bindedToWindow = window
        
        if messenger.isLoggedIn() {
            
            if autoPushMode == .afterLogin {
                requestPush()
            }
            
            var controller: UIViewController! = delegate.actorControllerForStart()
            if controller == nil {
                let tab = AARootTabViewController()
                tab.viewControllers = self.getMainNavigations()
                
                if let index = self.delegate.actorRootInitialControllerIndex() {
                    tab.selectedIndex = index
                } else {
                    tab.selectedIndex = 1
                }

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
                style?.barColor = self.style.statusBarConnectingBgColor
                style?.textColor = self.style.statusBarConnectingTextColor
                return style
            }
            
            dispatchOnUi { () -> Void in
                self.binder.bind(self.messenger.getGlobalState().isSyncing, valueModel2: self.messenger.getGlobalState().isConnecting) {
                    (isSyncing: JavaLangBoolean?, isConnecting: JavaLangBoolean?) -> () in
                    
                    if isSyncing!.booleanValue() || isConnecting!.booleanValue() {
                        if isConnecting!.booleanValue() {
                            JDStatusBarNotification.show(withStatus: AALocalized("StatusConnecting"))
                        } else {
                            JDStatusBarNotification.show(withStatus: AALocalized("StatusSyncing"))
                        }
                    } else {
                        JDStatusBarNotification.dismiss()
                    }
                }
            }
        }
    }
    
    open func presentMessengerInNewWindow() {
        let window = UIWindow(frame: UIScreen.main.bounds);
        window.backgroundColor = UIColor.white
        presentMessengerInWindow(window)
        window.makeKeyAndVisible()
    }

    //
    // Data Processing
    //
    
    /// Handling URL Opening in application
    func openUrl(_ url: String) {
        if let u = URL(string: url) {
            
            // Handle phone call
            if (u.scheme?.lowercased() == "telprompt") {
                 UIApplication.shared.openURL(u)
                return
            }
            
            // Handle web invite url
            if (u.scheme?.lowercased() == "http" || u.scheme?.lowercased() == "https") &&  inviteUrlHost != nil {
                
                if u.host == inviteUrlHost {
                    let token = u.lastPathComponent
                    joinGroup(token)
                    return
                }
            }
            
            // Handle custom scheme invite url
            if (u.scheme?.lowercased() == inviteUrlScheme?.lowercased()) {
                
                if (u.host == "invite") {
                    let token = u.query?.components(separatedBy: "=")[1]
                    if token != nil {
                        joinGroup(token!)
                        return
                    }
                }
                
                if let bindedController = bindedToWindow?.rootViewController {
                    let alert = UIAlertController(title: nil, message: AALocalized("ErrorUnableToJoin"), preferredStyle: .alert)
                    alert.addAction(UIAlertAction(title: AALocalized("AlertOk"), style: .cancel, handler: nil))
                    bindedController.present(alert, animated: true, completion: nil)
                }
                
                return
            }
            
            
            
            if (url.isValidUrl()){
                
                if let bindedController = bindedToWindow?.rootViewController {
                    // Dismiss Old Presented Controller to show new one
                    if let presented = bindedController.presentedViewController {
                        presented.dismiss(animated: true, completion: nil)
                    }
                    
                    // Building Controller for Web preview
                    let controller: UIViewController
                    if #available(iOS 9.0, *) {
                        controller = SFSafariViewController(url: u)
                    } else {
                        controller = AANavigationController(rootViewController: DZNWebViewController(url: u))
                    }
                    if AADevice.isiPad {
                        controller.modalPresentationStyle = .fullScreen
                    }
                    
                    // Presenting controller
                    bindedController.present(controller, animated: true, completion: nil)
                } else {
                    // Just Fallback. Might never happend
                    UIApplication.shared.openURL(u)
                }
            }
        }
    }
    
    /// Handling joining group by token
    func joinGroup(_ token: String) {
        if let bindedController = bindedToWindow?.rootViewController {
            let alert = UIAlertController(title: nil, message: AALocalized("GroupJoinMessage"), preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: AALocalized("AlertNo"), style: .cancel, handler: nil))
            alert.addAction(UIAlertAction(title: AALocalized("GroupJoinAction"), style: .default){ (action) -> Void in
                AAExecutions.execute(Actor.joinGroupViaLinkCommand(withToken: token), type: .safe, ignore: [], successBlock: { (val) -> Void in
                    
                    // TODO: Fix for iPad
                    let groupId = val as! JavaLangInteger
                    let tabBarController = bindedController as! UITabBarController
                    let index = tabBarController.selectedIndex
                    let navController = tabBarController.viewControllers![index] as! UINavigationController
                    if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.group(with: groupId.int32Value)) {
                        navController.pushViewController(customController, animated: true)
                    } else {
                        navController.pushViewController(ConversationViewController(peer: ACPeer.group(with: groupId.int32Value)), animated: true)
                    }
                    
                }, failureBlock: nil)
            })
            bindedController.present(alert, animated: true, completion: nil)
        }
    }
    
    /// Tracking page visible
    func trackPageVisible(_ page: ACPage) {
        analyticsDelegate?.analyticsPageVisible(page)
    }
    
    /// Tracking page hidden
    func trackPageHidden(_ page: ACPage) {
        analyticsDelegate?.analyticsPageHidden(page)
    }
    
    /// Tracking event
    func trackEvent(_ event: ACEvent) {
        analyticsDelegate?.analyticsEvent(event)
    }
    
    //
    // File System
    //
    
    open func fullFilePathForDescriptor(_ descriptor: String) -> String {
        return CocoaFiles.pathFromDescriptor(descriptor)
    }
    
    //
    // Manual Online handling
    //
    
    open func didBecameOnline() {
        
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
    
    open func didBecameOffline() {
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
        if UIApplication.shared.applicationState == .active {
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
    
    open func applicationDidFinishLaunching(_ application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    open func applicationDidBecomeActive(_ application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    open func applicationWillEnterForeground(_ application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
    }
    
    open func applicationDidEnterBackground(_ application: UIApplication) {
        if !automaticOnlineHandling || !isStarted {
            return
        }
        checkAppState()
        
        // Keep application running for 40 secs
        if messenger.isLoggedIn() {
            var completitionTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
            
            completitionTask = application.beginBackgroundTask(withName: "Completition", expirationHandler: { () -> Void in
                application.endBackgroundTask(completitionTask)
                completitionTask = UIBackgroundTaskInvalid
            })
            
            // Wait for 40 secs before app shutdown
            DispatchQueue.global(priority: DispatchQueue.GlobalQueuePriority.default).asyncAfter(deadline: DispatchTime.now() + Double(Int64(40.0 * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC)) { () -> Void in
                application.endBackgroundTask(completitionTask)
                completitionTask = UIBackgroundTaskInvalid
            }
        }
    }
    
    open func applicationWillResignActive(_ application: UIApplication) {
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
    
    open func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        
        if !messenger.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.noData)
            return
        }
        
        self.completionHandler = completionHandler
    }
    
    open func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
        // Nothing?
    }
    
    open func application(_ application: UIApplication, didRegisterUserNotificationSettings notificationSettings: UIUserNotificationSettings) {
        requestPushKit()
    }
    
    //
    // Handling background fetch events
    //
    
    open func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        
        if !messenger.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.noData)
            return
        }
        self.completionHandler = completionHandler
    }

    //
    // Handling invite url
    //
    
    func application(_ application: UIApplication, openURL url: URL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        
        dispatchOnUi { () -> Void in
            self.openUrl(url.absoluteString)
        }

        return true
    }
    
    open func application(_ application: UIApplication, handleOpenURL url: URL) -> Bool {
        
        dispatchOnUi { () -> Void in
            self.openUrl(url.absoluteString)
        }
        
        return true
    }
}

public enum AAAutoPush {
    case none
    case fromStart
    case afterLogin
}

public enum AAAuthStrategy {
    case phoneOnly
    case emailOnly
    case phoneEmail
}
