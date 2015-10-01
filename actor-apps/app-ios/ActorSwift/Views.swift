//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

private var targetReference = "target"
extension UITapGestureRecognizer {
    convenience init(closure: ()->()){
        let target = ClosureTarget(closure: closure)
        self.init(target: target, action: "invoke")
        setAssociatedObject(self, value: target, associativeKey: &targetReference)
    }
}

extension UIView {
    var viewDidTap: (()->())? {
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

extension UIView {
    func hideView() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 0
        })
    }
    
    func showView() {
        UIView.animateWithDuration(0.2, animations: { () -> Void in
            self.alpha = 1
        })
    }
    
    var height: CGFloat {
        get {
            return self.bounds.height
        }
    }
    
    var width: CGFloat {
        get {
            return self.bounds.width
        }
    }
    
    var left: CGFloat {
        get {
            return self.frame.minX
        }
    }
    
    var right: CGFloat {
        get {
            return self.frame.maxX
        }
    }

    var top: CGFloat {
        get {
            return self.frame.minY
        }
    }
    
    var bottom: CGFloat {
        get {
            return self.frame.maxY
        }
    }
    
}

class UIViewMeasure {
    
    class func measureText(text: String, width: CGFloat, fontSize: CGFloat) -> CGSize {
       return UIViewMeasure.measureText(text, width: width, font: UIFont.systemFontOfSize(fontSize))
    }
    
    class func measureText(text: String, width: CGFloat, font: UIFont) -> CGSize {
        
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
    
    class func measureText(attributedText: NSAttributedString, width: CGFloat) -> CGSize {
        
        // Measuring text with reduced width
        let rect = attributedText.boundingRectWithSize(CGSize(width: width - 2, height: CGFloat.max), options: [.UsesLineFragmentOrigin, .UsesFontLeading], context: nil)
        
        // Returning size with expanded width and height
        return CGSizeMake(ceil(rect.width + 2), CGFloat(ceil(rect.height)))
    }
}

private var registeredCells = "cells!"

extension UITableView {
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
    
    func dequeueCell(cellClass: AnyClass, indexPath: NSIndexPath) -> UITableViewCell {
        let reuseId = cellTypeForClass(cellClass)
        return self.dequeueReusableCellWithIdentifier(reuseId, forIndexPath: indexPath)
    }
    
    func dequeueCell<T where T: UITableViewCell>(indexPath: NSIndexPath) -> T {
        let reuseId = cellTypeForClass(T.self)
        return self.dequeueReusableCellWithIdentifier(reuseId, forIndexPath: indexPath) as! T
    }
}