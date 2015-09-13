//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit;

class DialogCell: UITableViewCell {
    
    let avatarView = AvatarView(frameSize: 48, type: .Rounded)
    let titleView: UILabel = UILabel()
    let messageView: UILabel = UILabel()
    let dateView: UILabel = UILabel()
    let statusView: UIImageView = UIImageView()
    let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor)
    
    let unreadView: UILabel = UILabel()
    let unreadViewBg: UIImageView = UIImageView()
    
    var bindedFile: jlong? = nil;
    var avatarCallback: CocoaDownloadCallback? = nil;
    
    init(reuseIdentifier:String) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        backgroundColor = MainAppTheme.list.bgColor
        
        titleView.font = UIFont(name: "Roboto-Medium", size: 19);
        titleView.textColor = MainAppTheme.list.dialogTitle
        
        messageView.font = UIFont(name: "HelveticaNeue", size: 16);
        messageView.textColor = MainAppTheme.list.dialogText
        
        dateView.font = UIFont(name: "HelveticaNeue", size: 14);
        dateView.textColor = MainAppTheme.list.dialogDate
        
        dateView.textAlignment = NSTextAlignment.Right;
        statusView.contentMode = UIViewContentMode.Center;
        
        unreadView.font = UIFont(name: "HelveticaNeue", size: 14);
        unreadView.textColor = MainAppTheme.list.unreadText
        unreadView.textAlignment = .Center
        
        unreadViewBg.image =
            Imaging.imageWithColor(MainAppTheme.list.unreadBg, size: CGSizeMake(18, 18))
            .roundImage(18).resizableImageWithCapInsets(UIEdgeInsetsMake(9, 9, 9, 9))
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(messageView)
        self.contentView.addSubview(dateView)
        self.contentView.addSubview(statusView)
        self.contentView.addSubview(separatorView)
        self.contentView.addSubview(unreadViewBg)
        self.contentView.addSubview(unreadView)
        
        let selectedView = UIView()
        selectedView.backgroundColor = MainAppTheme.list.bgSelectedColor
        selectedBackgroundView = selectedView
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bindDialog(dialog: ACDialog, isLast:Bool) {
        self.avatarView.bind(dialog.getDialogTitle(), id: dialog.getPeer().getPeerId(), avatar: dialog.getDialogAvatar());
        
        self.titleView.text = dialog.getDialogTitle();
        
        self.messageView.text = Actor.getFormatter().formatDialogText(dialog)
        
        if (dialog.getDate() > 0) {
            self.dateView.text = Actor.getFormatter().formatShortDate(dialog.getDate());
            self.dateView.hidden = false;
        } else {
            self.dateView.hidden = true;
        }
        
        if (dialog.getUnreadCount() != 0) {
            self.unreadView.text = "\(dialog.getUnreadCount())"
            self.unreadView.hidden = false
            self.unreadViewBg.hidden = false
        } else {
            self.unreadView.hidden = true
            self.unreadViewBg.hidden = true
        }
        
        let messageState = UInt(dialog.getStatus().ordinal());
        
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
        
        self.separatorView.hidden = isLast;
        
        setNeedsLayout()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews();
        
        // We expect height == 76;
        let width = self.contentView.frame.width;
        let leftPadding = CGFloat(76);
        let padding = CGFloat(14);
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48);
        
        titleView.frame = CGRectMake(leftPadding, 18, width - leftPadding - /*paddingRight*/(padding + 50), 18);
        
        var messagePadding:CGFloat = 0;
        if (!self.statusView.hidden) {
            messagePadding = 22;
            statusView.frame = CGRectMake(leftPadding, 44, 20, 18);
        }
        
        var unreadPadding = CGFloat(0)
        if (!self.unreadView.hidden) {
            unreadView.frame = CGRectMake(0, 0, 1000, 1000)
            unreadView.sizeToFit()
            let unreadW = max(unreadView.frame.width + 8, 18)
            unreadView.frame = CGRectMake(width - padding - unreadW, 44, unreadW, 18)
            unreadViewBg.frame = unreadView.frame
            unreadPadding = unreadW
        }

        messageView.frame = CGRectMake(leftPadding+messagePadding, 44, width - leftPadding - /*paddingRight*/padding - messagePadding - unreadPadding, 18);
        
        dateView.frame = CGRectMake(width - /*width*/60 - /*paddingRight*/padding , 18, 60, 18);
        separatorView.frame = CGRectMake(leftPadding, 75.5, width, 0.5);
        
    }
}