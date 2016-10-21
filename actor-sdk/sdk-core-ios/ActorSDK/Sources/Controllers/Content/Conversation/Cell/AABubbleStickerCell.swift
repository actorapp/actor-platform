//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton
import YYImage

open class AABubbleStickerCell: AABubbleBaseFileCell {

    // Views
    
    let preview = UIImageView()
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Binded data
    
    var bindedLayout: StikerCellLayout!
    var contentLoaded = false
    
    fileprivate var callback: AAFileCallback? = nil
    
    // Constructors
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = ActorSDK.sharedActor().style.statusBackgroundImage
        
        timeLabel.font = UIFont.italicSystemFont(ofSize: 11)
        timeLabel.textColor = appStyle.chatMediaDateColor
        
        statusView.contentMode = UIViewContentMode.center
        
        preview.contentMode = .scaleAspectFit
        
        contentView.addSubview(preview)
        
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        preview.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleStickerCell.mediaDidTap)))
        preview.isUserInteractionEnabled = true
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! StikerCellLayout
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            
            
            bindBubbleType(BubbleType.sticker, isCompact: false)
            
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
            statusView.isHidden = false
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
            statusView.isHidden = true
        }
    }
    
    // File state binding
    
    open override func fileStateChanged(_ reference: String?, progress: Int?, isPaused: Bool, isUploading: Bool, selfGeneration: Int) {
        if let r = reference {
            if (contentLoaded) {
                return
            }
            contentLoaded = true
            
            let filePath = CocoaFiles.pathFromDescriptor(r)
            let loadedContent = YYImage(contentsOfFile: filePath)
            if (loadedContent == nil) {
                return
            }
            
            runOnUiThread(selfGeneration, closure: { () -> () in
                self.preview.image = loadedContent!
            })
        }
    }

    // Media Action
    
    open func mediaDidTap() {
        
        
    }
    
    // Layouting
    
    open override func layoutContent(_ maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        _ = self.contentView.frame.height
        let bubbleWidth = self.bindedLayout.screenSize.width
        let bubbleHeight = self.bindedLayout.screenSize.height
        
        layoutBubble(bubbleWidth, contentHeight: bubbleHeight)
        
        if (isOut) {
            preview.frame = CGRect(x: contentWidth - insets.left - bubbleWidth, y: insets.top, width: bubbleWidth, height: bubbleHeight)
        } else {
            preview.frame = CGRect(x: insets.left, y: insets.top, width: bubbleWidth, height: bubbleHeight)
        }
        
        //progress.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 32, preview.frame.origin.y + preview.frame.height/2 - 32, 64, 64)
        
        timeLabel.frame = CGRect(x: 0, y: 0, width: 1000, height: 1000)
        timeLabel.sizeToFit()
        
        let timeWidth = (isOut ? 23 : 0) + timeLabel.bounds.width
        let timeHeight: CGFloat = 20
        
        timeLabel.frame = CGRect(x: preview.frame.maxX - timeWidth - 18, y: preview.frame.maxY - timeHeight - 6, width: timeLabel.frame.width, height: timeHeight)
        
        if (isOut) {
            statusView.frame = CGRect(x: timeLabel.frame.maxX, y: timeLabel.frame.minY, width: 23, height: timeHeight)
        }
        
        timeBg.frame = CGRect(x: timeLabel.frame.minX - 4, y: timeLabel.frame.minY - 1, width: timeWidth + 8, height: timeHeight + 2)
    }
    
}

/**
 Media cell layout
 */
open class StikerCellLayout: AACellLayout {
    
    // public let fastThumb: NSData?
    open let contentSize: CGSize
    open let screenSize: CGSize
    open let autoDownload: Bool
    
    /**
     Creting layout for media bubble
     */
    public init(id: Int64, width: CGFloat, height:CGFloat, date: Int64, stickerContent: ACStickerContent?, autoDownload: Bool, layouter: AABubbleLayouter) {
        
        // Saving content size
        self.contentSize = CGSize(width: width, height: height)
        
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
open class AABubbleStickerCellLayouter: AABubbleLayouter {
    
    open func isSuitable(_ message: ACMessage) -> Bool {
        if message.content is ACStickerContent {
            return true
        }
        
        return false
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        return StikerCellLayout(message: message, layouter: self)
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleStickerCell.self
    }
}

