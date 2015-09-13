//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
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
        self.drawInRect(rect, blendMode: .DestinationIn, alpha: 1.0)
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
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
        self.drawInRect(rect, blendMode: .Overlay, alpha: 1.0)
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        return image
    }
    
    func roundImage(newSize: Int) -> UIImage {
        let nSize = CGSize(width: newSize, height: newSize)
        UIGraphicsBeginImageContextWithOptions(nSize,false,UIScreen.mainScreen().scale);
        let context = UIGraphicsGetCurrentContext();
        
        // Background
        
        CGContextAddPath(context, CGPathCreateWithEllipseInRect(CGRect(origin: CGPointZero, size: nSize),nil));
        CGContextClip(context);
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (newSize+2), height: (newSize+2))));
        
        // Border
        
        CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x19/255.0).CGColor);
        CGContextAddArc(context,CGFloat(newSize)/2, CGFloat(newSize)/2, CGFloat(newSize)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
        CGContextDrawPath(context, .Stroke);
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
    
    func roundCorners(w: CGFloat, h: CGFloat, roundSize: CGFloat) -> UIImage {
        let nSize = CGSize(width: w, height: h)
        UIGraphicsBeginImageContextWithOptions(nSize, false, UIScreen.mainScreen().scale);
        
        // Background
        UIBezierPath(roundedRect: CGRectMake(0, 0, w, h), cornerRadius: roundSize).addClip()
        
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (w+2), height: (h+2))));

        let image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
    
    func resizeSquare(maxW: CGFloat, maxH: CGFloat) -> UIImage {
        let realW = self.size.width / self.scale;
        let realH = self.size.height / self.scale;
        let factor = min(maxW/realW, maxH/realH)
        return resize(factor * realW, h: factor * realH)
    }
    
    func resizeOptimize(maxPixels: Int) -> UIImage {
        let realW = self.size.width / self.scale;
        let realH = self.size.height / self.scale;
        let factor =  min(1.0,   CGFloat(maxPixels) / (realW * realH));
        return resize(factor * realW, h: factor * realH)
    }
    
    func resize(w: CGFloat, h: CGFloat) -> UIImage {
        let nSize = CGSize(width: w, height: h)
        
        UIGraphicsBeginImageContextWithOptions(nSize, false, 1.0);
        let context = UIGraphicsGetCurrentContext();
        
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (w+2), height: (h+2))));
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
}

class Imaging {
    
    class func roundedImage(color: UIColor, size: CGSize, radius: CGFloat) -> UIImage {
        let rect = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        let path = UIBezierPath(roundedRect: CGRectMake(0, 0, size.width, size.height), cornerRadius: radius)
        color.setFill()
        path.fill()
        let image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return image
            .resizableImageWithCapInsets(UIEdgeInsetsMake(radius, radius, radius, radius))
    }
    
    class func imageWithColor(color: UIColor, size: CGSize) -> UIImage {
        let rect = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        color.setFill()
        UIRectFill(rect)
        let image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image
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



