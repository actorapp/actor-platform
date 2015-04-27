//
//  DialogCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit;

class DialogCell: UITableViewCell {
    
    let avatarView = AAAvatarView(frameSize: 48, type: AAAvatarType.Rounded);
    let titleView: UILabel = UILabel();
    let messageView: UILabel = UILabel();
    let dateView: UILabel = UILabel();
    let statusView: UIImageView = UIImageView();
    let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor);
    
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
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(messageView)
        self.contentView.addSubview(dateView)
        self.contentView.addSubview(statusView)
        self.contentView.addSubview(separatorView)
        
        var selectedView = UIView()
        selectedView.backgroundColor = MainAppTheme.list.bgSelectedColor
        selectedBackgroundView = selectedView
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bindDialog(dialog: AMDialog, isLast:Bool) {        
        avatarView.bind(dialog.getDialogTitle(), id: dialog.getPeer().getPeerId(), avatar: dialog.getDialogAvatar());
        
        titleView.text = dialog.getDialogTitle();
        
        var text = MSG.getFormatter().formatContentDialogTextWithInt(dialog.getSenderId(), withAMContentTypeEnum: dialog.getMessageType(), withNSString: dialog.getText(), withInt: dialog.getRelatedUid());

        if (UInt(dialog.getPeer().getPeerType().ordinal()) == AMPeerType.GROUP.rawValue){
            if (MSG.getFormatter().isLargeDialogMessageWithAMContentTypeEnum(dialog.getMessageType())) {
                self.messageView.text = text
            } else {
                self.messageView.text = MSG.getFormatter().formatPerformerNameWithInt(dialog.getSenderId()) + ": " + text
            }
        } else {
           self.messageView.text = text
        }
        
        if (dialog.getDate() > 0) {
            self.dateView.text = MSG.getFormatter().formatShortDateWithLong(dialog.getDate());
            self.dateView.hidden = false;
        } else {
            self.dateView.hidden = true;
        }
        
        var messageState = UInt(dialog.getStatus().ordinal());
        
        if (messageState == AMMessageState.PENDING.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogSending
            self.statusView.image =  Resources.iconClock;
            self.statusView.hidden = false;
        } else if (messageState == AMMessageState.READ.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogRead
            self.statusView.image = Resources.iconCheck2;
            self.statusView.hidden = false;
        } else if (messageState == AMMessageState.RECEIVED.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogReceived
            self.statusView.image = Resources.iconCheck2;
            self.statusView.hidden = false;
        } else if (messageState == AMMessageState.SENT.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogSent
            self.statusView.image = Resources.iconCheck1;
            self.statusView.hidden = false;
        } else if (messageState == AMMessageState.ERROR.rawValue) {
            self.statusView.tintColor = MainAppTheme.bubbles.statusDialogError
            self.statusView.image = Resources.iconError;
            self.statusView.hidden = false;
        } else {
            self.statusView.hidden = true;
        }
        
        self.separatorView.hidden = isLast;
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
        messageView.frame = CGRectMake(leftPadding+messagePadding, 44, width - leftPadding - /*paddingRight*/padding - messagePadding, 18);
        
        dateView.frame = CGRectMake(width - /*width*/60 - /*paddingRight*/padding , 18, 60, 18);
        separatorView.frame = CGRectMake(leftPadding, 75.5, width, 0.5);
    }
}