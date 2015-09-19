//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

extension UIFont {
    class func thinSystemFontOfSize(size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFontOfSize(size, weight: UIFontWeightThin)
        } else {
            return UIFont(name: "HelveticaNeue-Thin", size: size)!
        }
    }
    
    class func lightSystemFontOfSize(size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFontOfSize(size, weight: UIFontWeightLight)
        } else {
            return UIFont(name: "HelveticaNeue-Light", size: size)!
        }
    }
}