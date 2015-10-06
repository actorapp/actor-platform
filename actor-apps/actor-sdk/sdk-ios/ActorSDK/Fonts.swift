//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public extension UIFont {
    public class func thinSystemFontOfSize(size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFontOfSize(size, weight: UIFontWeightThin)
        } else {
            return UIFont(name: "HelveticaNeue-Thin", size: size)!
        }
    }
    
    public class func lightSystemFontOfSize(size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFontOfSize(size, weight: UIFontWeightLight)
        } else {
            return UIFont(name: "HelveticaNeue-Light", size: size)!
        }
    }
    
    public class func mediumSystemFontOfSize(size: CGFloat) -> UIFont {
        if #available(iOS 8.2, *) {
            return UIFont.systemFontOfSize(size, weight: UIFontWeightMedium)
        } else {
            return UIFont(name: "HelveticaNeue-Medium", size: size)!
        }
    }
    
    // Texts
    
    public class func textFontOfSize(size: CGFloat) -> UIFont {
        return UIFont(name: "HelveticaNeue", size: size)!
    }
    
    public class func italicTextFontOfSize(size: CGFloat) -> UIFont {
        return UIFont(name: "HelveticaNeue-Italic", size: size)!
    }
    
    public class func boldTextFontOfSize(size: CGFloat) -> UIFont {
        return UIFont(name: "HelveticaNeue-Medium", size: size)!
    }
}