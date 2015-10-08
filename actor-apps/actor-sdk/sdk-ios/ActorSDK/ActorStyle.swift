//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class ActorStyle {
    
    //
    // View style
    //
    
    //
    // Generic colors of app
    //
    
    public var isDarkApp = false
    
    public var vcTintColor = UIColor(rgb: 0x5085CB)
    
    public var vcDestructiveColor = UIColor.redColor()
    
    public var vcTextColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0)
    
    public var vcHintColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
    
    public var vcStatusBarColor = UIStatusBarStyle.LightContent
    
    public var vcSeparatorColor = UIColor(rgb: 0xd4d4d4)
    
    public var vcSelectedColor = UIColor(rgb: 0xd9d9d9)
    
    public var vcSectionColor = UIColor(rgb: 0x5b5a60)
    
    public var vcPanelBgColor = UIColor.whiteColor()
    
    //
    // UISwitch styles
    //
    
    public var vcSwitchOff = UIColor(rgb: 0xe6e6e6)
    
    public var vcSwitchOn = UIColor(rgb: 0x4bd863)
    
    //
    // Root Views styles
    //
    
    public var vcBgColor = UIColor.whiteColor()
    
    public var vcBackyardColor = UIColor(red: 238/255.0, green: 238/255.0, blue: 238/255.0, alpha: 1)
    
    //
    // UINavigationBar
    //
    
    public var navigationBgColor: UIColor = UIColor(rgb: 0x3576cc)
    
    public var navigationTintColor: UIColor = UIColor.whiteColor()
    
    public var navigationTitleColor: UIColor = UIColor.whiteColor()
    
    private var _navigationSubtitleColor: UIColor?
    public var navigationSubtitleColor: UIColor {
        get { return _navigationSubtitleColor != nil ? _navigationSubtitleColor! : navigationTitleColor }
        set(v) { _navigationSubtitleColor = v }
    }
    
    //
    // Tab View style
    //
    
    private var _tabSelectedTextColor: UIColor?
    public var tabSelectedTextColor: UIColor {
        get { return _tabSelectedTextColor != nil ? _tabSelectedTextColor! : vcTintColor }
        set(v) { _tabSelectedTextColor = v }
    }
    
    private var _tabSelectedIconColor: UIColor?
    public var tabSelectedIconColor: UIColor {
        get { return _tabSelectedIconColor != nil ? _tabSelectedIconColor! : vcTintColor }
        set(v) { _tabSelectedIconColor = v }
    }
    
    private var _tabUnselectedTextColor: UIColor?
    public var tabUnselectedTextColor: UIColor {
        get { return _tabUnselectedTextColor != nil ? _tabUnselectedTextColor! : vcTextColor }
        set(v) { _tabUnselectedTextColor = v }
    }
    
    private var _tabUnselectedIconColor: UIColor?
    public var tabUnselectedIconColor: UIColor {
        get { return _tabUnselectedIconColor != nil ? _tabUnselectedIconColor! : vcTextColor }
        set(v) { _tabUnselectedIconColor = v }
    }
    
    private var _tabBgColor: UIColor?
    public var tabBgColor: UIColor {
        get { return _tabBgColor != nil ? _tabBgColor! : vcPanelBgColor }
        set(v) { _tabBgColor = v }
    }
    
    //
    // Cell View style
    //
    
    private var _cellBgColor: UIColor?
    public var cellBgColor: UIColor {
        get { return _cellBgColor != nil ? _cellBgColor! : vcBgColor }
        set(v) { _cellBgColor = v }
    }

    private var _cellBgSelectedColor: UIColor?
    public var cellBgSelectedColor: UIColor {
        get { return _cellBgSelectedColor != nil ? _cellBgSelectedColor! : vcSelectedColor }
        set(v) { _cellBgSelectedColor = v }
    }
    
    private var _cellTintColor: UIColor?
    public var cellTintColor: UIColor {
        get { return _cellTintColor != nil ? _cellTintColor! : vcTintColor }
        set(v) { _cellTintColor = v }
    }
    
    private var _cellTextColor: UIColor?
    public var cellTextColor: UIColor {
        get { return _cellTextColor != nil ? _cellTextColor! : vcTextColor }
        set(v) { _cellTextColor = v }
    }
    
    private var _cellHintColor: UIColor?
    public var cellHintColor: UIColor {
        get { return _cellHintColor != nil ? _cellHintColor! : vcHintColor }
        set(v) { _cellHintColor = v }
    }
    
    private var _cellDestructiveColor: UIColor?
    public var cellDestructiveColor: UIColor {
        get { return _cellDestructiveColor != nil ? _cellDestructiveColor! : vcDestructiveColor }
        set(v) { _cellDestructiveColor = v }
    }
    
    private var _cellHeaderColor: UIColor?
    public var cellHeaderColor: UIColor {
        get { return _cellHeaderColor != nil ? _cellHeaderColor! : vcSectionColor }
        set(v) { _cellHeaderColor = v }
    }
    
    private var _cellFooterColor: UIColor?
    public var cellFooterColor: UIColor {
        get { return _cellFooterColor != nil ? _cellFooterColor! : vcSectionColor }
        set(v) { _cellFooterColor = v }
    }
    
    //
    // Full screen placeholder style
    //
    
    public var placeholderBgColor = UIColor(rgb: 0x5085CB)
    
    //
    // Avatar Placeholder and name colors
    //
    
    public var avatarTextColor = UIColor.whiteColor()
    
    public var avatarLightBlue = UIColor.RGB(0x59b7d3)
    public var nameLightBlue = UIColor.RGB(0x59b7d3)
    
    public var avatarDarkBlue = UIColor.RGB(0x1d4e6f)
    public var nameDarkBlue = UIColor.RGB(0x1d4e6f)
    
    public var avatarPurple = UIColor.RGB(0x995794)
    public var namePurple = UIColor.RGB(0x995794)
    
    public var avatarPink = UIColor.RGB(0xff506c)
    public var namePink = UIColor.RGB(0xff506c)
    
    public var avatarOrange = UIColor.RGB(0xf99341)
    public var nameOrange = UIColor.RGB(0xf99341)
    
    public var avatarYellow = UIColor.RGB(0xe4d027)
    public var nameYellow = UIColor.RGB(0xe4d027)

    public var avatarGreen = UIColor.RGB(0xe4d027)
    public var nameGreen = UIColor.RGB(0xe4d027)
    
    private var _avatarColors: [UIColor]?
    public var avatarColors: [UIColor] {
        get {
            if _avatarColors == nil {
                return [
                    avatarLightBlue,
                    avatarDarkBlue,
                    avatarPurple,
                    avatarPink,
                    avatarOrange,
                    avatarYellow,
                    avatarGreen
                ]
            } else {
                return _avatarColors!
            }
        }
        set(v) { _avatarColors = v }
    }
    
    private var _nameColors: [UIColor]?
    public var nameColors: [UIColor] {
        get {
            if _nameColors == nil {
                return [
                    nameLightBlue,
                    nameDarkBlue,
                    namePurple,
                    namePink,
                    nameOrange,
                    nameYellow,
                    nameGreen
                ]
            } else {
                return _nameColors!
            }
        }
        set(v) { _nameColors = v }
    }
    
    //
    // Bubble styles
    //

    public var chatIconCheck1 = UIImage.templated("msg_check_1")
    public var chatIconCheck2 = UIImage.templated("msg_check_2")
    public var chatIconError = UIImage.templated("msg_error")
    public var chatIconWarring = UIImage.templated("msg_warring")
    public var chatIconClock = UIImage.templated("msg_clock")
    
    private var _chatStatusActive: UIColor?
    public var chatStatusActive: UIColor {
        get { return _chatStatusActive != nil ? _chatStatusActive! : vcTintColor }
        set(v) { _chatStatusActive = v }
    }
    
    private var _chatStatusPassive: UIColor?
    public var chatStatusPassive: UIColor {
        get { return _chatStatusPassive != nil ? _chatStatusPassive! : vcHintColor }
        set(v) { _chatStatusPassive = v }
    }
    
    private var _chatStatusDanger: UIColor?
    public var chatStatusDanger: UIColor {
        get { return _chatStatusDanger != nil ? _chatStatusDanger! : vcDestructiveColor }
        set(v) { _chatStatusDanger = v }
    }
    
    private var _chatStatusSending: UIColor?
    public var chatStatusSending: UIColor {
        get { return _chatStatusSending != nil ? _chatStatusSending! : chatStatusPassive }
        set(v) { _chatStatusSending = v }
    }

    private var _chatStatusSent: UIColor?
    public var chatStatusSent: UIColor {
        get { return _chatStatusSent != nil ? _chatStatusSent! : chatStatusPassive }
        set(v) { _chatStatusSent = v }
    }
    
    private var _chatStatusReceived: UIColor?
    public var chatStatusReceived: UIColor {
        get { return _chatStatusReceived != nil ? _chatStatusReceived! : chatStatusPassive }
        set(v) { _chatStatusReceived = v }
    }
    
    private var _chatStatusRead: UIColor?
    public var chatStatusRead: UIColor {
        get { return _chatStatusRead != nil ? _chatStatusRead! : chatStatusActive }
        set(v) { _chatStatusRead = v }
    }
    
    private var _chatStatusError: UIColor?
    public var chatStatusError: UIColor {
        get { return _chatStatusError != nil ? _chatStatusError! : chatStatusDanger }
        set(v) { _chatStatusError = v }
    }
    
    //
    // Dialogs styles
    //
    
    private var _dialogTitleColor: UIColor?
    public var dialogTitleColor: UIColor {
        get { return _dialogTitleColor != nil ? _dialogTitleColor! : vcTextColor }
        set(v) { _dialogTitleColor = v }
    }
    
    private var _dialogTextColor: UIColor?
    public var dialogTextColor: UIColor {
        get { return _dialogTextColor != nil ? _dialogTextColor! : vcHintColor }
        set(v) { _dialogTextColor = v }
    }
    
    private var _dialogTextActiveColor: UIColor?
    public var dialogTextActiveColor: UIColor {
        get { return _dialogTextActiveColor != nil ? _dialogTextActiveColor! : vcTextColor }
        set(v) { _dialogTextActiveColor = v }
    }
    
    private var _dialogDateColor: UIColor?
    public var dialogDateColor: UIColor {
        get { return _dialogDateColor != nil ? _dialogDateColor! : vcHintColor }
        set(v) { _dialogDateColor = v }
    }

    public var dialogCounterBgColor: UIColor = UIColor(rgb: 0x50A1D6)
    
    public var dialogCounterColor: UIColor = UIColor.whiteColor()
    
    private var _dialogStatusActive: UIColor?
    public var dialogStatusActive: UIColor {
        get { return _dialogStatusActive != nil ? _dialogStatusActive! : chatStatusActive }
        set(v) { _dialogStatusActive = v }
    }
    
    private var _dialogStatusPassive: UIColor?
    public var dialogStatusPassive: UIColor {
        get { return _dialogStatusPassive != nil ? _dialogStatusPassive! : chatStatusPassive }
        set(v) { _dialogStatusPassive = v }
    }

    private var _dialogStatusDanger: UIColor?
    public var dialogStatusDanger: UIColor {
        get { return _dialogStatusDanger != nil ? _dialogStatusDanger! : chatStatusDanger }
        set(v) { _dialogStatusDanger = v }
    }

    private var _dialogStatusSending: UIColor?
    public var dialogStatusSending: UIColor {
        get { return _dialogStatusSending != nil ? _dialogStatusSending! : dialogStatusPassive }
        set(v) { _dialogStatusSending = v }
    }
    
    private var _dialogStatusSent: UIColor?
    public var dialogStatusSent: UIColor {
        get { return _dialogStatusSent != nil ? _dialogStatusSent! : dialogStatusPassive }
        set(v) { _dialogStatusSent = v }
    }
    
    private var _dialogStatusReceived: UIColor?
    public var dialogStatusReceived: UIColor {
        get { return _dialogStatusReceived != nil ? _dialogStatusReceived! : dialogStatusPassive }
        set(v) { _dialogStatusReceived = v }
    }
    
    private var _dialogStatusRead: UIColor?
    public var dialogStatusRead: UIColor {
        get { return _dialogStatusRead != nil ? _dialogStatusRead! : dialogStatusActive }
        set(v) { _dialogStatusRead = v }
    }
    
    private var _dialogStatusError: UIColor?
    public var dialogStatusError: UIColor {
        get { return _dialogStatusError != nil ? _dialogStatusError! : dialogStatusDanger }
        set(v) { _dialogStatusError = v }
    }
    
    //
    // Contacts styles
    //

    private var _contactTitleColor: UIColor?
    public var contactTitleColor: UIColor {
        get { return _contactTitleColor != nil ? _contactTitleColor! : vcTextColor }
        set(v) { _contactTitleColor = v }
    }    
}