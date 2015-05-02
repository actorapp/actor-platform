//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class AABubbleTextCell : AABubbleCell {
    
    private static let bubbleFont = UIFont(name: "HelveticaNeue", size: 16)!
    private static let bubbleFontUnsupported = UIFont(name: "HelveticaNeue-Italic", size: 16)!
    private static let dateFont = UIFont(name: "HelveticaNeue-Italic", size: 11)!
    
    let messageText = UILabel();
    let statusView = UIImageView();
    let senderNameLabel = UILabel();
    var needRelayout = true
    var isCompact:Bool = false
    
    private let dateText = UILabel();
    private var messageState: UInt = AMMessageState.UNKNOWN.rawValue;
    
    init(reuseId: String, peer: AMPeer) {
        super.init(reuseId: reuseId, peer: peer, isFullSize: false)
        
        senderNameLabel.font = UIFont(name: "HelveticaNeue-Medium", size: 15)!
        
        messageText.lineBreakMode = .ByWordWrapping
        messageText.numberOfLines = 0
        
        dateText.font = AABubbleTextCell.dateFont
        dateText.lineBreakMode = .ByClipping
        dateText.numberOfLines = 1
        dateText.contentMode = UIViewContentMode.TopLeft
        dateText.textAlignment = NSTextAlignment.Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        contentView.addSubview(messageText)
        contentView.addSubview(dateText)
        contentView.addSubview(statusView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool, isPreferCompact: Bool) {
        if (!reuse) {
            needRelayout = true
            if let content = message.getContent() as? AMTextContent {
                messageText.text = content.getText();
                messageText.font = AABubbleTextCell.bubbleFont
                if (isOut) {
                    messageText.textColor = MainAppTheme.bubbles.textOut
                } else {
                    messageText.textColor = MainAppTheme.bubbles.textIn
                }
            } else {
                messageText.text = NSLocalizedString("UnsupportedContent", comment: "Unsupported text")
                messageText.font = AABubbleTextCell.bubbleFontUnsupported
                if (isOut) {
                    messageText.textColor = MainAppTheme.bubbles.textUnsupportedOut
                } else {
                    messageText.textColor = MainAppTheme.bubbles.textUnsupportedIn
                }
            }
            isCompact = isPreferCompact
            
            if (isOut) {
                bindBubbleType(.TextOut, isCompact: isPreferCompact)
                dateText.textColor = MainAppTheme.bubbles.textDateOut

                bubbleInsets = UIEdgeInsets(
                    top: (isPreferCompact ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop),
                    left: 0,
                    bottom: (isPreferCompact ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom),
                    right: isPreferCompact ? 10 : 4)
                contentInsets = UIEdgeInsets(
                    top: AABubbleCell.bubbleContentTop,
                    left: 10,
                    bottom: AABubbleCell.bubbleContentBottom,
                    right: (isPreferCompact ? 4 : 10))
            } else {
                bindBubbleType(.TextIn, isCompact: isPreferCompact)
                dateText.textColor = MainAppTheme.bubbles.textDateIn
                
                bubbleInsets = UIEdgeInsets(
                    top: (isPreferCompact ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop),
                    left: isPreferCompact ? 10 : 4,
                    bottom: (isPreferCompact ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom),
                    right: 0)
                contentInsets = UIEdgeInsets(
                    top: (isGroup ? 18 : 0) + AABubbleCell.bubbleContentTop,
                    left: isPreferCompact ? 11 : 17,
                    bottom: AABubbleCell.bubbleContentBottom,
                    right: 10)
            }
            
            if isGroup && !isOut {
                if let user = MSG.getUsers().getWithLong(jlong(message.getSenderId())) as? AMUserVM {
                    var username = ""
                    if let uname = user.getName().get() as? String {
                        username = uname
                    }
                    senderNameLabel.text = username
                    
                    var color = Resources.placeHolderColors[Int(abs(user.getId())) % Resources.placeHolderColors.count];
                    senderNameLabel.textColor = color
                }
                contentView.addSubview(senderNameLabel)

            } else {
                senderNameLabel.removeFromSuperview()
            }
        }
        
        // Always update date and state
        dateText.text = formatDate(message.getDate());
        messageState = UInt(message.getMessageState().ordinal());
        
        if (isOut) {
            switch(self.messageState) {
            case AMMessageState.PENDING.rawValue:
                self.statusView.image = Resources.iconClock;
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break;
            case AMMessageState.SENT.rawValue:
                self.statusView.image = Resources.iconCheck1;
                self.statusView.tintColor = MainAppTheme.bubbles.statusSent
                break;
            case AMMessageState.RECEIVED.rawValue:
                self.statusView.image = Resources.iconCheck2;
                self.statusView.tintColor = MainAppTheme.bubbles.statusReceived
                break;
            case AMMessageState.READ.rawValue:
                self.statusView.image = Resources.iconCheck2;
                self.statusView.tintColor = MainAppTheme.bubbles.statusRead
                break;
            case AMMessageState.ERROR.rawValue:
                self.statusView.image = Resources.iconError;
                self.statusView.tintColor = MainAppTheme.bubbles.statusError
                break
            default:
                self.statusView.image = Resources.iconClock;
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break;
            }
        }
    }
    
    // MARK: -
    // MARK: Getters
    
    class func measureTextHeight(message: AMMessage, isPreferCompact: Bool) -> CGFloat {
        var messageContent = ""
        if let content = message.getContent() as? AMTextContent {
            messageContent = content.getText()
        } else {
            messageContent = NSLocalizedString("UnsupportedContent", comment: "Unsupported text")
        }
        let contentHeight = AABubbleTextCell.measureText(messageContent, isOut: message.getSenderId() == MSG.myUid()).height
        
        if (isPreferCompact) {
            return contentHeight + bubbleBottomCompact + bubbleContentBottom + bubbleContentTop + bubbleTopCompact
        } else {
            return contentHeight + bubbleBottom + bubbleContentBottom + bubbleContentTop + bubbleTop
        }
    }
    
    // MARK: -
    // MARK: MenuController
    
    override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        if NSStringFromSelector(action) == "copy:" {
            return true
        }
        return false
    }
    
    override func copy(sender: AnyObject?) {
        UIPasteboard.generalPasteboard().string = messageText.text
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        // Convenience
        var insets = fullContentInsets
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        // Measure Text
        var textBounds = AABubbleTextCell.measureText(self.messageText.text!, isOut: self.isOut);
        var senderNameBounds = self.senderNameLabel.sizeThatFits(CGSize(width: CGFloat.max, height: CGFloat.max))
        
        self.messageText.frame = textBounds
        self.messageText.sizeToFit()
        
        var textWidth = round(textBounds.width)
        var textHeight = round(textBounds.height)
        
        if textWidth < senderNameBounds.width {
            textWidth = senderNameBounds.width + 5
        }
        
        // Layout elements
        if (self.isOut) {
            self.messageText.frame.origin = CGPoint(x: contentWidth - textWidth - insets.right, y: insets.top)
            self.dateText.frame = CGRectMake(contentWidth - insets.right - 70, textHeight + insets.top - 20, 46, 26)
            self.statusView.frame = CGRectMake(contentWidth - insets.right - 24, textHeight + insets.top - 20, 20, 26)
            self.statusView.hidden = false
        } else {
            self.messageText.frame.origin = CGPoint(x: insets.left, y: insets.top)
            self.dateText.frame = CGRectMake(insets.left + textWidth - 47, textHeight + insets.top - 20, 46, 26)
            self.statusView.hidden = true
        }
        
        if self.isGroup && !self.isOut {
            self.senderNameLabel.frame = CGRect(x: insets.left, y: insets.top - 18, width: textWidth, height: 20)
        }

        layoutBubble(textWidth, contentHeight: textHeight)
    }
    
    // Using padding for proper date align.
    // One space + 16 non-breakable spases for out messages
    private static let stringOutPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";
    
    // One space + 6 non-breakable spaces for in messages
    private static let stringInPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";
    
    private static let maxTextWidth = 210
    
    
    private class func measureText(message: String, isOut: Bool) -> CGRect {
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        var text = (message + (isOut ? stringOutPadding : stringInPadding)) as NSString;
        
        var size = CGSize(width: maxTextWidth, height: 0);
        var rect = text.boundingRectWithSize(size, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes: [NSFontAttributeName: bubbleFont, NSParagraphStyleAttributeName: style], context: nil);
        return CGRectMake(0, 0, round(rect.width), round(rect.height))
    }
}