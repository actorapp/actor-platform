//
//  BubbleUnsupportedContent.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 22.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class BubbleUnsupportedCell : BubbleCell {
    
    class func measureUnsupportedHeight(message: AMMessage) -> CGFloat {
        return 100+8;
    }
    
    let bubble = UIImageView()
    let unsupportedLabel = UILabel()
    var isOut = true
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        bubble.image = UIImage(named: "conv_media_bg")
        unsupportedLabel.text = "Unsupported content"
        unsupportedLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        unsupportedLabel.textAlignment = NSTextAlignment.Center
        
        contentView.addSubview(bubble)
        contentView.addSubview(unsupportedLabel)

        self.backgroundColor = UIColor.clearColor();
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool) {
        self.isOut = message.getSenderId() == MSG.myUid()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let padding = CGFloat(10)
        
        var width = contentView.frame.width
        var height = contentView.frame.height
        
        if (self.isOut) {
            self.bubble.frame = CGRectMake(width - 180 - padding, 4, 180, 100)
        } else {
            self.bubble.frame = CGRectMake(padding, 4, 180, 100)
        }
        self.unsupportedLabel.frame = self.bubble.frame
    }
}