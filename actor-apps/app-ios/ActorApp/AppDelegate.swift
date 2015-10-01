//
//  Copyright (C) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

import Fabric
import Crashlytics

@objc class AppDelegate : UIResponder,  UIApplicationDelegate {
    
    var window : UIWindow?
    private var binder = Binder()
    private var syncTask: UIBackgroundTaskIdentifier?
    private var completionHandler: ((UIBackgroundFetchResult) -> Void)?
    private let badgeView = UIImageView()
    
    private var isInited = false
    
    private var isVisible = false
    
    private var badgeCount = 0
    private var isBadgeVisible = false
    
    func assumeInited() {
        if isInited {
            return
        }
        isInited = true
        
        // Apply crash logging
        
        // Even when Fabric/Crashlytics not configured
        // this method doesn't crash
        Fabric.with([Crashlytics.self()])
        
        // Creating Actor
        createActor()
        
        // Creating app style
        initStyles()
    }
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        
        assumeInited()
        
        // Register hockey app
        if AppConfig.hockeyapp != nil {
            BITHockeyManager.sharedHockeyManager().configureWithIdentifier(AppConfig.hockeyapp!)
            BITHockeyManager.sharedHockeyManager().disableCrashManager = true
            BITHockeyManager.sharedHockeyManager().updateManager.checkForUpdateOnLaunch = true
            BITHockeyManager.sharedHockeyManager().startManager()
            BITHockeyManager.sharedHockeyManager().authenticator.authenticateInstallation()
        }
        
        // Register notifications
        // Register always even when not enabled in build for local notifications
        if #available(iOS 8.0, *) {
            let types: UIUserNotificationType = [.Alert, .Badge, .Sound]
            let settings: UIUserNotificationSettings = UIUserNotificationSettings(forTypes: types, categories: nil)
            application.registerUserNotificationSettings(settings)
            application.registerForRemoteNotifications()
        } else {
            application.registerForRemoteNotificationTypes([.Alert, .Badge, .Sound])
        }
        
        // Apply styles
        MainAppTheme.applyAppearance(application)

        // Bind Messenger LifeCycle
        binder.bind(Actor.getAppState().isSyncing, closure: { (value: JavaLangBoolean?) -> () in
            if value!.booleanValue() {
                if self.syncTask == nil {
                    self.syncTask = application.beginBackgroundTaskWithName("Background Sync", expirationHandler: { () -> Void in
                        
                    })
                }
            } else {
                if self.syncTask != nil {
                    application.endBackgroundTask(self.syncTask!)
                    self.syncTask = nil
                }
                if self.completionHandler != nil {
                    self.completionHandler!(UIBackgroundFetchResult.NewData)
                    self.completionHandler = nil
                }
            }
        })

        // Creating main window
        window = UIWindow(frame: UIScreen.mainScreen().bounds);
        window?.backgroundColor = UIColor.whiteColor()
        
        if (Actor.isLoggedIn()) {
            onLoggedIn(false)
        } else {
            // Create root layout for login
            
            let phoneController = AuthPhoneViewController()
            let loginNavigation =   AANavigationController(rootViewController: phoneController)
            loginNavigation.navigationBar.tintColor = MainAppTheme.navigation.barColor
            loginNavigation.makeBarTransparent()
            
            window?.rootViewController = loginNavigation
        }
        
        window?.makeKeyAndVisible();
        
        badgeView.image = Imaging.roundedImage(UIColor.RGB(0xfe0000), size: CGSizeMake(16, 16), radius: 8)
        // badgeView.frame = CGRectMake(16, 22, 32, 16)
        badgeView.alpha = 0
        
        let badgeText = UILabel()
        badgeText.text = "0"
        badgeText.textColor = UIColor.whiteColor()
        // badgeText.frame = CGRectMake(0, 0, 32, 16)
        badgeText.font = UIFont.systemFontOfSize(12)
        badgeText.textAlignment = NSTextAlignment.Center
        badgeView.addSubview(badgeText)
        
        window?.addSubview(badgeView)
        
        // Bind badge counter
        binder.bind(Actor.getAppState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            self.badgeCount = Int((value!).integerValue)
            application.applicationIconBadgeNumber = self.badgeCount
            badgeText.text = "\(self.badgeCount)"
            if (self.isBadgeVisible && self.badgeCount > 0) {
                self.badgeView.showView()
            } else if (self.badgeCount == 0) {
                self.badgeView.hideView()
            }
            
            badgeText.frame = CGRectMake(0, 0, 128, 16)
            badgeText.sizeToFit()
            
            if badgeText.frame.width < 8 {
                self.badgeView.frame = CGRectMake(16, 22, 16, 16)
            } else {
                self.badgeView.frame = CGRectMake(16, 22, badgeText.frame.width + 8, 16)
            }
            badgeText.frame = self.badgeView.bounds
        })
        
        checkAppState(application)
        
        return true;
    }
    
    func onLoggedIn(isAfterLogin: Bool) {
        // Create root layout for app
        var rootController : UIViewController? = nil
        if (isIPad) {
            let splitController = MainSplitViewController()
            splitController.viewControllers = [MainTabViewController(isAfterLogin: isAfterLogin), NoSelectionViewController()]
            
            rootController = splitController
        } else {
            let tabController = MainTabViewController(isAfterLogin: isAfterLogin)
            binder.bind(Actor.getAppState().isAppLoaded, valueModel2: Actor.getAppState().isAppEmpty) { (loaded: JavaLangBoolean?, empty: JavaLangBoolean?) -> () in
                if (empty!.booleanValue()) {
                    if (loaded!.booleanValue()) {
                        tabController.showAppIsEmptyPlaceholder()
                    } else {
                        tabController.showAppIsSyncingPlaceholder()
                    }
                } else {
                    tabController.hidePlaceholders()
                }
            }
            rootController = tabController
        }
        
        window?.rootViewController = rootController!
    }
    
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        
        assumeInited()
        
        if (url.scheme == "actor") {
            if (url.host == "invite") {
                if (Actor.isLoggedIn()) {
                    let token = url.query?.componentsSeparatedByString("=")[1]
                    if token != nil {
                        UIAlertView.showWithTitle(nil, message: localized("GroupJoinMessage"), cancelButtonTitle: localized("AlertNo"), otherButtonTitles: [localized("GroupJoinAction")], tapBlock: { (view, index) -> Void in
                            if (index == view.firstOtherButtonIndex) {
                                Executions.execute(Actor.joinGroupViaLinkCommandWithUrl(token), successBlock: { (val) -> Void in
                                    let groupId = val as! JavaLangInteger
                                    self.openChat(ACPeer.groupWithInt(groupId.intValue))
                                    }, failureBlock: { (val) -> Void in
                                        
                                        if let res = val as? ACRpcException {
                                            if res.getTag() == "USER_ALREADY_INVITED" {
                                                UIAlertView.showWithTitle(nil, message: localized("ErrorAlreadyJoined"), cancelButtonTitle: localized("AlertOk"), otherButtonTitles: nil, tapBlock: nil)
                                                return
                                            }
                                        }
                                        
                                        UIAlertView.showWithTitle(nil, message: localized("ErrorUnableToJoin"), cancelButtonTitle: localized("AlertOk"), otherButtonTitles: nil, tapBlock: nil)
                                })
                            }
                        })
                    }
                }
                
                return true
            }
        }
        return false
    }
    
    // Checking app visible state
    
    func checkAppState(application: UIApplication) {
        if application.applicationState == .Active {
            if !isVisible {
                isVisible = true
                
                // Mark app as visible
                Actor.onAppVisible();
                
                // Notify analytics about visibilibty change
                Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(true))
                
                // Hack for resync phone book
                Actor.onPhoneBookChanged()
            }
        } else {
            if isVisible {
                isVisible = false
                
                // Mark app as hidden
                Actor.onAppHidden();
                
                // Notify analytics about visibilibty change
                Analytics.track(ACAllEvents.APP_VISIBLEWithBoolean(false))
            }
        }
    }

    // Lifecycle
    
    func applicationDidFinishLaunching(application: UIApplication) {
        assumeInited()
        
        checkAppState(application)
    }
    
    func applicationDidBecomeActive(application: UIApplication) {
        assumeInited()
        
        checkAppState(application)
    }
    
    func applicationWillEnterForeground(application: UIApplication) {
        assumeInited()
        
        checkAppState(application)
    }
    
    func applicationDidEnterBackground(application: UIApplication) {
        assumeInited()
        
        checkAppState(application)
        
        // Keep application running for 40 secs
        if Actor.isLoggedIn() {
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
    
    func applicationWillResignActive(application: UIApplication) {
        assumeInited()
        
        checkAppState(application)
    }
    
    // Push notifications
    
    func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        assumeInited()
        
        let tokenString = "\(deviceToken)".stringByReplacingOccurrencesOfString(" ", withString: "").stringByReplacingOccurrencesOfString("<", withString: "").stringByReplacingOccurrencesOfString(">", withString: "")
        
        if AppConfig.pushId != nil {
            Actor.registerApplePushWithApnsId(jint(AppConfig.pushId!), withToken: tokenString)
        }
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        assumeInited()
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject], fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        
        assumeInited()
        
        if !Actor.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.NoData)
            return
        }
        
        self.completionHandler = completionHandler
    }
    
    func application(application: UIApplication, performFetchWithCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        assumeInited()
        
        if !Actor.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.NoData)
            return
        }
        self.completionHandler = completionHandler
    } 
    
    func openChat(peer: ACPeer) {
        for i in UIApplication.sharedApplication().windows {
            if let tab = i.rootViewController as? MainTabViewController {
                let controller = tab.viewControllers![tab.selectedIndex] as! AANavigationController
                let destController = ConversationViewController(peer: peer)
                destController.hidesBottomBarWhenPushed = true
                controller.pushViewController(destController, animated: true)
                return
            } else if let split = i.rootViewController as? MainSplitViewController {
                split.navigateDetail(ConversationViewController(peer: peer))
                return
            }
        }
    }
    
    func showBadge() {
        isBadgeVisible = true
        if badgeCount > 0 {
            self.badgeView.showView()
        }
    }
    
    func hideBadge() {
        isBadgeVisible = false
        self.badgeView.hideView()
    }
}