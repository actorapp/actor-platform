//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class LlectroTheme : AppTheme {
    override var navigation: AppNavigationBar { get { return LlectroNavigationBar() } }
    override var tab: AppTabBar { get { return LlectroAppTabBar() } }
    override var list: AppList { get { return LlectroList() } }
    override var bubbles: ChatBubbles { get { return LlectroBubbles() } }
    override var search: AppSearchBar { get { return LlectroSearchBar() } }
    override var chat: AppChat { get { return LlectroChat() } }
    override var common: AppCommon { get { return LlectroCommon() } }
}

class LlectroCommon: AppCommon {
    override var isDarkKeyboard: Bool { get { return true } }
    
    override var tokenFieldText: UIColor { get { return UIColor.alphaWhite(0xDE/255.0) } }
    override var tokenFieldTextSelected: UIColor { get { return UIColor.alphaWhite(0xDE/255.0) } }
    override var tokenFieldBg: UIColor { get { return UIColor.RGB(0x50A1D6) } }
}

class LlectroChat: AppChat {
    override var chatField: UIColor { get { return UIColor.RGB(0x2D394A) } }
    
    override var attachColor: UIColor { get { return UIColor.whiteColor() } }
    
    override var sendEnabled: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    override var sendDisabled: UIColor { get { return UIColor.alphaWhite(0.56) } }
    
    override var profileBgTint: UIColor { get { return UIColor.RGB(0x2D394A) } }
}

class LlectroSearchBar: AppSearchBar {
    override var statusBarLightContent : Bool { get { return true } }
    override var backgroundColor : UIColor { get { return UIColor.RGB(0x2D394A) } }
    override var cancelColor : UIColor { get { return UIColor.alphaWhite(0.56) } }
    override var fieldBackgroundColor: UIColor { get { return UIColor.RGB(0x3C4A60) } }
    override var fieldTextColor: UIColor { get { return UIColor.alphaWhite(0.56) } }
}

class LlectroBubbles: ChatBubbles {
    override var text : UIColor { get { return UIColor.whiteColor() } }
    override var textUnsupported : UIColor { get { return UIColor.alphaWhite(0.56) } }
    
    override var bgOut: UIColor { get { return UIColor.RGB(0x596F8E) } }
    override var bgOutBorder: UIColor { get { return UIColor.RGB(0x3E5576) } }
    
    override var bgIn : UIColor { get { return UIColor.RGB(0x598E7B) } }
    override var bgInBorder:UIColor { get { return  UIColor.RGB(0x285F4C) } }
    
    override var statusActive : UIColor { get { return UIColor.RGB(0x50A1D6) } }
    override var statusPassive : UIColor { get { return UIColor.alphaWhite(0.56) } }

    override var statusMediaActive : UIColor { get { return UIColor.RGB(0x50A1D6) } }
    override var statusMediaPassive : UIColor { get { return UIColor.whiteColor() } }
    
    override var statusDialogActive : UIColor { get { return UIColor.RGB(0x3397f9) } }
    override var statusDialogPassive : UIColor { get { return UIColor.alphaWhite(0.27) } }
    
    override var textDateOut : UIColor { get { return UIColor.alphaWhite(0.56) } }
    override var textDateIn : UIColor { get { return UIColor.alphaWhite(0.56) } }
    
    override var chatBgTint: UIColor { get { return UIColor.RGB(0x212A36) } }
}

class LlectroList : AppList {
    override var actionColor : UIColor { get { return UIColor.RGB(0x50A1D6) } }
    override var bgColor: UIColor { get { return UIColor.RGB(0x3C4A60) } }
    override var bgSelectedColor : UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    override var backyardColor : UIColor { get { return UIColor.RGB(0x2D394A) } }
    override var separatorColor : UIColor { get { return UIColor.RGB(0x6C7787) } }
    
    override var textColor : UIColor { get { return UIColor.whiteColor() } }
    override var sectionColor : UIColor { get { return UIColor.whiteColor() } }

    override var dialogTitle: UIColor { get { return UIColor.whiteColor() } }
    override var dialogText: UIColor { get { return UIColor.alphaWhite(0.5) } }
    override var dialogDate: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    override var contactsTitle: UIColor { get { return UIColor.whiteColor() } }
    override var contactsShortTitle: UIColor { get { return UIColor.whiteColor() } }
}

class LlectroNavigationBar: AppNavigationBar {
    override var barColor:UIColor { get { return UIColor.RGB(0x2D394A) } }
    override var barSolidColor:UIColor { get { return UIColor.RGB(0x2D394A) } }
}

class LlectroAppTabBar : AppTabBar {
    override var backgroundColor : UIColor { get { return UIColor.RGB(0x2D394A) } }
    
    override var selectedIconColor: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    override var selectedTextColor: UIColor { get { return UIColor.RGB(0x50A1D6) } }
    
    override var unselectedIconColor: UIColor { get { return UIColor.alphaWhite(0.56) } }
    override var unselectedTextColor: UIColor { get { return UIColor.alphaWhite(0.56) } }
}