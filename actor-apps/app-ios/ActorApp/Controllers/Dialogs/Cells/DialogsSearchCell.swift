//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class DialogsSearchCell: UATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private let avatarView: AvatarView = AvatarView(frameSize: 48, type: .Rounded);
    private let titleView: UILabel = UILabel();
    
    private var bindedFile: jlong? = nil;
    private var avatarCallback: CocoaDownloadCallback? = nil;
    
    // MARK: -
    // MARK: Constructors
    
    init(reuseIdentifier:String) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.mediumSystemFontOfSize(19)
        titleView.textColor = MainAppTheme.list.textColor
        
        contentView.addSubview(avatarView)
        contentView.addSubview(titleView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Methods
    
    func bindSearchEntity(searchEntity: ACSearchEntity, isLast: Bool) {
        avatarView.bind(searchEntity.getTitle(), id: searchEntity.getPeer().getPeerId(), avatar: searchEntity.getAvatar());
        titleView.text = searchEntity.getTitle();
        
        topSeparatorVisible = false
        bottomSeparatorLeftInset = isLast ? 0 : 75
        bottomSeparatorVisible = true
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews();
        
        let width = self.contentView.frame.width;
        let leftPadding = CGFloat(76);
        let padding = CGFloat(14);
        
        avatarView.frame = CGRectMake(padding, padding, 48, 48);
        titleView.frame = CGRectMake(leftPadding, 0, width - leftPadding - (padding + 50), contentView.bounds.size.height);
    }

}
