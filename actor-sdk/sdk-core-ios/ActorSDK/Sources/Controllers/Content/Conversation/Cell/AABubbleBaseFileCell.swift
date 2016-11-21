//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AABubbleBaseFileCell: AABubbleCell {
    
    fileprivate var bindGeneration = 0;
    
    fileprivate var bindedDownloadFile: jlong? = nil
    fileprivate var bindedDownloadCallback: AAFileCallback? = nil
    
    fileprivate var bindedUploadFile: jlong? = nil
    fileprivate var bindedUploadCallback: AAUploadFileCallback? = nil
    
    open func fileBind(_ message: ACMessage, autoDownload: Bool) {
        if let doc = message.content as? ACDocumentContent {
            
            let selfGeneration = prepareBind()
            
            if let source = doc.getSource() as? ACFileRemoteSource {
                let fileReference = source.getFileReference();
                
                bindedDownloadFile = fileReference?.getFileId()
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
                
                Actor.bindRawFile(with: fileReference, autoStart: autoDownload, with: bindedDownloadCallback)
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
                
                Actor.bindRawUploadFile(withRid: message.rid, with: bindedUploadCallback)
            } else {
                fatalError("Unsupported file source")
            }
        } else if let sticker = message.content as? ACStickerContent {
            
            let file = sticker.image256
            
            let selfGeneration = prepareBind()
            
            bindedDownloadFile = file?.reference.getFileId()
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
            
            Actor.bindRawFile(with: ACFileReference(arApiFileLocation: file?.reference.getFileLocation(), with: file?.reference.fileName, with: (file?.reference.fileSize)!), autoStart: autoDownload, with: bindedDownloadCallback)
        } else {
            fatalError("Unsupported message type")
        }
    }
    
    open func bindFile(_ fileReference: ACFileReference, autoDownload: Bool) {
        
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
        
        Actor.bindRawFile(with: fileReference, autoStart: autoDownload, with: bindedDownloadCallback)
    }
    
    fileprivate func prepareBind() -> Int {
        
        // Next generation of binding
        bindGeneration += 1
        // Saving generation to new binding
        let selfGeneration = bindGeneration;
        
        // Remove old bindings
        fileUnbind()
        
        return selfGeneration
    }
    
    open func fileStateChanged(_ reference: String?, progress: Int?, isPaused: Bool, isUploading: Bool, selfGeneration: Int) {
        
    }
    
    open func runOnUiThread(_ selfGeneration: Int, closure: (()->())?) -> Bool {
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
    
    open func fileUnbind() {
        if (bindedDownloadFile != nil && bindedDownloadCallback != nil) {
            Actor.unbindRawFile(withFileId: bindedDownloadFile!, autoCancel: false, with: bindedDownloadCallback)
            bindedDownloadFile = nil
            bindedDownloadCallback = nil
        }
        if (bindedUploadFile != nil && bindedUploadCallback != nil) {
            Actor.unbindRawUploadFile(withRid: bindedUploadFile!, with: bindedUploadCallback)
            bindedUploadFile = nil
            bindedUploadCallback = nil
        }
    }
    
}
