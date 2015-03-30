//
//  CircullarProgress.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 17.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

func materialInterpolate(x: Double) -> Double {
    return(6 * pow(x, 2) - 8 * pow(x, 3) + 3 * pow(x, 4));
}

class CircullarProgress : UIView {
    
    init() {
        super.init(frame: CGRectMake(0, 0, 0, 0));
        
        self.backgroundColor = UIColor.clearColor()
    }
    
    var startRawValue: Double = 0
    var endRawValue: Double = 0
    var rawValue: Double = 0
    var lastValueChange: Double = 0

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func resetProgress(value: Double) {
        self.lastValueChange = 0;
        self.startRawValue = value
        self.endRawValue = value
        self.setNeedsDisplay()
    }
    
    func setProgress(value: Double) {
        dispatch_async(dispatch_get_main_queue(), {
            self.lastValueChange = Double(CFAbsoluteTimeGetCurrent())
            self.startRawValue = self.rawValue
            self.endRawValue = value
            self.setNeedsDisplay()
        });
    }
    
    override func drawRect(rect: CGRect) {
        
        var valueChangeTime = CFAbsoluteTimeGetCurrent() - lastValueChange;
        
        if (valueChangeTime >= 0.3) {
            rawValue = endRawValue
        } else {
            var p = materialInterpolate(valueChangeTime / 0.3)
            rawValue = startRawValue + (endRawValue - startRawValue) * p;
        }
        
        let context = UIGraphicsGetCurrentContext()
        let strokeW : CGFloat = 3
        let rotationSpeed : Double = 1600
        
        let r = (min(frame.width, frame.height) - strokeW * 4) / 2
        let centerX = frame.width / 2
        let centerY = frame.height / 2  
        
        var baseAngle : Double = 2 * M_PI * ((CFAbsoluteTimeGetCurrent() * 1000) % rotationSpeed);
        let angle : CGFloat = CGFloat(baseAngle) / CGFloat(rotationSpeed);
        
        var angle2 = CGFloat(rawValue * 2 * M_PI);
        
        // Draw panel
        UIColor.whiteColor().set()
        CGContextSetLineWidth(context, strokeW)
        CGContextAddArc(context, centerX, centerY, r, CGFloat(angle), CGFloat(angle + angle2), 0);
        CGContextDrawPath(context, kCGPathStroke);
        
        var startX = CGFloat(cos(angle)) * r
        var startY = CGFloat(sin(angle)) * r
        
        var endX = CGFloat(cos(angle + angle2)) * r
        var endY = CGFloat(sin(angle + angle2)) * r
        
        CGContextAddArc(context, centerX + startX, centerY + startY, strokeW / 2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
        CGContextDrawPath(context, kCGPathFill);
        
        CGContextAddArc(context, centerX + endX, centerY + endY, strokeW / 2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
        CGContextDrawPath(context, kCGPathFill);
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 16 * Int64(NSEC_PER_MSEC)), dispatch_get_main_queue(), {
            self.setNeedsDisplay()
        });
    }
}