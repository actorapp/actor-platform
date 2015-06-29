//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class AABubbleMediaCell : AABubbleBaseFileCell {
    
    // Views
    let preview = UIImageView()

    let progressBg = UIImageView()
    let circullarNode = CircullarNode()
    let fileStatusIcon = UIImageView()
    
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    
    // Layout
    var contentWidth = 0
    var contentHeight = 0
    var contentViewSize: CGSize? = nil
    
    // Binded data
    var thumb : AMFastThumb? = nil
    var thumbLoaded = false
    var contentLoaded = false
    
    // MARK: -
    // MARK: Constructors
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = Imaging.imageWithColor(MainAppTheme.bubbles.mediaDateBg, size: CGSize(width: 1, height: 1))

        timeLabel.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
        timeLabel.textColor = MainAppTheme.bubbles.mediaDate
        
        statusView.contentMode = UIViewContentMode.Center
        fileStatusIcon.contentMode = UIViewContentMode.Center
        progressBg.image = Imaging.roundedImage(UIColor(red: 0, green: 0, blue: 0, alpha: 0x64/255.0), size: CGSizeMake(CGFloat(64.0),CGFloat(64.0)), radius: CGFloat(32.0))
        
        mainView.addSubview(preview)
        
        mainView.addSubview(progressBg)
        mainView.addSubview(fileStatusIcon)
        mainView.addSubview(circullarNode.view)
        
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
    
    // MARK: -
    
    override func bind(message: AMMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10,
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10)
        
        if (!reuse) {
            
            // Bind bubble
            if (self.isOut) {
                bindBubbleType(BubbleType.MediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.MediaIn, isCompact: false)
            }
            
            // Build bubble size
            if (message.getContent() is AMPhotoContent) {
                var photo = message.getContent() as! AMPhotoContent;
                thumb = photo.getFastThumb()
                contentWidth = Int(photo.getW())
                contentHeight = Int(photo.getH())
            } else if (message.getContent() is AMVideoContent) {
                var video = message.getContent() as! AMVideoContent;
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
            circullarNode.hidden = true
            circullarNode.setProgress(0, animated: false)
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.circullarNode.alpha = 0
                self.preview.alpha = 0
                self.progressBg.alpha = 0
            })
            
            // Bind file
            fileBind(message, autoDownload: message.getContent() is AMPhotoContent)
        }
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.hidden = false
            switch(UInt(message.getMessageState().ordinal())) {
            case AMMessageState.PENDING.rawValue:
                self.statusView.image = Resources.iconClock;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaSending
                break;
            case AMMessageState.SENT.rawValue:
                self.statusView.image = Resources.iconCheck1;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaSent
                break;
            case AMMessageState.RECEIVED.rawValue:
                self.statusView.image = Resources.iconCheck2;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaReceived
                break;
            case AMMessageState.READ.rawValue:
                self.statusView.image = Resources.iconCheck2;
                self.statusView.tintColor = MainAppTheme.bubbles.statusMediaRead
                break;
            case AMMessageState.ERROR.rawValue:
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
    
    func mediaDidTap() {
        var content = bindedMessage!.getContent() as! AMDocumentContent
        if let fileSource = content.getSource() as? AMFileRemoteSource {
            MSG.requestStateWithFileId(fileSource.getFileReference().getFileId(), withCallback: CocoaDownloadCallback(
                notDownloaded: { () -> () in
                    MSG.startDownloadingWithReference(fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    MSG.cancelDownloadingWithFileId(fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    var imageInfo = JTSImageInfo()
                    imageInfo.image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference))
                    imageInfo.referenceRect = self.preview.bounds
                    imageInfo.referenceView = self.preview
                    
                    var previewController = JTSImageViewController(imageInfo: imageInfo, mode: JTSImageViewControllerMode.Image, backgroundStyle: JTSImageViewControllerBackgroundOptions.Blurred)
                    previewController.showFromViewController(self.controller, transition: JTSImageViewControllerTransition._FromOriginalPosition)
            }))
        } else if let fileSource = content.getSource() as? AMFileLocalSource {
            var rid = bindedMessage!.getRid()
            MSG.requestUploadStateWithRid(rid, withCallback: CocoaUploadCallback(
                notUploaded: { () -> () in
                    MSG.resumeUploadWithRid(rid)
                }, onUploading: { (progress) -> () in
                    MSG.pauseUploadWithRid(rid)
                }, onUploadedClosure: { () -> () in
                    var imageInfo = JTSImageInfo()
                    imageInfo.image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))
                    imageInfo.referenceRect = self.preview.bounds
                    imageInfo.referenceView = self.preview
                    
                    var previewController = JTSImageViewController(imageInfo: imageInfo, mode: JTSImageViewControllerMode.Image, backgroundStyle: JTSImageViewControllerBackgroundOptions.Blurred)
                    previewController.showFromViewController(self.controller, transition: JTSImageViewControllerTransition._FromOriginalPosition)
            }))
        }
    }
    
    override func fileUploadPaused(reference: String, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        bgShowState(selfGeneration)
        bgShowIcon("ic_upload", selfGeneration: selfGeneration)
        bgHideProgress(selfGeneration)
    }
    
    override func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        bgShowState(selfGeneration)
        bgHideIcon(selfGeneration)
        bgShowProgress(progress, selfGeneration: selfGeneration)
    }
    
    override func fileDownloadPaused(selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        
        bgShowState(selfGeneration)
        bgShowIcon("ic_download", selfGeneration: selfGeneration)
        bgHideProgress(selfGeneration)
    }
    
    override func fileDownloading(progress: Double, selfGeneration: Int) {
        bgLoadThumb(selfGeneration)
        
        bgShowState(selfGeneration)
        bgHideIcon(selfGeneration)
        bgShowProgress(progress, selfGeneration: selfGeneration)
    }
    
    override func fileReady(reference: String, selfGeneration: Int) {
        bgLoadReference(reference, selfGeneration: selfGeneration)
        
        bgHideState(selfGeneration)
        bgHideIcon(selfGeneration)
        bgHideProgress(selfGeneration)
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
    
    // Progress show/hide
    func bgHideProgress(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            UIView.animateWithDuration(0.4, animations: { () -> Void in
                self.circullarNode.alpha = 0
            }, completion: { (val) -> Void in
                if (val) {
                    self.circullarNode.hidden = true
                }
            })
        })
    }
    func bgShowProgress(value: Double, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            if (self.circullarNode.hidden) {
                self.circullarNode.hidden = false
                self.circullarNode.alpha = 0
            }
            self.circullarNode.postProgress(value, animated: true)
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.circullarNode.alpha = 1
            })
        })
    }
    
    // State show/hide
    func bgHideState(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.progressBg.hideView()
        })
    }
    
    func bgShowState(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.progressBg.showView()
        })
    }
    
    // Icon show/hide
    func bgShowIcon(name: String, selfGeneration: Int) {
        var img = UIImage(named: name)?.tintImage(UIColor.whiteColor())
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.fileStatusIcon.image = img
            self.fileStatusIcon.showView()
        })
    }
    func bgHideIcon(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            self.fileStatusIcon.hideView()
        })
    }
    
    func setPreviewImage(img: UIImage, fast: Bool){
        if ((fast && self.preview.image == nil) || !fast) {
            self.preview.image = img;
            self.preview.showView()
        }
    }
    
    // MARK: -
    // MARK: Getters
    
    private class func measureMedia(w: Int, h: Int) -> CGSize {
        var screenScale = UIScreen.mainScreen().scale;
        var scaleW = 240 / CGFloat(w)
        var scaleH = 340 / CGFloat(h)
        var scale = min(scaleW, scaleH)
        return CGSize(width: scale * CGFloat(w), height: scale * CGFloat(h))
    }
    
    class func measureMediaHeight(message: AMMessage) -> CGFloat {
        if (message.getContent() is AMPhotoContent) {
            var photo = message.getContent() as! AMPhotoContent;
            return measureMedia(Int(photo.getW()), h: Int(photo.getH())).height + 2;
        } else if (message.getContent() is AMVideoContent) {
            var video = message.getContent() as! AMVideoContent;
            return measureMedia(Int(video.getW()), h: Int(video.getH())).height + 2;
        } else {
            fatalError("Unknown content type")
        }
    }
    
    // MARK: -
    // MARK: Layout
    
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
        circullarNode.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 32, preview.frame.origin.y + preview.frame.height/2 - 32, 64, 64)
        progressBg.frame = circullarNode.frame
        fileStatusIcon.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 24, preview.frame.origin.y + preview.frame.height/2 - 24, 48, 48)
        
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
}



