//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

public struct AADevice {

    //
    // Device Types
    //
    public static let isiPad = UIDevice.current.userInterfaceIdiom == .pad
    public static let isiPhone = UIDevice.current.userInterfaceIdiom == .phone
    
    //
    // OS Versions
    //
    public static let isiOS8 = true
    public static let isiOS9 = ProcessInfo.processInfo.isOperatingSystemAtLeast( OperatingSystemVersion(majorVersion: 9, minorVersion: 0, patchVersion: 0))
    
    //
    // Device Sizes
    //
    public static let screenWidth = min(UIScreen.main.bounds.size.width, UIScreen.main.bounds.size.height)
    public static let screenHeight = max(UIScreen.main.bounds.size.width, UIScreen.main.bounds.size.height)
    
    //
    // iPhone sizes
    //
    public static let isiPhone4 = isiPhone && screenHeight == 480.0
    public static let isiPhone5 = isiPhone && screenHeight == 568.0
    public static let isiPhone6 = isiPhone && screenHeight == 667.0
    public static let isiPhone6P = isiPhone && screenHeight == 736.0
}
