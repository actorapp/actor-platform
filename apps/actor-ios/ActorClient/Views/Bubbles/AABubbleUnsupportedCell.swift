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
    
    private let bubble = UIImageView()
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
    }
    
    // MARK: -
    // MARK: Getters
    
    class func measureUnsupportedHeight(message: AMMessage) -> CGFloat {
        return 100
    }
    
    class func bubbleTopPadding() -> CGFloat {
        return 3
    }
    
    class func bubbleBottomPadding() -> CGFloat {
        return 3
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var bubbleTopPadding = BubbleServiceCell.bubbleTopPadding()
        
        var contentWidth = contentView.frame.width
        var contentHeight = contentView.frame.height
        
        if (isOut) {
            bubble.frame = CGRectMake(contentWidth - 180 - bubbleMediaPadding, bubbleTopPadding, 180, 100)
        } else {
            bubble.frame = CGRectMake(bubbleMediaPadding, bubbleTopPadding, 180, 100)
        }
        
        unsupportedLabel.frame = bubble.frame
    }
}
