//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

public struct AADevice {

    //
    // Device Types
    //
    public static let isiPad = UIDevice.currentDevice().userInterfaceIdiom == .Pad
    public static let isiPhone = UIDevice.currentDevice().userInterfaceIdiom == .Phone
    
    //
    // OS Versions
    //
    public static let isiOS8 = true
    public static let isiOS9 = NSProcessInfo.processInfo().isOperatingSystemAtLeastVersion( NSOperatingSystemVersion(majorVersion: 9, minorVersion: 0, patchVersion: 0))
    
    //
    // Device Sizes
    //
    public static let screenWidth = min(UIScreen.mainScreen().bounds.size.width, UIScreen.mainScreen().bounds.size.height)
    public static let screenHeight = max(UIScreen.mainScreen().bounds.size.width, UIScreen.mainScreen().bounds.size.height)
    
    //
    // iPhone sizes
    //
    public static let isiPhone5 = isiPhone && screenHeight == 568.0
    public static let isiPhone6 = isiPhone && screenHeight == 667.0
    public static let isiPhone6P = isiPhone && screenHeight == 736.0
}