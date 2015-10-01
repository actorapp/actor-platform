//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit;

/**
    Bubble types
*/
enum BubbleType {
    // Outcome text bubble
    case TextOut
    // Income text bubble
    case TextIn
    // Outcome media bubble
    case MediaOut
    // Income media bubble
    case MediaIn
    // Service bubbl
    case Service
}

/**
    Root class for bubble layouter. Used for preprocessing bubble layout in background.
*/
protocol AABubbleLayouter  {
    
    func isSuitable(message: ACMessage) -> Bool
    
    func buildLayout(peer: ACPeer, message: ACMessage) -> CellLayout
    
    func cellClass() -> AnyClass
}

// Extension for automatically building reuse ids
extension AABubbleLayouter {
    
    var reuseId: String {
        get {
            return "\(self.dynamicType)"
        }
    }
}

/**
    Root class for bubble cells
*/
class AABubbleCell: UICollectionViewCell {
    
    // MARK: -
    // MARK: Private static vars
    
    static let bubbleContentTop: CGFloat = 6
    static let bubbleContentBottom: CGFloat = 6
    static let bubbleTop: CGFloat = 3
    static let bubbleTopCompact: CGFloat = 1
    static let bubbleBottom: CGFloat = 3
    static let bubbleBottomCompact: CGFloat = 1
    static let avatarPadding: CGFloat = 39
    static let dateSize: CGFloat = 30
    static let newMessageSize: CGFloat = 30
    
    // Cached bubble images
    private static var cachedOutTextBg:UIImage = UIImage(named: "BubbleOutgoingFull")!.tintImage(MainAppTheme.bubbles.textBgOut)
    private static var cachedOutTextBgBorder:UIImage = UIImage(named: "BubbleOutgoingFullBorder")!.tintImage(MainAppTheme.bubbles.textBgOutBorder)
    private static var cachedInTextBg:UIImage = UIImage(named: "BubbleIncomingFull")!.tintImage(MainAppTheme.bubbles.textBgIn)
    private static var cachedInTextBgBorder:UIImage = UIImage(named: "BubbleIncomingFullBorder")!.tintImage(MainAppTheme.bubbles.textBgInBorder)
    
    private static var cachedOutTextCompactBg:UIImage = UIImage(named: "BubbleOutgoingPartial")!.tintImage(MainAppTheme.bubbles.textBgOut)
    private static var cachedOutTextCompactBgBorder:UIImage = UIImage(named: "BubbleOutgoingPartialBorder")!.tintImage(MainAppTheme.bubbles.textBgOutBorder)
    private static var cachedInTextCompactBg:UIImage = UIImage(named: "BubbleIncomingPartial")!.tintImage(MainAppTheme.bubbles.textBgIn)
    private static var cachedInTextCompactBgBorder:UIImage = UIImage(named: "BubbleIncomingPartialBorder")!.tintImage(MainAppTheme.bubbles.textBgInBorder)
    
    private static let cachedOutMediaBg:UIImage = UIImage(named: "BubbleOutgoingPartial")!.tintImage(MainAppTheme.bubbles.mediaBgOut)
    private static var cachedOutMediaBgBorder:UIImage = UIImage(named: "BubbleIncomingPartialBorder")!.tintImage(MainAppTheme.bubbles.mediaBgInBorder)

    private static var cachedInMediaBg:UIImage? = nil;
    private static var cachedInMediaBgBorder:UIImage? = nil;
    
    private static var cachedServiceBg:UIImage = Imaging.roundedImage(MainAppTheme.bubbles.serviceBg, size: CGSizeMake(18, 18), radius: 9)
    
    private static var dateBgImage = Imaging.roundedImage(MainAppTheme.bubbles.serviceBg, size: CGSizeMake(18, 18), radius: 9)
    
    // MARK: -
    // MARK: Public vars
    
    // Views
    let mainView = UIView()
    let avatarView = AvatarView(frameSize: 39)
    var avatarAdded: Bool = false
    
    let bubble = UIImageView()
    let bubbleBorder = UIImageView()
    
    private let dateText = UILabel()
    private let dateBg = UIImageView()
    
    private let newMessage = UILabel()
    
    // Layout
    var contentInsets : UIEdgeInsets = UIEdgeInsets()
    var bubbleInsets : UIEdgeInsets = UIEdgeInsets()
    var fullContentInsets : UIEdgeInsets {
        get {
            return UIEdgeInsets(
                top: contentInsets.top + bubbleInsets.top + (isShowDate ? AABubbleCell.dateSize : 0) + (isShowNewMessages ? AABubbleCell.newMessageSize : 0),
                left: contentInsets.left + bubbleInsets.left + (isGroup && !isOut ? AABubbleCell.avatarPadding : 0),
                bottom: contentInsets.bottom + bubbleInsets.bottom,
                right: contentInsets.right + bubbleInsets.right)
        }
    }
    var needLayout: Bool = true
    
    let groupContentInsetY = 20.0
    let groupContentInsetX = 40.0
    var bubbleVerticalSpacing: CGFloat = 6.0
    let bubblePadding: CGFloat = 6;
    let bubbleMediaPadding: CGFloat = 10;
    
    // Binded data
    var peer: ACPeer!
    var controller: ConversationContentViewController!
    var isGroup: Bool = false
    var isFullSize: Bool!
    var bindedSetting: CellSetting?
    
    var bindedMessage: ACMessage? = nil
    var bubbleType:BubbleType? = nil
    var isOut: Bool = false
    var isShowDate: Bool = false
    var isShowNewMessages: Bool = false
    
    // MARK: -
    // MARK: Constructors

    init(frame: CGRect, isFullSize: Bool) {
        super.init(frame: frame)
        
        self.isFullSize = isFullSize
  
        dateBg.image = AABubbleCell.dateBgImage
        dateText.font = UIFont.mediumSystemFontOfSize(12)
        dateText.textColor = UIColor.whiteColor()
        dateText.contentMode = UIViewContentMode.Center
        dateText.textAlignment = NSTextAlignment.Center
        
        newMessage.font = UIFont.mediumSystemFontOfSize(14)
        newMessage.textColor = UIColor.whiteColor()
        newMessage.contentMode = UIViewContentMode.Center
        newMessage.textAlignment = NSTextAlignment.Center
        newMessage.backgroundColor = UIColor.alphaBlack(0.3)
        newMessage.text = "New Messages"
        
        // bubble.userInteractionEnabled = true
        
        mainView.transform = CGAffineTransformMake(1, 0, 0, -1, 0, 0)
        
        mainView.addSubview(bubble)
        mainView.addSubview(bubbleBorder)
        mainView.addSubview(newMessage)
        mainView.addSubview(dateBg)
        mainView.addSubview(dateText)
        
        contentView.addSubview(mainView)
        
        avatarView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "avatarDidTap"))
        avatarView.userInteractionEnabled = true
        
        backgroundColor = UIColor.clearColor()
        
        // Speed up animations
        self.layer.speed = 1.5
        
        self.layer.shouldRasterize = true
        self.layer.rasterizationScale = UIScreen.mainScreen().scale
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setConfig(peer: ACPeer, controller: ConversationContentViewController) {
        self.peer = peer
        self.controller = controller
        if (peer.isGroup && !isFullSize) {
            self.isGroup = true
        }
    }
    
    override func canBecomeFirstResponder() -> Bool {
        return false
    }

    override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        if action == "delete:" {
            return true
        }
        return false
    }
    
    override func delete(sender: AnyObject?) {
        let rids = IOSLongArray(length: 1)
        rids.replaceLongAtIndex(0, withLong: bindedMessage!.rid)
        Actor.deleteMessagesWithPeer(self.peer, withRids: rids)
    }
    
    func avatarDidTap() {
        if bindedMessage != nil {
            controller.onBubbleAvatarTap(self.avatarView, uid: bindedMessage!.senderId)
        }
    }
    
    func performBind(message: ACMessage, setting: CellSetting, isShowNewMessages: Bool, layout: CellLayout) {
        self.clipsToBounds = false
        self.contentView.clipsToBounds = false
        
        var reuse = false
        if (bindedMessage != nil && bindedMessage?.rid == message.rid) {
            reuse = true
        }
        isOut = message.senderId == Actor.myUid();
        bindedMessage = message
        self.isShowNewMessages = isShowNewMessages
        if (!reuse) {
            if (!isFullSize) {
                if (!isOut && isGroup) {
                    if let user = Actor.getUserWithUid(message.senderId) {
                        
                        // Small hack for replacing senter name and title
                        // with current group title
                        if user.isBot() && user.getNameModel().get() == "Bot" {
                            if let group = Actor.getGroupWithGid(self.peer.peerId) {
                                let avatar: ACAvatar? = group.getAvatarModel().get()
                                let name = group.getNameModel().get()
                                avatarView.bind(name, id: user.getId(), avatar: avatar)
                            }
                        } else {
                            let avatar: ACAvatar? = user.getAvatarModel().get()
                            let name = user.getNameModel().get()
                            avatarView.bind(name, id: user.getId(), avatar: avatar)
                        }
                    }
                    if !avatarAdded {
                        mainView.addSubview(avatarView)
                        avatarAdded = true
                    }
                } else {
                    if avatarAdded {
                        avatarView.removeFromSuperview()
                        avatarAdded = false
                    }
                }
            }
        }
        
        self.isShowDate = setting.showDate
        if (isShowDate) {
            self.dateText.text = Actor.getFormatter().formatDate(message.date)
        }
        
        self.bindedSetting = setting
        
        bind(message, reuse: reuse, cellLayout: layout, setting: setting)
        
        if (!reuse) {
            needLayout = true
            super.setNeedsLayout()
        }
    }
    
    func bind(message: ACMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        fatalError("bind(message:) has not been implemented")
    }
    
    func bindBubbleType(type: BubbleType, isCompact: Bool) {
        self.bubbleType = type
        
        // Update Bubble background images
        switch(type) {
            case BubbleType.TextIn:
                if (isCompact) {
                    bubble.image = AABubbleCell.cachedInTextCompactBg
                    bubbleBorder.image = AABubbleCell.cachedInTextCompactBgBorder
                } else {
                    bubble.image = AABubbleCell.cachedInTextBg
                    bubbleBorder.image = AABubbleCell.cachedInTextBgBorder
                }
            break
            case BubbleType.TextOut:
                if (isCompact) {
                    bubble.image =  AABubbleCell.cachedOutTextCompactBg
                    bubbleBorder.image =  AABubbleCell.cachedOutTextCompactBgBorder
                } else {
                    bubble.image =  AABubbleCell.cachedOutTextBg
                    bubbleBorder.image =  AABubbleCell.cachedOutTextBgBorder
                }
            break
            case BubbleType.MediaIn:
                bubble.image =  AABubbleCell.cachedOutMediaBg
                bubbleBorder.image =  AABubbleCell.cachedOutMediaBgBorder
            break
            case BubbleType.MediaOut:
                bubble.image =  AABubbleCell.cachedOutMediaBg
                bubbleBorder.image =  AABubbleCell.cachedOutMediaBgBorder
            break
            case BubbleType.Service:
                bubble.image = AABubbleCell.cachedServiceBg
                bubbleBorder.image = nil
            break
        }
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        mainView.frame = CGRectMake(0, 0, contentView.bounds.width, contentView.bounds.height)
        
//        if (!needLayout) {
//            return
//        }
//        needLayout = false
        
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
    
    func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        
    }
    
    func layoutAvatar() {
        let avatarSize = CGFloat(self.avatarView.frameSize)
        avatarView.frame = CGRect(x: 5 + (isIPad ? 16 : 0), y: self.contentView.frame.size.height - avatarSize - 2 - bubbleInsets.bottom, width: avatarSize, height: avatarSize)
    }
    
    // Need to be called in child cells
    
    func layoutBubble(contentWidth: CGFloat, contentHeight: CGFloat) {
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
    
    func layoutBubble(frame: CGRect) {
        bubble.frame = frame
        bubbleBorder.frame = frame
    }
    
    override func preferredLayoutAttributesFittingAttributes(layoutAttributes: UICollectionViewLayoutAttributes) -> UICollectionViewLayoutAttributes {
        return layoutAttributes
    }
}