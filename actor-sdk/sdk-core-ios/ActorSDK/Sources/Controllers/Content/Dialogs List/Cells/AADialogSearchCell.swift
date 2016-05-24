//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AADialogSearchCell: AATableViewCell, AABindedSearchCell {
    
    public typealias BindData = ACSearchEntity
    
    public static func bindedCellHeight(item: BindData) -> CGFloat {
        
        return 76
    }
    
    private let avatarView: AAAvatarView = AAAvatarView()
    private let titleView: UILabel = UILabel()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.mediumSystemFontOfSize(19)
        titleView.textColor = ActorSDK.sharedActor().style.dialogTextColor
        
        contentView.addSubview(avatarView)
        contentView.addSubview(titleView)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bind(item: ACSearchEntity, search: String?) {
        avatarView.bind(item.title, id: Int(item.peer.peerId), avatar: item.avatar)
        titleView.text = item.title
    }
    
    public override func prepareForReuse() {
        super.prepareForReuse()
        avatarView.unbind()
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = self.contentView.frame.width
        let leftPadding = CGFloat(76)
        let padding = CGFloat(14)
        
        avatarView.frame = CGRectMake(padding, padding, 52, 52)
        titleView.frame = CGRectMake(leftPadding, 0, width - leftPadding - (padding + 50), contentView.bounds.size.height)
    }
}
