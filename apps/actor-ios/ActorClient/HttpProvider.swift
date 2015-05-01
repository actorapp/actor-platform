//
//  HttpProvider.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 27.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class HttpProvider: NSObject, AMHttpDownloaderProvider {
    
    let queue:NSOperationQueue = NSOperationQueue()
    
    func downloadPartWithNSString(url: String!, withInt startOffset: jint, withInt size: jint, withInt totalSize: jint, withImActorModelHttpFileDownloadCallback callback: ImActorModelHttpFileDownloadCallback!) {

        var header = "bytes=\(startOffset)-\(startOffset + size)"
        
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
    
    func uploadPartWithNSString(url: String!, withByteArray contents: IOSByteArray!, withImActorModelHttpFileUploadCallback callback: ImActorModelHttpFileUploadCallback!) {
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