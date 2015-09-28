//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupMemberCell: UATableViewCell {

    // Views
    
    var nameLabel = UILabel(style: "members.name")
    var onlineLabel = UILabel(style: "members.online")
    var avatarView = AvatarView(style: "avatar.round.small")
    
    // Binder
    
    var binder = Binder()
    
    // Contstructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(cellStyle: "members.cell", reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(avatarView)
        contentView.addSubview(nameLabel)
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
        
        // Bind name and avatar
        let name = user.getNameModel().get()
        nameLabel.text = name
        avatarView.bind(name, id: user.getId(), avatar: user.getAvatarModel().get())
        
        // Bind onlines
        binder.bind(user.getPresenceModel()) { (value: ACUserPresence?) -> () in

            if value != nil {
                self.onlineLabel.showView()
                self.onlineLabel.text = Actor.getFormatter().formatPresence(value!, withSex: user.getSex())
                if value!.state.ordinal() == jint(ACUserPresence_State.ONLINE.rawValue) {
                    self.onlineLabel.applyStyle("user.online")
                } else {
                    self.onlineLabel.applyStyle("user.offline")
                }
            } else {
                self.onlineLabel.alpha = 0
                self.onlineLabel.text = ""
            }
        }
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        binder.unbindAll()
    }
    
    // Layouting
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let userAvatarViewFrameSize: CGFloat = CGFloat(avatarView.frameSize)
        avatarView.frame = CGRect(x: 14.0, y: (contentView.bounds.size.height - userAvatarViewFrameSize) / 2.0, width: userAvatarViewFrameSize, height: userAvatarViewFrameSize)
        
        nameLabel.frame = CGRect(x: 65.0, y: 5, width: contentView.bounds.size.width - 65.0 - 15.0, height: 22)
        onlineLabel.frame = CGRect(x: 65.0, y: 27, width: contentView.bounds.size.width - 65.0 - 15.0, height: 16)
    }

}
