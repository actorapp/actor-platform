//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class PublicCell: UITableViewCell {
    
    let avatarView = AvatarView(frameSize: 48, type: .Rounded)
    let title: UILabel = UILabel()
    let desc: UILabel = UILabel()
    let members: UILabel = UILabel()
    let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor)
    
    var group: ACPublicGroup!
    
    init() {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: "cell")
        
        title.font = UIFont.mediumSystemFontOfSize(19)
        title.textColor = MainAppTheme.list.dialogTitle
        
        members.font = UIFont.mediumSystemFontOfSize(19)
        members.textColor = MainAppTheme.list.dialogTitle
        
        desc.font = UIFont.systemFontOfSize(16)
        desc.textColor = MainAppTheme.list.dialogText
        desc.lineBreakMode = NSLineBreakMode.ByWordWrapping
        desc.numberOfLines = 2
        
        contentView.addSubview(avatarView)
        contentView.addSubview(title)
        contentView.addSubview(members)
        contentView.addSubview(desc)
        contentView.addSubview(separatorView)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(group: ACPublicGroup, isLast: Bool) {
        self.group = group
        
        title.text = self.group.title
        desc.text = self.group.getDescription()
        
        members.text = "\(self.group.members) members"
        if self.group.friends > 0 {
            members.text = members.text! + " and \(self.group.friends) friends"
        }
        
        avatarView.bind(self.group.title, id: self.group.groupId, avatar: self.group.avatar)
        
        separatorView.hidden = isLast
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let width = self.contentView.frame.width;
        let leftPadding = CGFloat(76);
        let padding = CGFloat(14);
        
        avatarView.frame = CGRectMake(padding, 18, 48, 48);
        
        title.frame = CGRectMake(leftPadding, 18, width - leftPadding - /*paddingRight*/padding, 20);
        
        desc.frame = CGRectMake(leftPadding, 40, width - leftPadding - /*paddingRight*/padding, 60)
        desc.sizeToFit()
        
        members.frame = CGRectMake(leftPadding, 80, width - leftPadding - /*paddingRight*/padding, 20)
        //members.sizeToFit()
        // members.frame = CGRectMake(width - members.bounds.width - padding, 80, members.bounds.width, 20)
        
        separatorView.frame = CGRectMake(leftPadding, 103.5, width, 0.5);
    }
}