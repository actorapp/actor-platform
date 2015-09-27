//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit;

class DialogCell: UATableViewCell {
    
    // Views
    
    let avatarView = AvatarView(style: "dialogs.avatar")
    let titleView: UILabel = UILabel(style: "dialogs.title")
    let messageView: UILabel = UILabel(style: "dialogs.message")
    let dateView: UILabel = UILabel(style: "dialogs.date")
    let statusView: UIImageView = UIImageView(style: "dialogs.status")
    let counterView: UILabel = UILabel(style: "dialogs.counter")
    let counterViewBg: UIImageView = UIImageView(style: "dialogs.counter.bg")
    
    init(reuseIdentifier: String) {
        super.init(cellStyle: "dialogs.cell", reuseIdentifier: reuseIdentifier)
        
        // Registering all views
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(messageView)
        self.contentView.addSubview(dateView)
        self.contentView.addSubview(statusView)
        self.contentView.addSubview(counterViewBg)
        self.contentView.addSubview(counterView)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bindDialog(dialog: ACDialog, isLast: Bool) {
        
        if isLast {
            applyStyle("dialogs.cell.last")
        } else {
            applyStyle("dialogs.cell")
        }
        
        self.avatarView.bind(dialog.dialogTitle, id: dialog.peer.peerId, avatar: dialog.dialogAvatar);
        
        self.titleView.text = dialog.dialogTitle;
    
        self.messageView.text = Actor.getFormatter().formatDialogText(dialog)
        if dialog.messageType.ordinal() != jint(ACContentType.TEXT.rawValue) {
            self.messageView.applyStyle("dialog.message")
        } else {
            self.messageView.applyStyle("dialog.message.hightlight")
        }
        
        if (dialog.date > 0) {
            self.dateView.text = Actor.getFormatter().formatShortDate(dialog.date);
            self.dateView.hidden = false;
        } else {
            self.dateView.hidden = true;
        }
        
        if (dialog.unreadCount != 0) {
            self.counterView.text = "\(dialog.unreadCount)"
            self.counterView.hidden = false
            self.counterViewBg.hidden = false
        } else {
            self.counterView.hidden = true
            self.counterViewBg.hidden = true
        }
        
        let messageState = UInt(dialog.status.ordinal());
        
        if (messageState == ACMessageState.PENDING.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogSending
            self.statusView.image =  Resources.iconClock;
            self.statusView.hidden = false;
        } else if (messageState == ACMessageState.READ.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogRead
            self.statusView.image = Resources.iconCheck2;
            self.statusView.hidden = false;
        } else if (messageState == ACMessageState.RECEIVED.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogReceived
            self.statusView.image = Resources.iconCheck2;
            self.statusView.hidden = false;
        } else if (messageState == ACMessageState.SENT.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogSent
            self.statusView.image = Resources.iconCheck1;
            self.statusView.hidden = false;
        } else if (messageState == ACMessageState.ERROR.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogError
            self.statusView.image = Resources.iconError;
            self.statusView.hidden = false;
        } else {
            self.statusView.hidden = true;
        }
        
        setNeedsLayout()
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        
        self.avatarView.unbind(true)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews();
        
        // We expect height == 76;
        let width = self.contentView.frame.width;
        let leftPadding = CGFloat(76);
        let padding = CGFloat(14);
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48);
        
        titleView.frame = CGRectMake(leftPadding, 16, width - leftPadding - /*paddingRight*/(padding + 50), 21);
        
        var messagePadding:CGFloat = 0;
        if (!self.statusView.hidden) {
            messagePadding = 22;
            statusView.frame = CGRectMake(leftPadding, 44, 20, 18);
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

        messageView.frame = CGRectMake(leftPadding+messagePadding, 44, width - leftPadding - /*paddingRight*/padding - messagePadding - unreadPadding, 18);
        
        dateView.frame = CGRectMake(width - /*width*/60 - /*paddingRight*/padding , 18, 60, 18);
    }
}