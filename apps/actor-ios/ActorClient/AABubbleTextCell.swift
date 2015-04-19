
//
//  BubbleTextCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit



// MARK: -

class AABubbleTextCell : AABubbleCell {
    
    private static let bubbleFont = UIFont(name: "HelveticaNeue", size: 16)!
    private static let dateFont = UIFont(name: "HelveticaNeue-Italic", size: 11)!
    
    let textPaddingStartOutgoing: CGFloat = 11.0;
    let textPaddingEndOutgoing: CGFloat = 10.0;
    
    let textPaddingStartIncoming: CGFloat = 17.0;
    let textPaddingEndIncoming: CGFloat = 10.0;
    
    let messageText = UILabel();
    let statusView = UIImageView();
    var isOut:Bool = false
    var needRelayout = true
    var isCompact:Bool = false
    
    private let dateText = UILabel();
    private var messageState: UInt = AMMessageState.UNKNOWN.rawValue;
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        messageText.font = AABubbleTextCell.bubbleFont;
        messageText.lineBreakMode = .ByWordWrapping;
        messageText.numberOfLines = 0;
        
        dateText.font = AABubbleTextCell.dateFont;
        dateText.lineBreakMode = .ByClipping;
        dateText.numberOfLines = 1;
        dateText.contentMode = UIViewContentMode.TopLeft
        dateText.textAlignment = NSTextAlignment.Right;
        
        statusView.contentMode = UIViewContentMode.Center;
        
        contentView.addSubview(messageText);
        contentView.addSubview(dateText);
        contentView.addSubview(statusView);
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool, isPreferCompact: Bool) {
        if (!reuse) {
            needRelayout = true
            messageText.text = (message.getContent() as! AMTextContent).getText();
            isOut = message.getSenderId() == MSG.myUid();
            isCompact = isPreferCompact
            
            if (isOut) {
                bindBubbleType(.TextOut, isCompact: isPreferCompact)
                messageText.textColor = MainAppTheme.bubbles.textOut
                dateText.textColor = MainAppTheme.bubbles.textDateOut
            } else {
                bindBubbleType(.TextIn, isCompact: isPreferCompact)
                messageText.textColor = MainAppTheme.bubbles.textIn
                dateText.textColor = MainAppTheme.bubbles.textDateIn
            }
            
            if group && !isOut {
                if let user = MSG.getUsers().getWithLong(jlong(message.getSenderId())) as? AMUserVM {
                    var username = ""
                    if let uname = user.getName().get() as? String {
                        username = uname
                    }
                    senderNameLabel.text = username
                    
                    var color = Resources.placeHolderColors[Int(abs(user.getId())) % Resources.placeHolderColors.count];
                    senderNameLabel.textColor = color
                    
                    let avatar: AMAvatar? = user.getAvatar().get() as? AMAvatar
                    avatarView.bind(username, id: user.getId(), avatar: avatar)
                }
                contentView.addSubview(senderNameLabel)
                contentView.addSubview(avatarView)
            } else {
                senderNameLabel.removeFromSuperview()
                avatarView.removeFromSuperview()
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
        var content = message.getContent() as! AMTextContent!
        return round(AABubbleTextCell.measureText(content.getText(), isOut: message.getSenderId() == MSG.myUid()).height) + textPaddingTop + textPaddingBottom // 3 text top, 3 text bottom
    }
    
    class func bubbleTopPadding() -> CGFloat {
        return 1 + retinaPixel
    }
    
    class func bubbleBottomPadding() -> CGFloat {
        return 1 + retinaPixel
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
    
    override func layoutSubviews() {
        super.layoutSubviews();

        UIView.performWithoutAnimation { () -> Void in
            
            var textBounds = AABubbleTextCell.measureText(self.messageText.text!, isOut: self.isOut);
            var senderNameBounds = self.senderNameLabel.sizeThatFits(CGSize(width: CGFloat.max, height: CGFloat.max))
            
            var bubbleTopPadding = AABubbleTextCell.bubbleTopPadding()
            var bubbleBottomPadding = self.isCompact ? 0 : AABubbleTextCell.bubbleBottomPadding()
            
            var contentWidth = self.contentView.frame.width
            var contentHeight = self.contentView.frame.height
            
            var bubbleHeight = contentHeight - bubbleTopPadding - bubbleBottomPadding
            
            var contentInsetY = CGFloat((self.group ? self.groupContentInsetY : 0.0))
            var contentInsetX = CGFloat((self.group ? self.groupContentInsetX : 0.0))
            
            self.messageText.frame = textBounds;
            self.messageText.sizeToFit()
            
            var textWidth = round(textBounds.width);
            var textHeight = round(textBounds.height);
            
            if textWidth < senderNameBounds.width {
                textWidth = senderNameBounds.width + 5
            }
            
            if (self.isOut) {
                self.messageText.frame.origin = CGPoint(x: contentWidth - textWidth - self.textPaddingEndOutgoing - self.bubblePadding, y: bubbleTopPadding + AABubbleTextCell.textPaddingTop);
            } else {
                self.messageText.frame.origin = CGPoint(x: self.bubblePadding + self.textPaddingStartIncoming + contentInsetX, y: bubbleTopPadding + AABubbleTextCell.textPaddingTop + contentInsetY)
            }
            
            let x = round(self.messageText.frame.origin.x);
            let y = round(self.messageText.frame.origin.y);
            
            if (self.isOut) {
                self.bubble.frame = CGRectMake(x - self.textPaddingStartOutgoing, bubbleTopPadding, textWidth + self.textPaddingStartOutgoing + self.textPaddingEndOutgoing, bubbleHeight);
                self.dateText.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26);
            } else {
                self.bubble.frame = CGRectMake(x - self.textPaddingStartIncoming, bubbleTopPadding, textWidth + self.textPaddingStartIncoming + self.textPaddingEndIncoming, textHeight + 8 + contentInsetY);
                self.dateText.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26);
            }
            self.bubbleBorder.frame = self.bubble.frame
            
            if self.group && !self.isOut {
                let avatarSize = CGFloat(self.avatarView.frameSize)
                self.avatarView.frame = CGRect(x: 5, y: self.bubble.frame.maxY - avatarSize - 1, width: avatarSize, height: avatarSize)
                self.senderNameLabel.frame = CGRect(x: self.messageText.frame.origin.x, y: 5, width: textWidth, height: 20)
            }
            
            if (self.isOut) {
                self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26);
                self.statusView.hidden = false;
            } else {
                self.statusView.hidden = true;
            }
        }
    }
    
    // Using padding for proper date align.
    // One space + 16 non-breakable spases for out messages
    private static let stringOutPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";
    
    // One space + 6 non-breakable spaces for in messages
    private static let stringInPadding = " \u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}\u{00A0}";
    
    private static let maxTextWidth = 210
    
    private static let textPaddingTop: CGFloat = 4
    private static let textPaddingBottom: CGFloat = 5
    
    private class func measureText(message: String, isOut: Bool) -> CGRect {
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        var text = (message + (isOut ? stringOutPadding : stringInPadding)) as NSString;
        
        var size = CGSize(width: maxTextWidth, height: 0);
        var rect = text.boundingRectWithSize(size, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes: [NSFontAttributeName: bubbleFont, NSParagraphStyleAttributeName: style], context: nil);
        return CGRectMake(0, 0, round(rect.width), round(rect.height))
    }
}