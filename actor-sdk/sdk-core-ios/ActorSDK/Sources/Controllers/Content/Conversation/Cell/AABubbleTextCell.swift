//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import TTTAttributedLabel
import YYKit

public class AABubbleTextCell : AABubbleCell, TTTAttributedLabelDelegate {
    
    // TODO: Better max width calculations
    
    static let fontSize: CGFloat = AADevice.isiPad ? 17 : 16
    static let fontRegular = UIFont.textFontOfSize(fontSize)
    static let fontItalic = UIFont.italicTextFontOfSize(fontSize)
    static let fontBold = UIFont.boldTextFontOfSize(fontSize)
    
    private static let dateFont = UIFont.italicSystemFontOfSize(11)
    private static let senderFont = UIFont.boldSystemFontOfSize(15)
    
    static let bubbleFont = fontRegular
    static let bubbleFontUnsupported = fontItalic
    static let senderHeight = CGFloat(20)
    
    let messageText = TTTAttributedLabel(frame: CGRectZero)
    let statusView = UIImageView();
    let senderNameLabel = AttributedLabel();
    var needRelayout = true
    var isClanchTop:Bool = false
    var isClanchBottom:Bool = false
    
    private let dateText = AttributedLabel()
    private var messageState = ACMessageState.UNKNOWN().ordinal()
    private var cellLayout: TextCellLayout!
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        senderNameLabel.font = AABubbleTextCell.senderFont
        
        messageText.font = AABubbleTextCell.bubbleFont
        messageText.lineBreakMode = .ByWordWrapping
        messageText.numberOfLines = 0
        messageText.userInteractionEnabled = true
        messageText.delegate = self
        messageText.linkAttributes = [kCTForegroundColorAttributeName: appStyle.chatUrlColor,
            kCTUnderlineStyleAttributeName: NSNumber(bool: false)]
        messageText.activeLinkAttributes = [kCTForegroundColorAttributeName: appStyle.chatUrlColor,
            kCTUnderlineStyleAttributeName: NSNumber(bool: false)]
        messageText.verticalAlignment = TTTAttributedLabelVerticalAlignment.Center
        
        
        dateText.font = AABubbleTextCell.dateFont
        dateText.lineBreakMode = .ByClipping
        dateText.numberOfLines = 1
        dateText.contentMode = UIViewContentMode.TopLeft
        dateText.contentAlignment = .Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        contentView.addSubview(messageText)
        contentView.addSubview(dateText)
        contentView.addSubview(statusView)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Data binding
    
    public override func bind(message: ACMessage, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
        // Saving cell settings
        self.cellLayout = cellLayout as! TextCellLayout
        self.isClanchTop = setting.clenchTop
        self.isClanchBottom = setting.clenchBottom
        
        if (!reuse) {
            
            // When not reusing force to relayout view
            needRelayout = true

            // Setting text's font size and color
            messageText.textColor = self.cellLayout.textColor
            
            if self.cellLayout.text != nil {

                // Setting text
                messageText.text = self.cellLayout.text
            } else {
                
                // Setting attributed text
                messageText.setText(self.cellLayout.attrText)
            }
            
            // Setting sender name if needed
            if isGroup && !isOut {

                let user = Actor.getUserWithUid(message.senderId)
                    
                let colors = ActorSDK.sharedActor().style.nameColors
                    
                if user.isBot() && user.getNameModel().get() == "Bot" {
                    let group = Actor.getGroupWithGid(self.peer.peerId)
                    senderNameLabel.text = group.getNameModel().get()
                    senderNameLabel.textColor = colors[Int(abs(group.getId())) % colors.count]
                
                } else {
                    senderNameLabel.text = user.getNameModel().get()
                    senderNameLabel.textColor = colors[Int(abs(user.getId())) % colors.count]
                }
                
                contentView.addSubview(senderNameLabel)
            } else {
                senderNameLabel.removeFromSuperview()
            }
        }
        
        // Always update bubble insets
        if (isOut) {
            bindBubbleType(.TextOut, isCompact: isClanchBottom)
            dateText.textColor = appStyle.chatTextDateOutColor
            
            bubbleInsets = UIEdgeInsets(
                top: (isClanchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop),
                left: 0 + (AADevice.isiPad ? 16 : 0),
                bottom: (isClanchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom),
                right: (isClanchBottom ? 10 : 4) + (AADevice.isiPad ? 16 : 0))
            contentInsets = UIEdgeInsets(
                top: AABubbleCell.bubbleContentTop,
                left: 10,
                bottom: AABubbleCell.bubbleContentBottom,
                right: (isClanchBottom ? 4 : 10))
        } else {
            bindBubbleType(.TextIn, isCompact: isClanchBottom)
            dateText.textColor = appStyle.chatTextDateInColor
            
            bubbleInsets = UIEdgeInsets(
                top: (isClanchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop),
                left: (isClanchBottom ? 10 : 4) + (AADevice.isiPad ? 16 : 0),
                bottom: (isClanchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom),
                right: 0 + (AADevice.isiPad ? 16 : 0))
            contentInsets = UIEdgeInsets(
                top: (isGroup ? 18 : 0) + AABubbleCell.bubbleContentTop,
                left: (isClanchBottom ? 11 : 17),
                bottom: AABubbleCell.bubbleContentBottom,
                right: 10)
        }

        
        // Always update date and state
        dateText.text = cellLayout.date
        messageState = message.messageState.ordinal();
        
        if (isOut) {
            switch(self.messageState) {
            case ACMessageState.PENDING().ordinal():
                self.statusView.image = appStyle.chatIconClock;
                self.statusView.tintColor = appStyle.chatStatusSending
                break;
            case ACMessageState.SENT().ordinal():
                self.statusView.image = appStyle.chatIconCheck1;
                self.statusView.tintColor = appStyle.chatStatusSent
                break;
            case ACMessageState.RECEIVED().ordinal():
                self.statusView.image = appStyle.chatIconCheck2;
                self.statusView.tintColor = appStyle.chatStatusReceived
                break;
            case ACMessageState.READ().ordinal():
                self.statusView.image = appStyle.chatIconCheck2;
                self.statusView.tintColor = appStyle.chatStatusRead
                break;
            case ACMessageState.ERROR().ordinal():
                self.statusView.image = appStyle.chatIconError;
                self.statusView.tintColor = appStyle.chatStatusError
                break
            default:
                self.statusView.image = appStyle.chatIconClock;
                self.statusView.tintColor = appStyle.chatStatusSending
                break;
            }
        }
    }
    
    // Menu for Text cell
    
    public override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        if action == "copy:" {
            if (bindedMessage!.content is ACTextContent) {
                return true
            }
        }
        if action == "delete:" {
            return true
        }
        return false
    }
    
    public override func copy(sender: AnyObject?) {
        UIPasteboard.generalPasteboard().string = (bindedMessage!.content as! ACTextContent).text
    }
    
    // Url open handling
    
    public func attributedLabel(label: TTTAttributedLabel!, didLongPressLinkWithURL url: NSURL!, atPoint point: CGPoint) {
        let actionSheet: UIAlertController = UIAlertController(title: nil, message: url.absoluteString, preferredStyle: .ActionSheet)
        actionSheet.addAction(UIAlertAction(title: AALocalized("ActionOpenLink"), style: .Default, handler: { action in
            self.openUrl(url)
        }))
        actionSheet.addAction(UIAlertAction(title: AALocalized("ActionCopyLink"), style: .Default, handler: { action in
            UIPasteboard.generalPasteboard().string = url.absoluteString
            self.controller.alertUser("AlertLinkCopied")
        }))
        actionSheet.addAction(UIAlertAction(title: AALocalized("ActionCancel"), style: .Cancel, handler:nil))
        self.controller.presentViewController(actionSheet, animated: true, completion: nil)
    }
    
    public func attributedLabel(label: TTTAttributedLabel!, didSelectLinkWithURL url: NSURL!) {
        openUrl(url)
    }
    
    public func openUrl(url: NSURL) {
        if url.scheme == "source" {
            let path = url.path!
            let index = Int(path.substringFromIndex(path.startIndex.advancedBy(1)))!
            let code = self.cellLayout.sources[index]
            self.controller.navigateNext(AACodePreviewController(code: code), removeCurrent: false)
        } else {
            ActorSDK.sharedActor().openUrl(url.absoluteString)
        }
    }
    
    // Layouting
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        // Convenience
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        // Measure Text
        let senderNameBounds = self.senderNameLabel.sizeThatFits(CGSize(width: CGFloat.max, height: CGFloat.max))
        
        self.messageText.frame = CGRectMake(0, 0, self.cellLayout.textSize.width, self.cellLayout.textSize.height)
        
        var textWidth = round(self.cellLayout.textSizeWithPadding.width)
        let textHeight = round(self.cellLayout.textSizeWithPadding.height)
        
        if textWidth < senderNameBounds.width {
            textWidth = senderNameBounds.width + 5
        }
        
        // Layout elements
        let topPadding : CGFloat = self.cellLayout.attrText != nil ? -0.5 : 0
        if (self.isOut) {
            self.messageText.frame.origin = CGPoint(x: contentWidth - textWidth - insets.right, y: insets.top + topPadding)
            self.dateText.frame = CGRectMake(contentWidth - insets.right - 70, textHeight + insets.top - 20, 46, 26)
            self.statusView.frame = CGRectMake(contentWidth - insets.right - 24, textHeight + insets.top - 20, 20, 26)
            self.statusView.hidden = false
        } else {
            self.messageText.frame.origin = CGPoint(x: insets.left, y: insets.top + topPadding)
            self.dateText.frame = CGRectMake(insets.left + textWidth - 47, textHeight + insets.top - 20, 46, 26)
            self.statusView.hidden = true
        }
        
        if self.isGroup && !self.isOut {
            self.senderNameLabel.frame = CGRect(x: insets.left, y: insets.top - 18, width: textWidth, height: 20)
        }

        layoutBubble(textWidth, contentHeight: textHeight)
    }
}

/**
    Text cell layout
*/
public class TextCellLayout: AACellLayout {
    
    private class func maxTextWidth(isOut: Bool, peer: ACPeer) -> CGFloat {
        if AADevice.isiPad {
            return 400
        } else {
            if peer.isGroup {
                if isOut {
                    return UIScreen.mainScreen().bounds.width - 110
                } else {
                    return UIScreen.mainScreen().bounds.width - 90
                }
            } else {
                return UIScreen.mainScreen().bounds.width - 40
            }
        }
    }
    
    private static let textKey = "text"
    private static let unsupportedKey = "unsupported"
    
    private static let stringOutPadding = " " + ("_".repeatString(7));
    private static let stringInPadding = " " + ("_".repeatString(4));
    private static let parser = ARMarkdownParser(int: ARMarkdownParser_MODE_FULL)
    
    var text: String?
    var attrText: NSAttributedString?
    var textColor: UIColor
    
    var isUnsupported: Bool = false
    var textSizeWithPadding: CGSize
    var textSize: CGSize
    var sources = [String]()
    
    /**
        Plain text layout
    */
    public init(text: String, textColor: UIColor, date: Int64, isOut: Bool, peer: ACPeer, layoutKey: String = TextCellLayout.textKey) {
        
        // Setting text
        self.text = text
        self.textColor = textColor
        
        // Calculating maximum text width
        let maxTextWidth = TextCellLayout.maxTextWidth(isOut, peer: peer)
        
        // Building padded text to make place for date and status checkmark
        let paddedText = (text + (isOut ? TextCellLayout.stringOutPadding : TextCellLayout.stringInPadding))
        
        // Measuring text and padded text heights
        textSize = UIViewMeasure.measureText(text, width: maxTextWidth, font: AABubbleTextCell.bubbleFont)
        textSizeWithPadding = UIViewMeasure.measureText(paddedText, width: maxTextWidth, font: AABubbleTextCell.bubbleFont)
        
        // Calculating bubble height
        var height = textSizeWithPadding.height + AABubbleCell.bubbleContentTop + AABubbleCell.bubbleContentBottom
        
        if peer.isGroup && !isOut {
            height += AABubbleTextCell.senderHeight
        }
        
        // Creating layout
        super.init(height: height, date: date, key: layoutKey)
    }
    
    /**
        NSAttributedString layout
    */
    public init(attributedText: NSAttributedString, textColor: UIColor, date: Int64, isOut: Bool, peer: ACPeer, layoutKey: String = TextCellLayout.textKey) {
        
        // Setting attributed text
        self.attrText = attributedText
        self.textColor = textColor
        self.isUnsupported = false
        
        // Calculating maximum text width
        let maxTextWidth = TextCellLayout.maxTextWidth(isOut, peer: peer)
        
        // Building padded text
        let paddedText = attributedText.append(isOut ? TextCellLayout.stringOutPadding : TextCellLayout.stringInPadding, font: AABubbleTextCell.bubbleFont)
        
        // Measuring text and padded text heights
        textSize = UIViewMeasure.measureText(attributedText, width: maxTextWidth)
        textSizeWithPadding = UIViewMeasure.measureText(paddedText, width: maxTextWidth)
        
        // Calculating bubble height
        var height = textSizeWithPadding.height + AABubbleCell.bubbleContentTop + AABubbleCell.bubbleContentBottom
        
        if peer.isGroup && !isOut {
            height += AABubbleTextCell.senderHeight
        }
        
        // Creating layout
        super.init(height: height, date: date, key: layoutKey)
    }
    
    /**
        Formatted text layout. Automatically parse text and detect formatting.
    */
    public convenience init(formattedText: String, textColor: UIColor, date: Int64, isOut: Bool, peer: ACPeer, layoutKey: String = TextCellLayout.textKey) {
        
        // Parsing markdown formatted text
        let text = TextCellLayout.parser.parse(formattedText, textColor: textColor, fontSize: AABubbleTextCell.fontSize)
        
        // If text is trivial don't use NSAttributedText
        if text.isTrivial {

            // Creating simple text layout
            self.init(text: formattedText, textColor: textColor, date: date, isOut: isOut, peer: peer, layoutKey: layoutKey)
            
        } else {
            
            // Creating attributed text layout
            self.init(attributedText: text.attributedText, textColor: textColor, date: date, isOut: isOut, peer: peer, layoutKey: layoutKey)
            
            // Setting source code references
            self.sources = text.code
        }
    }

    /**
        Creating text layout from message and peer
    */
    public convenience init(message: ACMessage, peer: ACPeer) {
        let style = ActorSDK.sharedActor().style
        
        if let content = message.content as? ACTextContent {
            
            // Creating generic layout
            self.init(
                formattedText: content.text,
                textColor: message.isOut ? style.chatTextOutColor : style.chatTextInColor,
                date: Int64(message.date),
                isOut: message.isOut,
                peer: peer,
                layoutKey: TextCellLayout.textKey
            )
        } else {
            
            // Creating unsupported layout
            let unsupportedText = AALocalized("UnsupportedContent")
            
            self.init(
                formattedText: "_\(unsupportedText)_",
                textColor: message.isOut ? style.chatTextOutUnsupportedColor : style.chatTextInUnsupportedColor,
                date: Int64(message.date),
                isOut: message.isOut,
                peer: peer,
                layoutKey: TextCellLayout.unsupportedKey
            )
        }
    }
}

/**
    Text cell layouter
*/
public class AABubbleTextCellLayouter: AABubbleLayouter {
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return TextCellLayout(message: message, peer: peer)
    }
    
    public func isSuitable(message: ACMessage) -> Bool {
        return message.content is ACTextContent
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleTextCell.self
    }
}
