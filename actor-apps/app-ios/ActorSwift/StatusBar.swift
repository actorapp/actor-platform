//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

private var selector = Selector("rs`str".encodeText(1) + "A`qVhmcnv".encodeText(1))
private var viewClass: AnyClass = NSClassFromString("VJTubuvtCbs".encodeText(-1))!

extension UIApplication {
    
    func animateStatusBarAppearance(animation: StatusBarAppearanceAnimation, duration: NSTimeInterval) {
        
        if self.respondsToSelector(selector) {
    
            let window = self.performSelector(selector).takeUnretainedValue() as! UIWindow
            
            var view: UIView! = nil
            for subview in window.subviews {
                if subview.dynamicType == viewClass {
                    view = subview
                    break
                }
            }
            
            if view != nil {
                
                let viewHeight = view.frame.size.height
                
                var startPosition = view.layer.position
                var position = view.layer.position
                
                if animation == .SlideDown {
                    startPosition = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2) - viewHeight);
                    position = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2));
                } else if animation == .SlideUp {
                    startPosition = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2));
                    position = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2) - viewHeight);
                }

                
                let animation = CABasicAnimation(keyPath: "position")
                animation.duration = duration
                animation.fromValue = NSValue(CGPoint: startPosition)
                animation.toValue = NSValue(CGPoint: position)

                animation.fillMode = kCAFillModeForwards
                animation.removedOnCompletion = false
                
                animation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut)
                
                view.layer.addAnimation(animation, forKey: "ac_position")
            }
        }
    }
}

enum StatusBarAppearanceAnimation {
    case SlideDown
    case SlideUp
}