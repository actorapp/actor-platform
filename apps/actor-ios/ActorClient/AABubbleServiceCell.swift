//
//  BubbleServiceCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 19.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation


// MARK: -

class AABubbleServiceCell : AABubbleCell {
    
    private static let serviceBubbleFont = UIFont(name: "HelveticaNeue-Medium", size: 12)!
    
    // MARK: -
    // MARK: Private vars
    
    private let serviceText = UILabel()
    
    // MARK: -
    // MARK: Constructors
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
       
        serviceText.font = AABubbleServiceCell.serviceBubbleFont;
        serviceText.lineBreakMode = .ByWordWrapping;
        serviceText.numberOfLines = 0;
        serviceText.textColor = UIColor.whiteColor()
        serviceText.contentMode = UIViewContentMode.Center
        serviceText.textAlignment = NSTextAlignment.Center
        
        contentView.addSubview(serviceText)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind

    override func bind(message: AMMessage, reuse: Bool) {
        if (!reuse) {
            serviceText.text = MSG.getFormatter().formatFullServiceMessageWithInt(message.getSenderId(), withAMServiceContent: message.getContent() as! AMServiceContent)
            bindBubbleType(.Service, isCompact: false)
        }
    }
    
    // MARK: -
    // MARK: Getters
    
    class func measureServiceHeight(message: AMMessage) -> CGFloat {
        var text = MSG.getFormatter().formatFullServiceMessageWithInt(message.getSenderId(), withAMServiceContent: message.getContent() as! AMServiceContent);
        return measureText(text).height + 3 + 3 // 3 text top 3 text bottom
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
        
        var bubbleTopPadding = AABubbleServiceCell.bubbleTopPadding()
        var bubbleBottomPadding = AABubbleServiceCell.bubbleBottomPadding()
        
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        var bubbleHeight = contentHeight - bubbleTopPadding - bubbleBottomPadding
        var bubbleWidth = CGFloat(AABubbleServiceCell.maxServiceTextWidth)
        
        let serviceTextSize = serviceText.sizeThatFits(CGSize(width: bubbleWidth, height: CGFloat.max))
        serviceText.frame = CGRectMake((contentWidth - serviceTextSize.width) / 2.0, bubbleTopPadding + 3, serviceTextSize.width, bubbleHeight - 6);
        
        layoutBubble(CGRectMake(serviceText.frame.origin.x - 8, serviceText.frame.origin.y - 3, serviceText.frame.width + 16, serviceText.frame.height + 6))
    }
    
    private static let maxServiceTextWidth = 260
    
    private class func measureText(message: String) -> CGRect {
        var messageValue = message as NSString;
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        var size = CGSize(width: maxServiceTextWidth, height: 0);
        var rect = messageValue.boundingRectWithSize(size, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes: [NSFontAttributeName: serviceBubbleFont, NSParagraphStyleAttributeName: style], context: nil);
        return CGRectMake(0, 0, round(rect.width), round(rect.height))
    }
}