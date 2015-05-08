//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
class CocoaHttpProvider: NSObject, AMHttpProvider {
    
    let queue:NSOperationQueue = NSOperationQueue()
    
    func getMethodWithNSString(url: String!, withInt startOffset: jint, withInt size: jint, withInt totalSize: jint, withImActorModelHttpFileDownloadCallback callback: ImActorModelHttpFileDownloadCallback!) {
            
            var header = "bytes=\(startOffset)-\(min(startOffset + size, totalSize))"
            var request = NSMutableURLRequest(URL: NSURL(string: url)!)
            request.HTTPShouldHandleCookies = false
            request.cachePolicy = NSURLRequestCachePolicy.ReloadIgnoringLocalAndRemoteCacheData
            request.setValue(header, forHTTPHeaderField: "Range")
            request.HTTPMethod = "GET"
            
            NSURLConnection.sendAsynchronousRequest(request, queue: queue, completionHandler:{ (response: NSURLResponse!, data: NSData!, error: NSError!) -> Void in
                if (error != nil) {
                    callback.onDownloadFailure()
                } else {
                    callback.onDownloadedWithByteArray(data.toJavaBytes())
                }
            })
    }
    
    func putMethodWithNSString(url: String!, withByteArray contents: IOSByteArray!, withImActorModelHttpFileUploadCallback callback: ImActorModelHttpFileUploadCallback!) {
        var request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPShouldHandleCookies = false
        request.cachePolicy = NSURLRequestCachePolicy.ReloadIgnoringLocalAndRemoteCacheData
        request.HTTPMethod = "PUT"
        request.HTTPBody = contents.toNSData()
        request.setValue("application/octet-stream", forHTTPHeaderField: "Content-Type")
        
        NSURLConnection.sendAsynchronousRequest(request, queue: queue, completionHandler:{ (response: NSURLResponse!, data: NSData!, error: NSError!) -> Void in
            if (error != nil) {
                callback.onUploadFailure()
            } else {
                callback.onUploaded()
            }
        })
    }
}