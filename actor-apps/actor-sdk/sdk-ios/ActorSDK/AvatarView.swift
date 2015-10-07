//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public enum AvatarType {
    case Rounded
    case Square
}

public class AvatarView: UIImageView {
    
    // MARK: -
    // MARK: Private vars
    
    private let cacheSize = 10;
    private var avatarCache = Dictionary<Int, SwiftlyLRU<Int64, UIImage>>()
    
    // MARK: -
    // MARK: Public vars
    
    public var frameSize: Int = 0;
    public var avatarType: AvatarType = .Rounded
    public var placeholderImage: UIImage?
    
    public var bindedFileId: jlong! = nil;
    public var bindedTitle: String! = nil;
    public var bindedId: jint! = nil;
    
    public var requestId: Int = 0;
    public var callback: CocoaDownloadCallback? = nil;
    
    public var enableAnimation: Bool = true
    
    // MARK: -
    // MARK: Constructors
    
    public init() {
        super.init(image: nil)
    }
    
    public init(frameSize: Int) {
        self.frameSize = frameSize
        
        super.init(image: nil)
    }
    
    public init(frameSize: Int, type: AvatarType) {
        self.frameSize = frameSize
        self.avatarType = type
        
        super.init(image: nil)
        
        if type == .Square {
            self.contentMode = UIViewContentMode.ScaleAspectFill
        }
    }
    
    public init(frameSize: Int, type: AvatarType, placeholderImage: UIImage?) {
        self.frameSize = frameSize
        self.avatarType = type
        self.placeholderImage = placeholderImage
        
        super.init(image: placeholderImage)
        
        if type == .Square {
            self.contentMode = UIViewContentMode.ScaleAspectFill
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
        
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
            let cache = SwiftlyLRU<jlong, UIImage>(capacity: cacheSize);
            cache[id] = image
            avatarCache.updateValue(cache, forKey: size)
        }
    }
    
    // MARK: -
    // MARK: Bind
    
    public func bind(var title: String, id: jint, fileName: String?) {
        unbind()
        
        title = title.smallValue()
        
        self.bindedTitle = title
        self.bindedId = -1
        
        image = nil
        if (fileName != nil) {
            image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileName!))
            
            if (image != nil && self.avatarType == .Rounded) {
                image = image!.roundImage(self.frameSize)
            }
        }
        
        if (image == nil) {
            if (self.placeholderImage == nil) {
                self.image = Placeholders.avatarPlaceholder(bindedId, size: frameSize, title: title, rounded: avatarType == .Rounded);
            }
            return
        }
    }
    
    public func bind(title: String, id: jint, avatar: ACAvatar!) {
        self.bind(title, id: id, avatar: avatar, clearPrev: true)
    }
    
    public func bind(var title: String, id: jint, avatar: ACAvatar!, clearPrev: Bool) {
        
        title = title.smallValue()
        
        let needSmallAvatar: Bool = frameSize < 100
        
        var fileLocation: ACFileReference?
        if needSmallAvatar == true {
            fileLocation = avatar?.smallImage?.fileReference
        } else {
            fileLocation = avatar?.smallImage?.fileReference
        }
        
        if (bindedId != nil && bindedId == id) {
            var notChanged = true;
            
            // Is Preview changed
            notChanged = notChanged && bindedTitle == title
            
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
            
            requestId++
            
            if (self.placeholderImage == nil) {
                self.placeholderImage = Placeholders.avatarPlaceholder(bindedId, size: self.frameSize, title: title, rounded: self.avatarType == .Rounded)
                self.image = self.placeholderImage
            }
            
            self.image = self.placeholderImage
            
            return
        }
        
        // Load avatar
        
        let cached = checkCache(frameSize, id: Int64(fileLocation!.getFileId()))
        if (cached != nil) {
            self.image = cached
            return
        }
        
        if needSmallAvatar == false {
            let smallFileLocation = avatar?.smallImage?.fileReference
            var smallAvatarCached = checkCache(40, id: Int64(smallFileLocation!.getFileId()))
            if smallAvatarCached == nil {
                smallAvatarCached = checkCache(48, id: Int64(smallFileLocation!.getFileId()))
            }
            if smallAvatarCached != nil {
                image = smallAvatarCached
            }
        }
        
        requestId++
        
        let callbackRequestId = requestId
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
            
            dispatchOnUi {
                if (callbackRequestId != self.requestId) {
                    return;
                }
                
                self.putToCache(self.frameSize, id: Int64(self.bindedFileId!), image: image!)
//                if (self.enableAnimation) {
//                    UIView.transitionWithView(self, duration: 0.4, options: UIViewAnimationOptions.TransitionCrossDissolve, animations: { () -> Void in
//                        self.image = image;
//                    }, completion: nil)
//                } else {
//                    self.image = image;
//                }
                self.image = image;
            }
        });
        Actor.bindRawFileWithReference(fileLocation, autoStart: true, withCallback: self.callback)
    }
    
    public func unbind() {
        self.unbind(true)
    }
    
    public func unbind(clearPrev: Bool) {
        if (clearPrev) {
            self.image = (self.placeholderImage != nil) ? self.placeholderImage : nil
        }
        self.bindedId = nil
        self.bindedTitle = nil
        
        if (bindedFileId != nil) {
            Actor.unbindRawFileWithFileId(bindedFileId!, autoCancel: false, withCallback: callback)
            bindedFileId = nil
            callback = nil
            requestId++;
        }
    }

}