//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class GroupPhotoCell: AACommonCell {
    
    // MARK: -
    // MARK: Public vars
    
    public var groupNameLabel: UILabel!
    public var groupAvatarView: AvatarView!
    
    private let shadow = UIImageView()
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        groupAvatarView = AvatarView(frameSize: Int(contentView.bounds.width), type: .Square, placeholderImage: UIImage())
        groupAvatarView.backgroundColor = MainAppTheme.chat.profileBgTint
        groupAvatarView.clipsToBounds = true
        contentView.addSubview(groupAvatarView)
        
        shadow.image = UIImage(named: "CardTop3")
        contentView.addSubview(shadow)
        
        groupNameLabel = UILabel()
        groupNameLabel.backgroundColor = UIColor.clearColor()
        groupNameLabel.textColor = MainAppTheme.text.bigAvatarPrimary
        groupNameLabel.font = UIFont.systemFontOfSize(20.0)
        groupNameLabel.text = " "
        groupNameLabel.sizeToFit()
        groupNameLabel.clipsToBounds = false
        contentView.addSubview(groupNameLabel)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    public func setGroupName(groupName: String) {
        groupNameLabel.text = groupName
        setNeedsLayout()
    }
    
    // MARK: -
    // MARK: Getters
    
    public func groupName() -> String {
        return groupNameLabel.text!
    }
    
    // MARK: -
    // MARK: Layout subviews 
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        groupAvatarView.frame = CGRect(x: 0.0, y: -1.0, width: contentView.bounds.width, height: contentView.bounds.height + 1.0)
        
        let groupNameLabelWidth = contentView.bounds.size.width - 30.0
        groupNameLabel.frame = CGRect(x: 15.0, y: contentView.bounds.height - 33, width: groupNameLabelWidth, height: groupNameLabel.bounds.size.height)
        
        shadow.frame = CGRect(x: 0, y: contentView.bounds.height - 6, width: contentView.bounds.width, height: 6)
    }
    
}