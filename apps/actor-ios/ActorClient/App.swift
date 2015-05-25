//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

var MainAppTheme = ((NSBundle.mainBundle().infoDictionary!["APP_THEME"] as! String) == "llectro") ? LlectroTheme() : AppTheme()

var isIPad = UIDevice.currentDevice().userInterfaceIdiom == .Pad

let isiOS8 = floor(NSFoundationVersionNumber) > floor(NSFoundationVersionNumber_iOS_7_1)

var isRetina : Bool {
    get {
        return UIScreen.mainScreen().scale > 1
    }
}

var retinaPixel : CGFloat {
    get {
        return isRetina ? 0.5 : 1.0
    }
}
