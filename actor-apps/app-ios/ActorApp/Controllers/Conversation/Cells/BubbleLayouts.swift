//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class MessagesLayouting {
    
    class func measureHeight(message: ACMessage, group: Bool, setting: CellSetting, layoutCache: LayoutCache) -> CGFloat {
        var content = message.content!
        
        var layout = layoutCache.pick(message.rid)
        if (layout == nil) {
            // Usually never happens
            layout = buildLayout(message, layoutCache: layoutCache)
            layoutCache.cache(message.rid, layout: layout!)
        }

        var height = layout!.height
        if content is ACServiceContent {
            height += AABubbleCell.bubbleTop
            height += AABubbleCell.bubbleBottom
        } else {
            height += (setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop)
            height += (setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom)
        }
        
        // Sender name
        let isIn = message.senderId != Actor.myUid()
        if group && isIn && !(content is ACServiceContent) && !(content is ACPhotoContent) && !(content is ACDocumentContent) {
            height += CGFloat(20.0)
        }
        
        // Date separator
        if (setting.showDate) {
            height += AABubbleCell.dateSize
        }
        
        // New message separator
//        if (setting.showNewMessages) {
//            height += AABubbleCell.newMessageSize
//        }
        
        
        return height
    }
    
    class func buildLayout(message: ACMessage, layoutCache: LayoutCache) -> CellLayout {

        var content = message.content!
        
        var res: CellLayout
        if (content is ACTextContent) {
            res = TextCellLayout(message: message)
        } else if (content is ACPhotoContent) {
            res = CellLayout(message: message)
            res.height = AABubbleMediaCell.measureMediaHeight(message)
        } else if (content is ACServiceContent) {
            res = CellLayout(message: message)
            res.height = AABubbleServiceCell.measureServiceHeight(message)
        } else if (content is ACDocumentContent) {
            res = CellLayout(message: message)
            res.height = AABubbleDocumentCell.measureDocumentHeight(message)
        } else {
            // Unsupported
            res = TextCellLayout(message: message)
        }
        
        return res
    }
}

class CellSetting {
    let showDate: Bool
    let clenchTop: Bool
    let clenchBottom: Bool
    
    init(showDate: Bool, clenchTop: Bool, clenchBottom: Bool) {
        self.showDate = showDate
        self.clenchTop = clenchTop
        self.clenchBottom = clenchBottom
    }
}

class CellLayout {
    var height: CGFloat = 0
    var date: String
    
    init(message: ACMessage) {
        self.date = CellLayout.formatDate(Int64(message.date))
    }
    
    class func formatDate(date: Int64) -> String {
        var dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "HH:mm"
        return dateFormatter.stringFromDate(NSDate(timeIntervalSince1970: NSTimeInterval(Double(date) / 1000.0)))
    }
}

class TextCellLayout: CellLayout {
    
    private static let stringOutPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";
    private static let stringInPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";
    private static let maxTextWidth = isIPad ? 400 : 210
    
    static let bubbleFont = isIPad ? UIFont(name: "HelveticaNeue", size: 17)! : UIFont(name: "HelveticaNeue", size: 16)!
    static let bubbleFontUnsupported = isIPad ? UIFont(name: "HelveticaNeue-Italic", size: 17)! : UIFont(name: "HelveticaNeue-Italic", size: 16)!
    
    var text: String
    var isUnsupported: Bool
    var textSizeWithPadding: CGSize
    var textSize: CGSize
    
    override init(message: ACMessage) {
        
        if let content = message.content as? ACTextContent {
            text = content.text
            isUnsupported = false
        } else {
            text = NSLocalizedString("UnsupportedContent", comment: "Unsupported text")
            isUnsupported = true
        }

        // Measure text
        var measureText = (text + (message.senderId == Actor.myUid() ? TextCellLayout.stringOutPadding : TextCellLayout.stringInPadding)) as NSString;
        
        var size = CGSize(width: TextCellLayout.maxTextWidth, height: 0);
        
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        var rect = measureText.boundingRectWithSize(size,
            options: NSStringDrawingOptions.UsesLineFragmentOrigin,
            attributes: [NSFontAttributeName: isUnsupported ?  TextCellLayout.bubbleFontUnsupported : TextCellLayout.bubbleFont, NSParagraphStyleAttributeName: style],
            context: nil);
        textSizeWithPadding = CGSizeMake(round(rect.width), round(rect.height))
        
        rect = text.boundingRectWithSize(size,
            options: NSStringDrawingOptions.UsesLineFragmentOrigin,
            attributes: [NSFontAttributeName: isUnsupported ?  TextCellLayout.bubbleFontUnsupported : TextCellLayout.bubbleFont, NSParagraphStyleAttributeName: style],
            context: nil);
        textSize = CGSizeMake(round(rect.width), round(rect.height))

        super.init(message: message)
        height = textSizeWithPadding.height + AABubbleCell.bubbleContentTop + AABubbleCell.bubbleContentBottom
    }
}





