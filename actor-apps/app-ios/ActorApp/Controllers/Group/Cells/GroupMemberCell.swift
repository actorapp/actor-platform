//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupMemberCell: UATableViewCell {

    // Views
    
    var nameLabel = UILabel()
    var onlineLabel = UILabel()
    var avatarView = AvatarView(frameSize: 40, type: .Rounded)
    
    // Binder
    
    var binder = Binder()
    
    // Contstructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(avatarView)
        
        nameLabel = UILabel()
        nameLabel.textColor = MainAppTheme.list.textColor
        nameLabel.font = UIFont.systemFontOfSize(18.0)
        contentView.addSubview(nameLabel)
        
        onlineLabel = UILabel()
        onlineLabel.textColor = MainAppTheme.list.textColor
        onlineLabel.font = UIFont.systemFontOfSize(16)
        contentView.addSubview(onlineLabel)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    func setUsername(username: String) {
        nameLabel.text = username
    }
    
    func bind(user: ACUserVM) {
        unbind()
        
        let name = user.getNameModel().get()
        nameLabel.text = name
        avatarView.bind(name, id: user.getId(), avatar: user.getAvatarModel().get())
        
        // Bind onlines
        binder.bind(user.getPresenceModel()) { (value: ACUserPresence?) -> () in
            if value != nil {
                self.onlineLabel.text = Actor.getFormatter().formatPresence(value!, withSex: user.getSex())
            } else {
                self.onlineLabel.text = ""
            }
        }
    }
    
    func unbind() {
        binder.unbindAll()
    }
    
    // Layouting
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let userAvatarViewFrameSize: CGFloat = CGFloat(avatarView.frameSize)
        avatarView.frame = CGRect(x: 14.0, y: (contentView.bounds.size.height - userAvatarViewFrameSize) / 2.0, width: userAvatarViewFrameSize, height: userAvatarViewFrameSize)
        
        nameLabel.frame = CGRect(x: 65.0, y: 10, width: contentView.bounds.size.width - 65.0 - 15.0, height: 20)
        onlineLabel.frame = CGRect(x: 65.0, y: 30, width: contentView.bounds.size.width - 65.0 - 15.0, height: 20)
    }

}
