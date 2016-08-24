//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AABubbleBaseFileCell: AABubbleCell {
    
    private var bindGeneration = 0;
    
    private var bindedDownloadFile: jlong? = nil
    private var bindedDownloadCallback: AAFileCallback? = nil
    
    private var bindedUploadFile: jlong? = nil
    private var bindedUploadCallback: AAUploadFileCallback? = nil
    
    public func fileBind(message: ACMessage, autoDownload: Bool) {
        if let doc = message.content as? ACDocumentContent {
            
            let selfGeneration = prepareBind()
            
            if let source = doc.getSource() as? ACFileRemoteSource {
                let fileReference = source.getFileReference();
                
                bindedDownloadFile = fileReference.getFileId()
                bindedDownloadCallback = AAFileCallback(notDownloaded: { () -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileStateChanged(nil, progress: nil, isPaused: true, isUploading: false, selfGeneration: selfGeneration)
                }, onDownloading: { (progress) -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileStateChanged(nil, progress: Int(progress * 100), isPaused: false, isUploading: false, selfGeneration: selfGeneration)
                }, onDownloaded: { (reference) -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileStateChanged(reference, progress: nil, isPaused: false, isUploading: false, selfGeneration: selfGeneration)
                })
                
                Actor.bindRawFileWithReference(fileReference, autoStart: autoDownload, withCallback: bindedDownloadCallback)
            } else if let source = doc.getSource() as? ACFileLocalSource {
                let fileReference = source.getFileDescriptor();
                
                bindedUploadFile = message.rid;
                bindedUploadCallback = AAUploadFileCallback(notUploaded: { () -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileStateChanged(fileReference, progress: nil, isPaused: true, isUploading: true, selfGeneration: selfGeneration)
                }, onUploading: { (progress) -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileStateChanged(fileReference, progress: Int(progress * 100), isPaused: false, isUploading: true, selfGeneration: selfGeneration)
                }, onUploadedClosure: { () -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileStateChanged(fileReference, progress: nil, isPaused: false, isUploading: false, selfGeneration: selfGeneration)
                });
                
                Actor.bindRawUploadFileWithRid(message.rid, withCallback: bindedUploadCallback)
            } else {
                fatalError("Unsupported file source")
            }
        } else if let sticker = message.content as? ACStickerContent {
            
            let file = sticker.image256
            
            let selfGeneration = prepareBind()
            
            bindedDownloadFile = file.reference.getFileId()
            bindedDownloadCallback = AAFileCallback(notDownloaded: { () -> () in
                if (self.bindGeneration != selfGeneration) {
                    return
                }
                self.fileStateChanged(nil, progress: nil, isPaused: true, isUploading: false, selfGeneration: selfGeneration)
            }, onDownloading: { (progress) -> () in
                if (self.bindGeneration != selfGeneration) {
                    return
                }
                self.fileStateChanged(nil, progress: Int(progress * 100), isPaused: false, isUploading: false, selfGeneration: selfGeneration)
            }, onDownloaded: { (reference) -> () in
                if (self.bindGeneration != selfGeneration) {
                    return
                }
                self.fileStateChanged(reference, progress: nil, isPaused: false, isUploading: false, selfGeneration: selfGeneration)
            })
            
            Actor.bindRawFileWithReference(file.reference, autoStart: autoDownload, withCallback: bindedDownloadCallback)
        } else {
            fatalError("Unsupported message type")
        }
    }
    
    public func bindFile(fileReference: ACFileReference, autoDownload: Bool) {
        
        let selfGeneration = prepareBind()
        
        bindedDownloadFile = fileReference.getFileId()
        bindedDownloadCallback = AAFileCallback(notDownloaded: { () -> () in
            if (self.bindGeneration != selfGeneration) {
                return
            }
            self.fileStateChanged(nil, progress: nil, isPaused: true, isUploading: false, selfGeneration: selfGeneration)
        }, onDownloading: { (progress) -> () in
            if (self.bindGeneration != selfGeneration) {
                return
            }
            self.fileStateChanged(nil, progress: Int(progress * 100), isPaused: false, isUploading: false, selfGeneration: selfGeneration)
        }, onDownloaded: { (reference) -> () in
            if (self.bindGeneration != selfGeneration) {
                return
            }
            self.fileStateChanged(reference, progress: nil, isPaused: false, isUploading: false, selfGeneration: selfGeneration)
        })
        
        Actor.bindRawFileWithReference(fileReference, autoStart: autoDownload, withCallback: bindedDownloadCallback)
    }
    
    private func prepareBind() -> Int {
        
        // Next generation of binding
        bindGeneration += 1
        // Saving generation to new binding
        let selfGeneration = bindGeneration;
        
        // Remove old bindings
        fileUnbind()
        
        return selfGeneration
    }
    
    public func fileStateChanged(reference: String?, progress: Int?, isPaused: Bool, isUploading: Bool, selfGeneration: Int) {
        
    }
    
    public func runOnUiThread(selfGeneration: Int, closure: (()->())?) -> Bool {
        if (selfGeneration != self.bindGeneration) {
            return false
        }
        
        var res = false
        dispatchOnUiSync {
            if (selfGeneration != self.bindGeneration) {
                return
            }
            
            res = true
            
            closure?()
        }
        
        return res
    }
    
    public func fileUnbind() {
        if (bindedDownloadFile != nil && bindedDownloadCallback != nil) {
            Actor.unbindRawFileWithFileId(bindedDownloadFile!, autoCancel: false, withCallback: bindedDownloadCallback)
            bindedDownloadFile = nil
            bindedDownloadCallback = nil
        }
        if (bindedUploadFile != nil && bindedUploadCallback != nil) {
            Actor.unbindRawUploadFileWithRid(bindedUploadFile!, withCallback: bindedUploadCallback)
            bindedUploadFile = nil
            bindedUploadCallback = nil
        }
    }
    
}