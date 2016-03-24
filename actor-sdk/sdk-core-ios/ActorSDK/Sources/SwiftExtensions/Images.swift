//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import Darwin
import UIKit
import Accelerate

public extension UIImage {
    
    class func tinted(named: String, color: UIColor) -> UIImage {
        return UIImage.bundled(named)!.tintImage(color)
    }
    
    class func templated(named: String) -> UIImage {
        return UIImage.bundled(named)!.imageWithRenderingMode(.AlwaysTemplate)
    }
    
    public func tintImage(color:UIColor) -> UIImage{
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
        
        return image.imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
    }
    
    public func tintBgImage(color: UIColor) -> UIImage {
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
    
    public func roundImage(newSize: Int) -> UIImage {
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
    
    
    public func roundCorners(w: CGFloat, h: CGFloat, roundSize: CGFloat) -> UIImage {
        let nSize = CGSize(width: w, height: h)
        UIGraphicsBeginImageContextWithOptions(nSize, false, UIScreen.mainScreen().scale);
        
        // Background
        UIBezierPath(roundedRect: CGRectMake(0, 0, w, h), cornerRadius: roundSize).addClip()
        
        self.drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (w+2), height: (h+2))));
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
    
    public func resizeSquare(maxW: CGFloat, maxH: CGFloat) -> UIImage {
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
    
    public func resize(w: CGFloat, h: CGFloat) -> UIImage {
        let nSize = CGSize(width: w, height: h)
        UIGraphicsBeginImageContextWithOptions(nSize, false, 1.0)
        drawInRect(CGRect(origin: CGPointMake(-1, -1), size: CGSize(width: (w + 2), height: (h + 2))))
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image;
    }
    
    public func aa_imageWithColor(color1: UIColor) -> UIImage {
        UIGraphicsBeginImageContextWithOptions(self.size, false, self.scale)
        
        let context  = UIGraphicsGetCurrentContext()
        CGContextTranslateCTM(context, 0, self.size.height)
        CGContextScaleCTM(context, 1.0, -1.0);
        CGContextSetBlendMode(context, CGBlendMode.Normal)
        
        let rect     = CGRectMake(0, 0, self.size.width, self.size.height) as CGRect
        CGContextClipToMask(context, rect, self.CGImage)
        color1.setFill()
        CGContextFillRect(context, rect)
        
        let newImage = UIGraphicsGetImageFromCurrentImageContext() as UIImage
        UIGraphicsEndImageContext()
        
        return newImage
    }
}

public class Imaging {
    
    public class func roundedImage(color: UIColor, radius: CGFloat) -> UIImage {
        return roundedImage(color, size: CGSizeMake(radius * 2, radius * 2), radius: radius)
    }
    
    public class func roundedImage(color: UIColor, size: CGSize, radius: CGFloat) -> UIImage {
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        let path = UIBezierPath(roundedRect: CGRectMake(0, 0, size.width, size.height), cornerRadius: radius)
        path.lineWidth = 1
        color.setFill()
        path.fill()
        let image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return image.resizableImageWithCapInsets(UIEdgeInsetsMake(radius, radius, radius, radius))
    }
    
    public class func circleImage(color: UIColor, radius: CGFloat) -> UIImage {
        return circleImage(color, size: CGSizeMake(radius * 2, radius * 2), radius: radius)
    }
    
    public class func circleImage(color: UIColor, size: CGSize, radius: CGFloat) -> UIImage {
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        let path = UIBezierPath(roundedRect: CGRectMake(1, 1, size.width - 2, size.height - 2), cornerRadius: radius)
        color.setStroke()
        path.stroke()
        let image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return image.resizableImageWithCapInsets(UIEdgeInsetsMake(radius, radius, radius, radius))
    }
    
    public class func imageWithColor(color: UIColor, size: CGSize) -> UIImage {
        let rect = CGRectMake(0, 0, size.width, size.height)
        UIGraphicsBeginImageContextWithOptions(size, false, 0)
        color.setFill()
        UIRectFill(rect)
        let image: UIImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image
    }
    
    
}

public extension UIImage {
    public func applyLightEffect() -> UIImage? {
        return applyBlurWithRadius(30, tintColor: UIColor(white: 1.0, alpha: 0.3), saturationDeltaFactor: 1.8)
    }
    
    public func applyExtraLightEffect() -> UIImage? {
        return applyBlurWithRadius(20, tintColor: UIColor(white: 0.97, alpha: 0.82), saturationDeltaFactor: 1.8)
    }
    
    public func applyDarkEffect() -> UIImage? {
        return applyBlurWithRadius(20, tintColor: UIColor(white: 0.11, alpha: 0.73), saturationDeltaFactor: 1.8)
    }
    
    public func applyTintEffectWithColor(tintColor: UIColor) -> UIImage? {
        let effectColorAlpha: CGFloat = 0.6
        var effectColor = tintColor
        
        let componentCount = CGColorGetNumberOfComponents(tintColor.CGColor)
        
        if componentCount == 2 {
            var b: CGFloat = 0
            if tintColor.getWhite(&b, alpha: nil) {
                effectColor = UIColor(white: b, alpha: effectColorAlpha)
            }
        } else {
            var red: CGFloat = 0
            var green: CGFloat = 0
            var blue: CGFloat = 0
            
            if tintColor.getRed(&red, green: &green, blue: &blue, alpha: nil) {
                effectColor = UIColor(red: red, green: green, blue: blue, alpha: effectColorAlpha)
            }
        }
        
        return applyBlurWithRadius(10, tintColor: effectColor, saturationDeltaFactor: -1.0, maskImage: nil)
    }
    
    public func applyBlur(blurRadius: CGFloat) -> UIImage? {
        return applyBlurWithRadius(blurRadius, tintColor: nil, saturationDeltaFactor: 1.0)
    }
    
    public func applyBlurWithRadius(blurRadius: CGFloat, tintColor: UIColor?, saturationDeltaFactor: CGFloat, maskImage: UIImage? = nil) -> UIImage? {
        // Check pre-conditions.
        if (size.width < 1 || size.height < 1) {
            print("*** error: invalid size: \(size.width) x \(size.height). Both dimensions must be >= 1: \(self)")
            return nil
        }
        if self.CGImage == nil {
            print("*** error: image must be backed by a CGImage: \(self)")
            return nil
        }
        if maskImage != nil && maskImage!.CGImage == nil {
            print("*** error: maskImage must be backed by a CGImage: \(maskImage)")
            return nil
        }
        
        let __FLT_EPSILON__ = CGFloat(FLT_EPSILON)
        let screenScale = UIScreen.mainScreen().scale
        let imageRect = CGRect(origin: CGPointZero, size: size)
        var effectImage = self
        
        let hasBlur = blurRadius > __FLT_EPSILON__
        let hasSaturationChange = fabs(saturationDeltaFactor - 1.0) > __FLT_EPSILON__
        
        if hasBlur || hasSaturationChange {
            func createEffectBuffer(context: CGContext) -> vImage_Buffer {
                let data = CGBitmapContextGetData(context)
                let width = vImagePixelCount(CGBitmapContextGetWidth(context))
                let height = vImagePixelCount(CGBitmapContextGetHeight(context))
                let rowBytes = CGBitmapContextGetBytesPerRow(context)
                
                return vImage_Buffer(data: data, height: height, width: width, rowBytes: rowBytes)
            }
            
            UIGraphicsBeginImageContextWithOptions(size, false, screenScale)
            let effectInContext = UIGraphicsGetCurrentContext()
            
            CGContextScaleCTM(effectInContext, 1.0, -1.0)
            CGContextTranslateCTM(effectInContext, 0, -size.height)
            CGContextDrawImage(effectInContext, imageRect, self.CGImage)
            
            var effectInBuffer = createEffectBuffer(effectInContext!)
            
            
            UIGraphicsBeginImageContextWithOptions(size, false, screenScale)
            let effectOutContext = UIGraphicsGetCurrentContext()
            
            var effectOutBuffer = createEffectBuffer(effectOutContext!)
            
            
            if hasBlur {
                // A description of how to compute the box kernel width from the Gaussian
                // radius (aka standard deviation) appears in the SVG spec:
                // http://www.w3.org/TR/SVG/filters.html#feGaussianBlurElement
                //
                // For larger values of 's' (s >= 2.0), an approximation can be used: Three
                // successive box-blurs build a piece-wise quadratic convolution kernel, which
                // approximates the Gaussian kernel to within roughly 3%.
                //
                // let d = floor(s * 3*sqrt(2*pi)/4 + 0.5)
                //
                // ... if d is odd, use three box-blurs of size 'd', centered on the output pixel.
                //
                
                let inputRadius = blurRadius * screenScale
                var radius = UInt32(floor(inputRadius * 3.0 * CGFloat(sqrt(2 * M_PI)) / 4 + 0.5))
                if radius % 2 != 1 {
                    radius += 1 // force radius to be odd so that the three box-blur methodology works.
                }
                
                let imageEdgeExtendFlags = vImage_Flags(kvImageEdgeExtend)
                
                vImageBoxConvolve_ARGB8888(&effectInBuffer, &effectOutBuffer, nil, 0, 0, radius, radius, nil, imageEdgeExtendFlags)
                vImageBoxConvolve_ARGB8888(&effectOutBuffer, &effectInBuffer, nil, 0, 0, radius, radius, nil, imageEdgeExtendFlags)
                vImageBoxConvolve_ARGB8888(&effectInBuffer, &effectOutBuffer, nil, 0, 0, radius, radius, nil, imageEdgeExtendFlags)
            }
            
            var effectImageBuffersAreSwapped = false
            
            if hasSaturationChange {
                let s: CGFloat = saturationDeltaFactor
                let floatingPointSaturationMatrix: [CGFloat] = [
                    0.0722 + 0.9278 * s,  0.0722 - 0.0722 * s,  0.0722 - 0.0722 * s,  0,
                    0.7152 - 0.7152 * s,  0.7152 + 0.2848 * s,  0.7152 - 0.7152 * s,  0,
                    0.2126 - 0.2126 * s,  0.2126 - 0.2126 * s,  0.2126 + 0.7873 * s,  0,
                    0,                    0,                    0,  1
                ]
                
                let divisor: CGFloat = 256
                let matrixSize = floatingPointSaturationMatrix.count
                var saturationMatrix = [Int16](count: matrixSize, repeatedValue: 0)
                
                for i: Int in 0 ..< matrixSize {
                    saturationMatrix[i] = Int16(round(floatingPointSaturationMatrix[i] * divisor))
                }
                
                if hasBlur {
                    vImageMatrixMultiply_ARGB8888(&effectOutBuffer, &effectInBuffer, saturationMatrix, Int32(divisor), nil, nil, vImage_Flags(kvImageNoFlags))
                    effectImageBuffersAreSwapped = true
                } else {
                    vImageMatrixMultiply_ARGB8888(&effectInBuffer, &effectOutBuffer, saturationMatrix, Int32(divisor), nil, nil, vImage_Flags(kvImageNoFlags))
                }
            }
            
            if !effectImageBuffersAreSwapped {
                effectImage = UIGraphicsGetImageFromCurrentImageContext()
            }
            
            UIGraphicsEndImageContext()
            
            if effectImageBuffersAreSwapped {
                effectImage = UIGraphicsGetImageFromCurrentImageContext()
            }
            
            UIGraphicsEndImageContext()
        }
        
        // Set up output context.
        UIGraphicsBeginImageContextWithOptions(size, false, screenScale)
        let outputContext = UIGraphicsGetCurrentContext()
        CGContextScaleCTM(outputContext, 1.0, -1.0)
        CGContextTranslateCTM(outputContext, 0, -size.height)
        
        // Draw base image.
        CGContextDrawImage(outputContext, imageRect, self.CGImage)
        
        // Draw effect image.
        if hasBlur {
            CGContextSaveGState(outputContext)
            if let image = maskImage {
                CGContextClipToMask(outputContext, imageRect, image.CGImage);
            }
            CGContextDrawImage(outputContext, imageRect, effectImage.CGImage)
            CGContextRestoreGState(outputContext)
        }
        
        // Add in color tint.
        if let color = tintColor {
            CGContextSaveGState(outputContext)
            CGContextSetFillColorWithColor(outputContext, color.CGColor)
            CGContextFillRect(outputContext, imageRect)
            CGContextRestoreGState(outputContext)
        }
        
        // Output image is ready.
        let outputImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return outputImage
    }
}

extension UIImage {
    public func imageRotatedByDegrees(degrees: CGFloat, flip: Bool) -> UIImage {
//        let radiansToDegrees: (CGFloat) -> CGFloat = {
//            return $0 * (180.0 / CGFloat(M_PI))
//        }
        let degreesToRadians: (CGFloat) -> CGFloat = {
            return $0 / 180.0 * CGFloat(M_PI)
        }
        
        // calculate the size of the rotated view's containing box for our drawing space
        let rotatedViewBox = UIView(frame: CGRect(origin: CGPointZero, size: size))
        let t = CGAffineTransformMakeRotation(degreesToRadians(degrees));
        rotatedViewBox.transform = t
        let rotatedSize = rotatedViewBox.frame.size
        
        // Create the bitmap context
        UIGraphicsBeginImageContext(rotatedSize)
        let bitmap = UIGraphicsGetCurrentContext()
        
        // Move the origin to the middle of the image so we will rotate and scale around the center.
        CGContextTranslateCTM(bitmap, rotatedSize.width / 2.0, rotatedSize.height / 2.0);
        
        //   // Rotate the image context
        CGContextRotateCTM(bitmap, degreesToRadians(degrees));
        
        // Now, draw the rotated/scaled image into the context
        var yFlip: CGFloat
        
        if(flip){
            yFlip = CGFloat(-1.0)
        } else {
            yFlip = CGFloat(1.0)
        }
        
        CGContextScaleCTM(bitmap, yFlip, -1.0)
        CGContextDrawImage(bitmap, CGRectMake(-size.width / 2, -size.height / 2, size.width, size.height), CGImage)
        
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage
    }
}


