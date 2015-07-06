//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

enum AvatarType {
    case Rounded
    case Square
}

class AvatarView: UIImageView {
    
    // MARK: -
    // MARK: Private vars
    
    private let cacheSize = 10;
    private var avatarCache = Dictionary<Int, SwiftlyLRU<Int64, UIImage>>()
    
    // MARK: -
    // MARK: Public vars
    
    var frameSize: Int = 0;
    var avatarType: AvatarType = .Rounded
    var placeholderImage: UIImage?
    
    var bindedFileId: jlong! = nil;
    var bindedTitle: String! = nil;
    var bindedId: jint! = nil;
    
    var requestId: Int = 0;
    var callback: CocoaDownloadCallback? = nil;
    
    var enableAnimation: Bool = true
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(image: nil)
    }
    
    init(frameSize: Int) {
        self.frameSize = frameSize
        
        super.init(image: nil)
    }
    
    init(frameSize: Int, type: AvatarType) {
        self.frameSize = frameSize
        self.avatarType = type
        
        super.init(image: nil)
        
        if type == .Square {
            self.contentMode = UIViewContentMode.ScaleAspectFill
        }
    }
    
    init(frameSize: Int, type: AvatarType, placeholderImage: UIImage?) {
        self.frameSize = frameSize
        self.avatarType = type
        self.placeholderImage = placeholderImage
        
        super.init(image: placeholderImage)
        
        if type == .Square {
            self.contentMode = UIViewContentMode.ScaleAspectFill
        }
    }
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        self.frameSize = Int(min(frame.width, frame.height))
    }
    
    // MARK: -
    // MARK: Cache
    
    private func checkCache(size: Int, id: Int64) -> UIImage? {
        if let cache = avatarCache[size] {
            if let img = cache[id] {
                return img
            }
        }
        return nil
    }
    
    private func putToCache(size: Int, id: Int64, image: UIImage) {
        if let cache = avatarCache[size] {
            cache[id] = image
        } else {
            var cache = SwiftlyLRU<jlong, UIImage>(capacity: cacheSize);
            cache[id] = image
            avatarCache.updateValue(cache, forKey: size)
        }
    }
    
    // MARK: -
    // MARK: Bind
    
    func bind(title: String, id: jint, fileName: String?) {
        unbind()
        
        self.bindedTitle = title
        self.bindedId = -1
        
        image = nil
        if (fileName != nil) {
            image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileName!))
        }
        
        if (image == nil) {
            if (self.placeholderImage == nil) {
                self.image = Imaging.avatarPlaceholder(bindedId, size: frameSize, title: title.smallValue(), rounded: avatarType == .Rounded);
            }
            return
        }
    }
    
    func bind(title: String, id: jint, avatar: AMAvatar!) {
        self.bind(title, id: id, avatar: avatar, clearPrev: true)
    }
    
    func bind(title: String, id: jint, avatar: AMAvatar!, clearPrev: Bool) {
        
        var needSmallAvatar: Bool = frameSize < 100
        
        var fileLocation: AMFileReference?
        if needSmallAvatar == true {
            fileLocation = avatar?.getSmallImage()?.getFileReference();
        } else {
            fileLocation = avatar?.getFullImage()?.getFileReference();
        }
        
        if (bindedId != nil && bindedId == id) {
            var notChanged = true;
            
            // Is Preview changed
            notChanged = notChanged && bindedTitle.smallValue() == title.smallValue()
            
            // Is avatar changed
            if (fileLocation == nil) {
                if (bindedFileId != nil) {
                    notChanged = false
                }
            } else if (bindedFileId == nil) {
                if (fileLocation != nil) {
                    notChanged = false
                }
            } else {
                if (bindedFileId != fileLocation?.getFileId()) {
                    notChanged = false
                }
            }
            
            if (notChanged) {
                return
            }
        }
        
        unbind(clearPrev)
        
        self.bindedId = id
        self.bindedTitle = title
        
        if (fileLocation == nil) {
            if (self.placeholderImage == nil) {
                self.image = Imaging.avatarPlaceholder(bindedId, size: frameSize, title: title.smallValue(), rounded: avatarType == .Rounded);
            }
            
            return
        }
        
        // Load avatar
        
        var cached = checkCache(frameSize, id: Int64(fileLocation!.getFileId()))
        if (cached != nil) {
            self.image = cached
            return
        }
        
        if needSmallAvatar == false {
            var smallFileLocation = avatar?.getSmallImage()?.getFileReference()
            var smallAvatarCached = checkCache(40, id: Int64(smallFileLocation!.getFileId()))
            if smallAvatarCached == nil {
                smallAvatarCached = checkCache(48, id: Int64(smallFileLocation!.getFileId()))
            }
            if smallAvatarCached != nil {
                image = smallAvatarCached
            }
        }
        
        requestId++
        
        var callbackRequestId = requestId
        self.bindedFileId = fileLocation?.getFileId()
        self.callback = CocoaDownloadCallback(onDownloaded: { (reference) -> () in
            
            if (callbackRequestId != self.requestId) {
                return;
            }
            
            var image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference));
            
            if (image == nil) {
                return
            }
            
            if (self.avatarType == .Rounded) {
                image = image!.roundImage(self.frameSize)
            }
            
            dispatch_async(dispatch_get_main_queue(), {
                if (callbackRequestId != self.requestId) {
                    return;
                }
                
                self.putToCache(self.frameSize, id: Int64(self.bindedFileId!), image: image!)
                if (self.enableAnimation) {
                    UIView.transitionWithView(self, duration: 0.4, options: UIViewAnimationOptions.TransitionCrossDissolve, animations: { () -> Void in
                        self.image = image;
                    }, completion: nil)
                } else {
                    self.image = image;
                }
            });
        });
        MSG.bindRawFileWithReference(fileLocation, autoStart: true, withCallback: self.callback)
    }
    
    func unbind() {
        self.unbind(true)
    }
    
    func unbind(clearPrev: Bool) {
        if (clearPrev) {
            self.image = (self.placeholderImage != nil) ? self.placeholderImage : nil
        }
        self.bindedId = nil
        self.bindedTitle = nil
        
        if (bindedFileId != nil) {
            MSG.unbindRawFileWithFileId(bindedFileId!, autoCancel: false, withCallback: callback)
            bindedFileId = nil
            callback = nil
            requestId++;
        }
    }

}

class BarAvatarView : AvatarView {
    
    override init(frameSize: Int, type: AvatarType) {
        super.init(frameSize: frameSize, type: type)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func alignmentRectInsets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 36, bottom: 0, right: 8)
    }
}