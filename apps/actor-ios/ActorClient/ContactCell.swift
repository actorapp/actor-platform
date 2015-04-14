//
//  ContactCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
import UIKit

class ContactCell : BasicCell {
    
    let avatarView = AAAvatarView(frameSize: 40, type: AAAvatarType.Rounded);
    let shortNameView = UILabel();
    let titleView = UILabel();
    
    init(reuseIdentifier:String) {
        super.init(reuseIdentifier: reuseIdentifier, separatorPadding: 80)
        
        titleView.font = UIFont(name: "HelveticaNeue", size: 18);
        shortNameView.font = UIFont(name: "HelveticaNeue-Bold", size: 18);
        shortNameView.textAlignment = NSTextAlignment.Center
        self.contentView.addSubview(avatarView);
        self.contentView.addSubview(shortNameView);
        self.contentView.addSubview(titleView);
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
        
        hideSeparator(isLast)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        var width = self.contentView.frame.width;
        shortNameView.frame = CGRectMake(0, 8, 30, 40);
        avatarView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}