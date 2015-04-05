//
//  AAConversationGroupInfoCell.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/2/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAConversationGroupInfoCell: AATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var groupNameBeforeEditing: String?
    private var textFieldSeparator: UIView!
    
    // MARK: -
    // MARK: Public vars
    
    var groupNameTextField: UITextField!
    var groupAvatarView: AvatarView!
    
    var groupNameChangedBlock: ((String) -> ())?
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        groupAvatarView = AvatarView(frameSize: 80)
        contentView.addSubview(groupAvatarView)
        
        groupNameTextField = UITextField()
        groupNameTextField.backgroundColor = UIColor.whiteColor()
        groupNameTextField.textColor = UIColor.blackColor()
        groupNameTextField.font = UIFont.systemFontOfSize(20.0)
        groupNameTextField.textAlignment = NSTextAlignment.Center
        groupNameTextField.enabled = false
        groupNameTextField.text = " "
        groupNameTextField.sizeToFit()
        groupNameTextField.delegate = self
        contentView.addSubview(groupNameTextField)
        
        textFieldSeparator = UIView()
        textFieldSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        textFieldSeparator.alpha = 0.0
        contentView.addSubview(textFieldSeparator)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters
    
    func setGroupName(groupName: String) {
        groupNameTextField.text = groupName
        setNeedsLayout()
    }
    
    override func setEditing(editing: Bool, animated: Bool) {
        UIView.animateWithDuration(0.3, animations: { () -> Void in
            self.textFieldSeparator.alpha = (editing ? 1.0 : 0.0)
        })
        groupNameTextField.enabled = editing
    }
    
    // MARK: -
    // MARK: Getters
    
    func groupName() -> String {
        return groupNameTextField.text
    }
    
    // MARK: -
    // MARK: Layout subviews 
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let groupAvatarViewFrameSize: CGFloat = CGFloat(groupAvatarView.frameSize)
        groupAvatarView.frame = CGRect(x: (contentView.bounds.size.width - groupAvatarViewFrameSize) / 2.0, y: 15, width: groupAvatarViewFrameSize, height: groupAvatarViewFrameSize)
        
        let groupNameTextFieldWidth = contentView.bounds.size.width - 40.0
        groupNameTextField.frame = CGRect(x: (contentView.bounds.size.width - groupNameTextFieldWidth) / 2.0, y: groupAvatarView.frame.origin.y + groupAvatarView.frame.size.height + 13, width: groupNameTextFieldWidth, height: groupNameTextField.bounds.size.height)
        
        let separatorHeight: CGFloat = Utils.isRetina() ? 0.5 : 1.0;
        textFieldSeparator.frame = CGRect(x: groupNameTextField.frame.origin.x, y: groupNameTextField.frame.origin.y + groupNameTextField.bounds.size.height + 10, width: groupNameTextField.bounds.size.width, height: separatorHeight)
    }
    
}

// MARK: -
// MARK: UITextField Delegate

extension AAConversationGroupInfoCell: UITextFieldDelegate {

    func textFieldDidBeginEditing(textField: UITextField) {
        groupNameBeforeEditing = textField.text
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
        if groupNameBeforeEditing != nil && groupNameBeforeEditing != textField.text {
            groupNameChangedBlock?(textField.text)
        }
    }
    
}
