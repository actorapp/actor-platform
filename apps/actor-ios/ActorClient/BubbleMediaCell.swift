//
//  BubbleMediaCell.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 17.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class BubbleMediaCell : BubbleCell {
    
    let preview = UIImageView();
    let circullarNode = CircullarNode()
    
    var isOut:Bool = false;
    
    var contentWidth = 0
    var contentHeight = 0
    var thumb : AMFastThumb? = nil
    var contentViewSize: CGSize? = nil
    var thumbLoaded = false
    var contentLoaded = false
    
    var bindedDownloadFile: jlong? = nil
    var bindedDownloadCallback: CocoaDownloadCallback? = nil
    
    var bindedUploadFile: jlong? = nil
    var bindedUploadCallback: CocoaUploadCallback? = nil
    
    var generation = 0;
    
    // MARK: -
    // MARK: Constructors
    
    override init(reuseId: String) {
        super.init(reuseId: reuseId)
        
        bubble.image = UIImage(named: "conv_media_bg")
        
        contentView.addSubview(bubble)
        contentView.addSubview(preview)
        contentView.addSubview(circullarNode.view)
        
        self.backgroundColor = UIColor.clearColor();
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func bind(message: AMMessage, reuse: Bool) {
        if (!reuse) {
            self.isOut = message.getSenderId() == MSG.myUid()
            
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
            contentViewSize = BubbleMediaCell.measureMedia(contentWidth, h: contentHeight)
            
            circullarNode.setProgress(0, animated: false)
            UIView.animateWithDuration(0, animations: { () -> Void in
                self.circullarNode.alpha = 0
                self.preview.alpha = 0
            })
            
            if group && !isOut {
                if let user = MSG.getUsers().getWithLong(jlong(message.getSenderId())) as? AMUserVM {
                    var username = ""
                    if let uname = user.getName().get() as? String {
                        username = uname
                    }
                    
                    let avatar: AMAvatar? = user.getAvatar().get() as? AMAvatar
                    avatarView.bind(username, id: user.getId(), avatar: avatar)
                }
                contentView.addSubview(avatarView)
            } else {
                avatarView.removeFromSuperview()
            }
        }
        
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
            return measureMedia(Int(photo.getW()), h: Int(photo.getH())).height;
        }
        
        fatalError("???")
    }
    
    class func bubbleTopPadding() -> CGFloat {
        return 1 + Utils.retinaPixel()
    }
    
    class func bubbleBottomPadding() -> CGFloat {
        return 1 + Utils.retinaPixel()
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        var bubbleTopPadding = BubbleMediaCell.bubbleTopPadding()
        var bubbleBottomPadding = BubbleMediaCell.bubbleBottomPadding()
        
        var contentWidth = self.contentView.frame.width
        var contentHeight = self.contentView.frame.height
        
        var bubbleHeight = contentHeight - bubbleTopPadding - bubbleBottomPadding
        var bubbleWidth = bubbleHeight * CGFloat(self.contentWidth) / CGFloat(self.contentHeight)
        
        var contentInsetX = CGFloat((self.group ? self.groupContentInsetX : 0.0))
        
        if (self.isOut) {
            bubble.frame = CGRectMake(contentWidth - bubbleWidth - bubbleMediaPadding, bubbleTopPadding, bubbleWidth, bubbleHeight)
        } else {
            bubble.frame = CGRectMake(bubbleMediaPadding + contentInsetX, bubbleTopPadding, bubbleWidth, bubbleHeight)
        }
        
        if self.group && !self.isOut {
            let avatarSize = CGFloat(self.avatarView.frameSize)
            self.avatarView.frame = CGRect(x: 5, y: self.bubble.frame.maxY - avatarSize - 1, width: avatarSize, height: avatarSize)
        }
        
        preview.frame = CGRectMake(bubble.frame.origin.x + 1, bubble.frame.origin.y + 1, bubble.frame.width - 2, bubble.frame.height - 2);
        
        circullarNode.frame = CGRectMake(preview.frame.origin.x + preview.frame.width/2 - 32, preview.frame.origin.y + preview.frame.height/2 - 32, 64, 64)
    }
}