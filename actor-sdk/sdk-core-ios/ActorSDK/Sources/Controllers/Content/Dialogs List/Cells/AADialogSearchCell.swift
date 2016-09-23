//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AADialogSearchCell: AATableViewCell, AABindedSearchCell {
    
    public typealias BindData = ACSearchResult
    
    open static func bindedCellHeight(_ item: BindData) -> CGFloat {
        
        return 76
    }
    
    fileprivate let avatarView: AAAvatarView = AAAvatarView()
    fileprivate let titleView: UILabel = UILabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        
        super.init(style: UITableViewCellStyle.default, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.mediumSystemFontOfSize(19)
        titleView.textColor = ActorSDK.sharedActor().style.dialogTextColor
        
        contentView.addSubview(avatarView)
        contentView.addSubview(titleView)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func bind(_ item: ACSearchResult, search: String?) {
        avatarView.bind(item.title, id: Int(item.peer.peerId), avatar: item.avatar)
        titleView.text = item.title
    }
    
    open override func prepareForReuse() {
        super.prepareForReuse()
        avatarView.unbind()
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = self.contentView.frame.width
        let leftPadding = CGFloat(76)
        let padding = CGFloat(14)
        
        avatarView.frame = CGRect(x: padding, y: padding, width: 52, height: 52)
        titleView.frame = CGRect(x: leftPadding, y: 0, width: width - leftPadding - (padding + 50), height: contentView.bounds.size.height)
    }
}
