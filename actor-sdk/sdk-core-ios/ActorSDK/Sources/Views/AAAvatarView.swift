//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYImage

public enum AAAvatarType {
    case Rounded
    case Square
}

public class AAAvatarView: UIView, YYAsyncLayerDelegate {
    
    var avatarTyle: AAAvatarType = .Rounded {
        didSet(v) {
            self.layer.setNeedsDisplay()
        }
    }
    
//    var frameSize: Int = 0
//    var avatarType: AAAvatarType = .Rounded
//    var placeholderImage: UIImage?
//
//    var enableAnimation: Bool = false
//    
//    private static let cacheSize = 10
//    private static var avatarCache = Dictionary<Int, AASwiftlyLRU<Int64, UIImage>>()
//    
//    private var bindedFileId: jlong! = nil
//    private var bindedTitle: String! = nil
//    private var bindedId: jint! = nil
//    
//    private var requestId: Int = 0
//    private var callback: AAFileCallback? = nil
    
    private var title: String?
    private var id: Int?
    private var fileName: String?
    private var showPlaceholder: Bool = false
    
    public init() {
        super.init(frame: CGRectZero)
        
        self.layer.delegate = self
        self.layer.contentsScale = UIScreen.mainScreen().scale
        self.backgroundColor = UIColor.clearColor()
        self.opaque = false
        self.contentMode = .Redraw;
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    //
    // Databinding
    //
    
    public func bind(title: String, id: Int, fileName: String?) {
//        unbind()
//        
//        title = title.smallValue()
//        
//        self.bindedTitle = title
//        self.bindedId = -1
//        
//        image = nil
//        if (fileName != nil) {
//            image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(fileName!))
//            
//            if (image != nil && self.avatarType == .Rounded) {
//                image = image!.roundImage(self.frameSize)
//            }
//        }
//        
//        if (image == nil) {
//            if (self.placeholderImage == nil) {
//                self.image = Placeholders.avatarPlaceholder(bindedId, size: frameSize, title: title, rounded: avatarType == .Rounded);
//            }
//            return
//        }
    }
    
    public func bind(title: String, id: Int, avatar: ACAvatar!, clearPrev: Bool = true) {
        
        
        self.title = title
        self.id = id
        self.fileName = nil
        self.showPlaceholder = false
        
        self.layer.setNeedsDisplay()
        
//        
//        title = title.smallValue()
//        
//        let needSmallAvatar: Bool = frameSize < 100
//        
//        var fileLocation: ACFileReference?
//        if needSmallAvatar == true {
//            fileLocation = avatar?.smallImage?.fileReference
//        } else {
//            fileLocation = avatar?.smallImage?.fileReference
//        }
//        
//        if (bindedId != nil && bindedId == id) {
//            var notChanged = true;
//            
//            // Is Preview changed
//            notChanged = notChanged && bindedTitle == title
//            
//            // Is avatar changed
//            if (fileLocation == nil) {
//                if (bindedFileId != nil) {
//                    notChanged = false
//                }
//            } else if (bindedFileId == nil) {
//                if (fileLocation != nil) {
//                    notChanged = false
//                }
//            } else {
//                if (bindedFileId != fileLocation?.getFileId()) {
//                    notChanged = false
//                }
//            }
//            
//            if (notChanged) {
//                return
//            }
//        }
//        
//        unbind(clearPrev)
//        
//        self.bindedId = id
//        self.bindedTitle = title
//        
//        if (fileLocation == nil) {
//            
//            requestId++
//            
//            self.image = nil
//            let callbackRequestId = requestId
//            let callbackBindedId = bindedId
//            dispatchBackground() {
//                if callbackRequestId == self.requestId {
//                    
//                    let image = Placeholders.avatarPlaceholder(callbackBindedId, size: self.frameSize, title: title, rounded: self.avatarType == .Rounded)
//                    
//                    dispatchOnUi() {
//                        if callbackRequestId == self.requestId {
//                            self.image = image
//                        }
//                    }
//                }
//            }
//            
//            return
//        }
//        
//        // Load avatar
//        
//        let cached = checkCache(frameSize, id: Int64(fileLocation!.getFileId()))
//        if (cached != nil) {
//            self.image = cached
//            return
//        }
//        
//        if needSmallAvatar == false {
//            let smallFileLocation = avatar?.smallImage?.fileReference
//            var smallAvatarCached = checkCache(40, id: Int64(smallFileLocation!.getFileId()))
//            if smallAvatarCached == nil {
//                smallAvatarCached = checkCache(48, id: Int64(smallFileLocation!.getFileId()))
//            }
//            if smallAvatarCached != nil {
//                image = smallAvatarCached
//            }
//        }
//        
//        requestId++
//        
//        let callbackRequestId = requestId
//        self.bindedFileId = fileLocation?.getFileId()
//        self.callback = AAFileCallback(onDownloaded: { (reference) -> () in
//            
//            if (callbackRequestId != self.requestId) {
//                return;
//            }
//            
//            var image = UIImage(contentsOfFile: CocoaFiles.pathFromDescriptor(reference));
//            
//            if (image == nil) {
//                return
//            }
//            
//            if (self.avatarType == .Rounded) {
//                image = image!.roundImage(self.frameSize)
//            }
//            
//            dispatchOnUi {
//                if (callbackRequestId != self.requestId) {
//                    return;
//                }
//                
//                self.putToCache(self.frameSize, id: Int64(self.bindedFileId!), image: image!)
//                if (self.enableAnimation) {
//                    UIView.transitionWithView(self, duration: 0.4, options: UIViewAnimationOptions.TransitionCrossDissolve, animations: { () -> Void in
//                        self.image = image;
//                    }, completion: nil)
//                } else {
//                    self.image = image;
//                }
//            }
//        })
//        let fl = fileLocation
//        let c = callback
//        dispatchBackground {
//            Actor.bindRawFileWithReference(fl, autoStart: true, withCallback: c)
//        }
    }
    
    public func unbind() {
        self.unbind(true)
    }
    
    public func unbind(clearPrev: Bool) {
//        if (clearPrev) {
//            self.image = (self.placeholderImage != nil) ? self.placeholderImage : nil
//        }
//        self.bindedId = nil
//        self.bindedTitle = nil
//        
//        if (bindedFileId != nil) {
//            let bfid = bindedFileId
//            let c = callback
//            dispatchBackground {
//                Actor.unbindRawFileWithFileId(bfid!, autoCancel: false, withCallback: c)
//            }
//            bindedFileId = nil
//            callback = nil
//            requestId++;
//        }
    }
//    
//    //
//    // Caching
//    //
//
//    private func checkCache(size: Int, id: Int64) -> UIImage? {
//        if let cache = AAAvatarView.avatarCache[size] {
//            if let img = cache[id] {
//                return img
//            }
//        }
//        return nil
//    }
//    
//    private func putToCache(size: Int, id: Int64, image: UIImage) {
//        if let cache = AAAvatarView.avatarCache[size] {
//            cache[id] = image
//        } else {
//            let cache = AASwiftlyLRU<jlong, UIImage>(capacity: AAAvatarView.cacheSize);
//            cache[id] = image
//            AAAvatarView.avatarCache.updateValue(cache, forKey: size)
//        }
//    }

    public override class func layerClass() -> AnyClass {
        return YYAsyncLayer.self
    }
    
    public func newAsyncDisplayTask() -> YYAsyncLayerDisplayTask {
        let res = YYAsyncLayerDisplayTask()
        
        let _id = id!
        let _title = title!
        
        res.display = { (context: CGContext,  size: CGSize, isCancelled: () -> Bool) -> () in
            let r = min(size.width, size.height) / 2
            let colors = ActorSDK.sharedActor().style.avatarColors
            let color = colors[_id % colors.count].CGColor
            
            // Background
            
            CGContextSetFillColorWithColor(context, color)
            
            CGContextAddArc(context, r, r, r, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0)

            CGContextDrawPath(context, .Fill)
            
            // Text
            
            UIColor.whiteColor().set()
            
            let font = UIFont.systemFontOfSize(r)
            var rect = CGRectMake(0, 0, r * 2, r * 2)
            rect.origin.y = round(CGFloat(r * 2 * 40 / 100) - font.pointSize / 2);
            
            let style : NSMutableParagraphStyle = NSParagraphStyle.defaultParagraphStyle().mutableCopy() as! NSMutableParagraphStyle
            style.alignment = NSTextAlignment.Center
            style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
            
            let short = _title.trim().smallValue()
            
            short.drawInRect(rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
                NSForegroundColorAttributeName:ActorSDK.sharedActor().style.avatarTextColor])

            // Border
            
            CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x10/255.0).CGColor);
            CGContextAddArc(context, r, r, r, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
            CGContextDrawPath(context, .Stroke);
        }
        return res
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

private class AvatarLayer: YYAsyncLayer {
    
}

private class AvatarRender {
    
}
