//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AABubbleServiceCell : AABubbleCell {
    
    fileprivate static let serviceBubbleFont = UIFont.boldSystemFont(ofSize: 12)
    fileprivate static let maxServiceTextWidth: CGFloat = 260
    
    fileprivate let serviceText = YYLabel()
    
    fileprivate var bindedLayout: ServiceCellLayout!
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: true)
       
        // Configuring service label
        serviceText.font = AABubbleServiceCell.serviceBubbleFont;
        serviceText.lineBreakMode = .byWordWrapping;
        serviceText.numberOfLines = 0;
        serviceText.textColor = appStyle.chatServiceTextColor
        serviceText.contentMode = UIViewContentMode.center
        serviceText.textAlignment = NSTextAlignment.center
        contentView.addSubview(serviceText)
        
        // Setting content and bubble insets
        contentInsets = UIEdgeInsets(top: 3, left: 8, bottom: 3, right: 8)
        bubbleInsets = UIEdgeInsets(top: 3, left: 0, bottom: 3, right: 0)
        
        // Setting bubble background
        bindBubbleType(.service, isCompact: false)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! ServiceCellLayout
        
        if (!reuse) {
            serviceText.text = bindedLayout.text
        }
    }
    
    open override func layoutContent(_ maxWidth: CGFloat, offsetX: CGFloat) {

        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        let serviceWidth = bindedLayout.textSize.width
        let serviceHeight = bindedLayout.textSize.height
        
        serviceText.frame = CGRect(x: (contentWidth - serviceWidth) / 2.0, y: insets.top, width: serviceWidth, height: serviceHeight);
        
        layoutBubble(serviceWidth, contentHeight: serviceHeight)
    }
}

open class ServiceCellLayout: AACellLayout {

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

open class AABubbleServiceCellLayouter: AABubbleLayouter {
    
    open func isSuitable(_ message: ACMessage) -> Bool {
        return message.content is ACServiceContent
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        
        let isChannel: Bool
        if peer.isGroup {
            isChannel = Actor.getGroupWithGid(peer.peerId).groupType == ACGroupType.channel()
        } else {
            isChannel = false
        }
        
        let serviceText = Actor.getFormatter().formatFullServiceMessage(withSenderId: message.senderId, with: message.content as! ACServiceContent, withIsChannel: isChannel)
        
        return ServiceCellLayout(text: serviceText!, date: Int64(message.date), layouter: self)
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleServiceCell.self
    }
}

