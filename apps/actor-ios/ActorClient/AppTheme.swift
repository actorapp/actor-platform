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
    
    var search:AppSearchBar {
        get {
            return AppSearchBar()
        }
    }
}

class AppSearchBar {
    
    var statusBarLightContent : Bool {
        get {
            return false
        }
    }
    
    var backgroundColor : UIColor {
        get {
            return UIColor.RGB(0xf1f1f1)
        }
    }
    
    var cancelColor : UIColor {
        get {
            return UIColor.RGB(0x8E8E93)
        }
    }
    
    var fieldBackgroundColor: UIColor {
        get {
            return UIColor.whiteColor()
        }
    }
    
    var fieldTextColor: UIColor {
        get {
            return UIColor.blackColor().alpha(0.56)
        }
    }
    
    func applyAppearance(application: UIApplication) {
        
        // SearchBar Text Color
        var textField = UITextField.my_appearanceWhenContainedIn(UISearchBar.self)
        // textField.tintColor = UIColor.redColor()
        var font = UIFont(name: "HelveticaNeue", size: 14.0)
        textField.defaultTextAttributes = [NSFontAttributeName: font!,
                        NSForegroundColorAttributeName : fieldTextColor]
    }
    
    func applyStatusBar() {
        if (statusBarLightContent) {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
        } else {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
        }
    }
    
    func styleSearchBar(searchBar: UISearchBar) {
        // SearchBar Minimal Style
        searchBar.searchBarStyle = UISearchBarStyle.Default
        // SearchBar Transculent
        searchBar.translucent = false
        // SearchBar placeholder animation fix
        searchBar.placeholder = "";
        
        // SearchBar background color
        searchBar.barTintColor = backgroundColor.forTransparentBar()
        searchBar.setBackgroundImage(Imaging.imageWithColor(backgroundColor, size: CGSize(width: 1, height: 1)), forBarPosition: UIBarPosition.Any, barMetrics: UIBarMetrics.Default)
        searchBar.backgroundColor = backgroundColor
        
        // SearchBar field color
        var fieldBg = Imaging.imageWithColor(fieldBackgroundColor, size: CGSize(width: 14,height: 28))
                                .roundCorners(14, h: 28, roundSize: 4)
        searchBar.setSearchFieldBackgroundImage(fieldBg.stretchableImageWithLeftCapWidth(7, topCapHeight: 0), forState: UIControlState.Normal)
        
        // SearchBar cancel color
        searchBar.tintColor = cancelColor
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
            return false
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
        navAppearance.setBackgroundImage(Imaging.imageWithColor(barColor, size: CGSize(width: 1, height: 1)), forBarMetrics: UIBarMetrics.Default)
//        navAppearance.shadowImage = Imaging.imageWithColor(barColor, size: CGSize(width: 1, height: 2))
        // Small hack for correct background color
        UISearchBar.appearance().backgroundColor = barColor
        
        // NavigationBar Transculency
        navAppearance.translucent = isTransculent;
        // NavigationBar Shadow
        if (shadowImage == nil) {
            navAppearance.shadowImage = UIImage()
        } else {
            navAppearance.shadowImage = UIImage(named: shadowImage!)
        }
    }
    
    func applyAuthStatusBar() {
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
    }
    
    func applyStatusBar() {
        if (statusBarLightContent) {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
        } else {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
        }
    }
}