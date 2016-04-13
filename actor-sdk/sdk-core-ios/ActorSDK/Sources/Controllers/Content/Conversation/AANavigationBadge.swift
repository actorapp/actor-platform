//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AANavigationBadge {
    
    private static var binder = AABinder()
    private static let badgeView = UIImageView()
    private static var badgeCount = 0
    private static var isBadgeVisible = false
    
    private static var isInited = false
    
    class private func start() {
        if isInited {
            return
        }
        isInited = true
        
        badgeView.image = Imaging.roundedImage(UIColor(rgb: 0xfe0000), size: CGSizeMake(16, 16), radius: 8)
        // badgeView.frame = CGRectMake(16, 22, 32, 16)
        badgeView.alpha = 0
        
        let badgeText = UILabel()
        badgeText.text = "0"
        badgeText.textColor = UIColor.whiteColor()
        // badgeText.frame = CGRectMake(0, 0, 32, 16)
        badgeText.font = UIFont.systemFontOfSize(12)
        badgeText.textAlignment = NSTextAlignment.Center
        badgeView.addSubview(badgeText)
        
        
        UIApplication.sharedApplication().windows.first!.addSubview(badgeView)
        
        // Bind badge counter
        binder.bind(Actor.getGlobalState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            if let v = value {
                self.badgeCount = Int(v.integerValue)
            } else {
                self.badgeCount = 0
            }
            
            badgeText.text = "\(self.badgeCount)"
            if (self.isBadgeVisible && self.badgeCount > 0) {
                self.badgeView.showView()
            } else if (self.badgeCount == 0) {
                self.badgeView.hideView()
            }
            
            badgeText.frame = CGRectMake(0, 0, 128, 16)
            badgeText.sizeToFit()
            
            if badgeText.frame.width < 8 {
                self.badgeView.frame = CGRectMake(16, 22, 16, 16)
            } else {
                self.badgeView.frame = CGRectMake(16, 22, badgeText.frame.width + 8, 16)
            }
            badgeText.frame = self.badgeView.bounds
        })
    }
    
    class func showBadge() {
        if (AADevice.isiPad) {
            return
        }
        start()
        isBadgeVisible = true
        if badgeCount > 0 {
            self.badgeView.showView()
        }
    }
    
    class func hideBadge() {
        if (AADevice.isiPad) {
            return
        }        
        start()
        isBadgeVisible = false
        self.badgeView.hideView()
    }
}