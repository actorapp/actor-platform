//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import YYImage

public class AABubbleTextCell : AABubbleCell {
    
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
    
    private let messageText = YYLabel()
    private let senderNameLabel = YYLabel()
    private let dateText = YYLabel()
    private let statusView = UIImageView()
    
    private var needRelayout = true
    private var isClanchTop:Bool = false
    private var isClanchBottom:Bool = false
    
    private var dateWidth: CGFloat = 0
    
    private var cellLayout: TextCellLayout!
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        messageText.displaysAsynchronously = true
        messageText.ignoreCommonProperties = true
        messageText.fadeOnAsynchronouslyDisplay = true
        messageText.clearContentsBeforeAsynchronouslyDisplay = true
        
        messageText.highlightTapAction = { (containerView: UIView, text: NSAttributedString, range: NSRange, rect: CGRect) -> () in
            let attributes = text.attributesAtIndex(range.location, effectiveRange: nil)
            if let attrs = attributes["YYTextHighlight"] as? YYTextHighlight {
                if let url = attrs.userInfo!["url"] as? String {
                    self.openUrl(NSURL(string: url)!)
                }
            }
        }
        
        messageText.highlightLongPressAction = { (containerView: UIView, text: NSAttributedString, range: NSRange, rect: CGRect) -> () in
            self.bubble
            let attributes = text.attributesAtIndex(range.location, effectiveRange: nil)
            if let attrs = attributes["YYTextHighlight"] as? YYTextHighlight {
                if let url = attrs.userInfo!["url"] as? String {
                    self.urlLongTap(NSURL(string: url)!)
                }
            }
        }

        senderNameLabel.displaysAsynchronously = true
        senderNameLabel.ignoreCommonProperties = true
        senderNameLabel.fadeOnAsynchronouslyDisplay = true
        senderNameLabel.clearContentsBeforeAsynchronouslyDisplay = true
        
        
        dateText.displaysAsynchronously = true
        dateText.ignoreCommonProperties = true
        // dateText.fadeOnAsynchronouslyDisplay = false
        // dateText.clearContentsBeforeAsynchronouslyDisplay = true
        
//        dateText.font = AABubbleTextCell.dateFont
//        dateText.lineBreakMode = .ByClipping
//        dateText.numberOfLines = 1
//        dateText.textAlignment = .Right
        
        statusView.contentMode = UIViewContentMode.Center
        
        contentView.addSubview(messageText)
        contentView.addSubview(dateText)
        contentView.addSubview(statusView)
        contentView.addSubview(senderNameLabel)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Data binding
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
        // Saving cell settings
        self.cellLayout = cellLayout as! TextCellLayout
        self.isClanchTop = setting.clenchTop
        self.isClanchBottom = setting.clenchBottom
        
        if (!reuse) {
            
            // When not reusing force to relayout view
            needRelayout = true

            // Text Layout
            messageText.textLayout = self.cellLayout.textLayout
            
            // Setting sender name if needed
            if isGroup && !isOut {
                senderNameLabel.hidden = false
                senderNameLabel.textLayout = self.cellLayout.senderLayout
            } else {
                senderNameLabel.hidden = true
                senderNameLabel.textLayout = nil
            }
        }
        
        // Always update bubble insets
        if (isOut) {
            bindBubbleType(.TextOut, isCompact: isClanchBottom)
            
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
            // dateText.textColor = appStyle.chatTextDateInColor
            
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

        dateText.textLayout = self.cellLayout.dateLayout
        dateWidth = self.cellLayout.dateWidth!
        
        if (isOut) {
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusSent
                }
                break
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusError
                break
            case .PENDING:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            }
        }
    }
    
    // Menu for Text cell
    
    public override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        if action == #selector(NSObject.copy(_:)) {
            if (bindedMessage!.content is ACTextContent) {
                return true
            }
        }
        if action == #selector(NSObject.delete(_:)) {
            return true
        }
        return false
    }
    
    public override func copy(sender: AnyObject?) {
        UIPasteboard.generalPasteboard().string = (bindedMessage!.content as! ACTextContent).text
    }
    
    public func urlLongTap(url: NSURL) {
        if url.scheme != "source" && url.scheme == "send" {
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
    }
    
    public func openUrl(url: NSURL) {
        if url.scheme == "source" {
            let path = url.path!
            let index = Int(path.substringFromIndex(path.startIndex.advancedBy(1)))!
            let code = self.cellLayout.sources[index]
            self.controller.navigateNext(AACodePreviewController(code: code), removeCurrent: false)
        } else if url.scheme == "send" {
            Actor.sendMessageWithPeer(self.peer, withText: url.absoluteString.skip(5))
        } else {
            ActorSDK.sharedActor().openUrl(url.absoluteString)
        }
    }
    
    // Layouting
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        
        // Convenience
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        let textSize = self.cellLayout.textLayout.textBoundingSize
        let bubbleWidth = round(self.cellLayout.bubbleSize.width)
        let bubbleHeight = round(self.cellLayout.bubbleSize.height)
        
        self.messageText.frame = CGRectMake(0, 0, textSize.width, textSize.height)

        // Layout elements
        if (self.isOut) {
            self.messageText.frame.origin = CGPoint(x: contentWidth - bubbleWidth - insets.right, y: insets.top /*+ topPadding*/)
            self.dateText.frame = CGRectMake(contentWidth - insets.right - 70 + 46 - dateWidth, bubbleHeight + insets.top - 20, dateWidth, 26)
            self.statusView.frame = CGRectMake(contentWidth - insets.right - 24, bubbleHeight + insets.top - 20, 20, 26)
            self.statusView.hidden = false
        } else {
            self.messageText.frame.origin = CGPoint(x: insets.left, y: insets.top/* + topPadding*/)
            self.dateText.frame = CGRectMake(insets.left + bubbleWidth - 47 + 46 - dateWidth, bubbleHeight + insets.top - 20, dateWidth, 26)
            self.statusView.hidden = true
        }
        
        if self.isGroup && !self.isOut {
            self.senderNameLabel.frame = CGRect(x: insets.left, y: insets.top - 18, width: contentWidth, height: 20)
        }

        layoutBubble(bubbleWidth, contentHeight: bubbleHeight)
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
    
    private class func timeWidth(isOut: Bool) -> CGFloat {
        if isOut {
            return 60
        } else {
            return 36
        }
    }
    
    private static let textKey = "text"
    private static let unsupportedKey = "unsupported"
    
    private static let stringOutPadding = " " + ("_".repeatString(7));
    private static let stringInPadding = " " + ("_".repeatString(4));
    private static let parser = ARMarkdownParser(int: ARMarkdownParser_MODE_FULL)
    
    var text: String
    var attrText: NSAttributedString
    var textLayout: YYTextLayout
    var senderLayout: YYTextLayout?
    var dateLayout: YYTextLayout?
    var dateWidth: CGFloat?
    
    var isUnsupported: Bool = false
    var bubbleSize: CGSize
    var sources = [String]()
    
    /**
     NSAttributedString layout
     */
    public init(senderId: Int, text: String, attributedText: NSAttributedString, date: Int64, isOut: Bool, peer: ACPeer, layoutKey: String = TextCellLayout.textKey, layouter: AABubbleLayouter) {
        
        // Setting attributed text
        self.text = text
        self.attrText = attributedText
        self.isUnsupported = false
        
        // Calculating maximum text width
        let maxTextWidth = TextCellLayout.maxTextWidth(isOut, peer: peer)
        let timeWidth = TextCellLayout.timeWidth(isOut)
        
        let container = YYTextContainer(size: CGSizeMake(maxTextWidth, CGFloat.max))
        
        textLayout = YYTextLayout(container: container, text: attributedText)!
        
        // print("Text Layouted")
        
        // Measuring text and padded text heights
        let textSize = textLayout.textBoundingSize
        
        if textLayout.lines.count == 1 {
            if textLayout.textBoundingSize.width < maxTextWidth - timeWidth {
                //
                // <line_0> <date>
                //
                bubbleSize = CGSize(width: textSize.width + timeWidth, height: textSize.height)
            } else {
                
                //
                // <line_________0>
                //           <date>
                //
                bubbleSize = CGSize(width: textSize.width, height: textSize.height + 16)
            }
        } else {
            let maxWidth = textSize.width
            let lastLine = textLayout.lines.last!.width
            if lastLine + timeWidth < maxWidth {
                //
                // <line_________0>
                // <line_________1>
                // ..
                // <line_n>  <date>
                //
                bubbleSize = textSize
            } else if lastLine + timeWidth < maxTextWidth {
                //
                // |------------------|
                // <line______0>
                // <line______1>
                // ..
                // <line______n> <date>
                //
                bubbleSize = CGSize(width: max(lastLine + timeWidth, maxWidth), height: textSize.height)
            } else {
                //
                // <line_________0>
                // <line_________1>
                // ..
                // <line_________n>
                //           <date>
                //
                bubbleSize = CGSize(width: max(timeWidth, maxWidth), height: textSize.height + 16)
            }
        }
        
        // Date Layouting
        if isOut {
            let attrDate = NSMutableAttributedString(string: AACellLayout.formatDate(date))
            attrDate.yy_font = AABubbleTextCell.dateFont
            attrDate.yy_color = ActorSDK.sharedActor().style.chatTextDateOutColor
            dateLayout = YYTextLayout(containerSize: CGSizeMake(timeWidth, CGFloat.max), text: attrDate)
            dateWidth = dateLayout?.textBoundingSize.width
        } else {
            let attrDate = NSMutableAttributedString(string: AACellLayout.formatDate(date))
            attrDate.yy_font = AABubbleTextCell.dateFont
            attrDate.yy_color = ActorSDK.sharedActor().style.chatTextDateInColor
            dateLayout = YYTextLayout(containerSize: CGSizeMake(timeWidth, CGFloat.max), text: attrDate)
            dateWidth = dateLayout?.textBoundingSize.width
        }
        
        // Calculating bubble height
        var height = bubbleSize.height + AABubbleCell.bubbleContentTop + AABubbleCell.bubbleContentBottom
        
        if peer.isGroup && !isOut {
            
            // Getting Name of sender
            let sender = Actor.getUserWithUid(jint(senderId))
            let colors = ActorSDK.sharedActor().style.nameColors
            var senderName: String
            var color: UIColor
            if sender.isBot() && sender.getNameModel().get() == "Bot" {
                senderName = Actor.getGroupWithGid(peer.peerId).getNameModel().get()
                color = colors[Int(abs(peer.peerId)) % colors.count]
            } else {
                senderName = sender.getNameModel().get()
                color = colors[Int(abs(senderId)) % colors.count]
            }
            
            // Building Layout
            let attributedSender = NSMutableAttributedString(string: senderName)
            let range = NSRange(location: 0, length: senderName.length)
            attributedSender.yy_setFont(AABubbleTextCell.senderFont, range: range)
            attributedSender.yy_setColor(color, range: range)
            senderLayout = YYTextLayout(container: container, text: attributedSender)!
            
            // Fixing too small width
            let senderWidth = senderLayout!.textBoundingSize.width
            if bubbleSize.width < senderWidth + 5 {
                bubbleSize = CGSize(width: senderWidth, height: bubbleSize.height)
            }
            
            height += AABubbleTextCell.senderHeight
        }
        
        // Creating layout
        super.init(height: height, date: date, key: layoutKey, layouter: layouter)
    }
    
    /**
        Formatted text layout. Automatically parse text and detect formatting.
    */
    public convenience init(senderId: Int, formattedText: String, textColor: UIColor, date: Int64, isOut: Bool, peer: ACPeer, layoutKey: String = TextCellLayout.textKey, layouter: AABubbleLayouter) {
        
        // Parsing markdown formatted text
        let parser = TextParser(textColor: textColor, linkColor: ActorSDK.sharedActor().style.chatUrlColor, fontSize: AABubbleTextCell.fontSize)        
        let text = parser.parse(formattedText)
        
        // Creating attributed text layout
        self.init(senderId: senderId, text: formattedText, attributedText: text.attributedText, date: date, isOut: isOut, peer: peer, layoutKey: layoutKey, layouter: layouter)
        
        // Setting source code references
        self.sources = text.code
    }

    /**
        Creating text layout from message and peer
    */
    public convenience init(message: ACMessage, peer: ACPeer, layouter: AABubbleLayouter) {
        let style = ActorSDK.sharedActor().style
        
        if let content = message.content as? ACTextContent {
            
            // Creating generic layout
            self.init(
                senderId: Int(message.senderId),
                formattedText: content.text,
                textColor: message.isOut ? style.chatTextOutColor : style.chatTextInColor,
                date: Int64(message.date),
                isOut: message.isOut,
                peer: peer,
                layoutKey: TextCellLayout.textKey,
                layouter: layouter
            )
        } else {
            
            // Creating unsupported layout
            let unsupportedText = AALocalized("UnsupportedContent")
            
            self.init(
                senderId: Int(message.senderId),
                formattedText: "_\(unsupportedText)_",
                textColor: message.isOut ? style.chatTextOutUnsupportedColor : style.chatTextInUnsupportedColor,
                date: Int64(message.date),
                isOut: message.isOut,
                peer: peer,
                layoutKey: TextCellLayout.unsupportedKey,
                layouter: layouter
            )
        }
    }
}

/**
    Text cell layouter
*/
public class AABubbleTextCellLayouter: AABubbleLayouter {
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return TextCellLayout(message: message, peer: peer, layouter: self)
    }
    
    public func isSuitable(message: ACMessage) -> Bool {
        return message.content is ACTextContent
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleTextCell.self
    }
}
