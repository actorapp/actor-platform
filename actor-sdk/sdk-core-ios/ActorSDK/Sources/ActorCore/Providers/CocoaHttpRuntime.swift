//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaHttpRuntime: NSObject, ARHttpRuntime {
    
    let queue:NSOperationQueue = NSOperationQueue()
    
    func getMethodWithUrl(url: String!, withStartOffset startOffset: jint, withSize size: jint, withTotalSize totalSize: jint) -> ARPromise! {
        
        return ARPromise { (resolver) in
            
            let header = "bytes=\(startOffset)-\(min(startOffset + size, totalSize))"
            let request = NSMutableURLRequest(URL: NSURL(string: url)!)
            request.HTTPShouldHandleCookies = false
            request.cachePolicy = NSURLRequestCachePolicy.ReloadIgnoringLocalAndRemoteCacheData
            request.setValue(header, forHTTPHeaderField: "Range")
            request.HTTPMethod = "GET"
            
            NSURLConnection.sendAsynchronousRequest(request, queue: self.queue, completionHandler:{ (response: NSURLResponse?, data: NSData?, error: NSError?) -> Void in
                if let respHttp = response as? NSHTTPURLResponse {
                    if (respHttp.statusCode >= 200 && respHttp.statusCode < 300) {
                        resolver.result(ARHTTPResponse(code: jint(respHttp.statusCode), withContent: data!.toJavaBytes()))
                    } else {
                        resolver.error(ARHTTPError(int: jint(respHttp.statusCode)))
                    }
                } else {
                    resolver.error(ARHTTPError(int: 0))
                }
            })
        }
    }
    
    func putMethodWithUrl(url: String!, withContents contents: IOSByteArray!) -> ARPromise! {
        return ARPromise { (resolver) in
            let request = NSMutableURLRequest(URL: NSURL(string: url)!)
            request.HTTPShouldHandleCookies = false
            request.cachePolicy = NSURLRequestCachePolicy.ReloadIgnoringLocalAndRemoteCacheData
            request.HTTPMethod = "PUT"
            request.HTTPBody = contents.toNSData()
            request.setValue("application/octet-stream", forHTTPHeaderField: "Content-Type")
            
            NSURLConnection.sendAsynchronousRequest(request, queue: self.queue, completionHandler:{ (response: NSURLResponse?, data: NSData?, error: NSError?) -> Void in
                if let respHttp = response as? NSHTTPURLResponse {
                    if (respHttp.statusCode >= 200 && respHttp.statusCode < 300) {
                        resolver.result(ARHTTPResponse(code: jint(respHttp.statusCode), withContent: nil))
                    } else {
                        resolver.error(ARHTTPError(int: jint(respHttp.statusCode)))
                    }
                } else {
                    resolver.error(ARHTTPError(int: 0))
                }
            })
        }
    }
}