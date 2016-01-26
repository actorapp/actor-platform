//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public enum AAAvatarType {
    case Rounded
    case Square
}

public class AAAvatarView: UIImageView {
    
    var frameSize: Int = 0
    var avatarType: AAAvatarType = .Rounded
    var placeholderImage: UIImage?

    var enableAnimation: Bool = false
    
    private static let cacheSize = 10
    private static var avatarCache = Dictionary<Int, AASwiftlyLRU<Int64, UIImage>>()
    
    private var bindedFileId: jlong! = nil
    private var bindedTitle: String! = nil
    private var bindedId: jint! = nil
    
    private var requestId: Int = 0
    private var callback: AAFileCallback? = nil
    
    public init() {
        super.init(image: nil)
    }
    
    public init(frameSize: Int) {
        self.frameSize = frameSize
        
        super.init(image: nil)
    }
    
    public init(frameSize: Int, type: AAAvatarType) {
        self.frameSize = frameSize
        self.avatarType = type
        
        super.init(image: nil)
        
        if type == .Square {
            self.contentMode = UIViewContentMode.ScaleAspectFill
        }
    }
    
    public init(frameSize: Int, type: AAAvatarType, placeholderImage: UIImage?) {
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
    
    //
    // Databinding
    //
    
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
            
            self.image = Placeholders.avatarPlaceholder(bindedId, size: self.frameSize, title: title, rounded: self.avatarType == .Rounded)
            
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
        self.callback = AAFileCallback(onDownloaded: { (reference) -> () in
            
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
                if (self.enableAnimation) {
                    UIView.transitionWithView(self, duration: 0.4, options: UIViewAnimationOptions.TransitionCrossDissolve, animations: { () -> Void in
                        self.image = image;
                    }, completion: nil)
                } else {
                    self.image = image;
                }
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
    
    //
    // Caching
    //

    private func checkCache(size: Int, id: Int64) -> UIImage? {
        if let cache = AAAvatarView.avatarCache[size] {
            if let img = cache[id] {
                return img
            }
        }
        return nil
    }
    
    private func putToCache(size: Int, id: Int64, image: UIImage) {
        if let cache = AAAvatarView.avatarCache[size] {
            cache[id] = image
        } else {
            let cache = AASwiftlyLRU<jlong, UIImage>(capacity: AAAvatarView.cacheSize);
            cache[id] = image
            AAAvatarView.avatarCache.updateValue(cache, forKey: size)
        }
    }
}

class Placeholders {
    
    class func avatarPlaceholder(index: jint, size: Int, title: NSString, rounded: Bool) -> UIImage {
        let colors = ActorSDK.sharedActor().style.avatarColors
        let color = colors[Int(abs(index)) % colors.count].CGColor
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: size, height: size), false, UIScreen.mainScreen().scale);
        let context = UIGraphicsGetCurrentContext();
        
        // Background
        
        CGContextSetFillColorWithColor(context, color);
        
        if rounded {
            CGContextAddArc(context, CGFloat(size)/2, CGFloat(size)/2, CGFloat(size)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
        } else {
            CGContextAddRect(context, CGRect(x: 0, y: 0, width: size, height: size))
        }
        
        CGContextDrawPath(context, .Fill);
        
        // Text
        
        UIColor.whiteColor().set()
        
        let font = UIFont.systemFontOfSize(CGFloat(size / 2));
        var rect = CGRectMake(0, 0, CGFloat(size), CGFloat(size))
        rect.origin.y = round(CGFloat(size * 47 / 100) - font.pointSize / 2);
        
        let style : NSMutableParagraphStyle = NSParagraphStyle.defaultParagraphStyle().mutableCopy() as! NSMutableParagraphStyle
        style.alignment = NSTextAlignment.Center
        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
        
        title.drawInRect(rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
            NSForegroundColorAttributeName:ActorSDK.sharedActor().style.avatarTextColor])
        
        // Border
        
        if rounded {
            CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x10/255.0).CGColor);
            CGContextAddArc(context,CGFloat(size)/2, CGFloat(size)/2, CGFloat(size)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
            CGContextDrawPath(context, .Stroke);
        }
        
        let image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return image;
    }
}

class AABarAvatarView : AAAvatarView {
    
    override init(frameSize: Int, type: AAAvatarType) {
        super.init(frameSize: frameSize, type: type)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func alignmentRectInsets() -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: -16, bottom: 0, right: 8)
    }
}