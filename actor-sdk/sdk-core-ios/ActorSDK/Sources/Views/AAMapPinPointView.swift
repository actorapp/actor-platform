//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
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
        if (rised) {
            pinView.hidden = true
        } else {
            pinView.hidden = false
        }
    }
}