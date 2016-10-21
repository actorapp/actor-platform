//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAMapPinPointView: UIView {
    
    let pinView = UIImageView()
    let pinPointView = UIImageView()
    let pinShadowView = UIImageView()
    
    public init() {
        super.init(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
        
        pinShadowView.frame = CGRect(x: 43, y: 47, width: 32, height: 39)
        pinShadowView.alpha = 0.9
        pinShadowView.image = UIImage.bundled("LocationPinShadow.png")
        addSubview(pinShadowView)
        
        pinPointView.frame = CGRect(x: 100 / 2 - 2, y: 100 - 18.5, width: 3.5, height: 1.5)
        pinPointView.image = UIImage.bundled("LocationPinPoint.png")
        addSubview(pinPointView)
        
        pinView.frame = CGRect(x: 100 / 2 - 7, y: 47, width: 13.5, height: 36)
        pinView.image = UIImage.bundled("LocationPin.png")
        addSubview(pinView)
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func risePin(_ rised: Bool, animated: Bool) {

        self.pinShadowView.layer.removeAllAnimations()
        self.pinView.layer.removeAllAnimations()
        
        if animated {
            if rised {
                UIView.animate(withDuration: 0.2, delay: 0.0, options: .beginFromCurrentState, animations: { () -> Void in
                    self.pinView.frame = CGRect(x: 100 / 2 - 7, y: 7, width: 13.5, height: 36)
                    self.pinShadowView.frame = CGRect(x: 87, y: -33, width: 32, height: 39)
                }, completion: nil)
            } else {
                UIView.animate(withDuration: 0.2, delay: 0.0, options: .beginFromCurrentState, animations: { () -> Void in
                    self.pinView.frame = CGRect(x: 100 / 2 - 7, y: 47, width: 13.5, height: 36)
                    self.pinShadowView.frame = CGRect(x: 43, y: 47, width: 32, height: 39)
                }, completion: { finished in
                    if !finished {
                        return
                    }
                    UIView.animate(withDuration: 0.1, delay: 0.0, options: .beginFromCurrentState, animations: { () -> Void in
                        self.pinView.frame = CGRect(x: 100 / 2 - 7, y: 47 + 2, width: 13.5, height: 36 - 2)
                    }, completion: { (finished) -> Void in
                        if !finished {
                            return
                        }
                        UIView.animate(withDuration: 0.1, delay: 0.0, options: .beginFromCurrentState, animations: { () -> Void in
                            self.pinView.frame = CGRect(x: 100 / 2 - 7, y: 47, width: 13.5, height: 36)
                        }, completion: nil)
                    })
                })
            }
        } else {
            if rised {
                self.pinView.frame = CGRect(x: 100 / 2 - 7, y: 7, width: 13.5, height: 36)
                self.pinShadowView.frame = CGRect(x: 87, y: -33, width: 32, height: 39)
            } else {
                self.pinView.frame = CGRect(x: 100 / 2 - 7, y: 47, width: 13.5, height: 36)
                self.pinShadowView.frame = CGRect(x: 43, y: 47, width: 32, height: 39)
            }
        }
    }
}
