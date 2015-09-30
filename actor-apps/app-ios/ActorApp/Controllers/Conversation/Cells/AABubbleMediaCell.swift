//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AABubbleMediaCell : AABubbleBaseFileCell, NYTPhotosViewControllerDelegate {
    
    // Views
    
    let preview = UIImageView()
    let progress = CircullarLayerProgress(size: CGSizeMake(64, 64))
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Binded data
    
    var bindedLayout: MediaCellLayout!
    var thumbLoaded = false
    var contentLoaded = false
    
    // Constructors
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = Imaging.imageWithColor(MainAppTheme.bubbles.mediaDateBg, size: CGSize(width: 1, height: 1))

        timeLabel.font = UIFont.italicSystemFontOfSize(11)
        timeLabel.textColor = MainAppTheme.bubbles.mediaDate
        
        statusView.contentMode = UIViewContentMode.Center
        
        mainView.addSubview(preview)
        mainView.addSubview(progress)
        
        mainView.addSubview(timeBg)
        mainView.addSubview(timeLabel)
        mainView.addSubview(statusView)
        
        preview.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "mediaDidTap"))
        preview.userInteractionEnabled = true
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    override func bind(message: ACMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        self.bindedLayout = cellLayout as! MediaCellLayout
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (isIPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (isIPad ? 16 : 0))
        
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
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.alpha = 0
                self.preview.alpha = 0
            })

            // Bind file
            fileBind(message, autoDownload: bindedLayout.autoDownload)
        }
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(UInt(message.messageState.ordinal())) {
            case ACMessageState.PENDING.rawValue:
                self.statusView.image = Resources.iconClock;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaSending
                break;
            case ACMessageState.SENT.rawValue:
                self.statusView.image = Resources.iconCheck1;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaSent
                break;
            case ACMessageState.RECEIVED.rawValue:
                self.statusView.image = Resources.iconCheck2;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaReceived
                break;
            case ACMessageState.READ.rawValue:
                self.statusView.image = Resources.iconCheck2;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaRead
                break;
            case ACMessageState.ERROR.rawValue:
                self.statusView.image = Resources.iconError;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaError
                break
            default:
                self.statusView.image = Resources.iconClock;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaSending
                break;
            }
        } else {
            statusView.hidden = true
        }
    }
    
    // File state binding
    
    override func fileUploadPaused(reference: String, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    override func fileDownloadPaused(selfGeneration: Int) {
        bgLoadThumb(selfGeneration)

        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
            self.progress.hideProgress()
        }
    }
    
    override func fileDownloading(progress: Double, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)

        runOnUiThread(selfGeneration) { () -> () in
            self.progress.showView()
            self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
            self.progress.setProgress(progress)
        }
    }
    
    override func fileReady(reference: String, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        runOnUiThread(selfGeneration) { () -> () in
            self.progress.setProgress(1)            
            self.progress.hideView()
        }
    }
    
    func bgLoadThumb(selfGeneration: Int) {
        if (thumbLoaded) {
            return
        }
        thumbLoaded = true
        
        if (bindedLayout.fastThumb != nil) {
            let loadedThumb = UIImage(data: bindedLayout.fastThumb!)?
                .roundCorners(bindedLayout.screenSize.width,
                    h: bindedLayout.screenSize.height,
                    roundSize: 14)
            
            runOnUiThread(selfGeneration,closure: { ()->() in
                self.setPreviewImage(loadedThumb!, fast: true)
            });
        }
    }
    
    func bgLoadReference(reference: String, selfGeneration: Int) {
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
    
    func setPreviewImage(img: UIImage, fast: Bool){
        if ((fast && self.preview.image == nil) || !fast) {
            self.preview.image = img;
            self.preview.showView()
        }
    }
    
    // Media Action
    
    func mediaDidTap() {
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: CocoaDownloadCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    if let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference)) {
                        let previewImage = PreviewImage(image: img)
                        let previewController = PhotoPreviewController(photo: previewImage, fromView: self.preview)
                        self.controller.presentViewController(previewController, animated: true, completion: nil)
                    }
            }))
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: CocoaUploadCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    
                    if let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))) {
                        let previewImage = PreviewImage(image: img)
                        let previewController = PhotoPreviewController(photo: previewImage, fromView: self.preview)
                        self.controller.presentViewController(previewController, animated: true, completion: nil)
                    }
            }))
        }
    }
    
    // Layouting
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        let contentHeight = self.contentView.frame.height
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
        
        timeBg.frame = CGRectMake(timeLabel.frame.minX - 3, timeLabel.frame.minY - 1, timeWidth + 6, timeHeight + 2)
    }
    
    // Photo preview
    
    func photosViewController(photosViewController: NYTPhotosViewController!, referenceViewForPhoto photo: NYTPhoto!) -> UIView! {
        return self.preview
    }
    
    func photosViewControllerWillDismiss(photosViewController: NYTPhotosViewController!) {
        (UIApplication.sharedApplication().delegate as! AppDelegate).showBadge()
        UIApplication.sharedApplication().setStatusBarHidden(false, withAnimation: UIStatusBarAnimation.Fade)
    }
}

/**
    Media cell layout
*/
class MediaCellLayout: CellLayout {
    
    let fastThumb: NSData?
    let contentSize: CGSize
    let screenSize: CGSize
    let autoDownload: Bool
    
    /**
        Creting layout for media bubble
    */
    init(id: Int64, width: CGFloat, height:CGFloat, date: Int64, fastThumb: ACFastThumb?, autoDownload: Bool) {
        
        // Saving content size
        self.contentSize = CGSizeMake(width, height)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload

        // Calculating bubble screen size
        let scaleW = 240 / width
        let scaleH = 340 / height
        let scale = min(scaleW, scaleH)
        self.screenSize = CGSize(width: scale * width, height: scale * height)
        
        // Prepare fast thumb
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "media")
    }
    
    /**
        Creating layout for photo content
    */
    convenience init(id: Int64, photoContent: ACPhotoContent, date: Int64) {
        self.init(id: id, width: CGFloat(photoContent.getW()), height: CGFloat(photoContent.getH()), date: date, fastThumb: photoContent.getFastThumb(), autoDownload: true)
    }
    
    /**
        Creating layout for video content
    */
    convenience init(id: Int64, videoContent: ACVideoContent, date: Int64) {
        self.init(id: id, width: CGFloat(videoContent.getW()), height: CGFloat(videoContent.getH()), date: date, fastThumb: videoContent.getFastThumb(),autoDownload: false)
    }
    
    /**
        Creating layout for message
    */
    convenience init(message: ACMessage) {
        if let content = message.content as? ACPhotoContent {
            self.init(id: Int64(message.rid), photoContent: content, date: Int64(message.date))
        } else if let content = message.content as? ACVideoContent {
            self.init(id: Int64(message.rid), videoContent: content, date: Int64(message.date))
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
    Layouter for media bubbles
*/
class AABubbleMediaCellLayouter: AABubbleLayouter {
    
    func isSuitable(message: ACMessage) -> Bool {
        if message.content is ACPhotoContent {
            return true
        } else if message.content is ACVideoContent {
            return true
        }
        return false
    }
    
    func buildLayout(peer: ACPeer, message: ACMessage) -> CellLayout {
        return MediaCellLayout(message: message)
    }
    
    func cellClass() -> AnyClass {
        return AABubbleMediaCell.self
    }
}