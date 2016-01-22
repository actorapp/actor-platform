//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public extension UIColor {
    
    public convenience init(rgb: UInt) {
        self.init(red: CGFloat((rgb & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgb & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgb & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0))
    }
    
    public convenience init(rgb: UInt, alpha: Double) {
        self.init(red: CGFloat((rgb & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgb & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgb & 0x0000FF) / 255.0,
            alpha: CGFloat(alpha))
    }
    
    public class func alphaBlack(alpha: Double) -> UIColor {
        return UIColor(red: 0, green: 0, blue: 0, alpha: CGFloat(alpha))
    }
    
    public class func alphaWhite(alpha: Double) -> UIColor {
        return UIColor(red: 1, green: 1, blue: 1, alpha: CGFloat(alpha))
    }
    
    public func alpha(alpha: Double) -> UIColor {
        var r:CGFloat = 0
        var g:CGFloat = 0
        var b:CGFloat = 0
        var a:CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return UIColor(red: r, green: g, blue: b, alpha: CGFloat(alpha))
    }
    
    public func forTransparentBar() -> UIColor {
        var r:CGFloat = 0
        var g:CGFloat = 0
        var b:CGFloat = 0
        var a:CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return UIColor(red: max(0, r - 40/256.0), green: max(0, g - 40/256.0), blue: max(0, b - 40/256.0), alpha: a)
    }
    
    public func fromTransparentBar() -> UIColor {
        var r:CGFloat = 0
        var g:CGFloat = 0
        var b:CGFloat = 0
        var a:CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return UIColor(red: min(255, r + 40/256.0), green: min(255, g + 40/256.0), blue: min(255, b + 40/256.0), alpha: a)
    }
}
