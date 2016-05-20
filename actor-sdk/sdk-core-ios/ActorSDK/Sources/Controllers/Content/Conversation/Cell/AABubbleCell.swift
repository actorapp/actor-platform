//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

/**
    Bubble types
*/
public enum BubbleType {
    // Outcome text bubble
    case TextOut
    // Income text bubble
    case TextIn
    // Outcome media bubble
    case MediaOut
    // Income media bubble
    case MediaIn
    // Service bubble
    case Service
    // Sticker bubble
    case Sticker
}

/**
    Root class for bubble layouter. Used for preprocessing bubble layout in background.
*/
public protocol AABubbleLayouter  {
    
    func isSuitable(message: ACMessage) -> Bool
    
    func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout
    
    func cellClass() -> AnyClass
}

extension AABubbleLayouter {
    func cellReuseId() -> String {
        return "cell_\(cellClass())"
    }
}

/**
    Root class for bubble cells
*/
public class AABubbleCell: UICollectionViewCell {
    
    public static let bubbleContentTop: CGFloat = 6
    public static let bubbleContentBottom: CGFloat = 6
    public static let bubbleTop: CGFloat = 3
    public static let bubbleTopCompact: CGFloat = 1
    public static let bubbleBottom: CGFloat = 3
    public static let bubbleBottomCompact: CGFloat = 1
    public static let avatarPadding: CGFloat = 39
    public static let dateSize: CGFloat = 30
    public static let newMessageSize: CGFloat = 30
    
    //
    // Cached text bubble images
    //
    
    private static var cachedOutTextBg = UIImage.tinted("BubbleOutgoingFull", color: ActorSDK.sharedActor().style.chatTextBubbleOutColor)
    private static var cachedOutTextBgBorder = UIImage.tinted("BubbleOutgoingFullBorder", color: ActorSDK.sharedActor().style.chatTextBubbleOutBorderColor)
    private static var cachedOutTextCompactBg = UIImage.tinted("BubbleOutgoingPartial", color: ActorSDK.sharedActor().style.chatTextBubbleOutColor)
    private static var cachedOutTextCompactSelectedBg = UIImage.tinted("BubbleOutgoingPartial", color: ActorSDK.sharedActor().style.chatTextBubbleOutSelectedColor)
    private static var cachedOutTextCompactBgBorder = UIImage.tinted("BubbleOutgoingPartialBorder", color: ActorSDK.sharedActor().style.chatTextBubbleOutBorderColor)

    private static var cachedInTextBg = UIImage.tinted("BubbleIncomingFull", color: ActorSDK.sharedActor().style.chatTextBubbleInColor)
    private static var cachedInTextBgBorder = UIImage.tinted("BubbleIncomingFullBorder", color: ActorSDK.sharedActor().style.chatTextBubbleInBorderColor)
    private static var cachedInTextCompactBg = UIImage.tinted("BubbleIncomingPartial", color: ActorSDK.sharedActor().style.chatTextBubbleInColor)
    private static var cachedInTextCompactSelectedBg = UIImage.tinted("BubbleIncomingPartial", color: ActorSDK.sharedActor().style.chatTextBubbleInSelectedColor)
    private static var cachedInTextCompactBgBorder = UIImage.tinted("BubbleIncomingPartialBorder", color: ActorSDK.sharedActor().style.chatTextBubbleInBorderColor)
    
    //
    // Cached media bubble images
    //
    
    private static let cachedMediaBg = UIImage.tinted("BubbleOutgoingPartial", color: ActorSDK.sharedActor().style.chatMediaBubbleColor)
    private static var cachedMediaBgBorder = UIImage.tinted("BubbleOutgoingPartialBorder", color: ActorSDK.sharedActor().style.chatMediaBubbleBorderColor)

    //
    // Cached Service bubble images
    //
    
    private static var cachedServiceBg:UIImage = Imaging.roundedImage(ActorSDK.sharedActor().style.chatServiceBubbleColor, size: CGSizeMake(18, 18), radius: 9)

    //
    // Cached Date bubble images
    //
    
//    private static var dateBgImage = Imaging.roundedImage(ActorSDK.sharedActor().style.chatDateBubbleColor, size: CGSizeMake(18, 18), radius: 9)
    
    private static var dateBgImage = ActorSDK.sharedActor().style.statusBackgroundImage
    
    // MARK: -
    // MARK: Public vars
    
    // Views
    public let avatarView = AAAvatarView()
    public var avatarAdded: Bool = false
    
    public let bubble = UIImageView()
    public let bubbleBorder = UIImageView()
    
    private let dateText = UILabel()
    private let dateBg = UIImageView()
    
    private let newMessage = UILabel()
    
    // Layout
    public var contentInsets : UIEdgeInsets = UIEdgeInsets()
    public var bubbleInsets : UIEdgeInsets = UIEdgeInsets()
    public var fullContentInsets : UIEdgeInsets {
        get {
            return UIEdgeInsets(
                top: contentInsets.top + bubbleInsets.top + (isShowDate ? AABubbleCell.dateSize : 0) + (isShowNewMessages ? AABubbleCell.newMessageSize : 0),
                left: contentInsets.left + bubbleInsets.left + (isGroup && !isOut ? AABubbleCell.avatarPadding : 0),
                bottom: contentInsets.bottom + bubbleInsets.bottom,
                right: contentInsets.right + bubbleInsets.right)
        }
    }
    public var needLayout: Bool = true
    
    public let groupContentInsetY = 20.0
    public let groupContentInsetX = 40.0
    public var bubbleVerticalSpacing: CGFloat = 6.0
    public let bubblePadding: CGFloat = 6;
    public let bubbleMediaPadding: CGFloat = 10;
    
    // Binded data
    public var peer: ACPeer!
    public weak var controller: AAConversationContentController!
    public var isGroup: Bool = false
    public var isFullSize: Bool!
    public var bindedSetting: AACellSetting?
    
    public var bindedMessage: ACMessage? = nil
    public var bubbleType:BubbleType? = nil
    public var isOut: Bool = false
    public var isShowDate: Bool = false
    public var isShowNewMessages: Bool = false
    
    var appStyle: ActorStyle {
        get {
            return ActorSDK.sharedActor().style
        }
    }
    
    // MARK: -
    // MARK: Constructors

    public init(frame: CGRect, isFullSize: Bool) {
        super.init(frame: frame)
        
        self.isFullSize = isFullSize
  
        dateBg.image = AABubbleCell.dateBgImage
        dateText.font = UIFont.mediumSystemFontOfSize(12)
        dateText.textColor = appStyle.chatDateTextColor
        dateText.contentMode = UIViewContentMode.Center
        dateText.textAlignment = NSTextAlignment.Center
        
        newMessage.font = UIFont.mediumSystemFontOfSize(14)
        newMessage.textColor = appStyle.chatUnreadTextColor
        newMessage.contentMode = UIViewContentMode.Center
        newMessage.textAlignment = NSTextAlignment.Center
        newMessage.backgroundColor = appStyle.chatUnreadBgColor
        newMessage.text = AALocalized("ChatNewMessages")
        
        //"New Messages"
        
        contentView.transform = CGAffineTransformMake(1, 0, 0, -1, 0, 0)
        
        contentView.addSubview(bubble)
        contentView.addSubview(bubbleBorder)
        contentView.addSubview(newMessage)
        contentView.addSubview(dateBg)
        contentView.addSubview(dateText)
        
        avatarView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleCell.avatarDidTap)))
        
        avatarView.userInteractionEnabled = true
        
        backgroundColor = UIColor.clearColor()
        
        // Speed up animations
        self.layer.speed = 1.5
        
        //self.layer.shouldRasterize = true
        //self.layer.rasterizationScale = UIScreen.mainScreen().scale
        //self.layer.drawsAsynchronously = true
        //self.contentView.layer.drawsAsynchronously = true
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setConfig(peer: ACPeer, controller: AAConversationContentController) {
        self.peer = peer
        self.controller = controller
        if (peer.isGroup && !isFullSize) {
            self.isGroup = true
        }
    }
    
    public override func canBecomeFirstResponder() -> Bool {
        return false
    }

    public override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        if action == #selector(NSObject.delete(_:)) {
            return true
        }
        return false
    }
    
    public override func delete(sender: AnyObject?) {
        let rids = IOSLongArray(length: 1)
        rids.replaceLongAtIndex(0, withLong: bindedMessage!.rid)
        Actor.deleteMessagesWithPeer(self.peer, withRids: rids)
    }
    
    func avatarDidTap() {
        if bindedMessage != nil {
            controller.onBubbleAvatarTap(self.avatarView, uid: bindedMessage!.senderId)
        }
    }
    
    public func performBind(message: ACMessage, receiveDate: jlong, readDate: jlong, setting: AACellSetting, isShowNewMessages: Bool, layout: AACellLayout) {
        
        var reuse = false
        if (bindedMessage != nil && bindedMessage?.rid == message.rid) {
            reuse = true
        }
        isOut = message.senderId == Actor.myUid();
        bindedMessage = message
        self.isShowNewMessages = isShowNewMessages
        if !reuse && !isFullSize {
            if (!isOut && isGroup) {
                let user = Actor.getUserWithUid(message.senderId)
                        
                // Small hack for replacing senter name and title
                // with current group title
                if user.isBot() && user.getNameModel().get() == "Bot" {
                    let group = Actor.getGroupWithGid(self.peer.peerId)
                    let avatar: ACAvatar? = group.getAvatarModel().get()
                    let name = group.getNameModel().get()
                    avatarView.bind(name, id: Int(user.getId()), avatar: avatar)
                } else {
                    let avatar: ACAvatar? = user.getAvatarModel().get()
                    let name = user.getNameModel().get()
                    avatarView.bind(name, id: Int(user.getId()), avatar: avatar)
                }
                if !avatarAdded {
                    contentView.addSubview(avatarView)
                    avatarAdded = true
                }
            } else {
                if avatarAdded {
                    avatarView.removeFromSuperview()
                    avatarAdded = false
                }
            }
        }
        
        self.isShowDate = setting.showDate
        if (isShowDate) {
            self.dateText.text = layout.date
        }
        
        self.bindedSetting = setting
        
        bind(message, receiveDate: receiveDate, readDate: readDate, reuse: reuse, cellLayout: layout, setting: setting)
        
        if (!reuse) {
            needLayout = true
            super.setNeedsLayout()
        }
    }
    
    public func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        fatalError("bind(message:) has not been implemented")
    }
    
    public func bindBubbleType(type: BubbleType, isCompact: Bool) {
        self.bubbleType = type
        
        // Update Bubble background images
        switch(type) {
            case BubbleType.TextIn:
                if (isCompact) {
                    bubble.image = AABubbleCell.cachedInTextCompactBg
                    bubbleBorder.image = AABubbleCell.cachedInTextCompactBgBorder
                    bubble.highlightedImage = AABubbleCell.cachedInTextCompactSelectedBg
                    bubbleBorder.highlightedImage = AABubbleCell.cachedInTextCompactBgBorder
                } else {
                    bubble.image = AABubbleCell.cachedInTextBg
                    bubbleBorder.image = AABubbleCell.cachedInTextBgBorder
                    bubble.highlightedImage = AABubbleCell.cachedInTextBg
                    bubbleBorder.highlightedImage = AABubbleCell.cachedInTextBgBorder
                }
            break
            case BubbleType.TextOut:
                if (isCompact) {
                    bubble.image =  AABubbleCell.cachedOutTextCompactBg
                    bubbleBorder.image =  AABubbleCell.cachedOutTextCompactBgBorder
                    bubble.highlightedImage =  AABubbleCell.cachedOutTextCompactSelectedBg
                    bubbleBorder.highlightedImage =  AABubbleCell.cachedOutTextCompactBgBorder
                } else {
                    bubble.image =  AABubbleCell.cachedOutTextBg
                    bubbleBorder.image =  AABubbleCell.cachedOutTextBgBorder
                    bubble.highlightedImage =  AABubbleCell.cachedOutTextBg
                    bubbleBorder.highlightedImage =  AABubbleCell.cachedOutTextBgBorder
                }
            break
            case BubbleType.MediaIn:
                bubble.image =  AABubbleCell.cachedMediaBg
                bubbleBorder.image =  AABubbleCell.cachedMediaBgBorder
                bubble.highlightedImage =  AABubbleCell.cachedMediaBg
                bubbleBorder.highlightedImage =  AABubbleCell.cachedMediaBgBorder
            break
            case BubbleType.MediaOut:
                bubble.image =  AABubbleCell.cachedMediaBg
                bubbleBorder.image =  AABubbleCell.cachedMediaBgBorder
                bubble.highlightedImage =  AABubbleCell.cachedMediaBg
                bubbleBorder.highlightedImage =  AABubbleCell.cachedMediaBgBorder
            break
            case BubbleType.Service:
                bubble.image = AABubbleCell.cachedServiceBg
                bubbleBorder.image = nil
                bubble.highlightedImage = AABubbleCell.cachedServiceBg
                bubbleBorder.highlightedImage = nil
            break
            case BubbleType.Sticker:
                bubble.image = nil;
                bubbleBorder.image = nil
                bubble.highlightedImage = nil;
                bubbleBorder.highlightedImage = nil
            break
        }
    }
    
    func updateView() {
      print(self.highlighted)
        let type = self.bubbleType! as BubbleType
        switch (type) {
        case BubbleType.TextIn:
            if (!isFullSize!) {
                bubble.image = AABubbleCell.cachedInTextCompactBg
                bubbleBorder.image = AABubbleCell.cachedInTextCompactBgBorder
                bubble.highlightedImage = AABubbleCell.cachedInTextCompactSelectedBg
                bubbleBorder.highlightedImage = AABubbleCell.cachedInTextCompactBgBorder
            } else {
                bubble.image = AABubbleCell.cachedInTextBg
                bubbleBorder.image = AABubbleCell.cachedInTextBgBorder
                bubble.highlightedImage = AABubbleCell.cachedInTextBg
                bubbleBorder.highlightedImage = AABubbleCell.cachedInTextBgBorder
            }
            break
        case BubbleType.TextOut:
            if (!isFullSize!) {
                bubble.image =  AABubbleCell.cachedOutTextCompactBg
                bubbleBorder.image =  AABubbleCell.cachedOutTextCompactBgBorder
                bubble.highlightedImage =  AABubbleCell.cachedOutTextCompactSelectedBg
                bubbleBorder.highlightedImage =  AABubbleCell.cachedOutTextCompactBgBorder
            } else {
                bubble.image =  AABubbleCell.cachedOutTextBg
                bubbleBorder.image =  AABubbleCell.cachedOutTextBgBorder
                bubble.highlightedImage =  AABubbleCell.cachedOutTextBg
                bubbleBorder.highlightedImage =  AABubbleCell.cachedOutTextBgBorder
            }
            break
        case BubbleType.MediaIn:
            bubble.image =  AABubbleCell.cachedMediaBg
            bubbleBorder.image =  AABubbleCell.cachedMediaBgBorder
            bubble.highlightedImage =  AABubbleCell.cachedMediaBg
            bubbleBorder.highlightedImage =  AABubbleCell.cachedMediaBgBorder
            break
        case BubbleType.MediaOut:
            bubble.image =  AABubbleCell.cachedMediaBg
            bubbleBorder.image =  AABubbleCell.cachedMediaBgBorder
            bubble.highlightedImage =  AABubbleCell.cachedMediaBg
            bubbleBorder.highlightedImage =  AABubbleCell.cachedMediaBgBorder
            break
        case BubbleType.Service:
            bubble.image = AABubbleCell.cachedServiceBg
            bubbleBorder.image = nil
            bubble.highlightedImage = AABubbleCell.cachedServiceBg
            bubbleBorder.highlightedImage = nil
            break
        case BubbleType.Sticker:
            bubble.image = nil;
            bubbleBorder.image = nil
            bubble.highlightedImage = nil;
            bubbleBorder.highlightedImage = nil
            break
        }
    }
    
    // MARK: -
    // MARK: Layout
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        UIView.performWithoutAnimation { () -> Void in
            let endPadding: CGFloat = 32
            let startPadding: CGFloat = (!self.isOut && self.isGroup) ? AABubbleCell.avatarPadding : 0
            let cellMaxWidth = self.contentView.frame.size.width - endPadding - startPadding
            self.layoutContent(cellMaxWidth, offsetX: startPadding)
            self.layoutAnchor()
            if (!self.isOut && self.isGroup && !self.isFullSize) {
                self.layoutAvatar()
            }
        }
    }
    
    func layoutAnchor() {
        if (isShowDate) {
            dateText.frame = CGRectMake(0, 0, 1000, 1000)
            dateText.sizeToFit()
            dateText.frame = CGRectMake(
                (self.contentView.frame.size.width-dateText.frame.width)/2, 8, dateText.frame.width, 18)
            dateBg.frame = CGRectMake(dateText.frame.minX - 8, dateText.frame.minY, dateText.frame.width + 16, 18)
            
            dateText.hidden = false
            dateBg.hidden = false
        } else {
            dateText.hidden = true
            dateBg.hidden = true
        }
        
        if (isShowNewMessages) {
            var top = CGFloat(0)
            if (isShowDate) {
                top += AABubbleCell.dateSize
            }
            newMessage.hidden = false
            newMessage.frame = CGRectMake(0, top + CGFloat(2), self.contentView.frame.width, AABubbleCell.newMessageSize - CGFloat(4))
        } else {
            newMessage.hidden = true
        }
    }
    
    public func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        
    }
    
    func layoutAvatar() {
        let avatarSize = CGFloat(39)
        avatarView.frame = CGRect(x: 5 + (AADevice.isiPad ? 16 : 0), y: self.contentView.frame.size.height - avatarSize - 2 - bubbleInsets.bottom, width: avatarSize, height: avatarSize)
    }
    
    // Need to be called in child cells
    
    public func layoutBubble(contentWidth: CGFloat, contentHeight: CGFloat) {
        let fullWidth = contentView.bounds.width
        let bubbleW = contentWidth + contentInsets.left + contentInsets.right
        let bubbleH = contentHeight + contentInsets.top + contentInsets.bottom
        var topOffset = CGFloat(0)
        if (isShowDate) {
            topOffset += AABubbleCell.dateSize
        }
        if (isShowNewMessages) {
            topOffset += AABubbleCell.newMessageSize
        }
        var bubbleFrame : CGRect!
        if (!isFullSize) {
            if (isOut) {
                bubbleFrame = CGRect(
                    x: fullWidth - contentWidth - contentInsets.left - contentInsets.right - bubbleInsets.right,
                    y: bubbleInsets.top + topOffset,
                    width: bubbleW,
                    height: bubbleH)
            } else {
                let padding : CGFloat = isGroup ? AABubbleCell.avatarPadding : 0
                bubbleFrame = CGRect(
                    x: bubbleInsets.left + padding,
                    y: bubbleInsets.top + topOffset,
                    width: bubbleW,
                    height: bubbleH)
            }
        } else {
            bubbleFrame = CGRect(
                x: (fullWidth - contentWidth - contentInsets.left - contentInsets.right)/2,
                y: bubbleInsets.top + topOffset,
                width: bubbleW,
                height: bubbleH)
        }
        bubble.frame = bubbleFrame
        bubbleBorder.frame = bubbleFrame
    }
    
    public func layoutBubble(frame: CGRect) {
        bubble.frame = frame
        bubbleBorder.frame = frame
    }
    
    public override func preferredLayoutAttributesFittingAttributes(layoutAttributes: UICollectionViewLayoutAttributes) -> UICollectionViewLayoutAttributes {
        return layoutAttributes
    }
}