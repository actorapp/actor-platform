//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYImage

open class AAStickerView: UIView, YYAsyncLayerDelegate, ACFileEventCallback {
    
    fileprivate var file: ACFileReference?
    
    public init() {
        super.init(frame: CGRect.zero)
        layer.delegate = self
        layer.contentsScale = UIScreen.main.scale
        backgroundColor = UIColor.clear
        Actor.subscribe(toDownloads: self)
    }

    public required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        Actor.unsubscribe(fromDownloads: self)
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
    
    open func setSticker(_ file: ACFileReference?) {
        self.file = file
        self.layer.setNeedsDisplay()
    }
    
    open override class var layerClass : AnyClass {
        return YYAsyncLayer.self
    }
    
    open func newAsyncDisplayTask() -> YYAsyncLayerDisplayTask {
        
        let res = YYAsyncLayerDisplayTask()
        
        let _file = file
        
        res.display = { (context: CGContext,  size: CGSize, isCancelled: () -> Bool) -> () in
             if _file != nil {
                let desc = Actor.findDownloadedDescriptor(withFileId: _file!.getFileId())
                if isCancelled() {
                    return
                }
                if desc != nil {
                    let filePath = CocoaFiles.pathFromDescriptor(desc!)
                    let image = YYImage(contentsOfFile: filePath)
                    if isCancelled() {
                        return
                    }
                    image?.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height), with: UIViewContentMode.scaleAspectFit, clipsToBounds: true)
                } else {
                    // Request if not available
                    Actor.startDownloading(with: _file!)
                }
            }
        }
        
        return res
    }
}
