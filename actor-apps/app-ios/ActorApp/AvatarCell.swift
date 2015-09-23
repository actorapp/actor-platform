//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import Foundation

class AvatarCell: UATableViewCell {
    
    var titleLabel: UILabel!
    var subtitleLabel: UILabel!
    var avatarView: AvatarView!
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        avatarView = AvatarView(frameSize: Int(64), type: .Rounded, placeholderImage: UIImage())
        contentView.addSubview(avatarView)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = MainAppTheme.list.textColor
        titleLabel.font = UIFont.systemFontOfSize(20.0)
        contentView.addSubview(titleLabel)
        
        subtitleLabel = UILabel()
        subtitleLabel.backgroundColor = UIColor.clearColor()
        subtitleLabel.textColor = MainAppTheme.list.hintColor
        subtitleLabel.font = UIFont.systemFontOfSize(14.0)
        contentView.addSubview(subtitleLabel)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRect(x: 14, y: 14, width: 64, height: 64)
        titleLabel.frame = CGRect(x: 82 + 6, y: 14 + 64/2 - 24, width: self.contentView.bounds.width - 82 - 14 - 10, height: 24)
        subtitleLabel.frame = CGRect(x: 82 + 6, y: 14 + 66/2 + 4, width: self.contentView.bounds.width - 82 - 14 - 10, height: 16)
    }
}