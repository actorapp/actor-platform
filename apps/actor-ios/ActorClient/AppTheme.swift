//
//  AppTheme.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 14.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

var MainAppTheme : AppTheme {
    get {
        return AppTheme()
    }
}

class AppTheme {
    
    var navigation: AppNavigationBar {
        get {
            return AppNavigationBar()
        }
    }
    
    var tab:AppTabBar {
        get {
            return AppTabBar()
        }
    }
}

class SearchBar {
    var backgroundColor : UIColor {
        get {
            return UIColor.whiteColor()
        }
    }
    
    var fieldBackgroundColor: UIColor {
        get {
            return 
        }
    }
}

class AppTabBar {
    
    private let mainColor = UIColor.RGB(0x5085CB)
    
    var backgroundColor : UIColor {
        get {
            return UIColor.whiteColor()
        }
    }
    
    var isTransculent : Bool {
        get {
            return true
        }
    }

    var showText : Bool {
        get {
            return true
        }
    }
    
    var selectedIconColor: UIColor {
        get {
            return mainColor
        }
    }
    var selectedTextColor : UIColor {
        get {
            return mainColor
        }
    }
    
    var unselectedIconColor:UIColor {
        get {
            return mainColor.alpha(0.56)
        }
    }
    var unselectedTextColor : UIColor {
        get {
            return mainColor.alpha(0.56)
        }
    }
    
    var barShadow : String? {
        get {
            return "CardTop2"
        }
    }
    
    func createSelectedIcon(name: String) -> UIImage {
        return UIImage(named: name)!.tintImage(selectedIconColor)
            .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
    }

    func createUnselectedIcon(name: String) -> UIImage {
        return UIImage(named: name)!.tintImage(unselectedIconColor)
            .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
    }
    
    func applyAppearance(application: UIApplication) {
        var tabBar = UITabBar.appearance()
        
        // TabBar Transculent
        tabBar.translucent = isTransculent
        
        // TabBar Background color
        tabBar.barTintColor = backgroundColor;

        // TabBar Shadow
        if (barShadow != nil) {
            tabBar.shadowImage = UIImage(named: barShadow!);
        } else {
            tabBar.shadowImage = nil
        }
        
        var tabBarItem = UITabBarItem.appearance()
        // TabBar Unselected Text
        tabBarItem.setTitleTextAttributes([NSForegroundColorAttributeName: unselectedTextColor], forState: UIControlState.Normal)
        // TabBar Selected Text
        tabBarItem.setTitleTextAttributes([NSForegroundColorAttributeName: selectedTextColor], forState: UIControlState.Selected)
    }
}

class AppNavigationBar {
    
    var statusBarLightContent : Bool {
        get {
            return true
        }
    }

    var barColor:UIColor {
        get {
            return UIColor.RGB(0x5085CB)
        }
    }
    
    var titleColor: UIColor {
        get {
            return UIColor.whiteColor()
        }
    }
    
    var subtitleColor: UIColor {
        get {
            return UIColor.whiteColor()
        }
    }
    
    var subtitleActiveColor: UIColor {
        get {
            return UIColor.whiteColor()
        }
    }
    
    var isTransculent : Bool {
        get {
            return true
        }
    }
    
    var shadowImage : String? {
        get {
            return nil
        }
    }
    
    func applyAppearance(application: UIApplication) {
        // StatusBar style
        if (statusBarLightContent) {
            application.statusBarStyle = UIStatusBarStyle.LightContent
        } else {
            application.statusBarStyle = UIStatusBarStyle.Default
        }
        
        var navAppearance = UINavigationBar.appearance();
        // NavigationBar Icon
        navAppearance.tintColor = titleColor;
        // NavigationBar Text
        navAppearance.titleTextAttributes = [NSForegroundColorAttributeName: titleColor];
        // NavigationBar Background
        navAppearance.barTintColor = barColor;
        // NavigationBar Transculency
        navAppearance.translucent = isTransculent;
        // NavigationBar Shadow
        if (shadowImage == nil) {
            navAppearance.shadowImage = nil
        } else {
            navAppearance.shadowImage = UIImage(named: shadowImage!)
        }
    }
}