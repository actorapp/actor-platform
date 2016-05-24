//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit

public class AAContactCell : AATableViewCell, AABindedCell, AABindedSearchCell {
    
    public typealias BindData = ACContact
    
    public static func bindedCellHeight(table: AAManagedTable, item: BindData) -> CGFloat {
        return 56
    }
    
    public static func bindedCellHeight(item: BindData) -> CGFloat {
        return 56
    }
    
    public let avatarView = AAAvatarView()
    public let shortNameView = YYLabel()
    public let titleView = YYLabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFontOfSize(18)
        titleView.textColor = appStyle.contactTitleColor
        titleView.displaysAsynchronously = true
        
        shortNameView.font = UIFont.boldSystemFontOfSize(18)
        shortNameView.textAlignment = NSTextAlignment.Center
        shortNameView.textColor = appStyle.contactTitleColor
        shortNameView.displaysAsynchronously = true
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(shortNameView)
        self.contentView.addSubview(titleView)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(item: ACContact, search: String?) {
        bind(item)
    }
    
    public func bind(item: ACContact, table: AAManagedTable, index: Int, totalCount: Int) {
        bind(item)
    }
    
    func bind(item: ACContact) {
        avatarView.bind(item.name, id: Int(item.uid), avatar: item.avatar);
        
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
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        shortNameView.frame = CGRectMake(0, 8, 30, 40);
        avatarView.frame = CGRectMake(30, 8, 44, 44);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}