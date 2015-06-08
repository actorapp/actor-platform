//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AAUserInfoCell: AATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var usernameLabel: UILabel!
    private var presenceLabel: UILabel!
    private let shadow = UIImageView()
    
    // MARK: -
    // MARK: Public vars
    
    var userAvatarView: AAAvatarView!
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = UITableViewCellSelectionStyle.None
        userAvatarView = AAAvatarView(frameSize: Int(contentView.bounds.width), type: AAAvatarType.Square, placeholderImage: UIImage())
        //userAvatarView.backgroundColor = UIColor(patternImage: UIImage(named: "bg_gplay")!.tintBgImage(MainAppTheme.chat.profileBgTint))
        userAvatarView.backgroundColor = MainAppTheme.chat.profileBgTint
        userAvatarView.clipsToBounds = true
        contentView.addSubview(userAvatarView)
        
        shadow.image = UIImage(named: "CardTop3")
        contentView.addSubview(shadow)
        
        usernameLabel = UILabel()
        usernameLabel.backgroundColor = UIColor.clearColor()
        usernameLabel.textColor = UIColor.whiteColor()
        usernameLabel.font = UIFont.boldSystemFontOfSize(20)
        usernameLabel.text = " "
        usernameLabel.sizeToFit()
//        usernameLabel.layer.shadowColor = UIColor.blackColor().CGColor
//        usernameLabel.layer.shadowOffset = CGSizeMake(0.0, Utils.retinaPixel());
//        usernameLabel.layer.shadowRadius = 0.0
//        usernameLabel.layer.shadowOpacity = 1.0
//        usernameLabel.layer.masksToBounds = false
        usernameLabel.clipsToBounds = false
        contentView.addSubview(usernameLabel)
        
        presenceLabel = UILabel()
        presenceLabel.backgroundColor = UIColor.clearColor()
        presenceLabel.textColor = Resources.SecondaryLightText
        presenceLabel.font = UIFont.systemFontOfSize(14)
        presenceLabel.text = " "
        presenceLabel.sizeToFit()
//        presenceLabel.layer.shadowColor = UIColor.blackColor().CGColor
//        presenceLabel.layer.shadowOffset = CGSizeMake(0.0, Utils.retinaPixel());
//        presenceLabel.layer.shadowRadius = 0.0
//        presenceLabel.layer.shadowOpacity = 1.0
//        presenceLabel.layer.masksToBounds = false
        presenceLabel.clipsToBounds = false
        contentView.addSubview(presenceLabel)
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
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        userAvatarView.frame = CGRect(x: 0.0, y: -1.0, width: contentView.bounds.width, height: contentView.bounds.height + 1.0)
        
        let textOriginX: CGFloat = 15.0
        usernameLabel.frame = CGRect(x: textOriginX, y: contentView.bounds.height - 53, width: contentView.bounds.size.width - textOriginX - 15.0, height: usernameLabel.bounds.size.height)
        presenceLabel.frame = CGRect(x: textOriginX, y: contentView.bounds.height - 30, width: usernameLabel.bounds.size.width, height: presenceLabel.bounds.size.height)
        
        shadow.frame = CGRectMake(0, contentView.bounds.height - 6, contentView.bounds.width, 6)
    }

}