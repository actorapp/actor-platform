//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

let frameworkBundle = NSBundle(identifier: "im.actor.ActorSDK")!

public extension NSBundle {
    static var framework: NSBundle {
        get {
            return frameworkBundle
        }
    }
}

public extension UIImage {
    class func bundled(named: String) -> UIImage? {
        
        if let appImage = UIImage(named: named) {
            return appImage
        }
        return UIImage(named: named, inBundle: NSBundle.framework, compatibleWithTraitCollection: UITraitCollection(displayScale: UIScreen.mainScreen().scale))
    }
}