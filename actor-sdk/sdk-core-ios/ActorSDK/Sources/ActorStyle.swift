//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYImage

public class ActorStyle {
    
    //
    // Main colors of app
    //
    
    /// Is Application have dark theme. Default is false.
    public var isDarkApp = false
    
    /// Tint Color. Star button
    public var vcStarButton = UIColor(red: 75/255.0, green: 110/255.0, blue: 152/255.0, alpha: 1)
    /// Tint Color. Used for "Actions". Default is sytem blue.
    public var vcTintColor = UIColor(rgb: 0x5085CB)
    /// Color of desctructive actions. Default is red
    public var vcDestructiveColor = UIColor.redColor()
    /// Default background color
    public var vcDefaultBackgroundColor = UIColor.whiteColor()
    /// Main Text color of app
    public var vcTextColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0)
    /// Text Hint colors
    public var vcHintColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
    /// App's main status bar style. Default is light content.
    public var vcStatusBarStyle = UIStatusBarStyle.Default
    /// UITableView separator color. Also used for other separators or borders.
    public var vcSeparatorColor = UIColor(rgb: 0xd4d4d4)
    /// Cell Selected color
    public var vcSelectedColor = UIColor(rgb: 0xd9d9d9)
    /// Header/Footer text color
    public var vcSectionColor = UIColor(rgb: 0x5b5a60)
    /// Pacgkround of various panels like UITabBar. Default is white.
    public var vcPanelBgColor = UIColor.whiteColor()
    /// UISwitch off border color
    public var vcSwitchOff = UIColor(rgb: 0xe6e6e6)
    /// UISwitch on color
    public var vcSwitchOn = UIColor(rgb: 0x4bd863)
    /// View Controller background color
    public var vcBgColor = UIColor.whiteColor()
    /// View Controller background color for settings
    public var vcBackyardColor = UIColor(red: 238/255.0, green: 238/255.0, blue: 238/255.0, alpha: 1)
    
    //
    // UINavigationBar
    //
    /// Main Navigation bar color
    public var navigationBgColor: UIColor = UIColor(red: 247.0/255.0, green: 247.0/255.0, blue: 247.0/255.0, alpha: 1)
    /// Main Navigation bar hairline color
    public var navigationHairlineHidden = false
    /// Navigation Bar icons colors
    public var navigationTintColor: UIColor = UIColor(rgb: 0x5085CB)
    /// Navigation Bar title color
    public var navigationTitleColor: UIColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0)
    /// Navigation Bar subtitle color, default is 0.8 alhpa of navigationTitleColor
    public var navigationSubtitleColor: UIColor {
        get { return _navigationSubtitleColor != nil ? _navigationSubtitleColor! : navigationTitleColor.alpha(0.8) }
        set(v) { _navigationSubtitleColor = v }
    }
    private var _navigationSubtitleColor: UIColor?
    /// Navigation Bar actove subtitle color, default is navigationTitleColor
    public var navigationSubtitleActiveColor: UIColor {
        get { return _navigationSubtitleActiveColor != nil ? _navigationSubtitleActiveColor! : navigationTitleColor }
        set(v) { _navigationSubtitleActiveColor = v }
    }
    private var _navigationSubtitleActiveColor: UIColor?

    //
    // Token Field. Used at entering members of new group.
    //
    
    /// Token Text Color. Default is vcTextColor.
    public var vcTokenFieldTextColor: UIColor {
        get { return _vcTokenFieldTextColor != nil ? _vcTokenFieldTextColor! : vcTextColor }
        set(v) { _vcTokenFieldTextColor = v }
    }
    private var _vcTokenFieldTextColor: UIColor?
    /// Background Color of Token field. Default is vcBgColor.
    public var vcTokenFieldBgColor: UIColor {
        get { return _vcTokenFieldBgColor != nil ? _vcTokenFieldBgColor! : vcBgColor }
        set(v) { _vcTokenFieldBgColor = v }
    }
    private var _vcTokenFieldBgColor: UIColor?
    /// Token Tint Color. Default is vcTintColor.
    public var vcTokenTintColor: UIColor {
        get { return _vcTokenTintColor != nil ? _vcTokenTintColor! : vcTintColor }
        set(v) { _vcTokenTintColor = v }
    }
    private var _vcTokenTintColor: UIColor?
    
    //
    // Search style
    //
    
    /// Style of status bar when search is active.
    public var searchStatusBarStyle = UIStatusBarStyle.Default
    /// Background Color of search bar
    public var searchBackgroundColor: UIColor {
        get { return _searchBackgroundColor != nil ? _searchBackgroundColor! : UIColor.whiteColor()  }
        set(v) { _searchBackgroundColor = v }
    }
    private var _searchBackgroundColor: UIColor?
    /// Cancel button color
    public var searchCancelColor: UIColor {
        get { return _searchCancelColor != nil ? _searchCancelColor! : vcTintColor }
        set(v) { _searchCancelColor = v }
    }
    private var _searchCancelColor: UIColor?
    /// Search Input Field background color
    public var searchFieldBgColor = UIColor(rgb: 0xededed)
    /// Search Input Field text color
    public var searchFieldTextColor = UIColor.blackColor().alpha(0.56)
    
    //
    // UITabBarView style
    //
    
    /// Selected Text Color of UITabViewItem. Default is vcTintColor.
    public var tabSelectedTextColor: UIColor {
        get { return _tabSelectedTextColor != nil ? _tabSelectedTextColor! : vcTintColor }
        set(v) { _tabSelectedTextColor = v }
    }
    private var _tabSelectedTextColor: UIColor?
    /// Selected Icon Color of UITableViewItem. Default is vcTintColor.
    public var tabSelectedIconColor: UIColor {
        get { return _tabSelectedIconColor != nil ? _tabSelectedIconColor! : vcTintColor }
        set(v) { _tabSelectedIconColor = v }
    }
    private var _tabSelectedIconColor: UIColor?
    /// Unselected Text Color of UITabViewItem. Default is vcHintColor.
    public var tabUnselectedTextColor: UIColor {
        get { return _tabUnselectedTextColor != nil ? _tabUnselectedTextColor! : vcHintColor }
        set(v) { _tabUnselectedTextColor = v }
    }
    private var _tabUnselectedTextColor: UIColor?
    /// Unselected Icon Color of UITableViewItem. Default is vcHintColor.
    private var _tabUnselectedIconColor: UIColor?
    public var tabUnselectedIconColor: UIColor {
        get { return _tabUnselectedIconColor != nil ? _tabUnselectedIconColor! : vcHintColor }
        set(v) { _tabUnselectedIconColor = v }
    }
    /// Background color of UITabBarView. Default is vcPanelBgColor.
    private var _tabBgColor: UIColor?
    public var tabBgColor: UIColor {
        get { return _tabBgColor != nil ? _tabBgColor! : vcPanelBgColor }
        set(v) { _tabBgColor = v }
    }
    
    //
    // Cell View style
    //
    
    /// Cell Background color. Default is vcBgColor.
    public var cellBgColor: UIColor {
        get { return _cellBgColor != nil ? _cellBgColor! : vcBgColor }
        set(v) { _cellBgColor = v }
    }
    private var _cellBgColor: UIColor?
    /// Cell Background selected color. Default is vcSelectedColor.
    public var cellBgSelectedColor: UIColor {
        get { return _cellBgSelectedColor != nil ? _cellBgSelectedColor! : vcSelectedColor }
        set(v) { _cellBgSelectedColor = v }
    }
    private var _cellBgSelectedColor: UIColor?
    /// Cell text color. Default is vcTextColor.
    public var cellTextColor: UIColor {
        get { return _cellTextColor != nil ? _cellTextColor! : vcTextColor }
        set(v) { _cellTextColor = v }
    }
    private var _cellTextColor: UIColor?
    /// Cell hint text color. Default is vcHintColor.
    public var cellHintColor: UIColor {
        get { return _cellHintColor != nil ? _cellHintColor! : vcHintColor }
        set(v) { _cellHintColor = v }
    }
    private var _cellHintColor: UIColor?
    /// Cell action color. Default is vcTintColor.
    public var cellTintColor: UIColor {
        get { return _cellTintColor != nil ? _cellTintColor! : vcTintColor }
        set(v) { _cellTintColor = v }
    }
    private var _cellTintColor: UIColor?
    /// Cell desctructive color. Default is vcDestructiveColor.
    public var cellDestructiveColor: UIColor {
        get { return _cellDestructiveColor != nil ? _cellDestructiveColor! : vcDestructiveColor }
        set(v) { _cellDestructiveColor = v }
    }
    private var _cellDestructiveColor: UIColor?
    /// Section header color. Default is vcSectionColor.
    public var cellHeaderColor: UIColor {
        get { return _cellHeaderColor != nil ? _cellHeaderColor! : vcSectionColor }
        set(v) { _cellHeaderColor = v }
    }
    private var _cellHeaderColor: UIColor?
    /// Section footer color. Default is vcSectionColor.
    public var cellFooterColor: UIColor {
        get { return _cellFooterColor != nil ? _cellFooterColor! : vcSectionColor }
        set(v) { _cellFooterColor = v }
    }
    private var _cellFooterColor: UIColor?
    
    //
    // Full screen placeholder style
    //
    
    /// Big Placeholder background color
    public var placeholderBgColor: UIColor {
        get { return _placeholderBgColor != nil ? _placeholderBgColor! : navigationBgColor.fromTransparentBar() }
        set(v) { _placeholderBgColor = v }
    }
    private var _placeholderBgColor: UIColor?
    /// Big placeholder title color
    public var placeholderTitleColor: UIColor {
        get { return _placeholderTitleColor != nil ? _placeholderTitleColor! : vcTextColor }
        set(v) { _placeholderTitleColor = v }
    }
    private var _placeholderTitleColor: UIColor?
    /// Bit Placeholder hint color
    public var placeholderHintColor: UIColor {
        get { return _placeholderHintColor != nil ? _placeholderHintColor! : vcHintColor }
        set(v) { _placeholderHintColor = v }
    }
    private var _placeholderHintColor: UIColor?
    
    //
    // Avatar Placeholder and name colors
    //
    
    public var avatarTextColor = UIColor.whiteColor()
    
    public var avatarLightBlue = UIColor(rgb: 0x59b7d3)
    public var nameLightBlue = UIColor(rgb: 0x59b7d3)
    
    public var avatarDarkBlue = UIColor(rgb: 0x1d4e6f)
    public var nameDarkBlue = UIColor(rgb: 0x1d4e6f)
    
    public var avatarPurple = UIColor(rgb: 0x995794)
    public var namePurple = UIColor(rgb: 0x995794)
    
    public var avatarPink = UIColor(rgb: 0xff506c)
    public var namePink = UIColor(rgb: 0xff506c)
    
    public var avatarOrange = UIColor(rgb: 0xf99341)
    public var nameOrange = UIColor(rgb: 0xf99341)
    
    public var avatarYellow = UIColor(rgb: 0xe4d027)
    public var nameYellow = UIColor(rgb: 0xe4d027)

    public var avatarGreen = UIColor(rgb: 0xe4d027)
    public var nameGreen = UIColor(rgb: 0xe4d027)
    
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
    
    // Text colors
    
    private var _chatTextColor: UIColor?
    public var chatTextColor: UIColor {
        get { return _chatTextColor != nil ? _chatTextColor! : vcTextColor }
        set(v) { _chatTextColor = v }
    }
    
    private var _chatUrlColor: UIColor?
    public var chatUrlColor: UIColor {
        get { return _chatUrlColor != nil ? _chatUrlColor! : vcTintColor }
        set(v) { _chatUrlColor = v }
    }
    
    private var _chatTextUnsupportedColor: UIColor?
    public var chatTextUnsupportedColor: UIColor {
        get { return _chatTextUnsupportedColor != nil ? _chatTextUnsupportedColor! : vcTintColor.alpha(0.54) }
        set(v) { _chatTextUnsupportedColor = v }
    }
    
    private var _chatTextOutColor: UIColor?
    public var chatTextOutColor: UIColor {
        get { return _chatTextOutColor != nil ? _chatTextOutColor! : chatTextColor }
        set(v) { _chatTextOutColor = v }
    }
    
    private var _chatTextInColor: UIColor?
    public var chatTextInColor: UIColor {
        get { return _chatTextInColor != nil ? _chatTextInColor! : chatTextColor }
        set(v) { _chatTextInColor = v }
    }

    private var _chatTextOutUnsupportedColor: UIColor?
    public var chatTextOutUnsupportedColor: UIColor {
        get { return _chatTextOutUnsupportedColor != nil ? _chatTextOutUnsupportedColor! : chatTextUnsupportedColor }
        set(v) { _chatTextOutUnsupportedColor = v }
    }
    
    private var _chatTextInUnsupportedColor: UIColor?
    public var chatTextInUnsupportedColor: UIColor {
        get { return _chatTextInUnsupportedColor != nil ? _chatTextInUnsupportedColor! : chatTextUnsupportedColor }
        set(v) { _chatTextInUnsupportedColor = v }
    }

    public var chatDateTextColor = UIColor.whiteColor()
    
    public var chatServiceTextColor = UIColor.whiteColor()
    
    public var chatUnreadTextColor = UIColor.whiteColor()
    
    // Date colors

    public var chatTextDateOutColor = UIColor.alphaBlack(0.27)
    public var chatTextDateInColor = UIColor(rgb: 0x979797)

    public var chatMediaDateColor = UIColor.whiteColor()
    public var chatMediaDateBgColor = UIColor(rgb: 0x2D394A, alpha: 0.54)
    
    // Bubble Colors
    
    public var chatTextBubbleOutColor = UIColor(rgb: 0xD2FEFD)

    public var chatTextBubbleOutSelectedColor = UIColor.lightGrayColor()
    
    public var chatTextBubbleOutBorderColor = UIColor(rgb: 0x99E4E3)
    
    public var chatTextBubbleInColor = UIColor.whiteColor()
    
    public var chatTextBubbleInSelectedColor = UIColor.blueColor()
    
    public var chatTextBubbleInBorderColor = UIColor(rgb: 0xCCCCCC)
    
    public var chatMediaBubbleColor = UIColor.whiteColor()
    public var chatMediaBubbleBorderColor = UIColor(rgb: 0xCCCCCC)

    public var chatDateBubbleColor = UIColor(rgb: 0x2D394A, alpha: 0.56)
    
    public var chatServiceBubbleColor = UIColor(rgb: 0x2D394A, alpha: 0.56)
    
    public var chatUnreadBgColor = UIColor.alphaBlack(0.3)
    
    public var chatReadMediaColor = UIColor(red: 46.6/255.0, green: 211.3/255.0, blue: 253.6/255.0, alpha: 1.0)
    
    // Bubble Shadow
    
    public var bubbleShadowEnabled = false
    
    public var chatTextBubbleShadowColor = UIColor.alphaBlack(0.1)
    
    // Status Colors
    
    public lazy var chatIconCheck1 = UIImage.templated("msg_check_1")
    public lazy var chatIconCheck2 = UIImage.templated("msg_check_2")
    public lazy var chatIconError = UIImage.templated("msg_error")
    public lazy var chatIconWarring = UIImage.templated("msg_warring")
    public lazy var chatIconClock = UIImage.templated("msg_clock")
    
    
    
    
    
    
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
    
    private var _chatStatusMediaActive: UIColor?
    public var chatStatusMediaActive: UIColor {
        get { return _chatStatusMediaActive != nil ? _chatStatusMediaActive! : chatReadMediaColor }
        set(v) { _chatStatusMediaActive = v }
    }

    private var _chatStatusMediaPassive: UIColor?
    public var chatStatusMediaPassive: UIColor {
        get { return _chatStatusMediaPassive != nil ? _chatStatusMediaPassive! : UIColor.whiteColor() }
        set(v) { _chatStatusMediaPassive = v }
    }
    
    private var _chatStatusMediaDanger: UIColor?
    public var chatStatusMediaDanger: UIColor {
        get { return _chatStatusMediaDanger != nil ? _chatStatusMediaDanger! : chatStatusDanger }
        set(v) { _chatStatusMediaDanger = v }
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
    
    private var _chatStatusMediaSending: UIColor?
    public var chatStatusMediaSending: UIColor {
        get { return _chatStatusMediaSending != nil ? _chatStatusMediaSending! : chatStatusMediaPassive }
        set(v) { _chatStatusMediaSending = v }
    }
    
    private var _chatStatusMediaSent: UIColor?
    public var chatStatusMediaSent: UIColor {
        get { return _chatStatusMediaSent != nil ? _chatStatusMediaSent! : chatStatusMediaPassive }
        set(v) { _chatStatusMediaSent = v }
    }
    
    private var _chatStatusMediaReceived: UIColor?
    public var chatStatusMediaReceived: UIColor {
        get { return _chatStatusMediaReceived != nil ? _chatStatusMediaReceived! : chatStatusMediaPassive }
        set(v) { _chatStatusMediaReceived = v }
    }
    
    private var _chatStatusMediaRead: UIColor?
    public var chatStatusMediaRead: UIColor {
        get { return _chatStatusMediaRead != nil ? _chatStatusMediaRead! : chatStatusMediaActive }
        set(v) { _chatStatusMediaRead = v }
    }
    
    private var _chatStatusMediaError: UIColor?
    public var chatStatusMediaError: UIColor {
        get { return _chatStatusMediaError != nil ? _chatStatusMediaError! : chatStatusMediaDanger }
        set(v) { _chatStatusMediaError = v }
    }

    // Chat screen
    
    private var _chatInputField: UIColor?
    public var chatInputFieldBgColor: UIColor {
        get { return _chatInputField != nil ? _chatInputField! : vcPanelBgColor }
        set(v) { _chatInputField = v }
    }
    
    private var _chatAttachColor: UIColor?
    public var chatAttachColor: UIColor {
        get { return _chatAttachColor != nil ? _chatAttachColor! : vcTintColor }
        set(v) { _chatAttachColor = v }
    }
    
    private var _chatSendColor: UIColor?
    public var chatSendColor: UIColor {
        get { return _chatSendColor != nil ? _chatSendColor! : vcTintColor }
        set(v) { _chatSendColor = v }
    }

    private var _chatSendDisabledColor: UIColor?
    public var chatSendDisabledColor: UIColor {
        get { return _chatSendDisabledColor != nil ? _chatSendDisabledColor! : vcTintColor.alpha(0.64) }
        set(v) { _chatSendDisabledColor = v }
    }
    
    private var _chatAutocompleteHighlight: UIColor?
    public var chatAutocompleteHighlight: UIColor {
        get { return _chatAutocompleteHighlight != nil ? _chatAutocompleteHighlight! : vcTintColor }
        set(v) { _chatAutocompleteHighlight = v }
    }
    
    public lazy var chatBgColor = UIColor(patternImage: UIImage.bundled("chat_bg")!)
    
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
        get { return _dialogTextColor != nil ? _dialogTextColor! : dialogTitleColor.alpha(0.64) }
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
    
    private var _statusBackgroundIcon: UIImage?
    public var statusBackgroundImage:UIImage {
        get {
            if (_statusBackgroundIcon == nil){

                let statusImage:UIImage = UIImage.bundled("bubble_service_bg")!.aa_imageWithColor(UIColor.blackColor().colorWithAlphaComponent(0.7)).imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
                
                let center:CGPoint = CGPointMake(statusImage.size.width / 2.0, statusImage.size.height / 2.0);
                let capInsets:UIEdgeInsets = UIEdgeInsetsMake(center.y, center.x, center.y, center.x);
                
                _statusBackgroundIcon = statusImage.resizableImageWithCapInsets(capInsets, resizingMode: UIImageResizingMode.Stretch)
                return _statusBackgroundIcon!
            } else {
                return _statusBackgroundIcon!
            }
        }
    }
    
    //
    // Contacts styles
    //

    private var _contactTitleColor: UIColor?
    public var contactTitleColor: UIColor {
        get { return _contactTitleColor != nil ? _contactTitleColor! : vcTextColor }
        set(v) { _contactTitleColor = v }
    }
    
    //
    // Online styles
    //
    
    private var _userOnlineColor: UIColor?
    public var userOnlineColor: UIColor {
        get { return _userOnlineColor != nil ? _userOnlineColor! : vcTintColor }
        set(v) { _userOnlineColor = v }
    }
    
    private var _userOfflineColor: UIColor?
    public var userOfflineColor: UIColor {
        get { return _userOfflineColor != nil ? _userOfflineColor! : vcTextColor.alpha(0.54) }
        set(v) { _userOfflineColor = v }
    }

    private var _userOnlineNavigationColor: UIColor?
    public var userOnlineNavigationColor: UIColor {
        get { return _userOnlineNavigationColor != nil ? _userOnlineNavigationColor! : userOnlineColor }
        set(v) { _userOnlineNavigationColor = v }
    }
    
    private var _userOfflineNavigationColor: UIColor?
    public var userOfflineNavigationColor: UIColor {
        get { return _userOfflineNavigationColor != nil ? _userOfflineNavigationColor! : navigationSubtitleColor }
        set(v) { _userOfflineNavigationColor = v }
    }
    
    //
    // Compose styles
    //
    
    private var _composeAvatarBgColor: UIColor?
    public var composeAvatarBgColor: UIColor {
        get { return _composeAvatarBgColor != nil ? _composeAvatarBgColor! : vcBgColor }
        set(v) { _composeAvatarBgColor = v }
    }

    private var _composeAvatarBorderColor: UIColor?
    public var composeAvatarBorderColor: UIColor {
        get { return _composeAvatarBorderColor != nil ? _composeAvatarBorderColor! : vcSeparatorColor }
        set(v) { _composeAvatarBorderColor = v }
    }
    
    private var _composeAvatarTextColor: UIColor?
    public var composeAvatarTextColor: UIColor {
        get { return _composeAvatarTextColor != nil ? _composeAvatarTextColor! : vcHintColor }
        set(v) { _composeAvatarTextColor = v }
    }
    
    
    //
    // Status Bar progress
    //
    
    /// Is Status Bar connecting status hidden
    public var statusBarConnectingHidden = false
    
    /// Is Status Bar background color
    private var _statusBarConnectingBgColor : UIColor?
    public var statusBarConnectingBgColor: UIColor {
        get { return _statusBarConnectingBgColor != nil ? _statusBarConnectingBgColor! : navigationBgColor }
        set(v) { _statusBarConnectingBgColor = v }
    }

    /// Is Status Bar background color
    private var _statusBarConnectingTextColor : UIColor?
    public var statusBarConnectingTextColor: UIColor {
        get { return _statusBarConnectingTextColor != nil ? _statusBarConnectingTextColor! : navigationTitleColor }
        set(v) { _statusBarConnectingTextColor = v }
    }
    
    // 
    // Welcome
    //
    
    /// Welcome Page Background color
    public var welcomeBgColor = UIColor(red: 94, green: 142, blue: 192)
    
    /// Welcome Page Background image
    public var welcomeBgImage: UIImage? = nil
    
    /// Welcome Page Title Color
    public var welcomeTitleColor = UIColor.whiteColor()

    /// Welcome Page Tagline Color
    public var welcomeTaglineColor = UIColor.whiteColor()
    
    /// Welcome Page Signup Background Color
    public var welcomeSignupBgColor = UIColor.whiteColor()
    
    /// Welcome Page Signup Text Color
    public var welcomeSignupTextColor = UIColor(red: 94, green: 142, blue: 192)
    
    /// Welcome Page Login Text Color
    public var welcomeLoginTextColor = UIColor.whiteColor()
    
    /// Welcome Logo
    public var welcomeLogo:     UIImage? = UIImage.bundled("logo_welcome")
    public var welcomeLogoSize: CGSize = CGSize(width: 90, height: 90)
    public var logoViewVerticalGap: CGFloat = 145
    
    //
    // Auth Screen
    //
    
    public var authTintColor = UIColor(rgb: 0x007aff)
    
    public var authTitleColor = UIColor.blackColor().alpha(0.87)
    
    public var authHintColor = UIColor.alphaBlack(0.64)
    
    public var authTextColor = UIColor.alphaBlack(0.87)
    
    public var authSeparatorColor = UIColor.blackColor().alpha(0.2)
    
    //
    // Settings VC
    //
    
    public var vcSettingsContactsHeaderTextColor: UIColor {
        get { return _vcSettingsContactsHeaderTextColor != nil ? _vcSettingsContactsHeaderTextColor! : vcTextColor }
        set(v) { _vcSettingsContactsHeaderTextColor = v }
    }
    private var _vcSettingsContactsHeaderTextColor : UIColor?
}




