//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

extension UIColor {
    
    class func RGB(rgbValue: UInt) -> UIColor {
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }

    class func RGB(rgbValue: UInt, alpha: Double) -> UIColor {
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(alpha)
        )
    }
    
    class func alphaBlack(alpha: Double) -> UIColor {
        return UIColor(red: 0, green: 0, blue: 0, alpha: CGFloat(alpha))
    }
    
    class func alphaWhite(alpha: Double) -> UIColor {
        return UIColor(red: 1, green: 1, blue: 1, alpha: CGFloat(alpha))
    }

    func alpha(alpha: Double) -> UIColor {
        var r:CGFloat = 0
        var g:CGFloat = 0
        var b:CGFloat = 0
        var a:CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return UIColor(red: r, green: g, blue: b, alpha: CGFloat(alpha))
    }
    
    func forTransparentBar() -> UIColor {
        var r:CGFloat = 0
        var g:CGFloat = 0
        var b:CGFloat = 0
        var a:CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return UIColor(red: max(0, r - 40/256.0), green: max(0, g - 40/256.0), blue: max(0, b - 40/256.0), alpha: a)
    }
}
