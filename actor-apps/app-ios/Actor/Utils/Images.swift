//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import Darwin

extension UIImage {
    func tintImage(color:UIColor) -> UIImage{
        UIGraphicsBeginImageContextWithOptions(self.size,false,UIScreen.mainScreen().scale);
        
        var rect = CGRectZero;
        rect.size = self.size;
        // Composite tint color at its own opacity.
        color.set();
        UIRectFill(rect);
        
        // Mask tint color-swatch to this image's opaque mask.
        // We want behaviour like NSCompositeDestinationIn on Mac OS X.
        self.drawInRect(rect, blendMode: kCGBlendModeDestinationIn, alpha: 1.0)
        
        var image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        if (self.capInsets.bottom != 0 || self.capInsets.top != 0 || self.capInsets.left != 0 || self.capInsets.right != 0) {
            return image.resizableImageWithCapInsets(capInsets, resizingMode: resizingMode)
        }
        
        return image;
    }
    
    func tintBgImage(color: UIColor) -> UIImage {
        UIGraphicsBeginImageContextWithOptions(self.size,false,UIScreen.mainScreen().scale);
        
        var rect = CGRectZero;
        rect.size = self.size;
        // Composite tint color at its own opacity.
        color.set();
        UIRectFill(rect);

        // Mask tint color-swatch to this image's opaque mask.
        // We want behaviour like NSCompositeDestinationIn on Mac OS X.
        self.drawInRect(rect, blendMode: kCGBlendModeOverlay, alpha: 1.0)
        
        var image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        return image
    }
    
    func roundImage(newSize: Int) -> UIImage {
        var nSize = CGSize(width: newSize, height: newSize)
        UIGraphicsBeginImageContextWithOptions(nSize,false,UIScreen.mainScreen().scale);
        var context = UIGraphicsGetCurrentContext();
        
        // Background
        
        CGContextAddPath(context, CGPathCreateWithEllipseInRect(CGRect(origin: CGPointZero, size: nSize),nil));
        CGContextClip(context);
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (newSize+2), height: (newSize+2))));
        
        // Border
        
        CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x19/255.0).CGColor);
        CGContextAddArc(context,CGFloat(newSize)/2, CGFloat(newSize)/2, CGFloat(newSize)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
        CGContextDrawPath(context, kCGPathStroke);
        
        var image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
    
    func roundCorners(w: CGFloat, h: CGFloat, roundSize: CGFloat) -> UIImage {
        var nSize = CGSize(width: w, height: h)
        UIGraphicsBeginImageContextWithOptions(nSize, false, UIScreen.mainScreen().scale);
        var context = UIGraphicsGetCurrentContext();
        
        // Background
  
        UIBezierPath(roundedRect: CGRectMake(0, 0, w, h), cornerRadius: roundSize).addClip()
//        CGContextClip(context);
        
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (w+2), height: (h+2))));

        var image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
    
    func resizeSquare(maxW: CGFloat, maxH: CGFloat) -> UIImage {
        var realW = self.size.width / self.scale;
        var realH = self.size.height / self.scale;
        var factor = min(maxW/realW, maxH/realH)
        return resize(factor * realW, h: factor * realH)
    }
    
    func resizeOptimize(maxPixels: Int) -> UIImage {
        var realW = self.size.width / self.scale;
        var realH = self.size.height / self.scale;
        var factor =  min(1.0,   CGFloat(maxPixels) / (realW * realH));
        return resize(factor * realW, h: factor * realH)
    }
    
    func resize(w: CGFloat, h: CGFloat) -> UIImage {
        var nSize = CGSize(width: w, height: h)
        
        UIGraphicsBeginImageContextWithOptions(nSize, false, 1.0);
        var context = UIGraphicsGetCurrentContext();
        
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (w+2), height: (h+2))));
        
        var image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
}

class Imaging {
    
    class func roundedImage(color: UIColor, size: CGSize, radius: CGFloat) -> UIImage {
        var rect = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        var path = UIBezierPath(roundedRect: CGRectMake(0, 0, size.width, size.height), cornerRadius: radius)
        color.setFill()
        path.fill()
        var image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return image
            .resizableImageWithCapInsets(UIEdgeInsetsMake(radius, radius, radius, radius))
    }
    
    class func imageWithColor(color: UIColor, size: CGSize) -> UIImage {
        var rect = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        color.setFill()
        UIRectFill(rect)
        var image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image
    }
    
    class func avatarPlaceholder(index: jint, size: Int, title: NSString, rounded: Bool) -> UIImage {
        var color = Resources.placeHolderColors[Int(abs(index)) % Resources.placeHolderColors.count].CGColor;
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: size, height: size), false, UIScreen.mainScreen().scale);
        var context = UIGraphicsGetCurrentContext();

        // Background
        
        CGContextSetFillColorWithColor(context, color);
        
        if rounded {
            CGContextAddArc(context, CGFloat(size)/2, CGFloat(size)/2, CGFloat(size)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
        } else {
            CGContextAddRect(context, CGRect(x: 0, y: 0, width: size, height: size))
        }
        
        CGContextDrawPath(context, kCGPathFill);

        // Text
        
        UIColor.whiteColor().set()
        
        var font = UIFont.systemFontOfSize(CGFloat(size / 2));
        var rect = CGRectMake(0, 0, CGFloat(size), CGFloat(size))
        rect.origin.y = round(CGFloat(size * 47 / 100) - font.pointSize / 2);
        
        var style : NSMutableParagraphStyle = NSParagraphStyle.defaultParagraphStyle().mutableCopy() as! NSMutableParagraphStyle
        style.alignment = NSTextAlignment.Center
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        title.drawInRect(rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
            NSForegroundColorAttributeName:UIColor.whiteColor()])
        
        // Border
        
        if rounded {
            CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x10/255.0).CGColor);
            CGContextAddArc(context,CGFloat(size)/2, CGFloat(size)/2, CGFloat(size)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
            CGContextDrawPath(context, kCGPathStroke);
        }
        
        var image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }

}

class AAPhoto: NSObject, NYTPhoto {
    
    let image: UIImage?
    let placeholderImage: UIImage?
    let attributedCaptionTitle: NSAttributedString?
    let attributedCaptionSummary: NSAttributedString?
    let attributedCaptionCredit: NSAttributedString?
    
    init(image: UIImage) {
        self.image = image
        self.placeholderImage = nil
        self.attributedCaptionTitle = nil
        self.attributedCaptionSummary = nil
        self.attributedCaptionCredit = nil
    }
    
    init(image: UIImage, placeholderImage: UIImage, attributedCaptionTitle: NSAttributedString, attributedCaptionSummary: NSAttributedString, attributedCaptionCredit: NSAttributedString) {
        self.image = image
        self.placeholderImage = placeholderImage
        self.attributedCaptionTitle = attributedCaptionTitle
        self.attributedCaptionSummary = attributedCaptionSummary
        self.attributedCaptionCredit = attributedCaptionCredit
    }
}



