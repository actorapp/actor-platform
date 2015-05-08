//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AAConversationGroupInfoCell: AATableViewCell {
    
    // MARK: -
    // MARK: Public vars
    
    var groupNameLabel: UILabel!
    var groupAvatarView: AAAvatarView!
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        println("\(contentView.bounds.width)")
        groupAvatarView = AAAvatarView(frameSize: Int(contentView.bounds.width), type: AAAvatarType.Square, placeholderImage: UIImage())
        groupAvatarView.backgroundColor = MainAppTheme.chat.profileBgTint
        contentView.addSubview(groupAvatarView)
        
        groupNameLabel = UILabel()
        groupNameLabel.backgroundColor = UIColor.clearColor()
        groupNameLabel.textColor = UIColor.whiteColor()
        groupNameLabel.font = UIFont.systemFontOfSize(20.0)
        groupNameLabel.text = " "
        groupNameLabel.sizeToFit()
        groupNameLabel.layer.shadowColor = UIColor.blackColor().CGColor
        groupNameLabel.layer.shadowOffset = CGSizeMake(0.0, Utils.retinaPixel());
        groupNameLabel.layer.shadowRadius = 0.0
        groupNameLabel.layer.shadowOpacity = 1.0
        groupNameLabel.layer.masksToBounds = false
        groupNameLabel.clipsToBounds = false
        contentView.addSubview(groupNameLabel)
        
        layer.masksToBounds = true
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    func setGroupName(groupName: String) {
        groupNameLabel.text = groupName
        setNeedsLayout()
    }
    
    // MARK: -
    // MARK: Getters
    
    func groupName() -> String {
        return groupNameLabel.text!
    }
    
    // MARK: -
    // MARK: Layout subviews 
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        groupAvatarView.frame = CGRect(x: 0.0, y: -1.0, width: contentView.bounds.width, height: contentView.bounds.height + 1.0)
        
        let groupNameLabelWidth = contentView.bounds.size.width - 30.0
        groupNameLabel.frame = CGRect(x: 15.0, y: contentView.bounds.height - 33, width: groupNameLabelWidth, height: groupNameLabel.bounds.size.height)
    }
    
}