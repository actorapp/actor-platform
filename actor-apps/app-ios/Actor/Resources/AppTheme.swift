//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

var MainAppTheme = AppTheme()

class AppTheme {
    
    var navigation: AppNavigationBar { get { return AppNavigationBar() } }
    
    var tab: AppTabBar { get { return AppTabBar() } }
    
    var search: AppSearchBar { get { return AppSearchBar() } }
    
    var list: AppList { get { return AppList() } }
    
    var bubbles: ChatBubbles { get { return ChatBubbles() } }
    
    var text: AppText { get { return AppText() } }
    
    var chat: AppChat { get { return AppChat() } }
    
    var common: AppCommon { get { return AppCommon() } }
    
    func applyAppearance(application: UIApplication) {
        navigation.applyAppearance(application)
        tab.applyAppearance(application)
        search.applyAppearance(application)
        list.applyAppearance(application)
    }
}

class AppText {
    var textPrimary: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
}

class AppChat {
    var chatField: UIColor { get { return UIColor.whiteColor() } }
    
    var attachColor: UIColor { get { return UIColor.RGB(0x5085CB) } }
    
    var sendEnabled: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    var sendDisabled: UIColor { get { return UIColor.alphaBlack(0.56) } }
    
    var profileBgTint: UIColor { get { return UIColor.RGB(0x5085CB) } }
}

class AppCommon {
    var isDarkKeyboard: Bool { get { return false } }
    
    var tokenFieldText: UIColor { get { return UIColor.alphaBlack(0xDE/255.0) } }
    var tokenFieldTextSelected: UIColor { get { return UIColor.alphaBlack(0xDE/255.0) } }
    var tokenFieldBg: UIColor { get { return UIColor.RGB(0x5085CB) } }
}

class ChatBubbles {
    
    // Basic colors
    var text : UIColor { get { return UIColor.RGB(0x141617) } }
    var textUnsupported : UIColor { get { return UIColor.RGB(0x50b1ae) } }

    var bgOut: UIColor { get { return UIColor.RGB(0xD2FEFD) } }
    var bgOutBorder: UIColor { get { return UIColor.RGB(0x99E4E3) } }
    
    var bgIn : UIColor { get { return UIColor.whiteColor() } }
    var bgInBorder:UIColor { get { return  UIColor.RGB(0xCCCCCC) } }
    
    var statusActive : UIColor { get { return UIColor.RGB(0x3397f9) } }
    var statusPassive : UIColor { get { return UIColor.alphaBlack(0.27) } }
    // TODO: Fix
    var statusDanger : UIColor { get { return UIColor.redColor() } }
    
    var statusMediaActive : UIColor { get { return UIColor.RGB(0x1ed2f9) } }
    var statusMediaPassive : UIColor { get { return UIColor.whiteColor() } }
    // TODO: Fix
    var statusMediaDanger : UIColor { get { return UIColor.redColor() } }
    
    var statusDialogActive : UIColor { get { return UIColor.RGB(0x3397f9) } }
    var statusDialogPassive : UIColor { get { return UIColor.alphaBlack(0.27) } }
    // TODO: Fix
    var statusDialogDanger : UIColor { get { return UIColor.redColor() } }

    // Dialog-based colors
    var statusDialogSending : UIColor { get { return statusDialogPassive } }
    var statusDialogSent : UIColor { get { return statusDialogPassive } }
    var statusDialogReceived : UIColor { get { return statusDialogPassive } }
    var statusDialogRead : UIColor { get { return statusDialogActive } }
    var statusDialogError : UIColor { get { return statusDialogDanger } }
    
    // Text-based bubble colors
    var statusSending : UIColor { get { return statusPassive } }
    var statusSent : UIColor { get { return statusPassive } }
    var statusReceived : UIColor { get { return statusPassive } }
    var statusRead : UIColor { get { return statusActive } }
    var statusError : UIColor { get { return statusDanger } }
    
    var textBgOut: UIColor { get { return bgOut } }
    var textBgOutBorder : UIColor { get { return bgOutBorder } }
    var textBgIn : UIColor { get { return bgIn } }
    var textBgInBorder : UIColor { get { return bgInBorder } }
    
    var textDateOut : UIColor { get { return UIColor.alphaBlack(0.27) } }
    var textDateIn : UIColor { get { return UIColor.RGB(0x979797) } }
    
    var textOut : UIColor { get { return text } }
    var textIn : UIColor { get { return text } }
    
    var textUnsupportedOut : UIColor { get { return textUnsupported } }
    var textUnsupportedIn : UIColor { get { return textUnsupported } }

    
    // Media-based bubble colors
    var statusMediaSending : UIColor { get { return statusMediaPassive } }
    var statusMediaSent : UIColor { get { return statusMediaPassive } }
    var statusMediaReceived : UIColor { get { return statusMediaPassive } }
    var statusMediaRead : UIColor { get { return statusMediaActive } }
    var statusMediaError : UIColor { get { return statusMediaDanger } }
    
    var mediaBgOut: UIColor { get { return UIColor.whiteColor() } }
    var mediaBgOutBorder: UIColor { get { return UIColor.RGB(0xCCCCCC) } }
    var mediaBgIn: UIColor { get { return mediaBgOut } }
    var mediaBgInBorder: UIColor { get { return mediaBgOutBorder } }
    var mediaDateBg: UIColor { get { return UIColor.RGB(0x2D394A, alpha: 0.54) } }
    var mediaDate: UIColor { get { return UIColor.whiteColor() } }
    
    // Service-based bubble colors
    var serviceBg: UIColor { get { return UIColor.RGB(0x2D394A, alpha: 0.56) } }
    
    var chatBgTint: UIColor { get { return UIColor.RGB(0xe7e0c4) } }
}

class AppList {
    var actionColor : UIColor { get { return UIColor.RGB(0x5085CB) } }
    var bgColor: UIColor { get { return UIColor.whiteColor() } }
    var bgSelectedColor : UIColor { get { return UIColor.RGB(0xd9d9d9) } }
    
    var backyardColor : UIColor { get { return UIColor(red: 238/255.0, green: 238/255.0, blue: 238/255.0, alpha: 1) } }
    var separatorColor : UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0x1e/255.0) } }
    
    var textColor : UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    var hintColor : UIColor { get { return UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1) } }
    var sectionColor : UIColor { get { return UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1) } }
//     var arrowColor : UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    
    var dialogTitle: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    var dialogText: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0x8A/255.0) } }
    var dialogDate: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0x8A/255.0) } }
    
    var unreadText: UIColor { get { return UIColor.whiteColor() } }
    var unreadBg: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    var contactsTitle: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    var contactsShortTitle: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    
    func applyAppearance(application: UIApplication) {
        UITableViewHeaderFooterView.appearance().tintColor = sectionColor
    }
}

class AppSearchBar {
    var statusBarLightContent : Bool { get { return false } }
    var backgroundColor : UIColor { get { return UIColor.RGB(0xf1f1f1) } }
    var cancelColor : UIColor { get { return UIColor.RGB(0x8E8E93) } }
    var fieldBackgroundColor: UIColor { get { return UIColor.whiteColor() } }
    var fieldTextColor: UIColor { get { return UIColor.blackColor().alpha(0.56) } }
    
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
    
    var backgroundColor : UIColor { get { return UIColor.whiteColor() } }
    
    var showText : Bool { get { return true } }
    
    var selectedIconColor: UIColor { get { return mainColor } }
    var selectedTextColor : UIColor { get { return mainColor } }
    
    var unselectedIconColor:UIColor { get { return mainColor.alpha(0.56) } }
    var unselectedTextColor : UIColor { get { return mainColor.alpha(0.56) } }
    
    var barShadow : String? { get { return "CardTop2" } }
    
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
    
    var statusBarLightContent : Bool { get { return true } }
    var barColor:UIColor { get { return /*UIColor.RGB(0x5085CB)*/ UIColor.RGB(0x3576cc) } }
    var barSolidColor:UIColor { get { return UIColor.RGB(0x5085CB) } }
    
    var titleColor: UIColor { get { return UIColor.whiteColor() } }
    var subtitleColor: UIColor { get { return UIColor.whiteColor() } }
    var subtitleActiveColor: UIColor { get { return UIColor.whiteColor() } }
    
    var shadowImage : String? { get { return nil } }
    
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
        
        // navAppearance.setBackgroundImage(Imaging.imageWithColor(barColor, size: CGSize(width: 1, height: 1)), forBarMetrics: UIBarMetrics.Default)
        // navAppearance.shadowImage = Imaging.imageWithColor(barColor, size: CGSize(width: 1, height: 2))
        // Small hack for correct background color
        UISearchBar.appearance().backgroundColor = barColor
        
        // NavigationBar Shadow
//        navAppearance.shadowImage = nil
//        if (shadowImage == nil) {
//            navAppearance.shadowImage = UIImage()
//            navAppearance.setBackgroundImage(UIImage(), forBarMetrics: UIBarMetrics.Default)
//        } else {
//            navAppearance.shadowImage = UIImage(named: shadowImage!)
//        }
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
    
    func applyStatusBarFast() {
        if (statusBarLightContent) {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: false)
        } else {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: false)
        }
    }
}