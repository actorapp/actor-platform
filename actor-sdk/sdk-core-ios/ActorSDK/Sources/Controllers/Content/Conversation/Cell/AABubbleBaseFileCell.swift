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
                    self.fileDownloadPaused(selfGeneration)
                    }, onDownloading: { (progress) -> () in
                        if (self.bindGeneration != selfGeneration) {
                            return
                        }
                        self.fileDownloading(progress, selfGeneration: selfGeneration)
                    }, onDownloaded: { (reference) -> () in
                        if (self.bindGeneration != selfGeneration) {
                            return
                        }
                        self.fileReady(reference, selfGeneration: selfGeneration)
                })
                
                Actor.bindRawFileWithReference(fileReference, autoStart: autoDownload, withCallback: bindedDownloadCallback)
            } else if let source = doc.getSource() as? ACFileLocalSource {
                let fileReference = source.getFileDescriptor();
                
                bindedUploadFile = message.rid;
                bindedUploadCallback = AAUploadFileCallback(notUploaded: { () -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileUploadPaused(fileReference, selfGeneration: selfGeneration)
                    }, onUploading: { (progress) -> () in
                        if (self.bindGeneration != selfGeneration) {
                            return
                        }
                        self.fileUploading(fileReference, progress: progress, selfGeneration: selfGeneration)
                    }, onUploadedClosure: { () -> () in
                        if (self.bindGeneration != selfGeneration) {
                            return
                        }
                        self.fileReady(fileReference, selfGeneration: selfGeneration)
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
                self.fileDownloadPaused(selfGeneration)
                }, onDownloading: { (progress) -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileDownloading(progress, selfGeneration: selfGeneration)
                }, onDownloaded: { (reference) -> () in
                    if (self.bindGeneration != selfGeneration) {
                        return
                    }
                    self.fileReady(reference, selfGeneration: selfGeneration)
            })
            
            Actor.bindRawFileWithReference(ACFileReference(ARApiFileLocation: file.reference.getFileLocation(), withNSString: file.reference.fileName, withInt: file.reference.fileSize), autoStart: autoDownload, withCallback: bindedDownloadCallback)
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
            self.fileDownloadPaused(selfGeneration)
            }, onDownloading: { (progress) -> () in
                if (self.bindGeneration != selfGeneration) {
                    return
                }
                self.fileDownloading(progress, selfGeneration: selfGeneration)
            }, onDownloaded: { (reference) -> () in
                if (self.bindGeneration != selfGeneration) {
                    return
                }
                self.fileReady(reference, selfGeneration: selfGeneration)
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
    
    public func fileUploadPaused(reference: String, selfGeneration: Int) {
        
    }
    
    public func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        
    }
    
    public func fileDownloadPaused(selfGeneration: Int) {
        
    }
    
    public func fileDownloading(progress: Double, selfGeneration: Int) {
        
    }
    
    public func fileReady(reference: String, selfGeneration: Int) {
        
    }
    
    public func runOnUiThread(selfGeneration: Int, closure: ()->()){
        if (selfGeneration != self.bindGeneration) {
            return
        }
        dispatchOnUi {
            if (selfGeneration != self.bindGeneration) {
                return
            }
            
            closure()
        }
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