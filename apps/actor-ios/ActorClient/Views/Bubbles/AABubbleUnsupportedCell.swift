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
    // MARK: Constructors
    
    init(reuseId: String, peer: AMPeer) {
        super.init(reuseId: reuseId, peer: peer, isFullSize: false)
        
        unsupportedLabel.text = "Unsupported content" // TODO: localize
        unsupportedLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        unsupportedLabel.textAlignment = NSTextAlignment.Center
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool, isPreferCompact: Bool) {        
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
        
            var contentWidth = self.contentView.frame.width
            var contentHeight = self.contentView.frame.height
        
            var contentInsetY = CGFloat((self.isGroup ? self.groupContentInsetY : 0.0))
            var contentInsetX = CGFloat((self.isGroup ? self.groupContentInsetX : 0.0))
        
            if (self.isOut) {
                self.layoutBubble(CGRectMake(contentWidth - 180 - self.bubbleMediaPadding, AABubbleCell.bubbleTop, 180, 100))
            } else {
                self.layoutBubble(CGRectMake(self.bubbleMediaPadding + contentInsetX, AABubbleCell.bubbleTop, 180, 100))
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
