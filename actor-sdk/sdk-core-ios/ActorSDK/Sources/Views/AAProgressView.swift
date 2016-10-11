//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import VBFPopFlatButton

open class AAProgressView: UIView {

    fileprivate let circlePathLayer = CAShapeLayer()
    fileprivate let backgroundPathLayer = CAShapeLayer()
    fileprivate var progressButton: VBFPopFlatButton!
    
    public init(size: CGSize) {
        super.init(frame: CGRect(x: 0, y: 0, width: size.width, height: size.height))

        self.backgroundColor = UIColor.clear
        self.isUserInteractionEnabled = false

        let bgPath = UIBezierPath(ovalIn: CGRect(x: 0, y: 0, width: self.bounds.width, height: self.bounds.height))
        backgroundPathLayer.frame = bounds
        backgroundPathLayer.path = bgPath.cgPath
        backgroundPathLayer.fillColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0x64/255.0).cgColor
        backgroundPathLayer.strokeColor = UIColor.clear.cgColor
        layer.addSublayer(backgroundPathLayer)
        
        let circlePath = UIBezierPath(ovalIn: CGRect(x: 3, y: 3, width: self.bounds.width - 6, height: self.bounds.height - 6))
        circlePathLayer.frame = bounds
        circlePathLayer.path = circlePath.cgPath
        circlePathLayer.lineWidth = 3
        circlePathLayer.lineCap = kCALineCapRound
        circlePathLayer.fillColor = UIColor.clear.cgColor
        circlePathLayer.strokeColor = UIColor.white.cgColor
        circlePathLayer.strokeStart = 0
        circlePathLayer.strokeEnd = 0.5
        layer.addSublayer(circlePathLayer)

        // Permanent rotation animation
//        var animation = POPBasicAnimation(propertyNamed: kPOPShapeLayerStrokeEnd)
//        animation.fromValue = NSNumber(float: Float(0))
//        animation.toValue = NSNumber(float: Float(1))//NSNumber(float: Float(M_PI * 2.0))
//        animation.repeatForever = true
//        animation.duration = 5000
//        circlePathLayer.pop_addAnimation(animation, forKey: "rotationAnimation")
        
//        var rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
//        rotationAnimation.fromValue = NSNumber(float: Float(0))
//        rotationAnimation.toValue = NSNumber(float: Float(M_PI * 2.0 * 3000))
//        rotationAnimation.duration = 5000
//        rotationAnimation.cumulative = false
//        rotationAnimation.repeatCount = 100000
//        circlePathLayer.addAnimation(rotationAnimation, forKey: "rotationAnimation")
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func setButtonType(_ type: FlatButtonType, animated: Bool) {
        if progressButton != nil && animated {
            progressButton.animate(to: type)
        } else {
            hideButton()
            let size: CGFloat =  self.bounds.width < 64 ? 24 : 32
            let x = (self.bounds.width - size) / 2
            let y = (self.bounds.height - size) / 2
            progressButton = VBFPopFlatButton(frame: CGRect(x: x, y: y, width: size, height: size), buttonType: type, buttonStyle: FlatButtonStyle.buttonPlainStyle, animateToInitialState: animated)
            progressButton.isUserInteractionEnabled = false
            self.addSubview(progressButton)
        }
    }
    
    open func setProgress(_ value: Double) {
        circlePathLayer.strokeEnd = CGFloat(value)
    }
    
    open func hideProgress() {
        circlePathLayer.strokeEnd = 0
    }
    
    open func hideButton() {
        if progressButton != nil {
            progressButton.removeFromSuperview()
            progressButton = nil
        }
    }
    
}
