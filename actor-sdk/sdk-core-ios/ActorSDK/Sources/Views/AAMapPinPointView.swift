//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAMapPinPointView: UIView {
    
    let pinView = UIImageView()
    let pinPointView = UIImageView()
    let pinShadowView = UIImageView()
    
    public init() {
        super.init(frame: CGRectMake(0, 0, 100, 100))
        
        pinShadowView.frame = CGRectMake(43, 47, 32, 39)
        pinShadowView.alpha = 0.9
        pinShadowView.image = UIImage.bundled("LocationPinShadow.png")
        addSubview(pinShadowView)
        
        pinPointView.frame = CGRectMake(100 / 2 - 2, 100 - 18.5, 3.5, 1.5)
        pinPointView.image = UIImage.bundled("LocationPinPoint.png")
        addSubview(pinPointView)
        
        pinView.frame = CGRectMake(100 / 2 - 7, 47, 13.5, 36)
        pinView.image = UIImage.bundled("LocationPin.png")
        addSubview(pinView)
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func risePin(rised: Bool, animated: Bool) {

        self.pinShadowView.layer.removeAllAnimations()
        self.pinView.layer.removeAllAnimations()
        
        if animated {
            if rised {
                UIView.animateWithDuration(0.2, delay: 0.0, options: .BeginFromCurrentState, animations: { () -> Void in
                    self.pinView.frame = CGRectMake(100 / 2 - 7, 7, 13.5, 36)
                    self.pinShadowView.frame = CGRectMake(87, -33, 32, 39)
                }, completion: nil)
            } else {
                UIView.animateWithDuration(0.2, delay: 0.0, options: .BeginFromCurrentState, animations: { () -> Void in
                    self.pinView.frame = CGRectMake(100 / 2 - 7, 47, 13.5, 36)
                    self.pinShadowView.frame = CGRectMake(43, 47, 32, 39)
                }, completion: { finished in
                    if !finished {
                        return
                    }
                    UIView.animateWithDuration(0.1, delay: 0.0, options: .BeginFromCurrentState, animations: { () -> Void in
                        self.pinView.frame = CGRectMake(100 / 2 - 7, 47 + 2, 13.5, 36 - 2)
                    }, completion: { (finished) -> Void in
                        if !finished {
                            return
                        }
                        UIView.animateWithDuration(0.1, delay: 0.0, options: .BeginFromCurrentState, animations: { () -> Void in
                            self.pinView.frame = CGRectMake(100 / 2 - 7, 47, 13.5, 36)
                        }, completion: nil)
                    })
                })
            }
        } else {
            if rised {
                self.pinView.frame = CGRectMake(100 / 2 - 7, 7, 13.5, 36)
                self.pinShadowView.frame = CGRectMake(87, -33, 32, 39)
            } else {
                self.pinView.frame = CGRectMake(100 / 2 - 7, 47, 13.5, 36)
                self.pinShadowView.frame = CGRectMake(43, 47, 32, 39)
            }
        }
    }
}