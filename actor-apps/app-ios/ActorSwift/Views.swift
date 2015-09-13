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
    class func measureText(text: String, width: CGFloat, fontSize: CGFloat) -> CGFloat {
        let style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        let rect = text.boundingRectWithSize(CGSize(width: width - 2, height: 10000),
            options: NSStringDrawingOptions.UsesLineFragmentOrigin,
            attributes: [NSFontAttributeName: UIFont.systemFontOfSize(fontSize), NSParagraphStyleAttributeName: style],
            context: nil);
        return CGFloat(ceil(rect.height))
    }
}