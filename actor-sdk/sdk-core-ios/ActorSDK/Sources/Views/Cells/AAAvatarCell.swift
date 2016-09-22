//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAAvatarCell: AATableViewCell {
    
    open var titleLabel = UILabel()
    open var subtitleLabel = UILabel()
    open var avatarView = AAAvatarView()
    open var progress = UIActivityIndicatorView(activityIndicatorStyle: .white)
    open var didTap: ((_ view: UIView)->())?
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        avatarView = AAAvatarView()
        avatarView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AAAvatarCell.avatarDidTap)))
        avatarView.isUserInteractionEnabled = true
        contentView.addSubview(avatarView)
        
        titleLabel.backgroundColor = UIColor.clear
        titleLabel.textColor = ActorSDK.sharedActor().style.cellTextColor
        titleLabel.font = UIFont.systemFont(ofSize: 20.0)
        contentView.addSubview(titleLabel)
        
        subtitleLabel.backgroundColor = UIColor.clear
        subtitleLabel.textColor = ActorSDK.sharedActor().style.cellHintColor
        subtitleLabel.font = UIFont.systemFont(ofSize: 14.0)
        contentView.addSubview(subtitleLabel)
        
        contentView.addSubview(progress)
        
        selectionStyle = .none
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func avatarDidTap() {
        didTap?(avatarView)
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRect(x: 14, y: 14, width: 66, height: 66)
        progress.frame = avatarView.frame

        if subtitleLabel.isHidden {
            titleLabel.frame = CGRect(x: 82 + 6, y: 14 + 64/2 - 14, width: self.contentView.bounds.width - 82 - 14 - 10, height: 24)
        } else {
            titleLabel.frame = CGRect(x: 82 + 6, y: 14 + 64/2 - 24, width: self.contentView.bounds.width - 82 - 14 - 10, height: 24)
            subtitleLabel.frame = CGRect(x: 82 + 6, y: 14 + 66/2 + 4, width: self.contentView.bounds.width - 82 - 14 - 10, height: 16)
        }
    }
}
