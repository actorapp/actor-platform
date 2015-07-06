//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupMemberCell: CommonCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var usernameLabel: UILabel!
    
    // MARK: -
    // MARK: Public vars
    
    var userAvatarView: AvatarView!
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        userAvatarView = AvatarView(frameSize: 40, type: .Rounded)
        contentView.addSubview(userAvatarView)
        
        usernameLabel = UILabel()
        usernameLabel.textColor = MainAppTheme.list.textColor
        usernameLabel.font = UIFont.systemFontOfSize(18.0)
        usernameLabel.text = " "
        usernameLabel.sizeToFit()
        contentView.addSubview(usernameLabel)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    func setUsername(username: String) {
        usernameLabel.text = username
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let userAvatarViewFrameSize: CGFloat = CGFloat(userAvatarView.frameSize)
        userAvatarView.frame = CGRect(x: 14.0, y: (contentView.bounds.size.height - userAvatarViewFrameSize) / 2.0, width: userAvatarViewFrameSize, height: userAvatarViewFrameSize)
        
        usernameLabel.frame = CGRect(x: 65.0, y: (contentView.bounds.size.height - usernameLabel.bounds.size.height) / 2.0, width: contentView.bounds.size.width - 65.0 - 15.0, height: usernameLabel.bounds.size.height)
    }

}
