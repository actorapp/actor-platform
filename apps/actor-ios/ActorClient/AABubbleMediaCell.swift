//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class AABubbleMediaCell : AABubbleCell {
    
    // Views
    let preview = UIImageView()
    let circullarNode = CircullarNode()
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
    var generation = 0;
    
    // File state callbacks
    var bindedDownloadFile: jlong? = nil
    var bindedDownloadCallback: CocoaDownloadCallback? = nil
    
    var bindedUploadFile: jlong? = nil
    var bindedUploadCallback: CocoaUploadCallback? = nil
    
    // MARK: -
    // MARK: Constructors
    
    init(reuseId: String, peer: AMPeer) {
        super.init(reuseId: reuseId, peer: peer, isFullSize: false)
        
        timeBg.image = Imaging.imageWithColor(MainAppTheme.bubbles.mediaDateBg, size: CGSize(width: 1, height: 1))

        timeLabel.font = UIFont(name: "HelveticaNeue-Italic", size: 11)
        timeLabel.textColor = MainAppTheme.bubbles.mediaDate
        
        statusView.contentMode = UIViewContentMode.Center        
        
        contentView.addSubview(preview)
        contentView.addSubview(circullarNode.view)
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        bubbleInsets = UIEdgeInsets(
            top: 3,
            left: 10,
            bottom: 3,
            right: 10)
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func bind(message: AMMessage, reuse: Bool, isPreferCompact: Bool) {
        if (!reuse) {
            
            if (self.isOut) {
                bindBubbleType(BubbleType.MediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.MediaIn, isCompact: false)
            }
            
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
            
            preview.image = nil
            thumbLoaded = false
            contentLoaded = false
            contentViewSize = AABubbleMediaCell.measureMedia(contentWidth, h: contentHeight)
            
            circullarNode.setProgress(0, animated: false)
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.circullarNode.alpha = 0
                self.preview.alpha = 0
            })
        }
        timeLabel.text = formatDate(message.getDate())
        
        var document = message.getContent() as! AMDocumentContent;
        
        var rebindRequired = !reuse;
        if (!rebindRequired) {
            // Force rebind if source is changed from local to remote
            if (document.getSource() is AMFileRemoteSource) {
                // TODO: check rebind need
            }
        }
        
        if (rebindRequired) {
            // Increase cell generation for any new bind
            generation++;
            
            clearBindings()
            
            var selfGeneration = generation;
            
            if (document.getSource() is AMFileRemoteSource) {
                var fileReference = (document.getSource() as! AMFileRemoteSource).getFileReference();
                
                bindedDownloadFile = fileReference.getFileId()
                bindedDownloadCallback = CocoaDownloadCallback(notDownloaded: { () -> () in
                        self.loadThumb(selfGeneration)
                        self.hideProgress(selfGeneration)
                    }, onDownloading: { (progress) -> () in
                        self.loadThumb(selfGeneration)
                        self.showProgress(progress, selfGeneration: selfGeneration)
                    }, onDownloaded: { (reference) -> () in
                        self.loadReference(reference, selfGeneration: selfGeneration)
                        self.hideProgress(selfGeneration)
                    })
                
                // TODO: Better logic for autodownload
                MSG.bindRawFileWith(fileReference, withAutoStart: true, withCallback: bindedDownloadCallback)
            } else if (document.getSource() is AMFileLocalSource) {
                var fileReference = (document.getSource() as! AMFileLocalSource).getFileDescriptor();
                
                bindedUploadFile = message.getRid();
                bindedUploadCallback = CocoaUploadCallback(notUploaded: { () -> () in
                    self.loadReference(fileReference, selfGeneration: selfGeneration)
                    self.hideProgress(selfGeneration)
                }, onUploading: { (progress) -> () in
                    self.loadReference(fileReference, selfGeneration: selfGeneration)
                    self.showProgress(progress, selfGeneration: selfGeneration)
                }, onUploadedClosure: { () -> () in
                    self.loadReference(fileReference, selfGeneration: selfGeneration)
                    self.hideProgress(selfGeneration)
                });
                
                MSG.bindRawUploadFile(message.getRid(), withCallback: bindedUploadCallback)
            } else {
                 fatalError("Unsupported file source")
            }
        }
        
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
    
    func loadThumb(selfGeneration: Int) {
        if (selfGeneration != generation) {
            return
        }
        
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
    
    func loadReference(reference: String, selfGeneration: Int) {
        if (selfGeneration != generation) {
            return
        }
        
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
            UIView.animateWithDuration(0.2, animations: { () -> Void in
                self.preview.alpha = 1
            })
        }
    }
    
    func hideProgress(selfGeneration: Int) {
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.circullarNode.alpha = 0
            })
        })
    }
    
    func showProgress(value: Double, selfGeneration: Int) {
        self.circullarNode.postProgress(value, animated: true)
        self.runOnUiThread(selfGeneration, closure: { () -> () in
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.circullarNode.alpha = 1
            })
        })
    }
    
    func clearBindings() {
        if (bindedDownloadFile != nil && bindedDownloadCallback != nil) {
            MSG.unbindRawFile(bindedDownloadFile!, withAutoCancel: false, withCallback: bindedDownloadCallback!)
            bindedDownloadFile = nil
            bindedDownloadCallback = nil
        }
        if (bindedUploadFile != nil && bindedUploadCallback != nil) {
            MSG.unbindRawUploadFile(bindedUploadFile!, withCallback: bindedUploadCallback!)
            bindedUploadFile = nil
            bindedUploadCallback = nil
        }
    }
    
    func runOnUiThread(selfGeneration: Int, closure: ()->()){
         dispatch_async(dispatch_get_main_queue(), {
            if (selfGeneration != self.generation) {
                return
            }
        
            closure()
        })
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
            return measureMedia(Int(photo.getW()), h: Int(photo.getH())).height + 3 + 3 + 2;
        } else if (message.getContent() is AMVideoContent) {
            var video = message.getContent() as! AMVideoContent;
            return measureMedia(Int(video.getW()), h: Int(video.getH())).height + 3 + 3 + 2;
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



