//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


// MARK: -

class AABubbleServiceCell : AABubbleCell {
    
    private static let serviceBubbleFont = UIFont.boldSystemFontOfSize(12)
    private static let maxServiceTextWidth: CGFloat = 260
    
    private let serviceText = UILabel()
    
    private var bindedLayout: ServiceCellLayout!
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: true)
       
        // Configuring service label
        serviceText.font = AABubbleServiceCell.serviceBubbleFont;
        serviceText.lineBreakMode = .ByWordWrapping;
        serviceText.numberOfLines = 0;
        serviceText.textColor = UIColor.whiteColor()
        serviceText.contentMode = UIViewContentMode.Center
        serviceText.textAlignment = NSTextAlignment.Center
        mainView.addSubview(serviceText)
        
        // Setting content and bubble insets
        contentInsets = UIEdgeInsets(top: 3, left: 8, bottom: 3, right: 8)
        bubbleInsets = UIEdgeInsets(top: 3, left: 0, bottom: 3, right: 0)
        
        // Setting bubble background
        bindBubbleType(.Service, isCompact: false)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: ACMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        self.bindedLayout = cellLayout as! ServiceCellLayout
        
        if (!reuse) {
            serviceText.text = bindedLayout.text
        }
    }
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {

        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        let serviceWidth = bindedLayout.textSize.width
        let serviceHeight = bindedLayout.textSize.height
        
        serviceText.frame = CGRectMake((contentWidth - serviceWidth) / 2.0, insets.top, serviceWidth, serviceHeight);
        
        layoutBubble(serviceWidth, contentHeight: serviceHeight)
    }
}

class ServiceCellLayout: CellLayout {

    var text: String
    var textSize: CGSize
    
    init(text: String, date: Int64) {
        
        // Saving text size
        self.text = text
        
        // Measuring text size
        self.textSize = UIViewMeasure.measureText(text, width: AABubbleServiceCell.maxServiceTextWidth, font: AABubbleServiceCell.serviceBubbleFont)
        
        // Creating layout
        super.init(height: textSize.height + 6, date: date, key: "service")
    }
}

class AABubbleServiceCellLayouter: AABubbleLayouter {
    
    func isSuitable(message: ACMessage) -> Bool {
        return message.content is ACServiceContent
    }
    
    func buildLayout(peer: ACPeer, message: ACMessage) -> CellLayout {
        var serviceText = Actor.getFormatter().formatFullServiceMessageWithSenderId(message.senderId, withContent: message.content as! ACServiceContent)
        
        return ServiceCellLayout(text: serviceText, date: Int64(message.date))
    }
    
    func cellClass() -> AnyClass {
        return AABubbleServiceCell.self
    }
}

