//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYImage

public enum AAAvatarType {
    case Rounded
    case Square
}

public class AAAvatarView: UIView, YYAsyncLayerDelegate, ACFileEventCallback {
    
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
    private var fileId: jlong?
    private var fileName: String?
    private var showPlaceholder: Bool = false
    
    public init() {
        super.init(frame: CGRectZero)
        
        self.layer.delegate = self
        self.layer.contentsScale = UIScreen.mainScreen().scale
        self.backgroundColor = UIColor.clearColor()
        self.opaque = false
        self.contentMode = .Redraw;
        Actor.subscribeToDownloads(self)
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        Actor.unsubscribeFromDownloads(self)
    }
    
    public func onDownloadedWithLong(fileId: jlong) {
        if self.fileId == fileId {
            dispatchOnUi {
                if self.fileId == fileId {
                    self.layer.setNeedsDisplay()
                }
            }
        }
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
    
    public func bind(title: String, id: Int, avatar: ACAvatar?, clearPrev: Bool = true) {
        
        
        self.title = title
        self.id = id
        self.fileName = nil
        
        if avatar?.smallImage != nil {
            let fileRef = avatar!.smallImage.fileReference!
            self.fileId = fileRef.getFileId()
            self.showPlaceholder = false
            Actor.startDownloadingWithReference(fileRef)
        } else {
            self.fileId = nil
            self.showPlaceholder = true
        }
        
        self.layer.setNeedsDisplay()
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
        let _fileName = fileName
        let _fileId = fileId
        let _showPlaceholder = showPlaceholder
        
        res.display = { (context: CGContext,  size: CGSize, isCancelled: () -> Bool) -> () in
            
            let r = min(size.width, size.height) / 2
            let filePath: String?
            if _fileName != nil {
                filePath = _fileName
            } else if _fileId != nil {
                let desc = Actor.findDownloadedDescriptorWithFileId(_fileId!)
                if isCancelled() {
                    return
                }
                if desc != nil {
                    filePath = CocoaFiles.pathFromDescriptor(desc!)
                } else {
                    filePath = nil
                }
            } else {
                filePath = nil
            }
            
            if isCancelled() {
                return
            }
            
            
            if filePath == nil && _showPlaceholder {
            
                let colors = ActorSDK.sharedActor().style.avatarColors
                let color = colors[_id % colors.count].CGColor
                
                // Background
                
                CGContextSetFillColorWithColor(context, color)
                
                CGContextAddArc(context, r, r, r, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0)
                
                if isCancelled() {
                    return
                }
                
                CGContextDrawPath(context, .Fill)
                
                if isCancelled() {
                    return
                }
                
                // Text
                
                UIColor.whiteColor().set()
                
                if isCancelled() {
                    return
                }
                
                let font = UIFont.systemFontOfSize(r)
                var rect = CGRectMake(0, 0, r * 2, r * 2)
                rect.origin.y = round(CGFloat(r * 2 * 47 / 100) - font.pointSize / 2)
                
                let style : NSMutableParagraphStyle = NSParagraphStyle.defaultParagraphStyle().mutableCopy() as! NSMutableParagraphStyle
                style.alignment = NSTextAlignment.Center
                style.lineBreakMode = NSLineBreakMode.ByWordWrapping
                
                let short = _title.trim().smallValue()
                
                short.drawInRect(rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
                    NSForegroundColorAttributeName:ActorSDK.sharedActor().style.avatarTextColor])
                
                if isCancelled() {
                    return
                }
            } else if let fp = filePath {
                
                let image: UIImage? = UIImage(contentsOfFile: fp)
                
                if isCancelled() {
                    return
                }
                
                if image != nil {
                    
                    let resized = image!.resize(r * 2, h: r * 2)
                    
                    if isCancelled() {
                        return
                    }
                    
                    resized.drawInRect(CGRectMake(0, 0, r * 2, r * 2))
                } else {
                    
                    // Clean BG
                    CGContextSetFillColorWithColor(context, UIColor.whiteColor().CGColor)
                    
                    CGContextAddArc(context, r, r, r, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0)
                    
                    if isCancelled() {
                        return
                    }
                    
                    CGContextDrawPath(context, .Fill)
                }
                
                if isCancelled() {
                    return
                }
            } else {
                // Clean BG
                CGContextSetFillColorWithColor(context, UIColor.whiteColor().CGColor)
                
                if isCancelled() {
                    return
                }
                
                CGContextAddArc(context, r, r, r, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0)
                
                if isCancelled() {
                    return
                }
                
                CGContextDrawPath(context, .Fill)
                
                if isCancelled() {
                    return
                }
            }
            
            // Border
            
            CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x10/255.0).CGColor)
            
            if isCancelled() {
                return
            }
            
            CGContextAddArc(context, r, r, r, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0)
            
            if isCancelled() {
                return
            }
            
            CGContextDrawPath(context, .Stroke)
        }
        return res
    }
}
//
//class Placeholders {
//    
//    class func avatarPlaceholder(index: jint, size: Int, title: NSString, rounded: Bool) -> UIImage {
//        let colors = ActorSDK.sharedActor().style.avatarColors
//        let color = colors[Int(abs(index)) % colors.count].CGColor
//        
//        UIGraphicsBeginImageContextWithOptions(CGSize(width: size, height: size), false, UIScreen.mainScreen().scale);
//        let context = UIGraphicsGetCurrentContext();
//        
//        // Background
//        
//        CGContextSetFillColorWithColor(context, color);
//        
//        if rounded {
//            CGContextAddArc(context, CGFloat(size)/2, CGFloat(size)/2, CGFloat(size)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
//        } else {
//            CGContextAddRect(context, CGRect(x: 0, y: 0, width: size, height: size))
//        }
//        
//        CGContextDrawPath(context, .Fill);
//        
//        // Text
//        
//        UIColor.whiteColor().set()
//        
//        let font = UIFont.systemFontOfSize(CGFloat(size / 2));
//        var rect = CGRectMake(0, 0, CGFloat(size), CGFloat(size))
//        rect.origin.y = round(CGFloat(size * 47 / 100) - font.pointSize / 2);
//        
//        let style : NSMutableParagraphStyle = NSParagraphStyle.defaultParagraphStyle().mutableCopy() as! NSMutableParagraphStyle
//        style.alignment = NSTextAlignment.Center
//        style.lineBreakMode = NSLineBreakMode.ByWordWrapping;
//        
//        title.drawInRect(rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
//            NSForegroundColorAttributeName:ActorSDK.sharedActor().style.avatarTextColor])
//        
//        // Border
//        
//        if rounded {
//            CGContextSetStrokeColorWithColor(context, UIColor(red: 0, green: 0, blue: 0, alpha: 0x10/255.0).CGColor);
//            CGContextAddArc(context,CGFloat(size)/2, CGFloat(size)/2, CGFloat(size)/2, CGFloat(M_PI * 0), CGFloat(M_PI * 2), 0);
//            CGContextDrawPath(context, .Stroke);
//        }
//        
//        let image = UIGraphicsGetImageFromCurrentImageContext();
//        UIGraphicsEndImageContext();
//        return image;
//    }
//    
//}
//
//private class AvatarLayer: YYAsyncLayer {
//    
//}
//
//private class AvatarRender {
//    
//}
