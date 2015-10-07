//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

public struct ScreenSize {
    public static let SCREEN_WIDTH = UIScreen.mainScreen().bounds.size.width
    public static let SCREEN_HEIGHT = UIScreen.mainScreen().bounds.size.height
    public static let SCREEN_MAX_LENGTH = max(ScreenSize.SCREEN_WIDTH, ScreenSize.SCREEN_HEIGHT)
    public static let SCREEN_MIN_LENGTH = min(ScreenSize.SCREEN_WIDTH, ScreenSize.SCREEN_HEIGHT)
}

public struct DeviceType {
    public static let IS_IPHONE_4_OR_LESS =  UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH < 568.0
    public static let IS_IPHONE_5 = UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH == 568.0
    public static let IS_IPHONE_6 = UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH == 667.0
    public static let IS_IPHONE_6P = UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH == 736.0
}

public let isIPad = UIDevice.currentDevice().userInterfaceIdiom == .Pad

public let isIPhone = UIDevice.currentDevice().userInterfaceIdiom == .Phone

public let isiOS8 = true

public let isiOS9 = NSProcessInfo.processInfo().isOperatingSystemAtLeastVersion( NSOperatingSystemVersion(majorVersion: 9, minorVersion: 0, patchVersion: 0))
