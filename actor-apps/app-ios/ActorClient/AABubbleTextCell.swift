//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class AABubbleTextCell : AABubbleCell, TTTAttributedLabelDelegate {
    
    private static let dateFont = UIFont(name: "HelveticaNeue-Italic", size: 11)!
    
    let messageText = UILabel()
    let statusView = UIImageView();
    let senderNameLabel = UILabel();
    var needRelayout = true
    var isClanchTop:Bool = false
    var isClanchBottom:Bool = false
    
    private let dateText = UILabel()
    private var messageState: UInt = AMMessageState.UNKNOWN.rawValue
    private var cellLayout: TextCellLayout!
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        senderNameLabel.font = UIFont(name: "HelveticaNeue-Medium", size: 15)!
        
        messageText.lineBreakMode = .ByWordWrapping
        messageText.numberOfLines = 0
        // messageText.userInteractionEnabled = true
        
        dateText.font = AABubbleTextCell.dateFont
        dateText.lineBreakMode = .ByClipping
        dateText.numberOfLines = 1
        dateText.contentMode = UIViewContentMode.TopLeft
        dateText.textAlignment = NSTextAlignment.Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        mainView.addSubview(messageText)
        mainView.addSubview(dateText)
        mainView.addSubview(statusView)
        
//        bubble.addGestureRecognizer(UILongPressGestureRecognizer(target: self, action: "textDidLongPress"))
//        bubble.userInteractionEnabled = true
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        if action == "copy:" {
            if (bindedMessage!.getContent() is AMTextContent) {
                return true
            }
        }
        if action == "delete:" {
            return true
        }
        return false
    }
    
    override func copy(sender: AnyObject?) {
        UIPasteboard.generalPasteboard().string = (bindedMessage!.getContent() as! AMTextContent).getText()
    }
    
//    func textDidLongPress() {
//        self.becomeFirstResponder()
//        var menuController = UIMenuController.sharedMenuController()
//        menuController.setTargetRect(bubble.frame, inView:bubble)
//        menuController.menuItems = [UIMenuItem(title: "Copy", action: "copy")]
//        menuController.setMenuVisible(true, animated: true)
//    }
//    
    func attributedLabel(label: TTTAttributedLabel!, didLongPressLinkWithURL url: NSURL!, atPoint point: CGPoint) {
        UIApplication.sharedApplication().openURL(url)
    }
    
    func attributedLabel(label: TTTAttributedLabel!, didSelectLinkWithURL url: NSURL!) {
        UIApplication.sharedApplication().openURL(url)
    }
    
    override func bind(message: AMMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        self.cellLayout = cellLayout as! TextCellLayout
        isClanchTop = setting.clenchTop
        isClanchBottom = setting.clenchBottom
        
        if (!reuse) {
            needRelayout = true
            
            messageText.text = self.cellLayout.text
            
            if self.cellLayout.isUnsupported {
                messageText.font = TextCellLayout.bubbleFontUnsupported
                if (isOut) {
                    messageText.textColor = MainAppTheme.bubbles.textUnsupportedOut
                } else {
                    messageText.textColor = MainAppTheme.bubbles.textUnsupportedIn
                }

            } else {
                messageText.font = TextCellLayout.bubbleFont
                if (isOut) {
                    messageText.textColor = MainAppTheme.bubbles.textOut
                } else {
                    messageText.textColor = MainAppTheme.bubbles.textIn
                }
            }
            
            
            if isGroup && !isOut {
                if let user = MSG.getUserWithUid(message.getSenderId()) {
                    senderNameLabel.text = user.getNameModel().get()
                    var color = Resources.placeHolderColors[Int(abs(user.getId())) % Resources.placeHolderColors.count];
                    senderNameLabel.textColor = color
                }
                mainView.addSubview(senderNameLabel)
            } else {
                senderNameLabel.removeFromSuperview()
            }
        }
        
        // Always update bubble insets
        if (isOut) {
            bindBubbleType(.TextOut, isCompact: isClanchBottom)
            dateText.textColor = MainAppTheme.bubbles.textDateOut
            
            bubbleInsets = UIEdgeInsets(
                top: (isClanchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop),
                left: 0 + (isIPad ? 16 : 0),
                bottom: (isClanchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom),
                right: (isClanchBottom ? 10 : 4) + (isIPad ? 16 : 0))
            contentInsets = UIEdgeInsets(
                top: AABubbleCell.bubbleContentTop,
                left: 10,
                bottom: AABubbleCell.bubbleContentBottom,
                right: (isClanchBottom ? 4 : 10))
        } else {
            bindBubbleType(.TextIn, isCompact: isClanchBottom)
            dateText.textColor = MainAppTheme.bubbles.textDateIn
            
            bubbleInsets = UIEdgeInsets(
                top: (isClanchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop),
                left: (isClanchBottom ? 10 : 4) + (isIPad ? 16 : 0),
                bottom: (isClanchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom),
                right: 0 + (isIPad ? 16 : 0))
            contentInsets = UIEdgeInsets(
                top: (isGroup ? 18 : 0) + AABubbleCell.bubbleContentTop,
                left: (isClanchBottom ? 11 : 17),
                bottom: AABubbleCell.bubbleContentBottom,
                right: 10)
        }

        
        // Always update date and state
        dateText.text = cellLayout.date
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
        
        setNeedsLayout()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        // Convenience
        var insets = fullContentInsets
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        // Measure Text
        var senderNameBounds = self.senderNameLabel.sizeThatFits(CGSize(width: CGFloat.max, height: CGFloat.max))
        
        self.messageText.frame = CGRectMake(0, 0, self.cellLayout.textSizeWithPadding.width, self.cellLayout.textSizeWithPadding.height)
        self.messageText.sizeToFit()
        
        var textWidth = round(self.cellLayout.textSizeWithPadding.width)
        var textHeight = round(self.cellLayout.textSizeWithPadding.height)
        
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
}