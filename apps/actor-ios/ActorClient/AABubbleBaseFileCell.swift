//
//  AABaseFileCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 17.05.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class AABubbleBaseFileCell: AABubbleCell {
    
    var bindGeneration = 0;
    
    var bindedDownloadFile: jlong? = nil
    var bindedDownloadCallback: CocoaDownloadCallback? = nil
    
    var bindedUploadFile: jlong? = nil
    var bindedUploadCallback: CocoaUploadCallback? = nil
    
    func fileBind(message: AMMessage, autoDownload: Bool) {
        if let doc = message.getContent() as? AMDocumentContent {
            
            // Next generation of binding
            bindGeneration++
            // Saving generation to new binding
            var selfGeneration = bindGeneration;
            
            // Remove old bindings
            fileUnbind()
            
            if let source = doc.getSource() as? AMFileRemoteSource {
                var fileReference = source.getFileReference();
            
                bindedDownloadFile = fileReference.getFileId()
                bindedDownloadCallback = CocoaDownloadCallback(notDownloaded: { () -> () in
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
            
                MSG.bindRawFileWithReference(fileReference, autoStart: autoDownload, withCallback: bindedDownloadCallback)
            } else if let source = doc.getSource() as? AMFileLocalSource {
                var fileReference = source.getFileDescriptor();
            
                bindedUploadFile = message.getRid();
                bindedUploadCallback = CocoaUploadCallback(notUploaded: { () -> () in
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
            
                MSG.bindRawUploadFileWithRid(message.getRid(), withCallback: bindedUploadCallback)
            } else {
                fatalError("Unsupported file source")
            }
        } else {
            fatalError("Unsupported message type")
        }
    }
    
    func fileUploadPaused(reference: String, selfGeneration: Int) {
        
    }
    
    func fileUploading(reference: String, progress: Double, selfGeneration: Int) {
        
    }
    
    func fileDownloadPaused(selfGeneration: Int) {
        
    }
    
    func fileDownloading(progress: Double, selfGeneration: Int) {
        
    }
    
    func fileReady(reference: String, selfGeneration: Int) {
        
    }
    
    func runOnUiThread(selfGeneration: Int, closure: ()->()){
        if (selfGeneration != self.bindGeneration) {
            return
        }
        dispatch_async(dispatch_get_main_queue(), {
            if (selfGeneration != self.bindGeneration) {
                return
            }
            
            closure()
        })
    }
    
    func fileUnbind() {
        if (bindedDownloadFile != nil && bindedDownloadCallback != nil) {
            MSG.unbindRawFileWithFileId(bindedDownloadFile!, autoCancel: false, withCallback: bindedDownloadCallback)
            bindedDownloadFile = nil
            bindedDownloadCallback = nil
        }
        if (bindedUploadFile != nil && bindedUploadCallback != nil) {
            MSG.unbindRawUploadFileWithRid(bindedUploadFile!, withCallback: bindedUploadCallback)
            bindedUploadFile = nil
            bindedUploadCallback = nil
        }
    }
}