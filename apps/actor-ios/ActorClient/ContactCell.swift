//
//  ContactCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit

class ContactCell : UITableViewCell {
    
    let avatarView = AvatarView(frameSize: 40);
    let shortNameView = UILabel();
    let titleView = UILabel();
    let separatorView = TableViewSeparator(color: Resources.SeparatorColor)
    
    init(reuseIdentifier:String) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont(name: "HelveticaNeue", size: 18);
        shortNameView.font = UIFont(name: "HelveticaNeue-Bold", size: 18);
        shortNameView.textAlignment = NSTextAlignment.Center
        self.contentView.addSubview(avatarView);
        self.contentView.addSubview(shortNameView);
        self.contentView.addSubview(titleView);
        self.contentView.addSubview(separatorView);
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bindContact(contact: AMContact, shortValue: String?, isLast: Bool) {
        avatarView.bind(contact.getName(), id: contact.getUid(), avatar: contact.getAvatar());
        
        titleView.text = contact.getName();
        
        if (shortValue == nil){
            shortNameView.hidden = true;
        } else {
            shortNameView.text = shortValue!;
            shortNameView.hidden = false;
        }
        
        separatorView.hidden = isLast
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        var width = self.contentView.frame.width;
        shortNameView.frame = CGRectMake(0, 8, 30, 40);
        avatarView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
        separatorView.frame = CGRectMake(80, 55.5, width - 80, 0.5);
    }
}