//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
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
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: true)
       
        serviceText.font = AABubbleServiceCell.serviceBubbleFont;
        serviceText.lineBreakMode = .ByWordWrapping;
        serviceText.numberOfLines = 0;
        serviceText.textColor = UIColor.whiteColor()
        serviceText.contentMode = UIViewContentMode.Center
        serviceText.textAlignment = NSTextAlignment.Center
        
        self.contentInsets = UIEdgeInsets(
            top: 3,
            left: 8,
            bottom: 3,
            right: 8)
        self.bubbleInsets = UIEdgeInsets(
            top: 3,
            left: 0,
            bottom: 3,
            right: 0)
        mainView.addSubview(serviceText)
        
        bindBubbleType(.Service, isCompact: false)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind

    override func bind(message: AMMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        if (!reuse) {
            serviceText.text = MSG.getFormatter().formatFullServiceMessageWithSenderId(message.getSenderId(), withContent: message.getContent() as! AMServiceContent)
        }
    }
    
    // MARK: -
    // MARK: Getters
    
    class func measureServiceHeight(message: AMMessage, isPreferCompact: Bool) -> CGFloat {
        var text = MSG.getFormatter().formatFullServiceMessageWithSenderId(message.getSenderId(), withContent: message.getContent() as! AMServiceContent)
        return measureText(text).height + 3 + 3 + 3 + (isPreferCompact ? 0 : 3)
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        var insets = fullContentInsets
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        var bubbleHeight = contentHeight - insets.top - insets.bottom
        var bubbleWidth = CGFloat(AABubbleServiceCell.maxServiceTextWidth)
        
        let serviceTextSize = serviceText.sizeThatFits(CGSize(width: bubbleWidth, height: CGFloat.max))
        serviceText.frame = CGRectMake((contentWidth - serviceTextSize.width) / 2.0, insets.top, serviceTextSize.width, serviceTextSize.height);
        
        layoutBubble(serviceTextSize.width, contentHeight: serviceTextSize.height)
    }
    
    private static let maxServiceTextWidth = 260
    
    private class func measureText(message: String) -> CGRect {
        println("measureText:service")
        var messageValue = message as NSString;
        var style = NSMutableParagraphStyle();
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        var size = CGSize(width: maxServiceTextWidth, height: 0);
        var rect = messageValue.boundingRectWithSize(size, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes: [NSFontAttributeName: serviceBubbleFont, NSParagraphStyleAttributeName: style], context: nil);
        return CGRectMake(0, 0, round(rect.width), round(rect.height))
    }
}