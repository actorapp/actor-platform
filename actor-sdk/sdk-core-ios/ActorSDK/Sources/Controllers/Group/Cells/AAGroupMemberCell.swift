//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAGroupMemberCell: AATableViewCell {

    // Views
    
    public var nameLabel = UILabel()
    public var onlineLabel = UILabel()
    public var avatarView = AAAvatarView()
    public var adminLabel = UILabel()
    
    // Binder
    
    public var binder = AABinder()
    
    // Contstructors
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        adminLabel.text = AALocalized("GroupMemberAdmin")
        adminLabel.sizeToFit()
        
        contentView.addSubview(avatarView)
        
        nameLabel.font = UIFont.systemFontOfSize(18.0)
        nameLabel.textColor = appStyle.cellTextColor
        contentView.addSubview(nameLabel)
        
        onlineLabel.font = UIFont.systemFontOfSize(14.0)
        contentView.addSubview(onlineLabel)
        
        adminLabel.font = UIFont.systemFontOfSize(14.0)
        adminLabel.textColor = appStyle.cellDestructiveColor
        contentView.addSubview(adminLabel)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    public func setUsername(username: String) {
        nameLabel.text = username
    }
    
    public func bind(user: ACUserVM, isAdmin: Bool) {
        
        // Bind name and avatar
        let name = user.getNameModel().get()
        nameLabel.text = name
        avatarView.bind(name, id: Int(user.getId()), avatar: user.getAvatarModel().get())
        
        // Bind admin flag
        adminLabel.hidden = !isAdmin
        
        // Bind onlines
        
        if user.isBot() {
            self.onlineLabel.textColor = self.appStyle.userOnlineColor
            self.onlineLabel.text = "bot"
            self.onlineLabel.alpha = 1
        } else {
            binder.bind(user.getPresenceModel()) { (value: ACUserPresence?) -> () in
                
                if value != nil {
                    self.onlineLabel.showView()
                    self.onlineLabel.text = Actor.getFormatter().formatPresence(value!, withSex: user.getSex())
                    if value!.state.ordinal() == ACUserPresence_State.ONLINE().ordinal() {
                        self.onlineLabel.textColor = self.appStyle.userOnlineColor
                    } else {
                        self.onlineLabel.textColor = self.appStyle.userOfflineColor
                    }
                } else {
                    self.onlineLabel.alpha = 0
                    self.onlineLabel.text = ""
                }
            }
        }
    }
    
    public override func prepareForReuse() {
        super.prepareForReuse()
        binder.unbindAll()
    }
    
    // Layouting
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        let userAvatarViewFrameSize: CGFloat = CGFloat(40)
        avatarView.frame = CGRect(x: 14.0, y: (contentView.bounds.size.height - userAvatarViewFrameSize) / 2.0, width: userAvatarViewFrameSize, height: userAvatarViewFrameSize)
        
        var w: CGFloat = contentView.bounds.size.width - 65.0 - 8.0
        
        if !adminLabel.hidden {
            adminLabel.frame = CGRect(x: contentView.width - adminLabel.width - 8, y: 5, width: adminLabel.width, height: 42)
            w -= adminLabel.width + 8
        }
        
        nameLabel.frame = CGRect(x: 65.0, y: 5, width: w, height: 22)
        onlineLabel.frame = CGRect(x: 65.0, y: 27, width: w, height: 16)
    }
}
