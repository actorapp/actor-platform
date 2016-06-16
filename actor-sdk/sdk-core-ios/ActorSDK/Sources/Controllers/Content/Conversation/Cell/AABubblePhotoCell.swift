//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import VBFPopFlatButton
import YYImage
import YYWebImage
import YYCategories

public class AABubblePhotoCell : AABubbleBaseFileCell, NYTPhotosViewControllerDelegate {
    
    // Views
    
    var preview = UIImageView()
    let progress = AAProgressView(size: CGSizeMake(64, 64))
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Binded data
    
    var bindedLayout: PhotoCellLayout!
    var thumbLoaded = false
    var contentLoaded = false
    
    var thumbShown = false
    var previewShown = false
    
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
        
        preview.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubblePhotoCell.mediaDidTap)))
        preview.userInteractionEnabled = true
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    public override func bind(message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! PhotoCellLayout
        
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
            thumbShown = false
            previewShown = false
            
            // Reset progress
            self.progress.hideButton()
            //UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.hidden = true
                self.preview.hidden = true
            //})

            // Bind file
            fileBind(message, autoDownload: ActorSDK.sharedActor().isAutomaticDownloadEnabled)
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
        print("paused \(selfGeneration)")
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        print("uploading \(selfGeneration)")
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileDownloadPaused(selfGeneration: Int) {
        bgLoadThumb(selfGeneration)

        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    public override func fileDownloading(progress: Double, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)

        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    public override func fileReady(reference: String, selfGeneration: Int) {
        print("ready \(selfGeneration)")
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.setProgress(1)            
            self.progress.hideView()
        }
    }
    
    public func bgLoadThumb(selfGeneration: Int) {
        if (thumbLoaded) {
            return
        }
        thumbLoaded = true
        
        if (bindedLayout.fastThumb != nil) {
            
            let preloadedThumb = YYImage(data: bindedLayout.fastThumb!)!
            
            let loadedThumb = preloadedThumb.imageByBlurLight()!
                .roundCorners(bindedLayout.screenSize.width, h: bindedLayout.screenSize.height, roundSize: 14)
            
            runOnUiThread(selfGeneration,closure: { ()->() in
                self.setFastPreviewImage(loadedThumb)
            });
        }
    }
    
    public func bgLoadReference(reference: String, selfGeneration: Int) {
        if (contentLoaded) {
            return
        }
        contentLoaded = true
        
        let path = CocoaFiles.pathFromDescriptor(reference)
        
        runOnUiThread(selfGeneration, closure: { () -> () in
            self.setPreviewPath(path)
        })
        
    }
    
    public func setFastPreviewImage(img: UIImage) {
        if !previewShown && !thumbShown {
            thumbShown = true
            preview.image = img
            preview.hidden = false
        }
    }
    
    public func setPreviewPath(path: String) {
        if !previewShown {
            previewShown = true
            preview.yy_setImageWithURL(NSURL.fileURLWithPath(path),
                placeholder: nil,
                options: YYWebImageOptions(),
                progress: nil,
                transform: { (img, url) -> UIImage? in
                    return img.roundCorners(self.bindedLayout.screenSize.width, h: self.bindedLayout.screenSize.height, roundSize: 14)
                },
                completion: nil)
            preview.hidden = false
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
                    if let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference)) {
                        let previewImage = PreviewImage(image: img)
                        let previewController = AAPhotoPreviewController(photo: previewImage, fromView: self.preview)
                        previewController.autoShowBadge = true
                        self.controller.presentViewController(previewController, animated: true, completion: nil)
                    }
            }))
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    
                    if let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))) {
                        let previewImage = PreviewImage(image: img)
                        let previewController = AAPhotoPreviewController(photo: previewImage, fromView: self.preview)
                        previewController.autoShowBadge = true
                        self.controller.presentViewController(previewController, animated: true, completion: nil)
                    }
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
    
    // Photo preview
    
    public func photosViewController(photosViewController: NYTPhotosViewController, referenceViewForPhoto photo: NYTPhoto) -> UIView? {
        return self.preview
    }
    
    public func photosViewControllerWillDismiss(photosViewController: NYTPhotosViewController) {
        // (UIApplication.sharedApplication().delegate as! AppDelegate).showBadge()
        UIApplication.sharedApplication().setStatusBarHidden(false, withAnimation: UIStatusBarAnimation.Fade)
    }
}

/**
    Media cell layout
*/
public class PhotoCellLayout: AACellLayout {
    
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
        self.screenSize = AACellLayout.pickApproriateSize(width, height: height)
        
        // Prepare fast thumb
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "media", layouter: layouter)
    }
    
    /**
        Creating layout for photo content
    */
    public convenience init(id: Int64, photoContent: ACPhotoContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(id: id, width: CGFloat(photoContent.getW()), height: CGFloat(photoContent.getH()), date: date, fastThumb: photoContent.getFastThumb(), autoDownload: ActorSDK.sharedActor().autoDownloadPhotoContent, layouter: layouter)
    }

    /**
        Creating layout for message
    */
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        if let content = message.content as? ACPhotoContent {
            self.init(id: Int64(message.rid), photoContent: content, date: Int64(message.date), layouter: layouter)
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
    Layouter for media bubbles
*/
public class AABubblePhotoCellLayouter: AABubbleLayouter {
    
    public func isSuitable(message: ACMessage) -> Bool {
        if message.content is ACPhotoContent {
            return true
        }
        
        return false
    }
    
    public func buildLayout(peer: ACPeer, message: ACMessage) -> AACellLayout {
        return PhotoCellLayout(message: message, layouter: self)
    }
    
    public func cellClass() -> AnyClass {
        return AABubblePhotoCell.self
    }
}