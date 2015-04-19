//
//  AABubbleUnsupportedCell.swift
//  ActorApp
//
//  Created by Danil Gontovnik on 4/10/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AABubbleUnsupportedCell: AABubbleCell {
    
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
        
        unsupportedLabel.text = "Unsupported content" // TODO: localize
        unsupportedLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        unsupportedLabel.textAlignment = NSTextAlignment.Center
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool, isPreferCompact: Bool) {
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
        
        if (!reuse) {
            if (isOut) {
                bindBubbleType(.MediaOut, isCompact: false)
            } else {
                bindBubbleType(.MediaIn, isCompact: false)
            }
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
        
        UIView.performWithoutAnimation { () -> Void in
        
            var bubbleTopPadding = AABubbleServiceCell.bubbleTopPadding()
        
            var contentWidth = self.contentView.frame.width
            var contentHeight = self.contentView.frame.height
        
            var contentInsetY = CGFloat((self.group ? self.groupContentInsetY : 0.0))
            var contentInsetX = CGFloat((self.group ? self.groupContentInsetX : 0.0))
        
            if (self.isOut) {
                self.layoutBubble(CGRectMake(contentWidth - 180 - self.bubbleMediaPadding, bubbleTopPadding, 180, 100))
            } else {
                self.layoutBubble(CGRectMake(self.bubbleMediaPadding + contentInsetX, bubbleTopPadding, 180, 100))
            }
        
            var labelFrame = self.bubble.frame
        
            if (!self.isOut) {
                labelFrame.origin.y = contentInsetY
                labelFrame.size.height -= contentInsetY
            }
        
            self.unsupportedLabel.frame = labelFrame
        }
    }
}
