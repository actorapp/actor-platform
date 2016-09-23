//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AANavigationBadge {
    
    fileprivate static var binder = AABinder()
    fileprivate static let badgeView = UIImageView()
    fileprivate static var badgeCount = 0
    fileprivate static var isBadgeVisible = false
    
    fileprivate static var isInited = false
    
    class fileprivate func start() {
        if isInited {
            return
        }
        isInited = true
        
        badgeView.image = Imaging.roundedImage(UIColor(rgb: 0xfe0000), size: CGSize(width: 16, height: 16), radius: 8)
        // badgeView.frame = CGRectMake(16, 22, 32, 16)
        badgeView.alpha = 0
        
        let badgeText = UILabel()
        badgeText.text = "0"
        badgeText.textColor = UIColor.white
        // badgeText.frame = CGRectMake(0, 0, 32, 16)
        badgeText.font = UIFont.systemFont(ofSize: 12)
        badgeText.textAlignment = NSTextAlignment.center
        badgeView.addSubview(badgeText)
        
        
        UIApplication.shared.windows.first!.addSubview(badgeView)
        
        // Bind badge counter
        binder.bind(Actor.getGlobalState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            if let v = value {
                self.badgeCount = Int(v.intValue)
            } else {
                self.badgeCount = 0
            }
            
            badgeText.text = "\(self.badgeCount)"
            if (self.isBadgeVisible && self.badgeCount > 0) {
                self.badgeView.showView()
            } else if (self.badgeCount == 0) {
                self.badgeView.hideView()
            }
            
            badgeText.frame = CGRect(x: 0, y: 0, width: 128, height: 16)
            badgeText.sizeToFit()
            
            if badgeText.frame.width < 8 {
                self.badgeView.frame = CGRect(x: 16, y: 22, width: 16, height: 16)
            } else {
                self.badgeView.frame = CGRect(x: 16, y: 22, width: badgeText.frame.width + 8, height: 16)
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
