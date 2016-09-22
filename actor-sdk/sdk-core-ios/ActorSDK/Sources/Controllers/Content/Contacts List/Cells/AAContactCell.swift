//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

open class AAContactCell : AATableViewCell, AABindedCell, AABindedSearchCell {
    
    public typealias BindData = ACContact
    
    open static func bindedCellHeight(_ table: AAManagedTable, item: BindData) -> CGFloat {
        return 56
    }
    
    open static func bindedCellHeight(_ item: BindData) -> CGFloat {
        return 56
    }
    
    open let avatarView = AAAvatarView()
    open let shortNameView = YYLabel()
    open let titleView = YYLabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFont(ofSize: 18)
        titleView.textColor = appStyle.contactTitleColor
        titleView.displaysAsynchronously = true
        
        shortNameView.font = UIFont.boldSystemFont(ofSize: 18)
        shortNameView.textAlignment = NSTextAlignment.center
        shortNameView.textColor = appStyle.contactTitleColor
        shortNameView.displaysAsynchronously = true
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(shortNameView)
        self.contentView.addSubview(titleView)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func bind(_ item: ACContact, search: String?) {
        bind(item)
    }
    
    open func bind(_ item: ACContact, table: AAManagedTable, index: Int, totalCount: Int) {
        bind(item)
    }
    
    func bind(_ item: ACContact) {
        avatarView.bind(item.name, id: Int(item.uid), avatar: item.avatar);
        
        titleView.text = item.name;
        
        shortNameView.isHidden = true
    }

    func bindDisabled(_ disabled: Bool) {
        if disabled {
            titleView.alpha = 0.5
            avatarView.alpha = 0.5
        } else {
            titleView.alpha = 1
            avatarView.alpha = 1
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        shortNameView.frame = CGRect(x: 0, y: 8, width: 30, height: 40);
        avatarView.frame = CGRect(x: 30, y: 8, width: 44, height: 44);
        titleView.frame = CGRect(x: 80, y: 8, width: width - 80 - 14, height: 40);
    }
}
