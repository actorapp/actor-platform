//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension UIFont {
    
    public class func thinSystemFontOfSize(_ size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFont(ofSize: size, weight: UIFontWeightThin)
        } else {
            return UIFont(name: "HelveticaNeue-Thin", size: size)!
        }
    }
    
    public class func lightSystemFontOfSize(_ size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFont(ofSize: size, weight: UIFontWeightLight)
        } else {
            return UIFont(name: "HelveticaNeue-Light", size: size)!
        }
    }
    
    public class func mediumSystemFontOfSize(_ size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFont(ofSize: size, weight: UIFontWeightMedium)
        } else {
            return UIFont(name: "HelveticaNeue-Medium", size: size)!
        }
    }
    
    // Texts
    
    public class func textFontOfSize(_ size: CGFloat) -> UIFont {
        return UIFont(name: "HelveticaNeue", size: size)!
    }
    
    public class func italicTextFontOfSize(_ size: CGFloat) -> UIFont {
        return UIFont(name: "HelveticaNeue-Italic", size: size)!
    }
    
    public class func boldTextFontOfSize(_ size: CGFloat) -> UIFont {
        return UIFont(name: "HelveticaNeue-Medium", size: size)!
    }
}
