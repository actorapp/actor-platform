//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public var MainAppTheme = AppTheme()

public class AppTheme {
    
    public var search: AppSearchBar { get { return AppSearchBar() } }
    
    public var bubbles: ChatBubbles { get { return ChatBubbles() } }
    
    public var text: AppText { get { return AppText() } }
    
    public var chat: AppChat { get { return AppChat() } }
    
    public var common: AppCommon { get { return AppCommon() } }
    
    public var placeholder: AppPlaceholder { get { return AppPlaceholder() } }
    
    public func applyAppearance(application: UIApplication) {
        search.applyAppearance(application)
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

public class AppSearchBar {
    public var statusBarLightContent : Bool { get { return false } }
    public var backgroundColor : UIColor { get { return UIColor.RGB(0xf1f1f1) } }
    public var cancelColor : UIColor { get { return UIColor.RGB(0x8E8E93) } }
    public var fieldBackgroundColor: UIColor { get { return UIColor.whiteColor() } }
    public var fieldTextColor: UIColor { get { return UIColor.blackColor().alpha(0.56) } }
    
    public func applyAppearance(application: UIApplication) {
        
        // SearchBar Text Color
        // let textField = UITextField.my_appearanceWhenContainedIn(UISearchBar.self)
        // textField.tintColor = UIColor.redColor()
//        let font = UIFont.systemFontOfSize(14)
//        textField.defaultTextAttributes = [NSFontAttributeName: font,
//                        NSForegroundColorAttributeName : fieldTextColor]
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