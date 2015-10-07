//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public var MainAppTheme = AppTheme()

public class AppTheme {
    
    public var navigation: AppNavigationBar { get { return AppNavigationBar() } }
    
    public var tab: AppTabBar { get { return AppTabBar() } }
    
    public var search: AppSearchBar { get { return AppSearchBar() } }
    
    public var list: AppList { get { return AppList() } }
    
    public var bubbles: ChatBubbles { get { return ChatBubbles() } }
    
    public var text: AppText { get { return AppText() } }
    
    public var chat: AppChat { get { return AppChat() } }
    
    public var common: AppCommon { get { return AppCommon() } }
    
    public var placeholder: AppPlaceholder { get { return AppPlaceholder() } }
    
    public func applyAppearance(application: UIApplication) {
        navigation.applyAppearance(application)
        tab.applyAppearance(application)
        search.applyAppearance(application)
        list.applyAppearance(application)
    }
}

public class AppText {
    public var textPrimary: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    
    public var bigAvatarPrimary: UIColor { get { return UIColor.whiteColor() } }
    public var bigAvatarSecondary: UIColor { get { return UIColor(red: 0.9, green: 0.9, blue: 0.9, alpha: 1) } }
}

public class AppPlaceholder {
    public var textTitle: UIColor { get { return UIColor.RGB(0x5085CB) } }
    public var textHint: UIColor { get { return UIColor(red: 80/255.0, green: 80/255.0, blue: 80/255.0, alpha: 1.0) } }
}

public class AppChat {
    public var chatField: UIColor { get { return UIColor.whiteColor() } }
    
    public var attachColor: UIColor { get { return UIColor.RGB(0x5085CB) } }
    
    public var sendEnabled: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    public var sendDisabled: UIColor { get { return UIColor.alphaBlack(0.56) } }
    
    public var profileBgTint: UIColor { get { return UIColor.RGB(0x5085CB) } }
    
    public var autocompleteHighlight: UIColor { get { return UIColor.RGB(0x5085CB) } }
}

public class AppCommon {
    public var isDarkKeyboard: Bool { get { return false } }
    
    public var tokenFieldText: UIColor { get { return UIColor.alphaBlack(0xDE/255.0) } }
    public var tokenFieldTextSelected: UIColor { get { return UIColor.alphaBlack(0xDE/255.0) } }
    public var tokenFieldBg: UIColor { get { return UIColor.RGB(0x5085CB) } }
    
    public var placeholders: [UIColor] {
        get {
            return [UIColor.RGB(0x59b7d3),
                    UIColor.RGB(0x1d4e6f),
                    UIColor.RGB(0x995794),
                    UIColor.RGB(0xff506c),
                    UIColor.RGB(0xf99341),
                    UIColor.RGB(0xe4d027),
                    UIColor.RGB(0x87c743)]
        }
    }
}

public class ChatBubbles {
    
    // Basic colors
    public var text : UIColor { get { return UIColor.RGB(0x141617) } }
    public var textUnsupported : UIColor { get { return UIColor.RGB(0x50b1ae) } }

    public var bgOut: UIColor { get { return UIColor.RGB(0xD2FEFD) } }
    public var bgOutBorder: UIColor { get { return UIColor.RGB(0x99E4E3) } }
    
    public var bgIn : UIColor { get { return UIColor.whiteColor() } }
    public var bgInBorder:UIColor { get { return  UIColor.RGB(0xCCCCCC) } }
    
    public var statusActive : UIColor { get { return UIColor.RGB(0x3397f9) } }
    public var statusPassive : UIColor { get { return UIColor.alphaBlack(0.27) } }
    // TODO: Fix
    public var statusDanger : UIColor { get { return UIColor.redColor() } }
    
    public var statusMediaActive : UIColor { get { return UIColor.RGB(0x1ed2f9) } }
    public var statusMediaPassive : UIColor { get { return UIColor.whiteColor() } }
    // TODO: Fix
    public var statusMediaDanger : UIColor { get { return UIColor.redColor() } }
    
    public var statusDialogActive : UIColor { get { return UIColor.RGB(0x3397f9) } }
    public var statusDialogPassive : UIColor { get { return UIColor.alphaBlack(0.27) } }
    // TODO: Fix
    public var statusDialogDanger : UIColor { get { return UIColor.redColor() } }

    // Dialog-based colors
    public var statusDialogSending : UIColor { get { return statusDialogPassive } }
    public var statusDialogSent : UIColor { get { return statusDialogPassive } }
    public var statusDialogReceived : UIColor { get { return statusDialogPassive } }
    public var statusDialogRead : UIColor { get { return statusDialogActive } }
    public var statusDialogError : UIColor { get { return statusDialogDanger } }
    
    // Text-based bubble colors
    public var statusSending : UIColor { get { return statusPassive } }
    public var statusSent : UIColor { get { return statusPassive } }
    public var statusReceived : UIColor { get { return statusPassive } }
    public var statusRead : UIColor { get { return statusActive } }
    public var statusError : UIColor { get { return statusDanger } }
    
    public var textBgOut: UIColor { get { return bgOut } }
    public var textBgOutBorder : UIColor { get { return bgOutBorder } }
    public var textBgIn : UIColor { get { return bgIn } }
    public var textBgInBorder : UIColor { get { return bgInBorder } }
    
    public var textDateOut : UIColor { get { return UIColor.alphaBlack(0.27) } }
    public var textDateIn : UIColor { get { return UIColor.RGB(0x979797) } }
    
    public var textOut : UIColor { get { return text } }
    public var textIn : UIColor { get { return text } }
    
    public var textUnsupportedOut : UIColor { get { return textUnsupported } }
    public var textUnsupportedIn : UIColor { get { return textUnsupported } }

    
    // Media-based bubble colors
    public var statusMediaSending : UIColor { get { return statusMediaPassive } }
    public var statusMediaSent : UIColor { get { return statusMediaPassive } }
    public var statusMediaReceived : UIColor { get { return statusMediaPassive } }
    public var statusMediaRead : UIColor { get { return statusMediaActive } }
    public var statusMediaError : UIColor { get { return statusMediaDanger } }
    
    public var mediaBgOut: UIColor { get { return UIColor.whiteColor() } }
    public var mediaBgOutBorder: UIColor { get { return UIColor.RGB(0xCCCCCC) } }
    public var mediaBgIn: UIColor { get { return mediaBgOut } }
    public var mediaBgInBorder: UIColor { get { return mediaBgOutBorder } }
    public var mediaDateBg: UIColor { get { return UIColor.RGB(0x2D394A, alpha: 0.54) } }
    public var mediaDate: UIColor { get { return UIColor.whiteColor() } }
    
    // Service-based bubble colors
    public var serviceBg: UIColor { get { return UIColor.RGB(0x2D394A, alpha: 0.56) } }
    
    public var chatBgTint: UIColor { get { return UIColor.RGB(0xe7e0c4) } }
}

public class AppList {
    public var actionColor : UIColor { get { return UIColor.RGB(0x5085CB) } }
    public var bgColor: UIColor { get { return UIColor.whiteColor() } }
    public var bgSelectedColor : UIColor { get { return UIColor.RGB(0xd9d9d9) } }
    
    public var backyardColor : UIColor { get { return UIColor(red: 238/255.0, green: 238/255.0, blue: 238/255.0, alpha: 1) } }
    public var separatorColor : UIColor { get { return UIColor.RGB(0xd4d4d4) } }
    
    public var textColor : UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    public var hintColor : UIColor { get { return UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1) } }
    public var sectionColor : UIColor { get { return UIColor.RGB(0x5b5a60) } }
    public var sectionHintColor : UIColor { get { return UIColor.RGB(0x5b5a60) } }
//     var arrowColor : UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    
    public var dialogTitle: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    public var dialogText: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0x8A/255.0) } }
    public var dialogDate: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0x8A/255.0) } }
    
    public var unreadText: UIColor { get { return UIColor.whiteColor() } }
    public var unreadBg: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    public var contactsTitle: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    public var contactsShortTitle: UIColor { get { return UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0) } }
    
    public func applyAppearance(application: UIApplication) {
        UITableViewHeaderFooterView.appearance().tintColor = sectionColor
    }
}

public class AppSearchBar {
    public var statusBarLightContent : Bool { get { return false } }
    public var backgroundColor : UIColor { get { return UIColor.RGB(0xf1f1f1) } }
    public var cancelColor : UIColor { get { return UIColor.RGB(0x8E8E93) } }
    public var fieldBackgroundColor: UIColor { get { return UIColor.whiteColor() } }
    public var fieldTextColor: UIColor { get { return UIColor.blackColor().alpha(0.56) } }
    
    public func applyAppearance(application: UIApplication) {
        
        // SearchBar Text Color
        let textField = UITextField.my_appearanceWhenContainedIn(UISearchBar.self)
        // textField.tintColor = UIColor.redColor()
        let font = UIFont.systemFontOfSize(14)
        textField.defaultTextAttributes = [NSFontAttributeName: font,
                        NSForegroundColorAttributeName : fieldTextColor]
    }
    
    public func applyStatusBar() {
        if (statusBarLightContent) {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
        } else {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
        }
    }
    
    public func styleSearchBar(searchBar: UISearchBar) {

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
        let fieldBg = Imaging.imageWithColor(fieldBackgroundColor, size: CGSize(width: 14,height: 28))
                                .roundCorners(14, h: 28, roundSize: 4)
        searchBar.setSearchFieldBackgroundImage(fieldBg.stretchableImageWithLeftCapWidth(7, topCapHeight: 0), forState: UIControlState.Normal)
        
        // SearchBar cancel color
        searchBar.tintColor = cancelColor
        
        // Apply keyboard color
        searchBar.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
    }
}

public class AppTabBar {
    
    private let mainColor = UIColor.RGB(0x5085CB)
    
    public var backgroundColor : UIColor { get { return UIColor.whiteColor() } }
    
    public var showText : Bool { get { return true } }
    
    public var selectedIconColor: UIColor { get { return mainColor } }
    public var selectedTextColor : UIColor { get { return mainColor } }
    
    public var unselectedIconColor:UIColor { get { return UIColor.RGB(0x929292) } }
    public var unselectedTextColor : UIColor { get { return UIColor.RGB(0x949494) } }
    
    public var barShadow : String? { get { return "CardTop2" } }
    
    public func createSelectedIcon(name: String) -> UIImage {
        return UIImage(named: name)!.tintImage(selectedIconColor)
            .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
    }

    public func createUnselectedIcon(name: String) -> UIImage {
        return UIImage(named: name)!.tintImage(unselectedIconColor)
            .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
    }
    
    public func applyAppearance(application: UIApplication) {
        let tabBar = UITabBar.appearance()
        // TabBar Background color
        tabBar.barTintColor = backgroundColor;

//        // TabBar Shadow
//        if (barShadow != nil) {
//            tabBar.shadowImage = UIImage(named: barShadow!);
//        } else {
//            tabBar.shadowImage = nil
//        }
        
        let tabBarItem = UITabBarItem.appearance()
        // TabBar Unselected Text
        tabBarItem.setTitleTextAttributes([NSForegroundColorAttributeName: unselectedTextColor], forState: UIControlState.Normal)
        // TabBar Selected Text
        tabBarItem.setTitleTextAttributes([NSForegroundColorAttributeName: selectedTextColor], forState: UIControlState.Selected)
    }
}

public class AppNavigationBar {
    
    public var statusBarLightContent : Bool { get { return true } }
    public var barColor:UIColor { get { return /*UIColor.RGB(0x5085CB)*/ UIColor.RGB(0x3576cc) } }
    public var barSolidColor:UIColor { get { return UIColor.RGB(0x5085CB) } }
    
    public var titleColor: UIColor { get { return UIColor.whiteColor() } }
    public var subtitleColor: UIColor { get { return UIColor.whiteColor() } }
    public var subtitleActiveColor: UIColor { get { return UIColor.whiteColor() } }
    
    public var shadowImage : String? { get { return nil } }
    
    public var progressPrimary: UIColor { get { return UIColor.RGB(0x1484ee) } }
    public var progressSecondary: UIColor { get { return UIColor.RGB(0xaccceb) } }
    
    public func applyAppearance(application: UIApplication) {
        // StatusBar style
        if (statusBarLightContent) {
            application.statusBarStyle = UIStatusBarStyle.LightContent
        } else {
            application.statusBarStyle = UIStatusBarStyle.Default
        }
        
        let navAppearance = UINavigationBar.appearance();
        
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
    
    public func applyAuthStatusBar() {
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
    }
    
    public func applyStatusBar() {
        if (statusBarLightContent) {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
        } else {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: true)
        }
    }
    
    public func applyStatusBarFast() {
        if (statusBarLightContent) {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: false)
        } else {
            UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.Default, animated: false)
        }
    }
}