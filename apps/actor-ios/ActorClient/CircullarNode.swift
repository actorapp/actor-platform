//
//  CircullarNode.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 20.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class CircullarNode: ASDisplayNode {
    
    override class func drawRect(frame: CGRect, withParameters parameters: NSObjectProtocol!,
        isCancelled isCancelledBlock: asdisplaynode_iscancelled_block_t!, isRasterizing: Bool) {
            var params =  (parameters as! Array<Double>)
            var rawValue = params[0] as Double;
            var time = params[0] as Double;
            
            let strokeW : CGFloat = 3
            let rotationSpeed : Double = 1600

            var size = min(frame.width, frame.height)
            let r = (size - strokeW * 4) / 2
            let centerX = frame.width / 2
            let centerY = frame.height / 2

            let context = UIGraphicsGetCurrentContext()
            
            // BG
            CGContextSetFillColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x76/255.0).CGColor)
            CGContextAddArc(context, centerX, centerY, (size - strokeW*2)/2, CGFloat(0), CGFloat(2 * M_PI), 0);
            CGContextDrawPath(context, kCGPathFill);
            
            UIColor.whiteColor().set()
            
            var font = UIFont.systemFontOfSize(CGFloat(size / 3));
            var rect = CGRectMake(0, 0, CGFloat(size), CGFloat(size))
            rect.origin.y = round(CGFloat(size * 47 / 100) - font.pointSize / 2);
            
            var style : NSMutableParagraphStyle = NSParagraphStyle.defaultParagraphStyle().mutableCopy() as! NSMutableParagraphStyle
            style.alignment = NSTextAlignment.Center
            style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
            
            var title = "\(Int(rawValue * 100))" as NSString
            
            
            title.drawInRect(rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
                NSForegroundColorAttributeName:UIColor.whiteColor()])
            
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
    }
    
    var startRawValue: Double = 0
    var endRawValue: Double = 0
    var rawValue: Double = 0
    var lastValueChange: Double = 0
    
    override init() {
        super.init()
        self.backgroundColor = UIColor.clearColor()
        self.opaque = false
    }

    func postProgress(value: Double, animated: Bool) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setProgress(value, animated: animated)
        })
    }
    
    func setProgress(value: Double, animated: Bool) {
        if (!animated) {
            self.rawValue = value
            self.endRawValue = value
            self.startRawValue = 0
            self.lastValueChange = 0
            setNeedsDisplay()
        } else {
            self.lastValueChange = Double(CFAbsoluteTimeGetCurrent())
            self.startRawValue = self.rawValue
            self.endRawValue = value
            setNeedsDisplay()
        }
    }

    override func drawParametersForAsyncLayer(layer: _ASDisplayLayer!) -> NSObject! {
        
        var now = CFAbsoluteTimeGetCurrent()
        
        var valueChangeTime = now - lastValueChange;
        if (valueChangeTime >= 0.3) {
            rawValue = endRawValue
        } else {
            var p = materialInterpolate(valueChangeTime / 0.3)
            rawValue = startRawValue + (endRawValue - startRawValue) * p;
        }

        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 16 * Int64(NSEC_PER_MSEC)), dispatch_get_main_queue(), {
            self.setNeedsDisplay()
        });
        return [rawValue, now];
    }
}