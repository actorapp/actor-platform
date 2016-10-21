//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaHttpRuntime: NSObject, ARHttpRuntime {
    
    let queue:OperationQueue = OperationQueue()
    
    func getMethodWithUrl(_ url: String!, withStartOffset startOffset: jint, withSize size: jint, withTotalSize totalSize: jint) -> ARPromise! {
        
        return ARPromise { (resolver) in
            
            let header = "bytes=\(startOffset)-\(min(startOffset + size, totalSize))"
            let request = NSMutableURLRequest(url: URL(string: url)!)
            request.httpShouldHandleCookies = false
            request.cachePolicy = NSURLRequest.CachePolicy.reloadIgnoringLocalAndRemoteCacheData
            request.setValue(header, forHTTPHeaderField: "Range")
            request.httpMethod = "GET"
            
            NSURLConnection.sendAsynchronousRequest(request as URLRequest, queue: self.queue, completionHandler:{ (response: URLResponse?, data: Data?, error: Error?) -> Void in
                if let respHttp = response as? HTTPURLResponse {
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
    
    func putMethod(withUrl url: String!, withContents contents: IOSByteArray!) -> ARPromise! {
        return ARPromise { (resolver) in
            let request = NSMutableURLRequest(url: URL(string: url)!)
            request.httpShouldHandleCookies = false
            request.cachePolicy = NSURLRequest.CachePolicy.reloadIgnoringLocalAndRemoteCacheData
            request.httpMethod = "PUT"
            request.httpBody = contents.toNSData()
            request.setValue("application/octet-stream", forHTTPHeaderField: "Content-Type")
            
            NSURLConnection.sendAsynchronousRequest(request as URLRequest, queue: self.queue, completionHandler:{ (response: URLResponse?, data: Data?, error: Error?) -> Void in
                if let respHttp = response as? HTTPURLResponse {
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
