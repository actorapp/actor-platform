//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class ContactCell : BasicCell {
    
    let avatarView = AvatarView(frameSize: 40, type: .Rounded);
    let shortNameView = UILabel();
    let titleView = UILabel();
    
    init(reuseIdentifier:String) {
        super.init(reuseIdentifier: reuseIdentifier, separatorPadding: 80)
        
        titleView.font = UIFont(name: "HelveticaNeue", size: 18);
        titleView.textColor = MainAppTheme.list.contactsTitle
        
        shortNameView.font = UIFont(name: "HelveticaNeue-Bold", size: 18);
        shortNameView.textAlignment = NSTextAlignment.Center
        shortNameView.textColor = MainAppTheme.list.contactsShortTitle
        
        self.contentView.addSubview(avatarView);
        self.contentView.addSubview(shortNameView);
        self.contentView.addSubview(titleView);
        
        backgroundColor = MainAppTheme.list.bgColor
        
        let selectedView = UIView()
        selectedView.backgroundColor = MainAppTheme.list.bgSelectedColor
        selectedBackgroundView = selectedView
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bindContact(contact: ACContact, shortValue: String?, isLast: Bool) {
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
        let width = self.contentView.frame.width;
        shortNameView.frame = CGRectMake(0, 8, 30, 40);
        avatarView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}