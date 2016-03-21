//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYImage

public class AAStickerView: UIView, YYAsyncLayerDelegate, ACFileEventCallback {
    
    private var file: ACFileReference?
    
    public init() {
        super.init(frame: CGRectZero)
        layer.delegate = self
        layer.contentsScale = UIScreen.mainScreen().scale
        backgroundColor = UIColor.clearColor()
        Actor.subscribeToDownloads(self)
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        Actor.unsubscribeFromDownloads(self)
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
    
    public func setSticker(file: ACFileReference?) {
        self.file = file
        self.layer.setNeedsDisplay()
    }
    
    public override class func layerClass() -> AnyClass {
        return YYAsyncLayer.self
    }
    
    public func newAsyncDisplayTask() -> YYAsyncLayerDisplayTask {
        
        let res = YYAsyncLayerDisplayTask()
        
        let _file = file
        
        res.display = { (context: CGContext,  size: CGSize, isCancelled: () -> Bool) -> () in
             if _file != nil {
                let desc = Actor.findDownloadedDescriptorWithFileId(_file!.getFileId())
                if isCancelled() {
                    return
                }
                if desc != nil {
                    let filePath = CocoaFiles.pathFromDescriptor(desc!)
                    let image = YYImage(contentsOfFile: filePath)
                    if isCancelled() {
                        return
                    }
                    image?.drawInRect(CGRectMake(0, 0, size.width, size.height), withContentMode: UIViewContentMode.ScaleAspectFit, clipsToBounds: true)
                } else {
                    // Request if not available
                    Actor.startDownloadingWithReference(_file!)
                }
            }
        }
        
        return res
    }
}