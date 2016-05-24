//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAvatarCell: AATableViewCell {
    
    public var titleLabel = UILabel()
    public var subtitleLabel = UILabel()
    public var avatarView = AAAvatarView()
    public var progress = UIActivityIndicatorView(activityIndicatorStyle: .White)
    public var didTap: ((view: UIView)->())?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        avatarView = AAAvatarView()
        avatarView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AAAvatarCell.avatarDidTap)))
        avatarView.userInteractionEnabled = true
        contentView.addSubview(avatarView)
        
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = ActorSDK.sharedActor().style.cellTextColor
        titleLabel.font = UIFont.systemFontOfSize(20.0)
        contentView.addSubview(titleLabel)
        
        subtitleLabel.backgroundColor = UIColor.clearColor()
        subtitleLabel.textColor = ActorSDK.sharedActor().style.cellHintColor
        subtitleLabel.font = UIFont.systemFontOfSize(14.0)
        contentView.addSubview(subtitleLabel)
        
        contentView.addSubview(progress)
        
        selectionStyle = .None
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func avatarDidTap() {
        didTap?(view: avatarView)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRect(x: 14, y: 14, width: 66, height: 66)
        progress.frame = avatarView.frame

        if subtitleLabel.hidden {
            titleLabel.frame = CGRect(x: 82 + 6, y: 14 + 64/2 - 14, width: self.contentView.bounds.width - 82 - 14 - 10, height: 24)
        } else {
            titleLabel.frame = CGRect(x: 82 + 6, y: 14 + 64/2 - 24, width: self.contentView.bounds.width - 82 - 14 - 10, height: 24)
            subtitleLabel.frame = CGRect(x: 82 + 6, y: 14 + 66/2 + 4, width: self.contentView.bounds.width - 82 - 14 - 10, height: 16)
        }
    }
}