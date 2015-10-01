//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

class ContactCell : UATableViewCell, ACBindedCell, ACBindedSearchCell {
    
    typealias BindData = ACContact
    
    static func bindedCellHeight(table: ACManagedTable, item: BindData) -> CGFloat {
        return 56
    }
    
    static func bindedCellHeight(item: BindData) -> CGFloat {
        return 56
    }
    
    let avatarView = AvatarView(frameSize: 40, type: .Rounded);
    let shortNameView = UILabel();
    let titleView = UILabel();
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(cellStyle: "cell", reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFontOfSize(18)
        titleView.textColor = MainAppTheme.list.contactsTitle
        
        shortNameView.font = UIFont.boldSystemFontOfSize(18)
        shortNameView.textAlignment = NSTextAlignment.Center
        shortNameView.textColor = MainAppTheme.list.contactsShortTitle
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(shortNameView)
        self.contentView.addSubview(titleView)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(item: ACContact, search: String?) {
        bind(item)
    }
    
    func bind(item: ACContact, table: ACManagedTable, index: Int, totalCount: Int) {
        bind(item)
    }
    
    func bind(item: ACContact) {
        avatarView.bind(item.name, id: item.uid, avatar: item.avatar);
        
        titleView.text = item.name;
        
        shortNameView.hidden = true
    }

    func bindDisabled(disabled: Bool) {
        if disabled {
            titleView.alpha = 0.5
            avatarView.alpha = 0.5
        } else {
            titleView.alpha = 1
            avatarView.alpha = 1
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        shortNameView.frame = CGRectMake(0, 8, 30, 40);
        avatarView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}