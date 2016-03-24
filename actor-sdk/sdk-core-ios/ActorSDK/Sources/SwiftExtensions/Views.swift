//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

// Closure based tap on views

private var targetReference = "target"
public extension UITapGestureRecognizer {
    public convenience init(closure: ()->()){
        let target = ClosureTarget(closure: closure)
        self.init(target: target, action: #selector(ClosureTarget.invoke))
        setAssociatedObject(self, value: target, associativeKey: &targetReference)
    }
}

public extension UIView {
    public var viewDidTap: (()->())? {
        set (value) {
            if value != nil {
                self.addGestureRecognizer(UITapGestureRecognizer(closure: value!))
                self.userInteractionEnabled = true
            }
        }
        get {
            return nil
        }
    }
}

private class ClosureTarget {
    
    private let closure: ()->()
    
    init(closure: ()->()) {
        self.closure = closure
    }
    
    @objc func invoke() {
        closure()
    }
}

// View Hide/Show and size extensions

public extension UIView {
    
    public func hideView() {
        self.hidden = true
//        UIView.animateWithDuration(0.2, animations: { () -> Void in
//            self.alpha = 0
//        })
    }
    
    public func showView() {
        self.hidden = false
//        UIView.animateWithDuration(0.2, animations: { () -> Void in
//            self.alpha = 1
//        })
    }
    
    public func showViewAnimated() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 1
        })
    }
    
    public func hideViewAnimated() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 0
        })
    }
    
//    public var height: CGFloat { get { return bounds.height } }
//    public var width: CGFloat { get { return bounds.width } }
//    
//    public var left: CGFloat { get { return frame.minX } }
//    public var right: CGFloat { get { return frame.maxX } }
//    public var top: CGFloat { get { return frame.minY } }
//    public var bottom: CGFloat { get { return frame.maxY } }
    
    public func centerIn(rect: CGRect) {
        self.frame = CGRectMake(rect.origin.x + (rect.width - self.bounds.width) / 2, rect.origin.y + (rect.height - self.bounds.height) / 2,
            self.bounds.width, self.bounds.height)
    }
    
    public func under(rect: CGRect, offset: CGFloat) {
        self.frame = CGRectMake(rect.origin.x + (rect.width - self.bounds.width) / 2, rect.origin.y + rect.height + offset,
            self.bounds.width, self.bounds.height)
    }
    
    public func topIn(rect: CGRect) {
        self.frame = CGRectMake(rect.origin.x + (rect.width - self.bounds.width) / 2, rect.origin.y,
            self.bounds.width, self.bounds.height)
    }
}

// Text measuring

public class UIViewMeasure {
    
    public class func measureText(text: String, width: CGFloat, fontSize: CGFloat) -> CGSize {
        return UIViewMeasure.measureText(text, width: width, font: UIFont.systemFontOfSize(fontSize))
    }
    
    public class func measureText(text: String, width: CGFloat, font: UIFont) -> CGSize {
        
        // Building paragraph styles
        let style = NSMutableParagraphStyle()
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping
        
        // Measuring text with reduced width
        let rect = text.boundingRectWithSize(CGSize(width: width - 2, height: CGFloat.max),
            options: NSStringDrawingOptions.UsesLineFragmentOrigin,
            attributes: [NSFontAttributeName: font, NSParagraphStyleAttributeName: style],
            context: nil)
        
        // Returning size with expanded width
        return CGSizeMake(ceil(rect.width + 2), CGFloat(ceil(rect.height)))
    }
    
    public class func measureText(attributedText: NSAttributedString, width: CGFloat) -> CGSize {
        
        // Measuring text with reduced width
        let rect = attributedText.boundingRectWithSize(CGSize(width: width - 2, height: CGFloat.max), options: [.UsesLineFragmentOrigin, .UsesFontLeading], context: nil)
        
        // Returning size with expanded width and height
        return CGSizeMake(ceil(rect.width + 2), CGFloat(ceil(rect.height)))
    }
}

// Loading cells without explict registration

private var registeredCells = "cells!"

public extension UITableView {
    private func cellTypeForClass(cellClass: AnyClass) -> String {
        let cellReuseId = "\(cellClass)"
        var registered: ([String])! = getAssociatedObject(self, associativeKey: &registeredCells)
        var found = false
        if registered != nil {
            if registered.contains(cellReuseId) {
                found = true
            } else {
                registered.append(cellReuseId)
                setAssociatedObject(self, value: registered, associativeKey: &registeredCells)
            }
        } else {
            setAssociatedObject(self, value: [cellReuseId], associativeKey: &registeredCells)
        }
        
        if !found {
            registerClass(cellClass, forCellReuseIdentifier: cellReuseId)
        }
        
        return cellReuseId
    }
    
    public func dequeueCell(cellClass: AnyClass, indexPath: NSIndexPath) -> UITableViewCell {
        let reuseId = cellTypeForClass(cellClass)
        return self.dequeueReusableCellWithIdentifier(reuseId, forIndexPath: indexPath)
    }
    
    public func dequeueCell<T where T: UITableViewCell>(indexPath: NSIndexPath) -> T {
        let reuseId = cellTypeForClass(T.self)
        return self.dequeueReusableCellWithIdentifier(reuseId, forIndexPath: indexPath) as! T
    }
    
    public func visibleCellForIndexPath(path: NSIndexPath) -> UITableViewCell? {
        if indexPathsForVisibleRows == nil {
            return nil
        }
        
        for i in 0..<indexPathsForVisibleRows!.count {
            let vPath = indexPathsForVisibleRows![i]
            if vPath.row == path.row && vPath.section == path.section {
                return visibleCells[i]
            }
        }
        
        return nil
    }
}

public extension UICollectionView {
//    private func cellTypeForClass(cellClass: AnyClass) -> String {
//        let cellReuseId = "\(cellClass)"
//        var registered: ([String])! = getAssociatedObject(self, associativeKey: &registeredCells)
//        var found = false
//        if registered != nil {
//            if registered.contains(cellReuseId) {
//                found = true
//            } else {
//                registered.append(cellReuseId)
//                setAssociatedObject(self, value: registered, associativeKey: &registeredCells)
//            }
//        } else {
//            setAssociatedObject(self, value: [cellReuseId], associativeKey: &registeredCells)
//        }
//        
//        if !found {
//            registerClass(cellClass, forCellWithReuseIdentifier: cellReuseId)
//        }
//        
//        return cellReuseId
//    }
//    
//    public func dequeueCell(cellClass: AnyClass, indexPath: NSIndexPath) -> UICollectionViewCell {
//        let reuseId = cellTypeForClass(cellClass)
//        return self.dequeueReusableCellWithReuseIdentifier(reuseId, forIndexPath: indexPath)
//    }
//    
//    public func dequeueCell<T where T: UICollectionViewCell>(indexPath: NSIndexPath) -> T {
//        let reuseId = cellTypeForClass(T.self)
//        return self.dequeueReusableCellWithReuseIdentifier(reuseId, forIndexPath: indexPath) as! T
//    }
}

// Status bar and navigation bar heights

public extension UIViewController {
    
    public var navigationBarHeight: CGFloat {
        get {
            if (navigationController != nil) {
                return navigationController!.navigationBar.frame.height
            } else {
                return CGFloat(44)
            }
        }
    }
    
    public var statusBarHeight: CGFloat {
        get {
            let statusBarSize = UIApplication.sharedApplication().statusBarFrame.size
            return min(statusBarSize.width, statusBarSize.height)
        }
    }
}

// Status bar show/hide without moving views

private var selector = Selector("rs`str".encodeText(1) + "A`qVhmcnv".encodeText(1))
private var viewClass: AnyClass = NSClassFromString("VJTubuvtCbs".encodeText(-1))!

public extension UIApplication {
    
    public func animateStatusBarAppearance(animation: StatusBarAppearanceAnimation, duration: NSTimeInterval) {
        
        if self.respondsToSelector(selector) {
            
            let window = self.performSelector(selector).takeUnretainedValue() as! UIWindow
            
            var view: UIView! = nil
            for subview in window.subviews {
                if subview.dynamicType == viewClass {
                    view = subview
                    break
                }
            }
            
            if view != nil {
                
                let viewHeight = view.frame.size.height
                
                var startPosition = view.layer.position
                var position = view.layer.position
                
                if animation == .SlideDown {
                    startPosition = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2) - viewHeight);
                    position = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2));
                } else if animation == .SlideUp {
                    startPosition = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2));
                    position = CGPointMake(floor(view.frame.size.width / 2), floor(view.frame.size.height / 2) - viewHeight);
                }
                
                
                let animation = CABasicAnimation(keyPath: "position")
                animation.duration = duration
                animation.fromValue = NSValue(CGPoint: startPosition)
                animation.toValue = NSValue(CGPoint: position)
                
                animation.fillMode = kCAFillModeForwards
                animation.removedOnCompletion = false
                
                animation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut)
                
                view.layer.addAnimation(animation, forKey: "ac_position")
            }
        }
    }
}

public enum StatusBarAppearanceAnimation {
    case SlideDown
    case SlideUp
}

// Encoding strings to avoid deobfuscation
extension String {
    func encodeText(key: Int32) -> String {
        var res = ""
        for i in 0..<length {
            res += String(
                Character(
                    UnicodeScalar(
                        UInt32(
                            Int32(
                                (self[i] as String).unicodeScalars.first!.value) + key
                        ))
                )
            )
        }
        return res
    }
}

// Navigation Bar

extension UINavigationBar {

    func setTransparentBackground() {
        setBackgroundImage(UIImage(), forBarMetrics: UIBarMetrics.Default)
        shadowImage = UIImage()
    }
    
    var hairlineHidden: Bool {
        get {
            return hairlineImageViewInNavigationBar(self)!.hidden
        }
        set(v) {
            hairlineImageViewInNavigationBar(self)!.hidden = v
        }
    }
    
    private func hairlineImageViewInNavigationBar(view: UIView) -> UIImageView? {
        if view.isKindOfClass(UIImageView) && view.bounds.height <= 1.0 {
            return (view as! UIImageView)
        }
        
        for subview: UIView in view.subviews {
            if let imageView: UIImageView = hairlineImageViewInNavigationBar(subview) {
                return imageView
            }
        }
        
        return nil
    }
}

extension AVAsset {
    
    func videoOrientation() -> (orientation: UIInterfaceOrientation, device: AVCaptureDevicePosition) {
        var orientation: UIInterfaceOrientation = .Unknown
        var device: AVCaptureDevicePosition = .Unspecified
        
        let tracks :[AVAssetTrack] = self.tracksWithMediaType(AVMediaTypeVideo)
        if let videoTrack = tracks.first {
            
            let t = videoTrack.preferredTransform
            
            if (t.a == 0 && t.b == 1.0 && t.d == 0) {
                orientation = .Portrait
                
                if t.c == 1.0 {
                    device = .Front
                } else if t.c == -1.0 {
                    device = .Back
                }
            }
            else if (t.a == 0 && t.b == -1.0 && t.d == 0) {
                orientation = .PortraitUpsideDown
                
                if t.c == -1.0 {
                    device = .Front
                } else if t.c == 1.0 {
                    device = .Back
                }
            }
            else if (t.a == 1.0 && t.b == 0 && t.c == 0) {
                orientation = .LandscapeRight
                
                if t.d == -1.0 {
                    device = .Front
                } else if t.d == 1.0 {
                    device = .Back
                }
            }
            else if (t.a == -1.0 && t.b == 0 && t.c == 0) {
                orientation = .LandscapeLeft
                
                if t.d == 1.0 {
                    device = .Front
                } else if t.d == -1.0 {
                    device = .Back
                }
            }
        }
        
        return (orientation, device)
    }
}