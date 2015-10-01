//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class DialogsSearchCell: UATableViewCell, ACBindedSearchCell {
    
    typealias BindData = ACSearchEntity
    
    static func bindedCellHeight(item: BindData) -> CGFloat {
        
        return 76
    }
    
    private let avatarView: AvatarView = AvatarView(frameSize: 48, type: .Rounded)
    private let titleView: UILabel = UILabel()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.mediumSystemFontOfSize(19)
        titleView.textColor = MainAppTheme.list.textColor
        
        contentView.addSubview(avatarView)
        contentView.addSubview(titleView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(item: ACSearchEntity, search: String?) {
        avatarView.bind(item.title, id: item.peer.peerId, avatar: item.avatar)
        titleView.text = item.title
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        avatarView.unbind()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = self.contentView.frame.width
        let leftPadding = CGFloat(76)
        let padding = CGFloat(14)
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48)
        titleView.frame = CGRectMake(leftPadding, 0, width - leftPadding - (padding + 50), contentView.bounds.size.height)
    }
}
