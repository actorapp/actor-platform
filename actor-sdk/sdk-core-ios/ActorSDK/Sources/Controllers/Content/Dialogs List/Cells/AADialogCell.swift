//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYText

public class AADialogCell: AATableViewCell, AABindedCell {
    
    // Binding data type
    
    public typealias BindData = ACDialog
    
    // Hight of cell
    
    public static func bindedCellHeight(table: AAManagedTable, item: ACDialog) -> CGFloat {
        
        return 76
    }
    
    // Views
    
    private static let counterBgImage = Imaging.imageWithColor(ActorSDK.sharedActor().style.dialogCounterBgColor, size: CGSizeMake(18, 18))
        .roundImage(18).resizableImageWithCapInsets(UIEdgeInsetsMake(9, 9, 9, 9))
    
    public let avatarView = AAAvatarView(frameSize: 48)
    public let titleView = YYLabel()
    public let messageView = YYLabel()
    public let dateView = YYLabel()
    public let statusView = UIImageView()
    public let counterView = UILabel()
    public let counterViewBg = UIImageView()
    
    private var bindedPeer: ACPeer?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.mediumSystemFontOfSize(17)
        titleView.textColor = appStyle.dialogTitleColor
        titleView.displaysAsynchronously = true
        titleView.clearContentsBeforeAsynchronouslyDisplay = false
        titleView.fadeOnAsynchronouslyDisplay = true
        
        messageView.font = UIFont.systemFontOfSize(16)
        messageView.textColor = appStyle.dialogTextColor
        messageView.displaysAsynchronously = true
        messageView.clearContentsBeforeAsynchronouslyDisplay = false
        messageView.fadeOnAsynchronouslyDisplay = true
        
        dateView.font = UIFont.systemFontOfSize(14)
        dateView.textColor = appStyle.dialogDateColor
        dateView.textAlignment = .Right
        dateView.displaysAsynchronously = true
        dateView.clearContentsBeforeAsynchronouslyDisplay = false
        dateView.fadeOnAsynchronouslyDisplay = false
        
        statusView.contentMode = .Center

        counterViewBg.image = AADialogCell.counterBgImage
        
        counterView.font = UIFont.systemFontOfSize(14)
        counterView.textColor = appStyle.dialogCounterColor
        counterView.textAlignment = .Center
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(messageView)
        self.contentView.addSubview(dateView)
        self.contentView.addSubview(statusView)
        self.contentView.addSubview(counterViewBg)
        self.contentView.addSubview(counterView)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(item: ACDialog, table: AAManagedTable, index: Int, totalCount: Int) {
        
        
        //
        // Checking dialog rebinding
        //
        var isCleanBind: Bool = false
        if bindedPeer != nil {
            if bindedPeer!.isEqual(item.peer).boolValue {
                isCleanBind = true
            }
        }
        bindedPeer = item.peer
        
        
        //
        // Avatar View
        //
        avatarView.bind(item.dialogTitle, id: item.peer.peerId, avatar: item.dialogAvatar)
        
        
        //
        // Title View
        //
        titleView.displaysAsynchronously = true
        titleView.fadeOnAsynchronouslyDisplay = true
        titleView.clearContentsBeforeAsynchronouslyDisplay = !isCleanBind
        titleView.text = item.dialogTitle
        
        
        //
        // Message Content
        //
        messageView.displaysAsynchronously = true
        messageView.fadeOnAsynchronouslyDisplay = true
        messageView.clearContentsBeforeAsynchronouslyDisplay = !isCleanBind
        messageView.text = Actor.getFormatter().formatDialogText(item)
        if item.messageType.ordinal() != ACContentType.TEXT().ordinal() {
            messageView.textColor = appStyle.dialogTextActiveColor
        } else {
            messageView.textColor = appStyle.dialogTextColor
        }
        
        
        //
        // Date
        //
        if (item.date > 0) {
            dateView.text = Actor.getFormatter().formatShortDate(item.date)
            if dateView.hidden {
                setNeedsLayout()
            }
            dateView.hidden = false
        } else {
            if !dateView.hidden {
                setNeedsLayout()
            }
            dateView.hidden = true
        }
        
        
        //
        // Counter View
        //
        if (item.unreadCount != 0) {
            self.counterView.text = "\(item.unreadCount)"
            if counterView.hidden {
                setNeedsLayout()
            }
            self.counterView.hidden = false
            self.counterViewBg.hidden = false
        } else {
            if !counterView.hidden {
                setNeedsLayout()
            }
            self.counterView.hidden = true
            self.counterViewBg.hidden = true
        }
        
        
        //
        // Message State
        //
        let messageState = item.status.ordinal()
        if (messageState == ACMessageState.PENDING().ordinal()) {
            self.statusView.tintColor = appStyle.dialogStatusSending
            self.statusView.image = appStyle.chatIconClock
            if self.statusView.hidden {
                setNeedsLayout()
            }
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.READ().ordinal()) {
            self.statusView.tintColor = appStyle.dialogStatusRead
            self.statusView.image = appStyle.chatIconCheck2
            if self.statusView.hidden {
                setNeedsLayout()
            }
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.RECEIVED().ordinal()) {
            self.statusView.tintColor = appStyle.dialogStatusReceived
            self.statusView.image = appStyle.chatIconCheck2
            if self.statusView.hidden {
                setNeedsLayout()
            }
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.SENT().ordinal()) {
            self.statusView.tintColor = appStyle.dialogStatusSent
            self.statusView.image = appStyle.chatIconCheck1
            if self.statusView.hidden {
                setNeedsLayout()
            }
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.ERROR().ordinal()) {
            self.statusView.tintColor = appStyle.dialogStatusError
            self.statusView.image = appStyle.chatIconError
            if self.statusView.hidden {
                setNeedsLayout()
            }
            self.statusView.hidden = false
        } else {
            if !self.statusView.hidden {
                setNeedsLayout()
            }
            self.statusView.hidden = true
        }
    }
    
    public override func prepareForReuse() {
        super.prepareForReuse()
        
        self.avatarView.unbind(true)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        // We expect height == 76;
        let width = self.contentView.frame.width
        let leftPadding = CGFloat(76)
        let padding = CGFloat(14)
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48)
        
        let titleFrame = CGRectMake(leftPadding, 16, width - leftPadding - /*paddingRight*/(padding + 50), 21)
        if titleView.frame != titleFrame {
            // Instantly update view on cell resize
            titleView.displaysAsynchronously = false
            titleView.fadeOnAsynchronouslyDisplay = false
            UIView.performWithoutAnimation {
                self.titleView.frame = titleFrame
            }
        }
        
        var messagePadding:CGFloat = 0
        if (!self.statusView.hidden) {
            messagePadding = 22
            statusView.frame = CGRectMake(leftPadding, 44, 20, 18)
        }
        
        var unreadPadding = CGFloat(0)
        if (!self.counterView.hidden) {
            counterView.frame = CGRectMake(0, 0, 1000, 1000)
            counterView.sizeToFit()
            let unreadW = max(counterView.frame.width + 8, 18)
            counterView.frame = CGRectMake(width - padding - unreadW, 44, unreadW, 18)
            counterViewBg.frame = counterView.frame
            unreadPadding = unreadW
        }
 
        let messageViewFrame = CGRectMake(leftPadding+messagePadding, 44, width - leftPadding - /*paddingRight*/padding - messagePadding - unreadPadding, 18)
        if messageView.frame != messageViewFrame {
            // Instantly update view on cell resize
            messageView.displaysAsynchronously = false
            messageView.fadeOnAsynchronouslyDisplay = false
            UIView.performWithoutAnimation {
                self.messageView.frame = messageViewFrame
            }
        }
        
        dateView.frame = CGRectMake(width - /*width*/60 - /*paddingRight*/padding , 18, 60, 18)
    }
}