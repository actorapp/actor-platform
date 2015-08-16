//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class UserPhotoCell: CommonCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var usernameLabel: UILabel!
    private var presenceLabel: UILabel!
    private let shadow = UIImageView()
    private let progress = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.WhiteLarge)
    
    // MARK: -
    // MARK: Public vars
    
    var userAvatarView: AvatarView!
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = UITableViewCellSelectionStyle.None
        userAvatarView = AvatarView(frameSize: Int(contentView.bounds.width), type: .Square, placeholderImage: UIImage())
        userAvatarView.backgroundColor = MainAppTheme.chat.profileBgTint
        userAvatarView.clipsToBounds = true
        userAvatarView.enableAnimation = false
        contentView.addSubview(userAvatarView)
        
        shadow.image = UIImage(named: "CardTop3")
        contentView.addSubview(shadow)
        
        usernameLabel = UILabel()
        usernameLabel.backgroundColor = UIColor.clearColor()
        usernameLabel.textColor = MainAppTheme.text.bigAvatarPrimary
        usernameLabel.font = UIFont.boldSystemFontOfSize(20)
        usernameLabel.text = " "
        usernameLabel.sizeToFit()
        usernameLabel.clipsToBounds = false
        contentView.addSubview(usernameLabel)
        
        presenceLabel = UILabel()
        presenceLabel.backgroundColor = UIColor.clearColor()
        presenceLabel.textColor = MainAppTheme.text.bigAvatarSecondary
        presenceLabel.font = UIFont.systemFontOfSize(14)
        presenceLabel.text = " "
        presenceLabel.sizeToFit()
        presenceLabel.clipsToBounds = false
        contentView.addSubview(presenceLabel)
        
        // progress.alpha = 0
        contentView.addSubview(progress)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    func setUsername(username: String) {
        usernameLabel.text = username
        setNeedsLayout()
    }
    
    func setPresence(presence: String) {
        presenceLabel.text = presence
        setNeedsLayout()
    }
    
    func setProgress(isLoading: Bool) {
        if (isLoading) {
            progress.startAnimating()
            progress.showView()
        } else {
            progress.stopAnimating()
            progress.hideView()
        }
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        userAvatarView.frame = CGRect(x: 0.0, y: -1.0, width: contentView.bounds.width, height: contentView.bounds.height + 1.0)
        
        progress.frame = CGRectMake((contentView.bounds.width-64)/2, (contentView.bounds.height-64)/2, 64, 64)
        
        let textOriginX: CGFloat = 15.0
        usernameLabel.frame = CGRect(x: textOriginX, y: contentView.bounds.height - 53, width: contentView.bounds.size.width - textOriginX - 15.0, height: usernameLabel.bounds.size.height)
        presenceLabel.frame = CGRect(x: textOriginX, y: contentView.bounds.height - 30, width: usernameLabel.bounds.size.width, height: presenceLabel.bounds.size.height)
        
        shadow.frame = CGRectMake(0, contentView.bounds.height - 6, contentView.bounds.width, 6)
    }

}