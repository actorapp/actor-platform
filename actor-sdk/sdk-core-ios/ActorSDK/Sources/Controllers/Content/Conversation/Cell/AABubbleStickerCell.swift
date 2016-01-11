//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton
import SDWebImage

public class AABubbleStickerCell: AABubbleBaseFileCell {

    // Views
    
    let preview = UIImageView()
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Binded data
    
    var bindedLayout: StikerCellLayout!
    var thumbLoaded = false
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
    
    public override func bind(message: ACMessage, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! StikerCellLayout
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            
            
            bindBubbleType(BubbleType.Sticker, isCompact: false)
            
            // Reset content state
            self.preview.image = nil
            contentLoaded = false
            thumbLoaded = false
            
            // Reset progress

            let sticker = message.content as! ACStickerContent
            
            var fileLocation: ACFileReference?
            fileLocation = sticker.getSticker().getFileReference512()
            
            self.callback = AAFileCallback(onDownloaded: { (reference) -> () in
                
                let data = NSFileManager.defaultManager().contentsAtPath(CocoaFiles.pathFromDescriptor(reference))
                let image = UIImage.sd_imageWithWebPData(data)
                
                if (image == nil) {
                    return
                }
                
                dispatchOnUi {
                    
                    UIView.transitionWithView(self, duration: 0.3, options: UIViewAnimationOptions.TransitionCrossDissolve, animations: { () -> Void in
                        self.preview.image = image;
                        }, completion: nil)
                    
                }
            });
            Actor.bindRawFileWithReference(fileLocation, autoStart: true, withCallback: self.callback)
            
            
            // Bind file
            //fileBind(message, autoDownload: bindedLayout.autoDownload)
            
        }
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(UInt(message.messageState.ordinal())) {
            case ACMessageState.PENDING.rawValue:
                self.statusView.image = appStyle.chatIconClock;
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break;
            case ACMessageState.SENT.rawValue:
                self.statusView.image = appStyle.chatIconCheck1;
                self.statusView.tintColor = appStyle.chatStatusMediaSent
                break;
            case ACMessageState.RECEIVED.rawValue:
                self.statusView.image = appStyle.chatIconCheck2;
                self.statusView.tintColor = appStyle.chatStatusMediaReceived
                break;
            case ACMessageState.READ.rawValue:
                self.statusView.image = appStyle.chatIconCheck2;
                self.statusView.tintColor = appStyle.chatStatusMediaRead
                break;
            case ACMessageState.ERROR.rawValue:
                self.statusView.image = appStyle.chatIconError;
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
    
    public override func fileUploadPaused(reference: String, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
//            self.progress.showView()
//            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
//            self.progress.hideProgress()
        }
    }
    
    public override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
//            self.progress.showView()
//            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
//            self.progress.setProgress(progress)
        }
    }
    
    public override func fileDownloadPaused(selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
//            self.progress.showView()
//            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
//            self.progress.hideProgress()
        }
    }
    
    public override func fileDownloading(progress: Double, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
//            self.progress.showView()
//            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
//            self.progress.setProgress(progress)
        }
    }
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
//            self.progress.setProgress(1)
//            self.progress.hideView()
        }
    }
    
    public func bgLoadThumb(selfGeneration: Int) {
//        if (thumbLoaded) {
//            return
//        }
//        thumbLoaded = true
//        
//        if (bindedLayout.fastThumb != nil) {
//            let loadedThumb = UIImage(data: bindedLayout.fastThumb!)?
//                .roundCorners(bindedLayout.screenSize.width,
//                    h: bindedLayout.screenSize.height,
//                    roundSize: 14)
//            
//            runOnUiThread(selfGeneration,closure: { ()->() in
//                self.setPreviewImage(loadedThumb!, fast: true)
//            });
//        }
    }
    
    public func bgLoadReference(reference: String, selfGeneration: Int) {
        if (contentLoaded) {
            return
        }
        contentLoaded = true
        
        let loadedContent = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))?.roundCorners(self.bindedLayout.screenSize.width, h: self.bindedLayout.screenSize.height, roundSize: 14)
        
        if (loadedContent == nil) {
            return
        }
        
        runOnUiThread(selfGeneration, closure: { () -> () in
            self.setPreviewImage(loadedContent!, fast: false)
        })
    }
    
    public func setPreviewImage(img: UIImage, fast: Bool){
        if ((fast && self.preview.image == nil) || !fast) {
            self.preview.image = img;
            self.preview.showView()
        }
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
    
    public let fastThumb: NSData?
    public let contentSize: CGSize
    public let screenSize: CGSize
    public let autoDownload: Bool
    
    /**
     Creting layout for media bubble
     */
    public init(id: Int64, width: CGFloat, height:CGFloat, date: Int64, sticker: ACSticker?, autoDownload: Bool) {
        
        // Saving content size
        self.contentSize = CGSizeMake(width, height)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload
        
        self.screenSize = CGSize(width: width, height:height)
        
        // Prepare fast thumb
        self.fastThumb = sticker?.getFileReference256().toByteArray().toNSData()
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "media")
    }
    
    /**
     Creating layout for sticker content
     */
    public convenience init(id: Int64, stickerContent: ACStickerContent, date: Int64) {
       self.init(id: id, width: CGFloat(200), height: CGFloat(200), date: date, sticker: stickerContent.getSticker(), autoDownload: true)
        
    }

    
    /**
     Creating layout for message
     */
    public convenience init(message: ACMessage) {
        if let content = message.content as? ACStickerContent {
            self.init(id: Int64(message.rid), stickerContent: content, date: Int64(message.date))
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
        return StikerCellLayout(message: message)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleStickerCell.self
    }
}

