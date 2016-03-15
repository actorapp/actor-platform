//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYImage

public class AAAvatarView: UIView, YYAsyncLayerDelegate, ACFileEventCallback {

    private var title: String?
    private var id: Int?
    private var file: ACFileReference?
    private var fileName: String?
    private var showPlaceholder: Bool = false
    
    public init() {
        super.init(frame: CGRectZero)
        
        self.layer.delegate = self
        self.layer.contentsScale = UIScreen.mainScreen().scale
        self.backgroundColor = UIColor.clearColor()
        self.opaque = false
        self.contentMode = .Redraw;
        if Actor.isLoggedIn() {
            Actor.subscribeToDownloads(self)
        }
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        if Actor.isLoggedIn() {
            Actor.unsubscribeFromDownloads(self)
        }
    }
    
    public func onDownloadedWithLong(fileId: jlong) {
        if self.file?.getFileId() == fileId {
            dispatchOnUi {
                if self.file?.getFileId() == fileId {
                    self.layer.setNeedsDisplay()
                }
            }
        }
    }
    
    //
    // Databinding
    //
    
    public func bind(title: String, id: Int, fileName: String?) {
        
        self.title = title
        self.id = id
        
        self.fileName = fileName
        self.file = nil
        self.showPlaceholder = false
        
        self.layer.setNeedsDisplay()
    }
    
    public func bind(title: String, id: Int, avatar: ACAvatar?) {
        
        if self.title == title
            && self.id == id
            && self.fileName == nil {
                // Do Nothing
                return
        }
        
        self.title = title
        self.id = id
        
        self.fileName = nil
        if avatar?.smallImage != nil {
            self.file = avatar!.smallImage.fileReference!
            self.showPlaceholder = false
        } else {
            self.file = nil
            self.showPlaceholder = true
        }
        
        self.layer.setNeedsDisplay()
    }
    
    public func unbind() {
        self.title = nil
        self.id = nil
        
        self.fileName = nil
        self.file = nil
        self.showPlaceholder = false
        
        self.layer.setNeedsDisplay()
    }
    
    public override class func layerClass() -> AnyClass {
        return YYAsyncLayer.self
    }
    
    public func newAsyncDisplayTask() -> YYAsyncLayerDisplayTask {
        let res = YYAsyncLayerDisplayTask()
        
        let _id = id
        let _title = title
        let _fileName = fileName
        let _file = file
        let _showPlaceholder = showPlaceholder
        
        res.display = { (context: CGContext,  size: CGSize, isCancelled: () -> Bool) -> () in
            
            let r = min(size.width, size.height) / 2
            let filePath: String?
            if _fileName != nil {
                filePath = _fileName
            } else if _file != nil {
                let desc = Actor.findDownloadedDescriptorWithFileId(_file!.getFileId())
                if isCancelled() {
                    return
                }
                if desc != nil {
                    filePath = CocoaFiles.pathFromDescriptor(desc!)
                } else {
                    // Request if not available
                    Actor.startDownloadingWithReference(_file!)
                    filePath = nil
                }
            } else {
                filePath = nil
            }
            
            if isCancelled() {
                return
            }
            
            
            if filePath == nil && _showPlaceholder && _id != nil && _title != nil {
            
                let colors = ActorSDK.sharedActor().style.avatarColors
                let color = colors[_id! % colors.count].CGColor
                
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
                
                let short = _title!.trim().smallValue()
                
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
                    
                    // Background
                    UIBezierPath(roundedRect: CGRectMake(0, 0, r * 2,  r * 2), cornerRadius: r).addClip()
                    
                    if isCancelled() {
                        return
                    }
                    
                    image!.drawInRect(CGRectMake(0, 0, r * 2, r * 2))
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
