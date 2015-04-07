//
//  AAUserInfoCell.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/5/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAUserInfoCell: AATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var usernameLabel: UILabel!
    private var presenceLabel: UILabel!
    
    private var usernameTextField: UITextField!
    private var textFieldSeparator: UIView!
    
    private var usernameBeforeEditing: String?
    
    // MARK: -
    // MARK: Public vars
    
    var userAvatarView: AvatarView!
    
    var usernameChangedBlock: ((String) -> ())?
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = UITableViewCellSelectionStyle.None
        
        userAvatarView = AvatarView(frameSize: 65)
        contentView.addSubview(userAvatarView)
        
        usernameLabel = UILabel()
        usernameLabel.backgroundColor = UIColor.whiteColor()
        usernameLabel.textColor = UIColor.blackColor()
        usernameLabel.font = UIFont.boldSystemFontOfSize(20)
        usernameLabel.text = " "
        usernameLabel.sizeToFit()
        contentView.addSubview(usernameLabel)
        
        presenceLabel = UILabel()
        presenceLabel.backgroundColor = UIColor.whiteColor()
        presenceLabel.textColor = Resources.PrimaryDarkText
        presenceLabel.font = UIFont.systemFontOfSize(14)
        presenceLabel.text = " "
        presenceLabel.sizeToFit()
        contentView.addSubview(presenceLabel)
        
        usernameTextField = UITextField()
        usernameTextField.backgroundColor = UIColor.whiteColor()
        usernameTextField.textColor = UIColor.blackColor()
        usernameTextField.font = UIFont.systemFontOfSize(20.0)
        usernameTextField.enabled = false
        usernameTextField.frame = CGRect(x: 0.0, y: 0.0, width: 0.0, height: 40.0)
        usernameTextField.delegate = self
        contentView.addSubview(usernameTextField)
        
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
    
    func setUsername(username: String) {
        usernameLabel.text = username
        usernameTextField.text = username
        setNeedsLayout()
    }
    
    func setPresence(presence: String) {
        presenceLabel.text = presence
        setNeedsLayout()
    }
    
    override func setEditing(editing: Bool, animated: Bool) {
        setNeedsLayout()
        
        UIView.animateWithDuration(0.3, animations: { () -> Void in
            self.usernameLabel.alpha = (editing ? 0.0 : 1.0)
            self.presenceLabel.alpha = (editing ? 0.0 : 1.0)
            self.usernameTextField.alpha = (editing ? 1.0 : 0.0)
            self.textFieldSeparator.alpha = (editing ? 1.0 : 0.0)
        })
        usernameTextField.enabled = editing
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let userAvatarViewFrameSize: CGFloat = CGFloat(userAvatarView.frameSize)
        userAvatarView.frame = CGRect(x: 15.0, y: (contentView.bounds.size.height - userAvatarViewFrameSize) / 2.0, width: userAvatarViewFrameSize, height: userAvatarViewFrameSize)
        
        let textOriginX: CGFloat = 92.0
        usernameLabel.frame = CGRect(x: textOriginX, y: 21.0, width: contentView.bounds.size.width - textOriginX - 15.0, height: usernameLabel.bounds.size.height)
        presenceLabel.frame = CGRect(x: textOriginX, y: 48.0, width: usernameLabel.bounds.size.width, height: presenceLabel.bounds.size.height)
        
        usernameTextField.frame = CGRect(x: textOriginX, y: (contentView.bounds.size.height - usernameTextField.bounds.size.height) / 2.0 - 4.0, width: usernameLabel.bounds.size.width, height: usernameTextField.bounds.size.height)
        textFieldSeparator.frame = CGRect(x: textOriginX, y: usernameTextField.frame.origin.y + usernameTextField.bounds.size.height, width: contentView.bounds.size.width - textOriginX, height: Utils.retinaPixel())
    }

}


// MARK: -
// MARK: UITextField Delegate

extension AAUserInfoCell: UITextFieldDelegate {
    
    func textFieldDidBeginEditing(textField: UITextField) {
        usernameBeforeEditing = textField.text
    }
    
    func textFieldDidEndEditing(textField: UITextField) {
        if usernameBeforeEditing != nil && usernameBeforeEditing != textField.text {
            usernameChangedBlock?(textField.text)
        }
    }
    
}