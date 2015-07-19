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
        
        if config.pushId != nil {
            // Register notifications
            if application.respondsToSelector("registerUserNotificationSettings:") {
                let types: UIUserNotificationType = (.Alert | .Badge | .Sound)
                let settings: UIUserNotificationSettings = UIUserNotificationSettings(forTypes: types, categories: nil)
                application.registerUserNotificationSettings(settings)
                application.registerForRemoteNotifications()
            } else {
                application.registerForRemoteNotificationTypes(.Alert | .Badge | .Sound)
            }
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
        if (url.scheme == "actor") {
            if (url.path == "group_invite") {
                if (MSG.isLoggedIn()) {
                    execute(MSG.joinGroupViaLinkCommandWithUrl(url.absoluteString))
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
    
}