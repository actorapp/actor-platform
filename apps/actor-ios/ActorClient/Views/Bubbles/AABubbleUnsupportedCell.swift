//
//  AABubbleUnsupportedCell.swift
//  ActorApp
//
//  Created by Danil Gontovnik on 4/10/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AABubbleUnsupportedCell: BubbleCell {
    
    // MARK: -
    // MARK: Private vars

    private let unsupportedLabel = UILabel()
    
    // MARK: -
    // MARK: Public vars
    
    var isOut = true
    
    // MARK: -
    // MARK: Constructors
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        bubble.image = UIImage(named: "conv_media_bg")
        unsupportedLabel.text = "Unsupported content" // TODO: localize
        unsupportedLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        unsupportedLabel.textAlignment = NSTextAlignment.Center
        
        contentView.addSubview(bubble)
        contentView.addSubview(unsupportedLabel)
        
        backgroundColor = UIColor.clearColor()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool) {
        self.isOut = message.getSenderId() == MSG.myUid()
        
        if group && !isOut {
            if let user = MSG.getUsers().getWithLong(jlong(message.getSenderId())) as? AMUserVM {
                var username = ""
                if let uname = user.getName().get() as? String {
                    username = uname
                }
                senderNameLabel.text = username
                
                var color = Resources.placeHolderColors[Int(abs(user.getId())) % Resources.placeHolderColors.count];
                senderNameLabel.textColor = color
                
                let avatar: AMAvatar? = user.getAvatar().get() as? AMAvatar
                avatarView.bind(username, id: user.getId(), avatar: avatar)
            }
            contentView.addSubview(senderNameLabel)
            contentView.addSubview(avatarView)
        } else {
            senderNameLabel.removeFromSuperview()
            avatarView.removeFromSuperview()
        }
    }
    
    // MARK: -
    // MARK: Getters
    
    class func measureUnsupportedHeight(message: AMMessage) -> CGFloat {
        return 100
    }
    
    class func bubbleTopPadding() -> CGFloat {
        return 1 + Utils.retinaPixel()
    }
    
    class func bubbleBottomPadding() -> CGFloat {
        return 1 + Utils.retinaPixel()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var bubbleTopPadding = BubbleServiceCell.bubbleTopPadding()
        
        var contentWidth = contentView.frame.width
        var contentHeight = contentView.frame.height
        
        var contentInsetY = CGFloat((self.group ? self.groupContentInsetY : 0.0))
        var contentInsetX = CGFloat((self.group ? self.groupContentInsetX : 0.0))
        
        if (isOut) {
            bubble.frame = CGRectMake(contentWidth - 180 - bubbleMediaPadding, bubbleTopPadding, 180, 100)
        } else {
            bubble.frame = CGRectMake(bubbleMediaPadding + contentInsetX, bubbleTopPadding, 180, 100)
        }
        
        var labelFrame = bubble.frame
        
        if (!isOut) {
            labelFrame.origin.y = contentInsetY
            labelFrame.size.height -= contentInsetY
        }
        
        unsupportedLabel.frame = labelFrame
    }
}
