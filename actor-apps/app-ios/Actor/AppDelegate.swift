//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

@objc class AppDelegate : UIResponder,  UIApplicationDelegate {
    
    // MARK: -
    // MARK: Private vars
    
    var window : UIWindow?
    private var binder = Binder()
    
    // MARK: -
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        // Apply crash logging
        if let apiKey = NSBundle.mainBundle().infoDictionary?["MINT_API_KEY"] as? String {
            if (apiKey.trim().size() > 0) {
                Mint.sharedInstance().initAndStartSession(apiKey)
            }
        }
        
        // Register hockey app
        if let hockey = NSBundle.mainBundle().infoDictionary?["HOCKEY"] as? String {
            if (hockey.trim().size() > 0) {
                BITHockeyManager.sharedHockeyManager().configureWithIdentifier(hockey)
                BITHockeyManager.sharedHockeyManager().disableCrashManager = true
                BITHockeyManager.sharedHockeyManager().updateManager.checkForUpdateOnLaunch = true
                BITHockeyManager.sharedHockeyManager().startManager()
                BITHockeyManager.sharedHockeyManager().authenticator.authenticateInstallation()
            }
        }
        
        // Register notifications
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
            splitController.viewControllers = [MainTabViewController(isAfterLogin: isAfterLogin), NoSelectionController()]
            
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
        application.beginBackgroundTaskWithExpirationHandler { () -> Void in
            
        }
    }
    
    // MARK: -
    // MARK: Notifications
    
    func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        let tokenString = "\(deviceToken)".stringByReplacingOccurrencesOfString(" ", withString: "").stringByReplacingOccurrencesOfString("<", withString: "").stringByReplacingOccurrencesOfString(">", withString: "")
        
        MSG.registerApplePushWithApnsId(jint((NSBundle.mainBundle().objectForInfoDictionaryKey("API_PUSH_ID") as! String).toInt()!), withToken: tokenString)
        
        if let apiKey = NSBundle.mainBundle().infoDictionary?["MIXPANEL_API_KEY"] as? String {
            if (apiKey.trim().size() > 0) {
                 Mixpanel.sharedInstance().people.addPushDeviceToken(deviceToken)
            }
        }
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        println("\(userInfo)")
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