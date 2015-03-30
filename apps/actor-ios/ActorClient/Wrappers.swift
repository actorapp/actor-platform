//
//  Wrappers.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class CocoaCallback: NSObject, AMCommandCallback {
    
    var resultClosure: ((val: AnyObject!) -> ())?;
    var errorClosure: ((val:JavaLangException!) -> ())?;
    
    init<T>(result: ((val:T?) -> ())?, error: ((val:JavaLangException!) -> ())?) {
        super.init()
        self.resultClosure = { (val: AnyObject!) -> () in
            result?(val: val as? T)
        }
        self.errorClosure = error
    }
    
    func onResultWithId(res: AnyObject!) {
        resultClosure?(val: res)
    }
    
    func onErrorWithJavaLangException(e: JavaLangException!) {
        errorClosure?(val: e)
    }
}

class CocoaDownloadCallback : NSObject, AMDownloadCallback {
    
    let notDownloaded: (()->())?
    let onDownloading: ((progress: Double) -> ())?
    let onDownloaded: ((fileName: String) -> ())?
    
    init(notDownloaded: (()->())?, onDownloading: ((progress: Double) -> ())?, onDownloaded: ((reference: String) -> ())?) {
        self.notDownloaded = notDownloaded;
        self.onDownloading = onDownloading;
        self.onDownloaded = onDownloaded;
    }
    
    init(onDownloaded: (reference: String) -> ()) {
        self.notDownloaded = nil;
        self.onDownloading = nil;
        self.onDownloaded = onDownloaded;
    }
    
    func onNotDownloaded() {
        self.notDownloaded?();
    }
    
    func onDownloadingWithFloat(progress: jfloat) {
        self.onDownloading?(progress: Double(progress));
    }
    
    func onDownloadedWithAMFileSystemReference(reference: AMFileSystemReference!) {
        self.onDownloaded?(fileName: reference!.getDescriptor());
    }
}

class CocoaUploadCallback : NSObject, AMUploadCallback {
    
    let notUploaded: (()->())?
    let onUploading: ((progress: Double) -> ())?
    let onUploadedClosure: (() -> ())?
    
    init(notUploaded: (()->())?, onUploading: ((progress: Double) -> ())?, onUploadedClosure: (() -> ())?) {
        self.onUploading = onUploading
        self.notUploaded = notUploaded
        self.onUploadedClosure = onUploadedClosure;
    }
    
    func onNotUploading() {
        notUploaded?();
    }
    
    func onUploaded() {
        onUploadedClosure?()
    }
    
    func onUploadingWithFloat(progress: jfloat) {
        onUploading?(progress: Double(progress))
    }
}