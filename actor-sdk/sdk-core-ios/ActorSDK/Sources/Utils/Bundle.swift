//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

let frameworkBundle = Bundle(identifier: "im.actor.ActorSDK")!

public extension Bundle {
    static var framework: Bundle {
        get {
            return frameworkBundle
        }
    }
}

public extension UIImage {
    class func bundled(_ named: String) -> UIImage? {
        
        if let appImage = UIImage(named: named) {
            return appImage
        }
        return UIImage(named: named, in: Bundle.framework, compatibleWith: UITraitCollection(displayScale: UIScreen.main.scale))
    }
}
