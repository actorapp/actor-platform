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
    public static let isiPhone4 = isiPhone && screenHeight == 480.0
    public static let isiPhone5 = isiPhone && screenHeight == 568.0
    
    public static let isStandardiPhone6 = isiPhone && screenHeight == 667.0 && UIScreen.mainScreen().nativeScale == UIScreen.mainScreen().scale
    public static let isZoomediPhone6   = isiPhone && screenHeight == 568.0 && UIScreen.mainScreen().nativeScale > UIScreen.mainScreen().scale
    
    public static let isStandardiPhone6P = isiPhone && screenHeight == 736.0
    public static let isZoomediPhone6P   = isiPhone && screenHeight == 667.0 && UIScreen.mainScreen().nativeScale < UIScreen.mainScreen().scale
    
    public static let isiPhone6  = isStandardiPhone6 || isZoomediPhone6
    public static let isiPhone6P = isStandardiPhone6P || isZoomediPhone6P
}