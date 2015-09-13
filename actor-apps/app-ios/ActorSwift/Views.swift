//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

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