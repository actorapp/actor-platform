//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AABubbleMediaCell : AABubbleBaseFileCell, NYTPhotosViewControllerDelegate {
    
    // Views
    
    let preview = UIImageView()
    let progress = CircullarLayerProgress(frame: CGRectZero)
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Layout
    
    var contentWidth = 0
    var contentHeight = 0
    var contentViewSize: CGSize? = nil
    
    // Binded data
    
    var thumb : ACFastThumb? = nil
    var thumbLoaded = false
    var contentLoaded = false
    
    // Constructors
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = Imaging.imageWithColor(MainAppTheme.bubbles.mediaDateBg, size: CGSize(width: 1, height: 1))

        timeLabel.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
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
            
            // Build bubble size
            if (message.content is ACPhotoContent) {
                var photo = message.content as! ACPhotoContent;
                thumb = photo.getFastThumb()
                contentWidth = Int(photo.getW())
                contentHeight = Int(photo.getH())
            } else if (message.content is ACVideoContent) {
                var video = message.content as! ACVideoContent;
                thumb = video.getFastThumb()
                contentWidth = Int(video.getW())
                contentHeight = Int(video.getH())
            } else {
                fatalError("Unsupported content")
            }
            contentViewSize = AABubbleMediaCell.measureMedia(contentWidth, h: contentHeight)
            
            // Reset loaded thumbs and contents
            preview.image = nil
            thumbLoaded = false
            contentLoaded = false
            
            // Reset progress
            self.progress.hideButton()
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.progress.alpha = 0
                self.preview.alpha = 0
            })

            // Bind file
            fileBind(message, autoDownload: message.content is ACPhotoContent)
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
            self.progress.hideView()
        }
    }
    
    func bgLoadThumb(selfGeneration: Int) {
        if (thumbLoaded) {
            return
        }
        thumbLoaded = true
        
        if (thumb != nil) {
            var loadedThumb = UIImage(data: self.thumb!.getImage().toNSData()!)?.roundCorners(contentViewSize!.width - 2, h: contentViewSize!.height - 2, roundSize: 14)
            
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
        
        var loadedContent = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))?.roundCorners(contentViewSize!.width - 2, h: contentViewSize!.height - 2, roundSize: 14)
        
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
        var content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: CocoaDownloadCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    var photoInfo = AAPhoto(image: UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))!)
                    var controller = NYTPhotosViewController(photos: [photoInfo])
                    controller.delegate = self
                    UIApplication.sharedApplication().setStatusBarHidden(true, withAnimation: UIStatusBarAnimation.Fade)
                    self.controller.presentViewController(controller, animated: true, completion: nil)
            }))
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            var rid = bindedMessage!.rid
            Actor.requestUploadStateWithRid(rid, withCallback: CocoaUploadCallback(
                notUploaded: { () -> () in
                    Actor.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    var photoInfo = AAPhoto(image: UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))!)
                    var controller = NYTPhotosViewController(photos: [photoInfo])
                    controller.delegate = self
                    
                    UIApplication.sharedApplication().setStatusBarHidden(true, withAnimation: UIStatusBarAnimation.Fade)
                    self.controller.presentViewController(controller, animated: true, completion: nil)
            }))
        }
    }
    
    // Layouting
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        var insets = fullContentInsets
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        var bubbleHeight = contentHeight - insets.top - insets.bottom
        var bubbleWidth = bubbleHeight * CGFloat(self.contentWidth) / CGFloat(self.contentHeight)
        
        layoutBubble(bubbleWidth, contentHeight: bubbleHeight)
        
        if (isOut) {
            preview.frame = CGRectMake(contentWidth - insets.left - bubbleWidth, insets.top, bubbleWidth, bubbleHeight)
        } else {
            preview.frame = CGRectMake(insets.left, insets.top, bubbleWidth, bubbleHeight)
        }
        
        progress.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 32, preview.frame.origin.y + preview.frame.height/2 - 32, 64, 64)
        
        timeLabel.frame = CGRectMake(0, 0, 1000, 1000)
        timeLabel.sizeToFit()
        
        var timeWidth = (isOut ? 23 : 0) + timeLabel.bounds.width
        var timeHeight: CGFloat = 20
        
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
        UIApplication.sharedApplication().setStatusBarHidden(false, withAnimation: UIStatusBarAnimation.Fade)
    }
    
    // Measures
    
    private class func measureMedia(w: Int, h: Int) -> CGSize {
        var screenScale = UIScreen.mainScreen().scale;
        var scaleW = 240 / CGFloat(w)
        var scaleH = 340 / CGFloat(h)
        var scale = min(scaleW, scaleH)
        return CGSize(width: scale * CGFloat(w), height: scale * CGFloat(h))
    }
    
    class func measureMediaHeight(message: ACMessage) -> CGFloat {
        if (message.content is ACPhotoContent) {
            var photo = message.content as! ACPhotoContent;
            return measureMedia(Int(photo.getW()), h: Int(photo.getH())).height + 2;
        } else if (message.content is ACVideoContent) {
            var video = message.content as! ACVideoContent;
            return measureMedia(Int(video.getW()), h: Int(video.getH())).height + 2;
        } else {
            fatalError("Unknown content type")
        }
    }
}



