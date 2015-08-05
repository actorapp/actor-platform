//
//  AutoCompleteCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 05.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class AutoCompleteCell: UITableViewCell {
    
    var avatarView = AvatarView(frameSize: 32)
    var nickView = UILabel()
    var nameView = UILabel()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        avatarView.enableAnimation = false
        nickView.font = UIFont.systemFontOfSize(14)
        nickView.textColor = MainAppTheme.list.textColor
        
        nameView.font = UIFont.systemFontOfSize(14)
        nameView.textColor = MainAppTheme.list.hintColor
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(nickView)
        self.contentView.addSubview(nameView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setUser(user: AMUserVM) {
        avatarView.bind(user.getNameModel().get(), id: user.getId(), avatar: user.getAvatarModel().get(), clearPrev: true)
        if user.getNickModel().get() == nil {
            if user.getLocalNameModel().get() == nil {
                nickView.text = user.getNameModel().get()
                nameView.text = ""
            } else {
                nickView.text = user.getServerNameModel().get()
                nameView.text = " \u{2022} \(user.getLocalNameModel().get())"
            }
        } else {
            nickView.text = "@\(user.getNickModel().get())"
            nameView.text = " \u{2022} \(user.getNameModel().get())"
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRectMake(6, 6, 32, 32)
        nickView.frame = CGRectMake(44, 6, 100, 32)
        nickView.sizeToFit()
        nickView.frame = CGRectMake(44, 6, nickView.frame.width, 32)
        
        var left = 44 + nickView.frame.width
        nameView.frame = CGRectMake(left, 6, self.contentView.frame.width - left, 32)
    }
}