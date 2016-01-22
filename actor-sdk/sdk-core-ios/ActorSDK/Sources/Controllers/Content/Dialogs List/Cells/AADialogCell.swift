//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class AADialogCell: AATableViewCell, AABindedCell {
    
    // Binding data type
    
    public typealias BindData = ACDialog
    
    // Hight of cell
    
    public static func bindedCellHeight(table: AAManagedTable, item: ACDialog) -> CGFloat {
        
        return 76
    }
    
    // Views
    
    public let avatarView = AAAvatarView(frameSize: 48)
    public let titleView = UILabel()
    public let messageView = UILabel()
    public let dateView = UILabel()
    public let statusView = UIImageView()
    public let counterView = UILabel()
    public let counterViewBg = UIImageView()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        self.avatarView.layer.drawsAsynchronously       = true
        self.titleView.layer.drawsAsynchronously        = true
        self.dateView.layer.drawsAsynchronously         = true
        self.statusView.layer.drawsAsynchronously       = true
        self.counterViewBg.layer.drawsAsynchronously    = true
        self.counterView.layer.drawsAsynchronously      = true
        
        ////////////////////////////////////////////////
        
        self.contentView.addSubview(avatarView)
        
        titleView.font = UIFont.mediumSystemFontOfSize(17)
        titleView.textColor = appStyle.dialogTitleColor
        self.contentView.addSubview(titleView)
        
        messageView.font = UIFont.systemFontOfSize(16)
        messageView.textColor = appStyle.dialogTextColor
        self.contentView.addSubview(messageView)
        
        dateView.font = UIFont.systemFontOfSize(14)
        dateView.textColor = appStyle.dialogDateColor
        dateView.textAlignment = .Right
        self.contentView.addSubview(dateView)
        
        statusView.contentMode = .Center
        self.contentView.addSubview(statusView)
        
        counterViewBg.image = Imaging.imageWithColor(appStyle.dialogCounterBgColor, size: CGSizeMake(18, 18))
            .roundImage(18).resizableImageWithCapInsets(UIEdgeInsetsMake(9, 9, 9, 9))
        self.contentView.addSubview(counterViewBg)
        
        counterView.font = UIFont.systemFontOfSize(14)
        counterView.textColor = appStyle.dialogCounterColor
        counterView.textAlignment = .Center
        self.contentView.addSubview(counterView)
        
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(item: ACDialog, table: AAManagedTable, index: Int, totalCount: Int) {
        
        self.avatarView.bind(item.dialogTitle, id: item.peer.peerId, avatar: item.dialogAvatar)
        
        self.titleView.text = item.dialogTitle
        
        self.messageView.text = Actor.getFormatter().formatDialogText(item)
        if item.messageType.ordinal() != jint(ACContentType.TEXT.rawValue) {
            self.messageView.textColor = appStyle.dialogTextActiveColor
        } else {
            self.messageView.textColor = appStyle.dialogTextColor
        }
        
        if (item.date > 0) {
            self.dateView.text = Actor.getFormatter().formatShortDate(item.date)
            self.dateView.hidden = false
        } else {
            self.dateView.hidden = true
        }
        
        if (item.unreadCount != 0) {
            self.counterView.text = "\(item.unreadCount)"
            self.counterView.hidden = false
            self.counterViewBg.hidden = false
        } else {
            self.counterView.hidden = true
            self.counterViewBg.hidden = true
        }
        
        let messageState = UInt(item.status.ordinal())
        
        if (messageState == ACMessageState.PENDING.rawValue) {
            self.statusView.tintColor = appStyle.dialogStatusSending
            self.statusView.image = appStyle.chatIconClock
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.READ.rawValue) {
            self.statusView.tintColor = appStyle.dialogStatusRead
            self.statusView.image = appStyle.chatIconCheck2
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.RECEIVED.rawValue) {
            self.statusView.tintColor = appStyle.dialogStatusReceived
            self.statusView.image = appStyle.chatIconCheck2
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.SENT.rawValue) {
            self.statusView.tintColor = appStyle.dialogStatusSent
            self.statusView.image = appStyle.chatIconCheck1
            self.statusView.hidden = false
        } else if (messageState == ACMessageState.ERROR.rawValue) {
            self.statusView.tintColor = appStyle.dialogStatusError
            self.statusView.image = appStyle.chatIconError
            self.statusView.hidden = false
        } else {
            self.statusView.hidden = true
        }
        
        setNeedsLayout()
    }
    
    public override func prepareForReuse() {
        super.prepareForReuse()
        
        self.avatarView.unbind(true)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews();
        
        // We expect height == 76;
        let width = self.contentView.frame.width
        let leftPadding = CGFloat(76)
        let padding = CGFloat(14)
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48)
        
        titleView.frame = CGRectMake(leftPadding, 16, width - leftPadding - /*paddingRight*/(padding + 50), 21)
        
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

        messageView.frame = CGRectMake(leftPadding+messagePadding, 44, width - leftPadding - /*paddingRight*/padding - messagePadding - unreadPadding, 18)
        
        dateView.frame = CGRectMake(width - /*width*/60 - /*paddingRight*/padding , 18, 60, 18)
    }
}