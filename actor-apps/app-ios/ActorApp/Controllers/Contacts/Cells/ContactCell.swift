//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class ContactCell : UATableViewCell {
    
    let avatarView = AvatarView(frameSize: 40, type: .Rounded);
    let shortNameView = UILabel();
    let titleView = UILabel();
    
    init(reuseIdentifier:String) {
        super.init(cellStyle: "cell", reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFontOfSize(18)
        titleView.textColor = MainAppTheme.list.contactsTitle
        
        shortNameView.font = UIFont.boldSystemFontOfSize(18)
        shortNameView.textAlignment = NSTextAlignment.Center
        shortNameView.textColor = MainAppTheme.list.contactsShortTitle
        
        self.contentView.addSubview(avatarView);
        self.contentView.addSubview(shortNameView);
        self.contentView.addSubview(titleView);
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
        
        
        topSeparatorVisible = false
        bottomSeparatorVisible = true
        bottomSeparatorLeftInset = isLast ? 0 : 80
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        shortNameView.frame = CGRectMake(0, 8, 30, 40);
        avatarView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}