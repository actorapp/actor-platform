//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import VBFPopFlatButton
import YYImage
import YYWebImage
import YYCategories

open class AABubbleMediaCell : AABubbleBaseFileCell, NYTPhotosViewControllerDelegate {
    
    // Views
    
    var preview = YYAnimatedImageView()
    let progress = AAProgressView(size: CGSize(width: 64, height: 64))
    let timeBg = UIImageView()
    let timeLabel = UILabel()
    let statusView = UIImageView()
    let playView = UIImageView(image: UIImage.bundled("aa_playbutton"))
    
    // Binded data
    
    var bindedLayout: MediaCellLayout!
    var thumb: UIImage!
    var thumbLoaded = false
    var contentLoaded = false
    
    var thumbShown = false
    var previewShown = false
    
    // Constructors
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        preview.autoPlayAnimatedImage = true
        preview.runloopMode = RunLoopMode.defaultRunLoopMode.rawValue
        
        timeBg.image = Imaging.roundedImage(ActorSDK.sharedActor().style.chatMediaDateBgColor, radius: 10)
        
        timeLabel.font = UIFont.italicSystemFont(ofSize: 11)
        timeLabel.textColor = appStyle.chatMediaDateColor
        
        statusView.contentMode = UIViewContentMode.center
        
        contentView.addSubview(preview)
        contentView.addSubview(progress)
        
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        contentView.addSubview(playView)
        
        preview.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleMediaCell.mediaDidTap)))
        preview.isUserInteractionEnabled = true
        contentInsets = UIEdgeInsets(top: 2, left: 2, bottom: 2, right: 2)
        
        playView.isUserInteractionEnabled = false
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Binding
    
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! MediaCellLayout
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            
            // Bind bubble
            if (self.isOut) {
                bindBubbleType(BubbleType.mediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.mediaIn, isCompact: false)
            }
            
            // Reset content state
            preview.image = nil
            contentLoaded = false
            thumbLoaded = false
            thumbShown = false
            previewShown = false
            thumb = nil
            
            // Reset progress
            self.progress.alpha = 0
            self.progress.setProgress(0)
            self.progress.hideButton()
            self.preview.alpha = 0
            
            // Show/Hide play button
            self.playView.isHidden = true
            
            // Rounding Animations
            if (message.content is ACAnimationContent) {
                self.preview.layer.cornerRadius = 14
                self.preview.autoPlayAnimatedImage = ActorSDK.sharedActor().isGIFAutoplayEnabled
            } else {
                self.preview.layer.cornerRadius = 0
            }

            // Bind file
            let autoDownload: Bool
            if message.content is ACAnimationContent {
                autoDownload = true
            } else if message.content is ACVideoContent {
                autoDownload = false
            } else if message.content is ACPhotoContent {
                if self.peer.isGroup {
                    autoDownload = ActorSDK.sharedActor().isPhotoAutoDownloadGroup
                } else if self.peer.isPrivate {
                    autoDownload = ActorSDK.sharedActor().isPhotoAutoDownloadPrivate
                } else {
                    autoDownload = false
                }
            } else {
                autoDownload = false
            }
            fileBind(message, autoDownload: autoDownload)
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
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break
            }
        } else {
            statusView.isHidden = true
        }
    }
    
    // File state binding
    
    open override func fileStateChanged(_ reference: String?, progress: Int?, isPaused: Bool, isUploading: Bool, selfGeneration: Int) {
        
        // Loading Fast Thumb
        // 1. Check Current generation
        // 2. Loading thumb
        // 3. Apply on UI Thread
        
        var needToLoadThumb = false
        if !(runOnUiThread(selfGeneration, closure: {
            needToLoadThumb = self.bindedLayout.fastThumb != nil && self.thumb == nil
        })) { return }
        
        if (needToLoadThumb) {
            let loadedThumb = YYImage(data: bindedLayout.fastThumb!)!.byBlurSoft()!
                .roundCorners(bindedLayout.screenSize.width, h: bindedLayout.screenSize.height, roundSize: 14)
            if !(runOnUiThread(selfGeneration, closure: {
                self.thumb = loadedThumb
            })) { return }
        }
        
        // Loading Image and State
        
        runOnUiThread(selfGeneration) { () -> () in
            
            // Loading Image
            
            if (self.bindedMessage?.content is ACVideoContent) {
                if let t = self.thumb {
                    if !self.thumbShown {
                        self.thumbShown = true
                        self.preview.image = t
                        self.preview.showViewAnimated()
                    }
                }
            } else if (self.bindedMessage?.content is ACPhotoContent) {
                if let r = reference {
                    if !self.previewShown {
                        self.previewShown = true
                        self.preview.yy_setImage(with: URL(fileURLWithPath: CocoaFiles.pathFromDescriptor(r)),
                                                        placeholder: self.thumb,
                                                        options: YYWebImageOptions.setImageWithFadeAnimation,
                                                        progress: nil,
                                                        transform: { (img, url) -> UIImage? in
                                                            return img.roundCorners(self.bindedLayout.screenSize.width, h: self.bindedLayout.screenSize.height, roundSize: 14)
                            },completion: nil)
                        self.preview.showViewAnimated()
                    }
                } else if let t = self.thumb {
                    if !self.thumbShown {
                        self.thumbShown = true
                        self.preview.image = t
                        self.preview.showViewAnimated()
                    }
                }
            } else if (self.bindedMessage?.content is ACAnimationContent) {
                if let r = reference {
                    if !self.previewShown {
                        self.previewShown = true
                        self.preview.yy_setImage(with: URL(fileURLWithPath: CocoaFiles.pathFromDescriptor(r)),
                                                        placeholder: self.thumb, options: YYWebImageOptions.setImageWithFadeAnimation,
                                                        progress: nil, transform: nil,completion: nil)
                        self.preview.showViewAnimated()
                    }
                } else if let t = self.thumb {
                    if !self.thumbShown {
                        self.thumbShown = true
                        self.preview.image = t
                        self.preview.showViewAnimated()
                    }
                }
            }
            
            // Updating State
            
            if isUploading {
                if isPaused {
                    self.progress.showViewAnimated()
                    self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: false)
                    self.progress.hideProgress()
                } else {
                    self.progress.showViewAnimated()
                    self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: false)
                    self.progress.setProgress(Double(progress!)/100.0)
                }
            } else {
                if reference != nil {
                    self.progress.setProgress(1)
                    self.progress.hideViewAnimated()
                    
                    // Play Button
                    if self.bindedMessage?.content is ACAnimationContent {
                        if ActorSDK.sharedActor().isGIFAutoplayEnabled {
                            self.playView.isHidden = true
                        } else {
                            self.playView.isHidden = false
                        }
                    } else {
                        self.playView.isHidden = !(self.bindedMessage?.content is ACVideoContent)
                    }
                } else {
                    if isPaused {
                        self.progress.showViewAnimated()
                        self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: false)
                        self.progress.hideProgress()
                    } else {
                        self.progress.showViewAnimated()
                        self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: false)
                        self.progress.setProgress(Double(progress!)/100.0)
                    }
                }
            }
        }
    }
    
    // Media Action
    
    open func mediaDidTap() {
        
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestState(withFileId: fileSource.getFileReference().getFileId(), with: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloading(with: fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloading(withFileId: fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    if content is ACPhotoContent {
                        if let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference)) {
                            let previewImage = PreviewImage(image: img)
                            let previewController = AAPhotoPreviewController(photo: previewImage, fromView: self.preview)
                            previewController.autoShowBadge = true
                            self.controller.present(previewController, animated: true, completion: nil)
                        }
                    } else if content is ACVideoContent {
                        self.controller.playVideoFromPath(CocoaFiles.pathFromDescriptor(reference))
                    } else if self.bindedMessage?.content is ACAnimationContent {
                        if !ActorSDK.sharedActor().isGIFAutoplayEnabled {
                            if self.playView.isHidden {
                                self.preview.stopAnimating()
                                self.playView.isHidden = false
                            } else {
                                self.preview.startAnimating()
                                self.playView.isHidden = true
                            }
                        }
                    }
            }))
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadState(withRid: rid, with: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUpload(withRid: rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUpload(withRid: rid)
                }, onUploadedClosure: { () -> () in
                    if content is ACPhotoContent {
                        if let img = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())) {
                            let previewImage = PreviewImage(image: img)
                            let previewController = AAPhotoPreviewController(photo: previewImage, fromView: self.preview)
                            previewController.autoShowBadge = true
                            self.controller.present(previewController, animated: true, completion: nil)
                        }
                    } else if content is ACVideoContent {
                        self.controller.playVideoFromPath(CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor()))
                    } else if self.bindedMessage?.content is ACAnimationContent {
                        if !ActorSDK.sharedActor().isGIFAutoplayEnabled {
                            if self.playView.isHidden {
                                self.preview.stopAnimating()
                                self.playView.isHidden = false
                            } else {
                                self.preview.startAnimating()
                                self.playView.isHidden = true
                            }
                        }
                    }
            }))
        }
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
        
        playView.centerIn(preview.frame)
        
        progress.frame = CGRect(x: preview.frame.origin.x + preview.frame.width/2 - 32, y: preview.frame.origin.y + preview.frame.height/2 - 32, width: 64, height: 64)
        
        timeLabel.frame = CGRect(x: 0, y: 0, width: 1000, height: 1000)
        timeLabel.sizeToFit()
        
        let timeWidth = (isOut ? 23 : 0) + timeLabel.bounds.width
        let timeHeight: CGFloat = 20
        
        timeLabel.frame = CGRect(x: preview.frame.maxX - timeWidth - 8, y: preview.frame.maxY - timeHeight - 4, width: timeLabel.frame.width, height: timeHeight)
        
        if (isOut) {
            statusView.frame = CGRect(x: timeLabel.frame.maxX, y: timeLabel.frame.minY, width: 23, height: timeHeight)
        }
        
        timeBg.frame = CGRect(x: timeLabel.frame.minX - 6, y: timeLabel.frame.minY, width: timeWidth + 10, height: timeHeight)
    }
    
    // Photo preview
    
    open func photosViewController(_ photosViewController: NYTPhotosViewController, referenceViewFor photo: NYTPhoto) -> UIView? {
        return self.preview
    }
    
    open func photosViewControllerWillDismiss(_ photosViewController: NYTPhotosViewController) {
        // (UIApplication.sharedApplication().delegate as! AppDelegate).showBadge()
        UIApplication.shared.setStatusBarHidden(false, with: UIStatusBarAnimation.fade)
    }
}

/**
    Media cell layout
*/
open class MediaCellLayout: AACellLayout {
    
    open let fastThumb: Data?
    open let contentSize: CGSize
    open let screenSize: CGSize
    open let autoDownload: Bool
    open let duration: Int?
    
    /**
        Creting layout for media bubble
    */
    public init(id: Int64, width: CGFloat, height:CGFloat, date: Int64, fastThumb: ACFastThumb?, autoDownload: Bool, layouter: AABubbleLayouter, duration: Int?) {
        
        // Media Duration
        self.duration = duration
        
        // Saving content size
        self.contentSize = CGSize(width: width, height: height)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload

        // Calculating bubble screen size
        self.screenSize = AACellLayout.pickApproriateSize(width, height: height)
        
        // Prepare fast thumb
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // Creating layout
        super.init(height: self.screenSize.height + 4, date: date, key: "media", layouter: layouter)
    }
    
    /**
     Creating layout for animation content
     */
    public convenience init(id: Int64, animationContent: ACAnimationContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(id: id, width: CGFloat(animationContent.getW()), height: CGFloat(animationContent.getH()), date: date, fastThumb: animationContent.getFastThumb(), autoDownload: true, layouter: layouter, duration: nil)
    }
    
    /**
        Creating layout for photo content
    */
    public convenience init(id: Int64, photoContent: ACPhotoContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(id: id, width: CGFloat(photoContent.getW()), height: CGFloat(photoContent.getH()), date: date, fastThumb: photoContent.getFastThumb(), autoDownload: true, layouter: layouter, duration: nil)
    }
    
    /**
     Creating layout for video content
     */
    public convenience init(id: Int64, videoContent: ACVideoContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(id: id, width: CGFloat(videoContent.getW()), height: CGFloat(videoContent.getH()), date: date, fastThumb: videoContent.getFastThumb(), autoDownload: true, layouter: layouter, duration: Int(videoContent.getDuration()))
    }

    /**
        Creating layout for message
    */
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        if let content = message.content as? ACPhotoContent {
            self.init(id: Int64(message.rid), photoContent: content, date: Int64(message.date), layouter: layouter)
        } else if let content = message.content as? ACVideoContent {
            self.init(id: Int64(message.rid), videoContent: content, date: Int64(message.date), layouter: layouter)
        } else if let content = message.content as? ACAnimationContent {
            self.init(id: Int64(message.rid), animationContent: content, date: Int64(message.date), layouter: layouter)
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
    Layouter for media bubbles
*/
open class AABubbleMediaCellLayouter: AABubbleLayouter {
    
    open func isSuitable(_ message: ACMessage) -> Bool {
        if message.content is ACPhotoContent {
            return true
        }
        if message.content is ACVideoContent {
            return true
        }
        if message.content is ACAnimationContent {
            return true
        }
        return false
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        return MediaCellLayout(message: message, layouter: self)
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleMediaCell.self
    }
}
