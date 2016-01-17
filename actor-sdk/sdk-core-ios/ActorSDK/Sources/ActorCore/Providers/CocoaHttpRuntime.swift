//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaHttpRuntime: NSObject, ARHttpRuntime {
    
    let queue:NSOperationQueue = NSOperationQueue()
    
    func getMethodWithUrl(url: String!, withStartOffset startOffset: jint, withSize size: jint, withTotalSize totalSize: jint, withCallback callback: ARFileDownloadCallback!) {
        let header = "bytes=\(startOffset)-\(min(startOffset + size, totalSize))"
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPShouldHandleCookies = false
        request.cachePolicy = NSURLRequestCachePolicy.ReloadIgnoringLocalAndRemoteCacheData
        request.setValue(header, forHTTPHeaderField: "Range")
        request.HTTPMethod = "GET"
        
        NSURLConnection.sendAsynchronousRequest(request, queue: queue, completionHandler:{ (response: NSURLResponse?, data: NSData?, error: NSError?) -> Void in
            if let respHttp = response as? NSHTTPURLResponse {
                if (respHttp.statusCode >= 200 && respHttp.statusCode < 300) {
                    callback.onDownloadedWithByteArray(data!.toJavaBytes())
                } else {
                    callback.onDownloadFailureWithError(jint(respHttp.statusCode), withRetryIn: 0)
                }
            } else {
                callback.onDownloadFailureWithError(0, withRetryIn: 0)
            }
        })
    }
    
    func putMethodWithUrl(url: String!, withContents contents: IOSByteArray!, withCallback callback: ARFileUploadCallback!) {
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPShouldHandleCookies = false
        request.cachePolicy = NSURLRequestCachePolicy.ReloadIgnoringLocalAndRemoteCacheData
        request.HTTPMethod = "PUT"
        request.HTTPBody = contents.toNSData()
        request.setValue("application/octet-stream", forHTTPHeaderField: "Content-Type")
        
        NSURLConnection.sendAsynchronousRequest(request, queue: queue, completionHandler:{ (response: NSURLResponse?, data: NSData?, error: NSError?) -> Void in
            if let respHttp = response as? NSHTTPURLResponse {
                if (respHttp.statusCode >= 200 && respHttp.statusCode < 300) {
                    callback.onUploaded()
                } else {
                    callback.onUploadFailureWithError(jint(respHttp.statusCode), withRetryIn: 0)
                }
            } else {
                callback.onUploadFailureWithError(0, withRetryIn: 0)
            }
        })
    }
}