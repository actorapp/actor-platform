//
//  AppDelegate.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

@objc class AppDelegate : UIResponder,  UIApplicationDelegate {
    
    private var window : UIWindow?;
    
    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject : AnyObject]?) -> Bool {
        
        application.statusBarStyle = UIStatusBarStyle.LightContent
        
        var navAppearance = UINavigationBar.appearance();
        navAppearance.tintColor = UIColor.whiteColor();
        navAppearance.barTintColor = UIColor.whiteColor();
        navAppearance.backgroundColor = Resources.TintColor;
        navAppearance.setBackgroundImage(Imaging.imageWithColor(Resources.TintColor, size: CGSize(width: 1, height: 46)), forBarMetrics: UIBarMetrics.Default)
        navAppearance.shadowImage = UIImage(named: "CardBottom2")
        navAppearance.titleTextAttributes = [NSForegroundColorAttributeName: UIColor.whiteColor()];
        navAppearance.translucent = false;
        
        var textFieldAppearance = UITextField.appearance();
        textFieldAppearance.tintColor = Resources.TintColor;
        
        //var searchBarAppearance = UISearchBar.appearance();
        //searchBarAppearance.tintColor = Resources.TintColor;
        
        UITabBar.appearance().translucent = false
        UITabBar.appearance().tintColor = Resources.BarTintColor;
        UITabBar.appearance().backgroundImage = Imaging.imageWithColor(UIColor.whiteColor(), size: CGSize(width: 1, height: 46))
        UITabBar.appearance().shadowImage = UIImage(named: "CardTop2");
        UITabBar.appearance().selectionIndicatorImage = Imaging.imageWithColor(UIColor(red: 0xeb/255.0, green: 0xed/255.0, blue: 0xf2/255.0, alpha: 1), size: CGSize(width: 1, height: 46)).resizableImageWithCapInsets(UIEdgeInsetsZero);
        
        
        
//        setTitleTextAttributes(NSForegroundColorAttributeName, );
        
//        var barButtonItemAppearance = UIBarButtonItem.appearance();
//        barButtonItemAppearance
        
//        [UINavigationBar appearance].tintColor = [UIColor whiteColor];
//        [UINavigationBar appearance].barTintColor = BAR_COLOR;
//        [UINavigationBar appearance].backgroundColor = BAR_COLOR;
//        [UINavigationBar appearance].titleTextAttributes = @{NSForegroundColorAttributeName:[UIColor whiteColor]};
//        [UITextField appearance].tintColor = BAR_COLOR;
//        [UISearchBar appearance].tintColor = BAR_COLOR;
//        //[UISearchBar appearance].backgroundImage = [UIImage new];
//        [[UIBarButtonItem appearanceWhenContainedIn:[UISearchBar class],nil]
//        setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]} forState:UIControlStateNormal];
//        
//        [UITableViewCell appearance].tintColor = BAR_COLOR;
//        [UITableView appearance].sectionIndexColor = BAR_COLOR;
//        [UITabBar appearance].tintColor = BAR_COLOR;
//        
//        [MagicalRecord setupAutoMigratingCoreDataStack];
//        
//        [CocoaMessenger messenger];

        // Starting app
        

        var rootController = UINavigationController(rootViewController: MainTabController());
    
        window = UIWindow(frame: UIScreen.mainScreen().bounds);
//        window?.tintColor = TintColor;
        window?.rootViewController = rootController;
        window?.makeKeyAndVisible();
        
        if (!MSG.isLoggedIn()){
            var controller = UIStoryboard(name: "Auth", bundle: nil).instantiateInitialViewController() as! UIViewController;
            rootController.presentViewController(controller, animated: false, completion: nil)
        }
        
        return true;
    }
    
    func applicationWillEnterForeground(application: UIApplication) {
        MSG.onAppVisible();
    }

    func applicationDidEnterBackground(application: UIApplication) {
        MSG.onAppHidden();
    }
}