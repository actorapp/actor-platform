//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

struct ScreenSize {
    static let SCREEN_WIDTH = UIScreen.mainScreen().bounds.size.width
    static let SCREEN_HEIGHT = UIScreen.mainScreen().bounds.size.height
    static let SCREEN_MAX_LENGTH = max(ScreenSize.SCREEN_WIDTH, ScreenSize.SCREEN_HEIGHT)
    static let SCREEN_MIN_LENGTH = min(ScreenSize.SCREEN_WIDTH, ScreenSize.SCREEN_HEIGHT)
}

struct DeviceType {
    static let IS_IPHONE_4_OR_LESS =  UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH < 568.0
    static let IS_IPHONE_5 = UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH == 568.0
    static let IS_IPHONE_6 = UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH == 667.0
    static let IS_IPHONE_6P = UIDevice.currentDevice().userInterfaceIdiom == .Phone && ScreenSize.SCREEN_MAX_LENGTH == 736.0
}

let isIPad = UIDevice.currentDevice().userInterfaceIdiom == .Pad

let isIPhone = UIDevice.currentDevice().userInterfaceIdiom == .Phone

private let device = Device()

let isiOS8 = device.atLeastiOS8

let isiOS9 = device.atLeastiOS9

private class Device {
    let atLeastiOS8: Bool
    let atLeastiOS9: Bool
    
    init() {
        atLeastiOS8 = true
        atLeastiOS9 = NSProcessInfo.processInfo().isOperatingSystemAtLeastVersion( NSOperatingSystemVersion(majorVersion: 9, minorVersion: 0, patchVersion: 0))
    }
}