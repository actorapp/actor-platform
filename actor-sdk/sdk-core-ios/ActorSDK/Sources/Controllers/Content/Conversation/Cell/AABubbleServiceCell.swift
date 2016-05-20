//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AABubbleServiceCell : AABubbleCell {
    
    private static let serviceBubbleFont = UIFont.boldSystemFontOfSize(12)
    private static let maxServiceTextWidth: CGFloat = 260
    
    private let serviceText = YYLabel()
    
    private var bindedLayout: ServiceCellLayout!
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: true)
       
        // Configuring service label
        serviceText.font = AABubbleServiceCell.serviceBubbleFont;
        serviceText.lineBreakMode = .ByWordWrapping;
        serviceText.numberOfLines = 0;
        serviceText.textColor = appStyle.chatServiceTextColor
        serviceText.contentMode = UIViewContentMode.Center
        serviceText.textAlignment = NSTextAlignment.Center
        contentView.addSubview(serviceText)
        
        // Setting content and bubble insets
        contentInsets = UIEdgeInsets(top: 3, left: 8, bottom: 3, right: 8)
        bubbleInsets = UIEdgeInsets(top: 3, left: 0, bottom: 3, right: 0)
        
        // Setting bubble background
        bindBubbleType(.Service, isCompact: false)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! ServiceCellLayout
        
        if (!reuse) {
            serviceText.text = bindedLayout.text
        }
    }
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {

        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        let serviceWidth = bindedLayout.textSize.width
        let serviceHeight = bindedLayout.textSize.height
        
        serviceText.frame = CGRectMake((contentWidth - serviceWidth) / 2.0, insets.top, serviceWidth, serviceHeight);
        
        layoutBubble(serviceWidth, contentHeight: serviceHeight)
    }
}

public class ServiceCellLayout: AACellLayout {

    var text: String
    var textSize: CGSize
    
    public init(text: String, date: Int64, layouter: AABubbleLayouter) {
        
        // Saving text size
        self.text = text
        
        // Measuring text size
        self.textSize = UIViewMeasure.measureText(text, width: AABubbleServiceCell.maxServiceTextWidth, font: AABubbleServiceCell.serviceBubbleFont)
        
        // Creating layout
        super.init(height: textSize.height + 6, date: date, key: "service", layouter: layouter)
    }
}

public class AABubbleServiceCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        return message.content is ACServiceContent
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        let serviceText = Actor.getFormatter().formatFullServiceMessageWithSenderId(message.senderId, withContent: message.content as! ACServiceContent)
        
        return ServiceCellLayout(text: serviceText, date: Int64(message.date), layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleServiceCell.self
    }
}

