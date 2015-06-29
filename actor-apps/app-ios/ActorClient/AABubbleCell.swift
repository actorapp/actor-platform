//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit;

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
    private static var cacnedOutTextBg:UIImage? = nil;
    private static var cacnedOutTextBgBorder:UIImage? = nil;
    private static var cacnedInTextBg:UIImage? = nil;
    private static var cacnedInTextBgBorder:UIImage? = nil;
    
    private static var cacnedOutTextCompactBg:UIImage? = nil;
    private static var cacnedOutTextCompactBgBorder:UIImage? = nil;
    private static var cacnedInTextCompactBg:UIImage? = nil;
    private static var cacnedInTextCompactBgBorder:UIImage? = nil;
    
    private static var cacnedOutMediaBg:UIImage? = nil;
    private static var cacnedOutMediaBgBorder:UIImage? = nil;
    private static var cacnedInMediaBg:UIImage? = nil;
    private static var cacnedInMediaBgBorder:UIImage? = nil;
    
    private static var cacnedServiceBg:UIImage? = nil;
    
    // MARK: -
    // MARK: Public vars
    
    // Views
    let mainView = UIView()
    let avatarView = AAAvatarView(frameSize: 39)
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
    var peer: AMPeer!
    var isGroup: Bool = false
    var isFullSize: Bool!
    var bindedSetting: CellSetting?
    
    var bindedMessage: AMMessage? = nil
    var bubbleType:BubbleType? = nil
    var isOut: Bool = false
    var isShowDate: Bool = false
    var isShowNewMessages: Bool = false
    
    // MARK: -
    // MARK: Constructors

    init(frame: CGRect, isFullSize: Bool) {
        super.init(frame: frame)
        
        self.isFullSize = isFullSize
  
        dateBg.image = Imaging.roundedImage(MainAppTheme.bubbles.serviceBg, size: CGSizeMake(18, 18), radius: 9)
        dateText.font = UIFont(name: "HelveticaNeue-Medium", size: 12)!
        dateText.textColor = UIColor.whiteColor()
        dateText.contentMode = UIViewContentMode.Center
        dateText.textAlignment = NSTextAlignment.Center
        
        newMessage.font = UIFont(name: "HelveticaNeue-Medium", size: 14)!
        newMessage.textColor = UIColor.whiteColor()
        newMessage.contentMode = UIViewContentMode.Center
        newMessage.textAlignment = NSTextAlignment.Center
        newMessage.backgroundColor = UIColor.alphaBlack(0.3)
        newMessage.text = "New Messages"
        
        bubble.userInteractionEnabled = true
        
        
        mainView.transform = CGAffineTransformMake(1, 0, 0, -1, 0, 0)
        
        mainView.addSubview(bubble)
        mainView.addSubview(bubbleBorder)
        mainView.addSubview(newMessage)
        mainView.addSubview(dateBg)
        mainView.addSubview(dateText)
        
        contentView.addSubview(mainView)
        
        backgroundColor = UIColor.clearColor()
        
        // Speed up animations
        self.layer.speed = 1.5
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setConfig(peer: AMPeer) {
        self.peer = peer
        if (peer.getPeerType().ordinal() == jint(AMPeerType.GROUP.rawValue) && !isFullSize) {
            self.isGroup = true
        }
    }
    
    override func canBecomeFirstResponder() -> Bool {
        return true
    }
    
    override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
        return false
    }
    
    func performBind(message: AMMessage, setting: CellSetting, layoutCache: LayoutCache) {
        self.clipsToBounds = false
        self.contentView.clipsToBounds = false
        
        var reuse = false
        if (bindedMessage != nil && bindedMessage?.getRid() == message.getRid()) {
            reuse = true
        }
        isOut = message.getSenderId() == MSG.myUid();
        bindedMessage = message
        self.isShowNewMessages = setting.showNewMessages
        if (!reuse) {
            if (!isFullSize) {
                if (!isOut && isGroup) {
                    if let user = MSG.getUserWithUid(message.getSenderId()) {
                        let avatar: AMAvatar? = user.getAvatarModel().get()
                        let name = user.getNameModel().get()
                        avatarView.bind(name, id: user.getId(), avatar: avatar)
                    }
                    mainView.addSubview(avatarView)
                } else {
                    avatarView.removeFromSuperview()
                }
            }
        }
        
        self.isShowDate = setting.showDate
        if (isShowDate) {
            self.dateText.text = MSG.getFormatter().formatDate(message.getDate())
        }
        
        var layout = layoutCache.pick(message.getRid())
            
        if (layout == nil) {
            layout = MessagesLayouting.buildLayout(message, layoutCache: layoutCache)
            layoutCache.cache(message.getRid(), layout: layout!)
        }
        
        self.bindedSetting = setting
        
        bind(message, reuse: reuse, cellLayout: layout!, setting: setting)
        
        if (!reuse) {
            needLayout = true
            super.setNeedsLayout()
        }
    }
    
    func bind(message: AMMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        fatalError("bind(message:) has not been implemented")
    }
    
    func bindBubbleType(type: BubbleType, isCompact: Bool) {
        self.bubbleType = type
        
        // Update Bubble background images
        switch(type) {
            case BubbleType.TextIn:
                if (isCompact) {
                    if (AABubbleCell.cacnedInTextCompactBg == nil) {
                        AABubbleCell.cacnedInTextCompactBg = UIImage(named: "BubbleIncomingPartial")?.tintImage(MainAppTheme.bubbles.textBgIn)
                    }
                    if (AABubbleCell.cacnedInTextCompactBgBorder == nil) {
                        AABubbleCell.cacnedInTextCompactBgBorder = UIImage(named: "BubbleIncomingPartialBorder")?.tintImage(MainAppTheme.bubbles.textBgInBorder)
                    }
                    
                    bubble.image = AABubbleCell.cacnedInTextCompactBg
                    bubbleBorder.image = AABubbleCell.cacnedInTextCompactBgBorder
                } else {
                    if (AABubbleCell.cacnedInTextBg == nil) {
                        AABubbleCell.cacnedInTextBg = UIImage(named: "BubbleIncomingFull")?.tintImage(MainAppTheme.bubbles.textBgIn)
                    }
                    if (AABubbleCell.cacnedInTextBgBorder == nil) {
                        AABubbleCell.cacnedInTextBgBorder = UIImage(named: "BubbleIncomingFullBorder")?.tintImage(MainAppTheme.bubbles.textBgInBorder)
                    }
                    
                    bubble.image = AABubbleCell.cacnedInTextBg
                    bubbleBorder.image = AABubbleCell.cacnedInTextBgBorder
                }
            break
            case BubbleType.TextOut:
                if (isCompact) {
                    if (AABubbleCell.cacnedOutTextCompactBg == nil) {
                        AABubbleCell.cacnedOutTextCompactBg = UIImage(named: "BubbleOutgoingPartial")?.tintImage(MainAppTheme.bubbles.textBgOut)
                    }
                    if (AABubbleCell.cacnedOutTextCompactBgBorder == nil) {
                        AABubbleCell.cacnedOutTextCompactBgBorder = UIImage(named: "BubbleOutgoingPartialBorder")?.tintImage(MainAppTheme.bubbles.textBgOutBorder)
                    }
                    
                    bubble.image =  AABubbleCell.cacnedOutTextCompactBg!
                    bubbleBorder.image =  AABubbleCell.cacnedOutTextCompactBgBorder!
                } else {
                    if (AABubbleCell.cacnedOutTextBg == nil) {
                        AABubbleCell.cacnedOutTextBg = UIImage(named: "BubbleOutgoingFull")?.tintImage(MainAppTheme.bubbles.textBgOut)
                    }
                    if (AABubbleCell.cacnedOutTextBgBorder == nil) {
                        AABubbleCell.cacnedOutTextBgBorder = UIImage(named: "BubbleOutgoingFullBorder")?.tintImage(MainAppTheme.bubbles.textBgOutBorder)
                    }
                    
                    bubble.image =  AABubbleCell.cacnedOutTextBg!
                    bubbleBorder.image =  AABubbleCell.cacnedOutTextBgBorder!
                }
            break
            case BubbleType.MediaIn:
                if (AABubbleCell.cacnedOutMediaBg == nil) {
                    AABubbleCell.cacnedOutMediaBg = UIImage(named: "BubbleIncomingPartial")?.tintImage(MainAppTheme.bubbles.mediaBgIn)
                }
                if (AABubbleCell.cacnedOutMediaBgBorder == nil) {
                    AABubbleCell.cacnedOutMediaBgBorder = UIImage(named: "BubbleIncomingPartialBorder")?.tintImage(MainAppTheme.bubbles.mediaBgInBorder)
                }
                
                bubble.image =  AABubbleCell.cacnedOutMediaBg!
                bubbleBorder.image =  AABubbleCell.cacnedOutMediaBgBorder!
            break
            case BubbleType.MediaOut:
                if (AABubbleCell.cacnedOutMediaBg == nil) {
                    AABubbleCell.cacnedOutMediaBg = UIImage(named: "BubbleOutgoingPartial")?.tintImage(MainAppTheme.bubbles.mediaBgOut)
                }
                if (AABubbleCell.cacnedOutMediaBgBorder == nil) {
                    AABubbleCell.cacnedOutMediaBgBorder = UIImage(named: "BubbleOutgoingPartialBorder")?.tintImage(MainAppTheme.bubbles.mediaBgOutBorder)
                }
                
                bubble.image =  AABubbleCell.cacnedOutMediaBg!
                bubbleBorder.image =  AABubbleCell.cacnedOutMediaBgBorder!
            break
            case BubbleType.Service:
                if (AABubbleCell.cacnedServiceBg == nil) {
                    AABubbleCell.cacnedServiceBg = UIImage(named: "bubble_service_bg")?.tintImage(MainAppTheme.bubbles.serviceBg)
                }
                bubble.image = AABubbleCell.cacnedServiceBg
                bubbleBorder.image = nil
            break
            default:
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
            var cellMaxWidth = self.contentView.frame.size.width - endPadding - startPadding
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
        avatarView.frame = CGRect(x: 5, y: self.contentView.frame.size.height - avatarSize - 1, width: avatarSize, height: avatarSize)
    }
    
    // Need to be called in child cells
    
    func layoutBubble(contentWidth: CGFloat, contentHeight: CGFloat) {
        let fullWidth = contentView.bounds.width
        let fullHeight = contentView.bounds.height
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
}

enum BubbleType {
    case TextOut
    case TextIn
    case MediaOut
    case MediaIn
    case Service
}