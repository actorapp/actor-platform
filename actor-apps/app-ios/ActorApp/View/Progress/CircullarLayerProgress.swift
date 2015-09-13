//
//  CircullarLayerProgress.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 21.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CircullarLayerProgress: UIView {

    private let circlePathLayer = CAShapeLayer()
    private let backgroundPathLayer = CAShapeLayer()
    private var progressButton: VBFPopFlatButton!
    
    init(size: CGSize) {
        super.init(frame: CGRectMake(0, 0, size.width, size.height))

        self.backgroundColor = UIColor.clearColor()
        self.userInteractionEnabled = false

        let bgPath = UIBezierPath(ovalInRect: CGRectMake(0, 0, self.bounds.width, self.bounds.height))
        backgroundPathLayer.frame = bounds
        backgroundPathLayer.path = bgPath.CGPath
        backgroundPathLayer.fillColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0x64/255.0).CGColor
        backgroundPathLayer.strokeColor = UIColor.clearColor().CGColor
        layer.addSublayer(backgroundPathLayer)
        
        let circlePath = UIBezierPath(ovalInRect: CGRectMake(3, 3, self.bounds.width - 6, self.bounds.height - 6))
        circlePathLayer.frame = bounds
        circlePathLayer.path = circlePath.CGPath
        circlePathLayer.lineWidth = 3
        circlePathLayer.fillColor = UIColor.clearColor().CGColor
        circlePathLayer.strokeColor = UIColor.whiteColor().CGColor
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
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setButtonType(type: FlatButtonType, animated: Bool) {
        if progressButton != nil && animated {
            progressButton.animateToType(type)
        } else {
            hideButton()
            let size: CGFloat =  self.bounds.width < 64 ? 24 : 32
            let x = (self.bounds.width - size) / 2
            let y = (self.bounds.height - size) / 2
            progressButton = VBFPopFlatButton(frame: CGRect(x: x, y: y, width: size, height: size), buttonType: type, buttonStyle: FlatButtonStyle.buttonPlainStyle, animateToInitialState: animated)
            progressButton.userInteractionEnabled = false
            self.addSubview(progressButton)
        }
    }
    
    func setProgress(value: Double) {
        circlePathLayer.strokeEnd = CGFloat(value)
    }
    
    func hideProgress() {
        circlePathLayer.strokeEnd = 0
    }
    
    func hideButton() {
        if progressButton != nil {
            progressButton.removeFromSuperview()
            progressButton = nil
        }
    }
    
}