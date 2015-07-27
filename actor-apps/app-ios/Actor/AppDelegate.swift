//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

@objc class AppDelegate : UIResponder,  UIApplicationDelegate {
    
    // MARK: -
    // MARK: Private vars
    
    var window : UIWindow?
    private var binder = Binder()
    private var syncTask: UIBackgroundTaskIdentifier?
    private var completionHandler: ((UIBackgroundFetchResult) -> Void)?
    
    // MARK: -
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        
        var config = MSG.config
        
        // Apply crash logging
        if config.mint != nil {
            Mint.sharedInstance().initAndStartSession(config.mint!)
        }
        
        // Register hockey app
        if config.hockeyapp != nil {
            BITHockeyManager.sharedHockeyManager().configureWithIdentifier(config.hockeyapp!)
            BITHockeyManager.sharedHockeyManager().disableCrashManager = true
            BITHockeyManager.sharedHockeyManager().updateManager.checkForUpdateOnLaunch = true
            BITHockeyManager.sharedHockeyManager().startManager()
            BITHockeyManager.sharedHockeyManager().authenticator.authenticateInstallation()
        }
        
        // Register notifications
        // Register always even when not enabled in build for local notifications
        if application.respondsToSelector("registerUserNotificationSettings:") {
            let types: UIUserNotificationType = (.Alert | .Badge | .Sound)
            let settings: UIUserNotificationSettings = UIUserNotificationSettings(forTypes: types, categories: nil)
            application.registerUserNotificationSettings(settings)
            application.registerForRemoteNotifications()
        } else {
            application.registerForRemoteNotificationTypes(.Alert | .Badge | .Sound)
        }
        
        // Apply styles
        MainAppTheme.applyAppearance(application)
       
        // Bind Messenger LifeCycle
        binder.bind(MSG.getAppState().getIsSyncing(), closure: { (value: JavaLangBoolean?) -> () in
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

        if (MSG.isLoggedIn()) {
            onLoggedIn(false)
        } else {
            // Create root layout for login
            
            let phoneController = AuthPhoneViewController()
            var loginNavigation = AANavigationController(rootViewController: phoneController)
            loginNavigation.navigationBar.tintColor = Resources.BarTintColor
            loginNavigation.makeBarTransparent()
            
            window?.rootViewController = loginNavigation
            window?.makeKeyAndVisible();
        }
        
        
        return true;
    }
    
    func onLoggedIn(isAfterLogin: Bool) {
        // Create root layout for app
        MSG.onAppVisible()
        var rootController : UIViewController? = nil
        if (isIPad) {
            var splitController = MainSplitViewController()
            splitController.viewControllers = [MainTabViewController(isAfterLogin: isAfterLogin), NoSelectionViewController()]
            
            rootController = splitController
        } else {
            var tabController = MainTabViewController(isAfterLogin: isAfterLogin)
            binder.bind(MSG.getAppState().getIsAppLoaded(), valueModel2: MSG.getAppState().getIsAppEmpty()) { (loaded: JavaLangBoolean?, empty: JavaLangBoolean?) -> () in
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
        window?.makeKeyAndVisible();
    }
    
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject?) -> Bool {
        println("open url: \(url)")
        
        if (url.scheme == "actor") {
            if (url.host == "invite") {
                if (MSG.isLoggedIn()) {
                    var token = url.query?.componentsSeparatedByString("=")[1]
                    if token != nil {
                        UIAlertView.showWithTitle(nil, message: localized("GroupJoinMessage"), cancelButtonTitle: localized("AlertNo"), otherButtonTitles: [localized("GroupJoinAction")], tapBlock: { (view, index) -> Void in
                            if (index == view.firstOtherButtonIndex) {
                                self.execute(MSG.joinGroupViaLinkCommandWithUrl(token), successBlock: { (val) -> Void in
                                    var groupId = val as! JavaLangInteger
                                    self.openChat(AMPeer.groupWithInt(groupId.intValue))
                                }, failureBlock: { (val) -> Void in
                                    
                                    if let res = val as? AMRpcException {
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
    
    func applicationWillEnterForeground(application: UIApplication) {
        MSG.onAppVisible();
        // Hack for resync phone book
        MSG.onPhoneBookChanged()
    }

    func applicationDidEnterBackground(application: UIApplication) {
        MSG.onAppHidden();
        
        if MSG.isLoggedIn() {
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
    
    // MARK: -
    // MARK: Notifications
    
    func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        
        let tokenString = "\(deviceToken)".stringByReplacingOccurrencesOfString(" ", withString: "").stringByReplacingOccurrencesOfString("<", withString: "").stringByReplacingOccurrencesOfString(">", withString: "")
        
        var config = MSG.config
        
        if config.pushId != nil {
            MSG.registerApplePushWithApnsId(jint(config.pushId!), withToken: tokenString)
        }
        
        if config.mixpanel != nil {
            Mixpanel.sharedInstance().people.addPushDeviceToken(deviceToken)
        }
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject], fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        
        if !MSG.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.NoData)
            return
        }
        self.completionHandler = completionHandler
    }
    
    func application(application: UIApplication, performFetchWithCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
        if !MSG.isLoggedIn() {
            completionHandler(UIBackgroundFetchResult.NoData)
            return
        }
        self.completionHandler = completionHandler
    }
    
    func execute(command: AMCommand) {
        execute(command, successBlock: nil, failureBlock: nil)
    }
    
    func execute(command: AMCommand, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        var window = UIApplication.sharedApplication().windows[1] as! UIWindow
        var hud = MBProgressHUD(window: window)
        hud.mode = MBProgressHUDMode.Indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubviewToFront(hud)
        hud.show(true)
        command.startWithCallback(CocoaCallback(result: { (val:Any?) -> () in
            dispatch_async(dispatch_get_main_queue(), {
                hud.hide(true)
                successBlock?(val: val)
            })
            }, error: { (val) -> () in
                dispatch_async(dispatch_get_main_queue(), {
                    hud.hide(true)
                    failureBlock?(val: val)
                })
        }))
    }
    
    func openChat(peer: AMPeer) {
        for i in UIApplication.sharedApplication().windows {
            var root = (i as! UIWindow).rootViewController
            if let tab = root as? MainTabViewController {
                var controller = tab.viewControllers![tab.selectedIndex] as! AANavigationController
                var destController = ConversationViewController(peer: peer)
                destController.hidesBottomBarWhenPushed = true
                controller.pushViewController(destController, animated: true)
                return
            } else if let split = root as? MainSplitViewController {
                split.navigateDetail(ConversationViewController(peer: peer))
                return
            }
        }
    }
}