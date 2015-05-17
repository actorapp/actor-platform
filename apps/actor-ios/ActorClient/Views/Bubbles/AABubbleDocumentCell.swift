//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AABubbleDocumentCell: AABubbleCell {
    
    // MARK: -
    // MARK: Private vars
    
    private let circullarNode = CircullarNode()
    private let fileIcon = UIImageView()
    
    private let titleLabel = UILabel()
    private let sizeLabel = UILabel()
    
    private let dateLabel = UILabel()
    private let statusView = UIImageView()
    
    private var messageState: UInt = AMMessageState.UNKNOWN.rawValue
    
    var bindedDownloadFile: jlong? = nil
    var bindedDownloadCallback: CocoaDownloadCallback? = nil
    
    var bindedUploadFile: jlong? = nil
    var bindedUploadCallback: CocoaUploadCallback? = nil
    
    // MARK: -
    // MARK: Constructors
    
    init(reuseId: String, peer: AMPeer) {
        super.init(reuseId: reuseId, peer: peer, isFullSize: false)
        
        dateLabel.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
        dateLabel.lineBreakMode = .ByClipping
        dateLabel.numberOfLines = 1
        dateLabel.contentMode = UIViewContentMode.TopLeft
        dateLabel.textAlignment = NSTextAlignment.Right
        dateLabel.textColor = MainAppTheme.bubbles.textDateOut
        
        statusView.contentMode = UIViewContentMode.Center
        
        titleLabel.font = UIFont.systemFontOfSize(16.0)
        titleLabel.textColor = MainAppTheme.bubbles.textOut
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.lineBreakMode = NSLineBreakMode.ByTruncatingTail
        
        sizeLabel.font = UIFont.systemFontOfSize(13.0)
        sizeLabel.textColor = MainAppTheme.bubbles.textOut
        sizeLabel.text = " "
        sizeLabel.sizeToFit()
        
        fileIcon.image = UIImage(named: "file_apk")
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(sizeLabel)
        
        contentView.addSubview(dateLabel)
        contentView.addSubview(statusView)
        
        contentView.addSubview(fileIcon)
        contentView.addSubview(circullarNode.view)
        
        self.bubbleInsets = UIEdgeInsets(
            top: 3,
            left: 10,
            bottom: 3,
            right: 10)
        self.contentInsets = UIEdgeInsetsMake(0, 0, 0, 0)        
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    override func bind(message: AMMessage, reuse: Bool, isPreferCompact: Bool) {
        let document = message.getContent() as! AMDocumentContent
        
        if (!reuse) {
            if (isOut) {
                bindBubbleType(.MediaOut, isCompact: false)
                dateLabel.textColor = MainAppTheme.bubbles.textDateOut
            } else {
                bindBubbleType(.MediaIn, isCompact: false)
                dateLabel.textColor = MainAppTheme.bubbles.textDateIn
            }
            
            titleLabel.text = document.getName()
            
            let source = document.getSource()
            
            sizeLabel.text = MSG.getFormatter().formatFileSizeWithInt(source.getSize())
        }
        
        // Always update date and state
        dateLabel.text = formatDate(message.getDate())
        messageState = UInt(message.getMessageState().ordinal())
        
        if (isOut) {
            switch(self.messageState) {
            case AMMessageState.PENDING.rawValue:
                self.statusView.image = Resources.iconClock
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break
            case AMMessageState.SENT.rawValue:
                self.statusView.image = Resources.iconCheck1
                self.statusView.tintColor = MainAppTheme.bubbles.statusSent
                break
            case AMMessageState.RECEIVED.rawValue:
                self.statusView.image = Resources.iconCheck2
                self.statusView.tintColor = MainAppTheme.bubbles.statusReceived
                break
            case AMMessageState.READ.rawValue:
                self.statusView.image = Resources.iconCheck2
                self.statusView.tintColor = MainAppTheme.bubbles.statusRead
                break
            case AMMessageState.ERROR.rawValue:
                self.statusView.image = Resources.iconError
                self.statusView.tintColor = MainAppTheme.bubbles.statusError
                break
            default:
                self.statusView.image = Resources.iconClock
                self.statusView.tintColor = MainAppTheme.bubbles.statusSending
                break
            }
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    // MARK: -
    // MARK: Getters
    
    class func measureServiceHeight(message: AMMessage) -> CGFloat {
        return 66 + 6
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        var insets = fullContentInsets
        
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        layoutBubble(200, contentHeight: 66)
        
        var contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        self.titleLabel.frame = CGRectMake(contentLeft + 62, 16, 200 - 64, 22)
        self.sizeLabel.frame = CGRectMake(contentLeft + 62, 16 + 22, 200 - 64, 22)
        
        self.fileIcon.frame = CGRectMake(contentLeft + 8, 12, 48, 48)
        
        // var bubbleHeight = contentHeight - bubbleTopPadding - bubbleBottomPadding
        // var bubbleWidth = CGFloat(201)
        
//        var contentInsetY = CGFloat((self.isGroup ? self.groupContentInsetY : 0.0))
//        var contentInsetX = CGFloat((self.isGroup ? self.groupContentInsetX : 0.0))
//
//        if (self.isOut) {
//            // self.layoutBubble(CGRectMake(contentWidth - bubbleWidth - self.bubblePadding, bubbleTopPadding, bubbleWidth, bubbleHeight))
//            
//            self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 70 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
//            
//            self.titleLabel.frame = CGRect(x: self.bubble.frame.minX + 76.0, y: 25.0, width: bubbleWidth - 76.0 - 8.0 - 6.0, height: self.titleLabel.bounds.height)
//            self.sizeLabel.frame = CGRect(x: self.bubble.frame.minX + 76.0, y: 47.0, width: self.titleLabel.bounds.width, height: self.sizeLabel.bounds.height)
//        } else {
//            // self.layoutBubble(CGRectMake(self.bubblePadding + contentInsetX, bubbleTopPadding, bubbleWidth, bubbleHeight))
//            
//            self.dateLabel.frame = CGRectMake(self.bubble.frame.maxX - 47 - self.bubblePadding, self.bubble.frame.maxY - 24, 46, 26)
//            
//            self.titleLabel.frame = CGRect(x: self.bubble.frame.minX + 82.0, y: 25.0 + contentInsetY, width: bubbleWidth - 82.0 - 8.0, height: self.titleLabel.bounds.height)
//            self.sizeLabel.frame = CGRect(x: self.bubble.frame.minX + 82.0, y: 47.0 + contentInsetY, width: self.titleLabel.bounds.width, height: self.sizeLabel.bounds.height)
//        }
//        
        if (self.isOut) {
            self.statusView.frame = CGRectMake(self.bubble.frame.maxX - 24 - self.bubblePadding, self.bubble.frame.maxY - 24, 20, 26)
            self.statusView.hidden = false
        } else {
            self.statusView.hidden = true
        }
    }

}
