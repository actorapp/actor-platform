//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton
import AVFoundation
import YYImage

public class AABubbleVideoCell: AABubbleBaseFileCell {
    
    // Views
    
    let preview = UIImageView()
    let progress = AAProgressView(size: CGSizeMake(64, 64))
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    let playView = UIImageView(image: UIImage.bundled("aa_playbutton"))
    
    // Binded data
    
    var bindedLayout: VideoCellLayout!
    var thumbLoaded = false
    var contentLoaded = false
    
    // Constructors
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = ActorSDK.sharedActor().style.statusBackgroundImage
        
        timeLabel.font = UIFont.italicSystemFontOfSize(11)
        timeLabel.textColor = appStyle.chatMediaDateColor
        
        statusView.contentMode = UIViewContentMode.Center
        
        contentView.addSubview(preview)
        contentView.addSubview(progress)
        
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        contentView.addSubview(playView)
        
        preview.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleVideoCell.mediaDidTap)))
        preview.userInteractionEnabled = true
        
        playView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleVideoCell.mediaDidTap)))
        playView.userInteractionEnabled = true
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! VideoCellLayout
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            
            // Bind bubble
            if (self.isOut) {
                bindBubbleType(BubbleType.MediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.MediaIn, isCompact: false)
            }
            
            // Reset content state
            self.preview.image = nil
            contentLoaded = false
            thumbLoaded = false
            
            // Reset progress
            self.progress.hideButton()
            //UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.hidden = true
                self.preview.hidden = true
            //})
            
            // Bind file
            fileBind(message, autoDownload: bindedLayout.autoDownload)
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
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusMediaError
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break
            }
        } else {
            statusView.hidden = true
        }
    }
    
    // File state binding
    
    public override func fileUploadPaused(reference: String, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.playView.hideView()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.playView.hideView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileDownloadPaused(selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.playView.hideView()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileDownloading(progress: Double, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.playView.hideView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        self.bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.setProgress(1)
            self.progress.hideView()
            self.playView.showView()
        }
    }
    
    public func bgLoadThumb(selfGeneration: Int) {
        if (thumbLoaded) {
            return
        }
        
        thumbLoaded = true
        
        if (bindedLayout.fastThumb != nil) {
            let loadedThumb = UIImage(data: bindedLayout.fastThumb!)?
                .imageByBlurLight()!
                .roundCorners(bindedLayout.screenSize.width,
                    h: bindedLayout.screenSize.height,
                    roundSize: 14)
            
            runOnUiThread(selfGeneration,closure: { ()->() in
                self.setPreviewImage(loadedThumb!, fast: true)
            });
        }
        
    }
    
    public func bgLoadReference(reference: String, selfGeneration: Int) {
        
        let movieAsset = AVAsset(URL: NSURL(fileURLWithPath: CocoaFiles.pathFromDescriptor(reference))) // video asset
        let imageGenerator = AVAssetImageGenerator(asset: movieAsset)
        var thumbnailTime = movieAsset.duration
        thumbnailTime.value = 25
        
        do {
            
            let imageRef = try imageGenerator.copyCGImageAtTime(thumbnailTime, actualTime: nil)
            var thumbnail = UIImage(CGImage: imageRef)
            
            let orientation = movieAsset.videoOrientation()
            
            if (orientation.orientation.isPortrait) == true {
                thumbnail = thumbnail.imageRotatedByDegrees(90, flip: false)
            }
            
            let loadedContent = thumbnail.roundCorners(self.bindedLayout.screenSize.width, h: self.bindedLayout.screenSize.height, roundSize: 14)
            
            runOnUiThread(selfGeneration, closure: { () -> () in
                self.setPreviewImage(loadedContent, fast: false)
                self.contentLoaded = true
            })
            
        } catch {
            
        }
        
    }
    
    public func setPreviewImage(img: UIImage, fast: Bool){
        if ((fast && self.preview.image == nil) || !fast) {
            self.preview.image = img
            self.preview.showView()
        }
    }
    
    // Media Action
    
    public func mediaDidTap() {
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    
                    self.controller.playVideoFromPath(CocoaFiles.pathFromDescriptor(reference))
                    
            }))
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    
                    
                    self.controller.playVideoFromPath(CocoaFiles.pathFromDescriptor(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())))
                    
            }))
        }
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
        
        progress.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 32, preview.frame.origin.y + preview.frame.height/2 - 32, 64, 64)
        
        playView.frame = progress.frame
        
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

public class VideoCellLayout: AACellLayout {
    
    public let fastThumb: NSData?
    public let contentSize: CGSize
    public let screenSize: CGSize
    public let autoDownload: Bool
    
    /**
     Creting layout for media bubble
     */
    public init(id: Int64, width: CGFloat, height:CGFloat, date: Int64, fastThumb: ACFastThumb?, autoDownload: Bool, layouter: AABubbleLayouter) {
        
        // Saving content size
        self.contentSize = CGSizeMake(width, height)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload
        
        // Calculating bubble screen size
        let scaleW = 240 / width
        let scaleH = 240 / height
        let scale = min(scaleW, scaleH)
        self.screenSize = CGSize(width: scale * width, height: scale * height)
        
        // Prepare fast thumb
        print("video thumb === \(fastThumb?.getImage().toNSData())")
        
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "media", layouter: layouter)
    }
    
    
    /**
     Creating layout for video content
     */
    public convenience init(id: Int64, videoContent: ACVideoContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(id: id, width: CGFloat(videoContent.getW()), height: CGFloat(videoContent.getH()), date: date, fastThumb: videoContent.getFastThumb(), autoDownload: false, layouter: layouter)
    }
    
    /**
     Creating layout for message
     */
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        if let content = message.content as? ACVideoContent {
            self.init(id: Int64(message.rid), videoContent: content, date: Int64(message.date), layouter: layouter)
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}



public class AABubbleVideoCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        if message.content is ACVideoContent {
            return true
        }
        return false
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return VideoCellLayout(message: message, layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubbleVideoCell.self
    }
}