//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import YYImage

open class AAAvatarView: UIView, YYAsyncLayerDelegate, ACFileEventCallback {

    fileprivate var title: String?
    fileprivate var id: Int?
    fileprivate var file: ACFileReference?
    fileprivate var fileName: String?
    fileprivate var showPlaceholder: Bool = false
    
    public init() {
        super.init(frame: CGRect.zero)
        
        self.layer.delegate = self
        self.layer.contentsScale = UIScreen.main.scale
        self.backgroundColor = UIColor.clear
        self.isOpaque = false
        self.contentMode = .redraw;
        if Actor.isLoggedIn() {
            Actor.subscribe(toDownloads: self)
        }
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        if Actor.isLoggedIn() {
            //FIXME: crash on AvatarView 
            //Actor.unsubscribeFromDownloads(self)
        }
    }
    
    open func onDownloaded(withLong fileId: jlong) {
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
    
    open func bind(_ title: String, id: Int, fileName: String?) {
        
        self.title = title
        self.id = id
        
        self.fileName = fileName
        self.file = nil
        self.showPlaceholder = false
        
        self.layer.setNeedsDisplay()
    }
    
    open func bind(_ title: String, id: Int, avatar: ACAvatar?) {
        
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
    
    open func unbind() {
        self.title = nil
        self.id = nil
        
        self.fileName = nil
        self.file = nil
        self.showPlaceholder = false
        
        self.layer.setNeedsDisplay()
    }
    
    open override class var layerClass : AnyClass {
        return YYAsyncLayer.self
    }
    
    open func newAsyncDisplayTask() -> YYAsyncLayerDisplayTask {
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
                let desc = Actor.findDownloadedDescriptor(withFileId: _file!.getFileId())
                if isCancelled() {
                    return
                }
                if desc != nil {
                    filePath = CocoaFiles.pathFromDescriptor(desc!)
                } else {
                    // Request if not available
                    Actor.startDownloading(with: _file!)
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
                let color = colors[_id! % colors.count].cgColor
                
                // Background
                
                context.setFillColor(color)
                
                context.addEllipse(in: CGRect(x: 0, y: 0, width: r * 2, height: r * 2))
                
                if isCancelled() {
                    return
                }
                
                context.drawPath(using: .fill)
                
                if isCancelled() {
                    return
                }
                
                // Text
                
                UIColor.white.set()
                
                if isCancelled() {
                    return
                }
                
                let font = UIFont.systemFont(ofSize: r)
                var rect = CGRect(x: 0, y: 0, width: r * 2, height: r * 2)
                rect.origin.y = round(CGFloat(r * 2 * 47 / 100) - font.pointSize / 2)
                
                let style : NSMutableParagraphStyle = NSParagraphStyle.default.mutableCopy() as! NSMutableParagraphStyle
                style.alignment = NSTextAlignment.center
                style.lineBreakMode = NSLineBreakMode.byWordWrapping
                
                let short = _title!.trim().smallValue()
                
                short.draw(in: rect, withAttributes: [NSParagraphStyleAttributeName:style, NSFontAttributeName:font,
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
                    UIBezierPath(roundedRect: CGRect(x: 0, y: 0, width: r * 2,  height: r * 2), cornerRadius: r).addClip()
                    
                    if isCancelled() {
                        return
                    }
                    
                    image!.draw(in: CGRect(x: 0, y: 0, width: r * 2, height: r * 2))
                } else {
                    
                    // Clean BG
                    context.setFillColor(UIColor.white.cgColor)
                    
                    context.addEllipse(in: CGRect(x: 0, y: 0, width: r * 2, height: r * 2))
                    
                    if isCancelled() {
                        return
                    }
                    
                    context.drawPath(using: .fill)
                }
                
                if isCancelled() {
                    return
                }
            } else {
                // Clean BG
                context.setFillColor(UIColor.white.cgColor)
                
                if isCancelled() {
                    return
                }
                
                context.addEllipse(in: CGRect(x: 0, y: 0, width: r * 2, height: r * 2))
                
                if isCancelled() {
                    return
                }
                
                context.drawPath(using: .fill)
                
                if isCancelled() {
                    return
                }
            }
            
            // Border
            
            context.setStrokeColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0x10/255.0).cgColor)
            
            if isCancelled() {
                return
            }
            
            context.addEllipse(in: CGRect(x: 0, y: 0, width: r * 2, height: r * 2))
            
            if isCancelled() {
                return
            }
            
            context.drawPath(using: .stroke)
        }
        return res
    }
}
