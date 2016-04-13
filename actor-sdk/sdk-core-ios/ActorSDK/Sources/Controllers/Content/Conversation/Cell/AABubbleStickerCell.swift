//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton
import YYImage

public class AABubbleStickerCell: AABubbleBaseFileCell {

    // Views
    
    let preview = UIImageView()
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Binded data
    
    var bindedLayout: StikerCellLayout!
    var contentLoaded = false
    
    private var callback: AAFileCallback? = nil
    
    // Constructors
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = ActorSDK.sharedActor().style.statusBackgroundImage
        
        timeLabel.font = UIFont.italicSystemFontOfSize(11)
        timeLabel.textColor = appStyle.chatMediaDateColor
        
        statusView.contentMode = UIViewContentMode.Center
        
        preview.contentMode = .ScaleAspectFit
        
        contentView.addSubview(preview)
        
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        preview.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "mediaDidTap"))
        preview.userInteractionEnabled = true
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! StikerCellLayout
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            
            
            bindBubbleType(BubbleType.Sticker, isCompact: false)
            
            // Reset content state
            preview.image = nil
            contentLoaded = false

            // Bind file
            fileBind(message, autoDownload: true)
            
        }
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusMediaRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusMediaReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusMediaSent
                }
                break
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusMediaError
                break
            default:
                self.statusView.image = appStyle.chatIconClock;
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break;
            }
        } else {
            statusView.hidden = true
        }
    }
    
    // File state binding
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        
        if (contentLoaded) {
            return
        }
        contentLoaded = true
        
        let filePath = CocoaFiles.pathFromDescriptor(reference)
        let loadedContent = YYImage(contentsOfFile: filePath)
        if (loadedContent == nil) {
            return
        }
        
        runOnUiThread(selfGeneration, closure: { () -> () in
            self.preview.image = loadedContent!
        })
    }

    // Media Action
    
    public func mediaDidTap() {
        
        
    }
    
    // Layouting
    
    public override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        _ = self.contentView.frame.height
        let bubbleWidth = self.bindedLayout.screenSize.width
        let bubbleHeight = self.bindedLayout.screenSize.height
        
        layoutBubble(bubbleWidth, contentHeight: bubbleHeight)
        
        if (isOut) {
            preview.frame = CGRectMake(contentWidth - insets.left - bubbleWidth, insets.top, bubbleWidth, bubbleHeight)
        } else {
            preview.frame = CGRectMake(insets.left, insets.top, bubbleWidth, bubbleHeight)
        }
        
        //progress.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 32, preview.frame.origin.y + preview.frame.height/2 - 32, 64, 64)
        
        timeLabel.frame = CGRectMake(0, 0, 1000, 1000)
        timeLabel.sizeToFit()
        
        let timeWidth = (isOut ? 23 : 0) + timeLabel.bounds.width
        let timeHeight: CGFloat = 20
        
        timeLabel.frame = CGRectMake(preview.frame.maxX - timeWidth - 18, preview.frame.maxY - timeHeight - 6, timeLabel.frame.width, timeHeight)
        
        if (isOut) {
            statusView.frame = CGRectMake(timeLabel.frame.maxX, timeLabel.frame.minY, 23, timeHeight)
        }
        
        timeBg.frame = CGRectMake(timeLabel.frame.minX - 4, timeLabel.frame.minY - 1, timeWidth + 8, timeHeight + 2)
    }
    
}

/**
 Media cell layout
 */
public class StikerCellLayout: AACellLayout {
    
    // public let fastThumb: NSData?
    public let contentSize: CGSize
    public let screenSize: CGSize
    public let autoDownload: Bool
    
    /**
     Creting layout for media bubble
     */
    public init(id: Int64, width: CGFloat, height:CGFloat, date: Int64, stickerContent: ACStickerContent?, autoDownload: Bool, layouter: AABubbleLayouter) {
        
        // Saving content size
        self.contentSize = CGSizeMake(width, height)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload
        
        self.screenSize = CGSize(width: width, height:height)
        
        // Prepare fast thumb
        // self.fastThumb = sticker?.getFileReference256().toByteArray().toNSData()
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "media", layouter: layouter)
    }
    
    /**
     Creating layout for sticker content
     */
    public convenience init(id: Int64, stickerContent: ACStickerContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(id: id, width: CGFloat(150), height: CGFloat(150), date: date, stickerContent: stickerContent, autoDownload: true, layouter: layouter)
        
    }

    
    /**
     Creating layout for message
     */
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        if let content = message.content as? ACStickerContent {
            self.init(id: Int64(message.rid), stickerContent: content, date: Int64(message.date), layouter: layouter)
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
 Layouter for media bubbles
 */
public class AABubbleStickerCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        if message.content is ACStickerContent {
            return true
        }
        
        return false
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return StikerCellLayout(message: message, layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleStickerCell.self
    }
}

