//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
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
    
    func onResult(res: AnyObject!) {
        resultClosure?(val: res)
    }
    
    func onError(e: JavaLangException!) {
        errorClosure?(val: e)
    }
}

class CocoaDownloadCallback : NSObject, AMFileCallback {
    
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
    
    func onDownloading(progress: jfloat) {
        self.onDownloading?(progress: Double(progress));
    }
    
    func onDownloaded(reference: AMFileSystemReference!) {
        self.onDownloaded?(fileName: reference!.getDescriptor());
    }
}

class CocoaUploadCallback : NSObject, AMUploadFileCallback {
    
    let notUploaded: (()->())?
    let onUploading: ((progress: Double) -> ())?
    let onUploadedClosure: (() -> ())?
    
    init(notUploaded: (()->())?, onUploading: ((progress: Double) -> ())?, onUploadedClosure: (() -> ())?) {
        self.onUploading = onUploading
        self.notUploaded = notUploaded
        self.onUploadedClosure = onUploadedClosure;
    }
    
    func onNotUploading() {
        self.notUploaded?();
    }
    
    func onUploaded() {
        self.onUploadedClosure?()
    }
    
    func onUploading(progress: jfloat) {
        self.onUploading?(progress: Double(progress))
    }
}

class CocoaConversationVMCallback: NSObject, AMConversationVMCallback {
    
    let closure: ((unreadId: jlong, index: jint)->())?
    
    init(loadClosure: ((unreadId: jlong, index: jint)->())) {
        self.closure = loadClosure
    }
    
    func onLoadedWithLong(unreadId: jlong, withInt index: jint) {
        
    }
}