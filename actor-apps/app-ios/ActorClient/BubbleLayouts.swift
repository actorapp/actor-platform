//
//  Layouts.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 26.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class MessagesLayouting {
    
    class func measureHeight(message: AMMessage, group: Bool, setting: CellSetting, layoutCache: LayoutCache) -> CGFloat {
        var content = message.getContent()!
        
        var layout = layoutCache.pick(message.getRid())
        if (layout == nil) {
            // Usually never happens
            layout = buildLayout(message, layoutCache: layoutCache)
            layoutCache.cache(message.getRid(), layout: layout!)
        }

        var height = layout!.height
        if content is AMServiceContent {
            height += AABubbleCell.bubbleTop
            height += AABubbleCell.bubbleBottom
        } else {
            height += (setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop)
            height += (setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom)
        }
        
        // Sender name
        let isIn = message.getSenderId() != MSG.myUid()
        if group && isIn && !(content is AMServiceContent) && !(content is AMPhotoContent) && !(content is AMDocumentContent) {
            height += CGFloat(20.0)
        }
        
        // Date separator
        if (setting.showDate) {
            height += AABubbleCell.dateSize
        }
        
        // New message separator
        if (setting.showNewMessages) {
            height += AABubbleCell.newMessageSize
        }
        
        
        return height
    }
    
    class func buildLayout(message: AMMessage, layoutCache: LayoutCache) -> CellLayout {

        var content = message.getContent()!
        
        var res: CellLayout
        if (content is AMTextContent) {
            res = TextCellLayout(message: message)
        } else if (content is AMPhotoContent) {
            res = CellLayout(message: message)
            res.height = AABubbleMediaCell.measureMediaHeight(message)
        } else if (content is AMServiceContent) {
            res = CellLayout(message: message)
            res.height = AABubbleServiceCell.measureServiceHeight(message)
        } else if (content is AMDocumentContent) {
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
    let showNewMessages: Bool
    let showDate: Bool
    let clenchTop: Bool
    let clenchBottom: Bool
    
    init(showDate: Bool, clenchTop: Bool, clenchBottom: Bool, showNewMessages: Bool) {
        self.showDate = showDate
        self.clenchTop = clenchTop
        self.clenchBottom = clenchBottom
        self.showNewMessages = showNewMessages
    }
}

class CellLayout {
    var height: CGFloat = 0
    var date: String
    
    init(message: AMMessage) {
        self.date = CellLayout.formatDate(Int64(message.getDate()))
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
    
    override init(message: AMMessage) {
        
        if let content = message.getContent() as? AMTextContent {
            text = content.getText()
            isUnsupported = false
        } else {
            text = NSLocalizedString("UnsupportedContent", comment: "Unsupported text")
            isUnsupported = true
        }

        // Measure text
        var measureText = (text + (message.getSenderId() == MSG.myUid() ? TextCellLayout.stringOutPadding : TextCellLayout.stringInPadding)) as NSString;
        
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





