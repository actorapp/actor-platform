//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import AVFoundation

// Closure based tap on views

private var targetReference = "target"
public extension UITapGestureRecognizer {
    public convenience init(closure: @escaping ()->()){
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
                self.isUserInteractionEnabled = true
            }
        }
        get {
            return nil
        }
    }
}

private class ClosureTarget {
    
    fileprivate let closure: ()->()
    
    init(closure: @escaping ()->()) {
        self.closure = closure
    }
    
    @objc func invoke() {
        closure()
    }
}

// View Hide/Show and size extensions

public extension UIView {
    
    public func hideView() {
        self.isHidden = true
//        UIView.animateWithDuration(0.2, animations: { () -> Void in
//            self.alpha = 0
//        })
    }
    
    public func showView() {
        self.isHidden = false
//        UIView.animateWithDuration(0.2, animations: { () -> Void in
//            self.alpha = 1
//        })
    }
    
    public func showViewAnimated() {
        UIView.animate(withDuration: 0.2, animations: { () -> Void in
            self.alpha = 1
        })
    }
    
    public func hideViewAnimated() {
        UIView.animate(withDuration: 0.2, animations: { () -> Void in
            self.alpha = 0
        })
    }
    
    public func showViewPop() {
        UIView.animate(withDuration: 1, delay: 0, usingSpringWithDamping: 0.7,
                                   initialSpringVelocity: 0.6, options: .curveEaseOut, animations: { () -> Void in
            self.transform = CGAffineTransform.identity;
            self.alpha = 1
        }, completion: nil)
    }
    
    public func hideViewPop() {
        UIView.animate(withDuration: 1, delay: 0, usingSpringWithDamping: 0.7,
                                   initialSpringVelocity: 1.0,options: .curveEaseOut,animations: { () -> Void in
            self.transform = CGAffineTransform(scaleX: 0.01, y: 0.01)
            self.alpha = 0
        }, completion: nil)
    }
    
//    public var height: CGFloat { get { return bounds.height } }
//    public var width: CGFloat { get { return bounds.width } }
//    
//    public var left: CGFloat { get { return frame.minX } }
//    public var right: CGFloat { get { return frame.maxX } }
//    public var top: CGFloat { get { return frame.minY } }
//    public var bottom: CGFloat { get { return frame.maxY } }
    
    public func centerIn(_ rect: CGRect) {
        self.frame = CGRect(x: rect.origin.x + (rect.width - self.bounds.width) / 2, y: rect.origin.y + (rect.height - self.bounds.height) / 2,
            width: self.bounds.width, height: self.bounds.height)
    }
    
    public func under(_ rect: CGRect, offset: CGFloat) {
        self.frame = CGRect(x: rect.origin.x + (rect.width - self.bounds.width) / 2, y: rect.origin.y + rect.height + offset,
            width: self.bounds.width, height: self.bounds.height)
    }
    
    public func topIn(_ rect: CGRect) {
        self.frame = CGRect(x: rect.origin.x + (rect.width - self.bounds.width) / 2, y: rect.origin.y,
            width: self.bounds.width, height: self.bounds.height)
    }
}

// Text measuring

open class UIViewMeasure {
    
    open class func measureText(_ text: String, width: CGFloat, fontSize: CGFloat) -> CGSize {
        return UIViewMeasure.measureText(text, width: width, font: UIFont.systemFont(ofSize: fontSize))
    }
    
    open class func measureText(_ text: String, width: CGFloat, font: UIFont) -> CGSize {
        
        // Building paragraph styles
        let style = NSMutableParagraphStyle()
        style.lineBreakMode = NSLineBreakMode.byWordWrapping
        
        // Measuring text with reduced width
        let rect = text.boundingRect(with: CGSize(width: width - 2, height: CGFloat.greatestFiniteMagnitude),
            options: NSStringDrawingOptions.usesLineFragmentOrigin,
            attributes: [NSFontAttributeName: font, NSParagraphStyleAttributeName: style],
            context: nil)
        
        // Returning size with expanded width
        return CGSize(width: ceil(rect.width + 2), height: CGFloat(ceil(rect.height)))
    }
    
    open class func measureText(_ attributedText: NSAttributedString, width: CGFloat) -> CGSize {
        
        // Measuring text with reduced width
        let rect = attributedText.boundingRect(with: CGSize(width: width - 2, height: CGFloat.greatestFiniteMagnitude), options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
        
        // Returning size with expanded width and height
        return CGSize(width: ceil(rect.width + 2), height: CGFloat(ceil(rect.height)))
    }
}

// Loading cells without explict registration

private var registeredCells = "cells!"

public extension UITableView {
    fileprivate func cellTypeForClass(_ cellClass: AnyClass) -> String {
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
            register(cellClass, forCellReuseIdentifier: cellReuseId)
        }
        
        return cellReuseId
    }
    
    public func dequeueCell(_ cellClass: AnyClass, indexPath: IndexPath) -> UITableViewCell {
        let reuseId = cellTypeForClass(cellClass)
        return self.dequeueReusableCell(withIdentifier: reuseId, for: indexPath)
    }
    
    public func dequeueCell<T>(_ indexPath: IndexPath) -> T where T: UITableViewCell {
        let reuseId = cellTypeForClass(T.self)
        return self.dequeueReusableCell(withIdentifier: reuseId, for: indexPath) as! T
    }
    
    public func visibleCellForIndexPath(_ path: IndexPath) -> UITableViewCell? {
        if indexPathsForVisibleRows == nil {
            return nil
        }
        
        for i in 0..<indexPathsForVisibleRows!.count {
            let vPath = indexPathsForVisibleRows![i]
            if (vPath as NSIndexPath).row == (path as NSIndexPath).row && (vPath as NSIndexPath).section == (path as NSIndexPath).section {
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
            let statusBarSize = UIApplication.shared.statusBarFrame.size
            return min(statusBarSize.width, statusBarSize.height)
        }
    }
}

// Status bar show/hide without moving views

private var selector = Selector("rs`str".encodeText(1) + "A`qVhmcnv".encodeText(1))
private var viewClass: AnyClass = NSClassFromString("VJTubuvtCbs".encodeText(-1))!

public extension UIApplication {
    
    public func animateStatusBarAppearance(_ animation: StatusBarAppearanceAnimation, duration: TimeInterval) {
        
        if self.responds(to: selector) {
            
            let window = self.perform(selector).takeUnretainedValue() as! UIWindow
            
            var view: UIView! = nil
            for subview in window.subviews {
                if type(of: subview) == viewClass {
                    view = subview
                    break
                }
            }
            
            if view != nil {
                
                let viewHeight = view.frame.size.height
                
                var startPosition = view.layer.position
                var position = view.layer.position
                
                if animation == .slideDown {
                    startPosition = CGPoint(x: floor(view.frame.size.width / 2), y: floor(view.frame.size.height / 2) - viewHeight);
                    position = CGPoint(x: floor(view.frame.size.width / 2), y: floor(view.frame.size.height / 2));
                } else if animation == .slideUp {
                    startPosition = CGPoint(x: floor(view.frame.size.width / 2), y: floor(view.frame.size.height / 2));
                    position = CGPoint(x: floor(view.frame.size.width / 2), y: floor(view.frame.size.height / 2) - viewHeight);
                }
                
                
                let animation = CABasicAnimation(keyPath: "position")
                animation.duration = duration
                animation.fromValue = NSValue(cgPoint: startPosition)
                animation.toValue = NSValue(cgPoint: position)
                
                animation.fillMode = kCAFillModeForwards
                animation.isRemovedOnCompletion = false
                
                animation.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut)
                
                view.layer.add(animation, forKey: "ac_position")
            }
        }
    }
}

public enum StatusBarAppearanceAnimation {
    case slideDown
    case slideUp
}

// Encoding strings to avoid deobfuscation
extension String {
    func encodeText(_ key: Int32) -> String {
        var res = ""
        for i in 0..<length {
            res += String(
                Character(
                    UnicodeScalar(
                        UInt32(
                            Int32(
                                (self[i] as String).unicodeScalars.first!.value) + key
                        ))!
                )
            )
        }
        return res
    }
}

// Navigation Bar

extension UINavigationBar {

    func setTransparentBackground() {
        setBackgroundImage(UIImage(), for: UIBarMetrics.default)
        shadowImage = UIImage()
    }
    
    var hairlineHidden: Bool {
        get {
            return hairlineImageViewInNavigationBar(self)!.isHidden
        }
        set(v) {
            hairlineImageViewInNavigationBar(self)!.isHidden = v
        }
    }
    
    fileprivate func hairlineImageViewInNavigationBar(_ view: UIView) -> UIImageView? {
        if view.isKind(of: UIImageView.self) && view.bounds.height <= 1.0 {
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
        var orientation: UIInterfaceOrientation = .unknown
        var device: AVCaptureDevicePosition = .unspecified
        
        let tracks :[AVAssetTrack] = self.tracks(withMediaType: AVMediaTypeVideo)
        if let videoTrack = tracks.first {
            
            let t = videoTrack.preferredTransform
            
            if (t.a == 0 && t.b == 1.0 && t.d == 0) {
                orientation = .portrait
                
                if t.c == 1.0 {
                    device = .front
                } else if t.c == -1.0 {
                    device = .back
                }
            }
            else if (t.a == 0 && t.b == -1.0 && t.d == 0) {
                orientation = .portraitUpsideDown
                
                if t.c == -1.0 {
                    device = .front
                } else if t.c == 1.0 {
                    device = .back
                }
            }
            else if (t.a == 1.0 && t.b == 0 && t.c == 0) {
                orientation = .landscapeRight
                
                if t.d == -1.0 {
                    device = .front
                } else if t.d == 1.0 {
                    device = .back
                }
            }
            else if (t.a == -1.0 && t.b == 0 && t.c == 0) {
                orientation = .landscapeLeft
                
                if t.d == 1.0 {
                    device = .front
                } else if t.d == -1.0 {
                    device = .back
                }
            }
        }
        
        return (orientation, device)
    }
}
