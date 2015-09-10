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
    
    static let fontSize: CGFloat = isIPad ? 17 : 16
    static let fontRegular = UIFont(name: "HelveticaNeue", size: fontSize)!
    static let fontItalic = UIFont(name: "HelveticaNeue-Italic", size: fontSize)!
    static let fontBold = UIFont(name: "HelveticaNeue-Bold", size: fontSize)!
    
    static let bubbleFont = fontRegular
    static let bubbleFontUnsupported = fontItalic
    
    var text: String
    var attrText: NSAttributedString?
    var isUnsupported: Bool
    var textSizeWithPadding: CGSize
    var textSize: CGSize
    var sources = [String]()
    
    override init(message: ACMessage) {
        
        var isOut = message.isOut
        if let content = message.content as? ACTextContent {
            text = content.text
            isUnsupported = false
        } else {
            text = NSLocalizedString("UnsupportedContent", comment: "Unsupported text")
            isUnsupported = true
        }
        
        // Markdown processing
        var parser = ARMarkdownParser(int: ARMarkdownParser_MODE_LITE)
        var doc = parser.processDocumentWithNSString(text)
        
        if !doc.isTrivial() {
            var sections: [ARMDSection] = doc.getSections().toSwiftArray()
            var nAttrText = NSMutableAttributedString()
            var isFirst = true
            for s in sections {
                if !isFirst {
                    nAttrText.appendAttributedString(NSAttributedString(string: "\n"))
                }
                isFirst = false
                
                if s.getType() == ARMDSection_TYPE_CODE {
                    var attributes = [NSLinkAttributeName: NSURL(string: "source:///\(sources.count)") as! AnyObject,
                        NSFontAttributeName: TextCellLayout.fontRegular]
                    nAttrText.appendAttributedString(NSAttributedString(string: "Open Code", attributes: attributes))
                    
                    sources.append(s.getCode().getCode())
                } else if s.getType() == ARMDSection_TYPE_TEXT {
                    var child: [ARMDText] = s.getText().toSwiftArray()
                    for c in child {
                        nAttrText.appendAttributedString(TextCellLayout.buildText(c))
                    }
                } else {
                    fatalError("Unsupported section type")
                }
            }

            self.attrText = nAttrText
        }
        
        // Measure text        
        var size = CGSize(width: TextCellLayout.maxTextWidth - 2, height: 100000);
        
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        if self.attrText == nil {
            var measureText = (text + (isOut ? TextCellLayout.stringOutPadding : TextCellLayout.stringInPadding)) as NSString;
            var rect = measureText.boundingRectWithSize(size, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes: [NSFontAttributeName: isUnsupported ?  TextCellLayout.bubbleFontUnsupported : TextCellLayout.bubbleFont, NSParagraphStyleAttributeName: style], context: nil);
            textSizeWithPadding = CGSizeMake(ceil(rect.width + 2), ceil(rect.height))
            
            rect = text.boundingRectWithSize(size,
                options: NSStringDrawingOptions.UsesLineFragmentOrigin,
                attributes: [NSFontAttributeName: isUnsupported ?  TextCellLayout.bubbleFontUnsupported : TextCellLayout.bubbleFont, NSParagraphStyleAttributeName: style],
                context: nil);
            textSize = CGSizeMake(ceil(rect.width + 2), ceil(rect.height))
        } else {
            var measureText = NSMutableAttributedString()
            measureText.appendAttributedString(self.attrText!)
            if isOut {
                measureText.appendAttributedString(NSAttributedString(string: TextCellLayout.stringOutPadding, attributes: [NSFontAttributeName: TextCellLayout.fontRegular]))
            } else {
                measureText.appendAttributedString(NSAttributedString(string: TextCellLayout.stringInPadding, attributes: [NSFontAttributeName: TextCellLayout.fontRegular]))
            }
            
            var rect = measureText.boundingRectWithSize(size, options: [.UsesLineFragmentOrigin, .UsesFontLeading], context: nil)
            textSizeWithPadding = CGSizeMake(ceil(rect.width + 2), ceil(rect.height - 1))
            
            rect = self.attrText!.boundingRectWithSize(size, options: [.UsesLineFragmentOrigin, .UsesFontLeading], context: nil)
            textSize = CGSizeMake(ceil(rect.width + 2), ceil(rect.height + 8))
        }

        super.init(message: message)
        height = textSizeWithPadding.height + AABubbleCell.bubbleContentTop + AABubbleCell.bubbleContentBottom
    }
    
    class func buildText(text: ARMDText) -> NSAttributedString {
        if let raw = text as? ARMDRawText {
            return NSAttributedString(string: raw.getRawText(), attributes: [NSFontAttributeName: fontRegular])
        } else if let span = text as? ARMDSpan {
            var res = NSMutableAttributedString()
            res.beginEditing()
            
            // Processing child texts
            var child: [ARMDText] = span.getChild().toSwiftArray()
            for c in child {
                res.appendAttributedString(buildText(c))
            }
            
            // Setting span elements
            if span.getSpanType() == ARMDSpan_TYPE_BOLD {
                res.addAttribute(NSFontAttributeName, value: fontBold, range: NSMakeRange(0, res.length))
            } else if span.getSpanType() == ARMDSpan_TYPE_ITALIC {
                res.addAttribute(NSFontAttributeName, value: fontItalic, range: NSMakeRange(0, res.length))
            } else {
                fatalError("Unsupported span type")
            }
            
            res.endEditing()
            return res
        } else {
            fatalError("Unsupported text type")
        }
    }
}





