//
//  PublicCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 30.06.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class PublicCell: UITableViewCell {
    
    let avatarView = AAAvatarView(frameSize: 48, type: AAAvatarType.Rounded)
    let title: UILabel = UILabel()
    let desc: UILabel = UILabel()
    let members: UILabel = UILabel()
    let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor)
    
    var group: AMPublicGroup!
    
    init() {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: "cell")
        
        title.font = UIFont(name: "Roboto-Medium", size: 19);
        title.textColor = MainAppTheme.list.dialogTitle
        
        members.font = UIFont(name: "Roboto-Medium", size: 19);
        members.textColor = MainAppTheme.list.dialogTitle
        
        desc.font = UIFont(name: "HelveticaNeue", size: 16);
        desc.textColor = MainAppTheme.list.dialogText
        
        contentView.addSubview(avatarView)
        contentView.addSubview(title)
        contentView.addSubview(members)
        contentView.addSubview(desc)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(group: AMPublicGroup) {
        self.group = group
        
        title.text = self.group.getTitle()
        desc.text = self.group.getDescription()
        members.text = "Members: \(self.group.getMembers()) Friends: \(self.group.getFriends())"
        
        avatarView.bind(self.group.getTitle(), id: self.group.getId(), avatar: self.group.getAvatar())
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = self.contentView.frame.width;
        let leftPadding = CGFloat(76);
        let padding = CGFloat(14);
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48);
        
        title.frame = CGRectMake(leftPadding, 18, width - leftPadding - /*paddingRight*/padding, 20);
        
        desc.frame = CGRectMake(leftPadding, 40, width - leftPadding - /*paddingRight*/padding, 60)
        
        members.frame = CGRectMake(0, 0, 100, 20)
        members.sizeToFit()
        members.frame = CGRectMake(width - members.bounds.width - padding, 80, members.bounds.width, 20)
    }
}